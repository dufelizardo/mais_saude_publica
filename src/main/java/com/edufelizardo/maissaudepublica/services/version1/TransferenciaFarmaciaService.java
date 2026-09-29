package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.Lote;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.TransferenciaFarmacia;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.TransferenciaFarmaciaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.TransferenciaFarmaciaResponseDto;
import com.edufelizardo.maissaudepublica.models.enuns.TipoMovimentacaoFarmacia;
import com.edufelizardo.maissaudepublica.repositories.LoteRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.TransferenciaFarmaciaRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Transferência de estoque entre unidades (ADR-0059): saída no lote de origem e entrada no lote da
 * mesma remessa na unidade de destino, numa transação só, pelo livro de movimentação (ADR-0057).
 */
@Service
public class TransferenciaFarmaciaService {

    @Autowired
    private TransferenciaFarmaciaRepository transferenciaRepository;

    @Autowired
    private LoteRepository loteRepository;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private MovimentacaoFarmaciaService movimentacaoFarmaciaService;

    @Transactional
    public TransferenciaFarmaciaResponseDto transferir(TransferenciaFarmaciaRequestDto dto) {
        UUID origemId = dto.getLoteOrigemId();
        UUID unidadeDestinoId = dto.getUnidadeDestinoId();

        // A trava da unidade de destino vem antes de procurar a remessa: uma segunda operação simultânea
        // para a mesma unidade espera e já encontra o lote criado pela primeira (ADR-0060).
        UnidadeDeSaude unidadeDestino = unidadeDeSaudeRepository.findByIdParaMovimentarEstoque(unidadeDestinoId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar uma unidade de saúde com o id " + unidadeDestinoId + " em nossos registros."));
        Profissional profissional = profissionalRepository.findByMatricula(dto.getProfissionalMatricula())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um profissional com a matrícula " + dto.getProfissionalMatricula()
                                + " em nossos registros."));

        // Trava origem e destino sempre em ordem crescente de id, para duas transferências cruzadas
        // (A→B e B→A da mesma remessa) não ficarem esperando uma pela outra.
        UUID destinoExistenteId = loteRepository.findIdsDaMesmaRemessaNaUnidade(origemId, unidadeDestinoId)
                .stream().findFirst().orElse(null);
        Lote origem;
        Lote destino = null;
        if (destinoExistenteId != null && destinoExistenteId.compareTo(origemId) < 0) {
            destino = travar(destinoExistenteId);
            origem = travar(origemId);
        } else {
            origem = travar(origemId);
            if (destinoExistenteId != null && !destinoExistenteId.equals(origemId)) {
                destino = travar(destinoExistenteId);
            }
        }

        if (origem.getUnidade().getUuid().equals(unidadeDestinoId)) {
            throw new ResourceBadRequestException("A unidade de destino precisa ser diferente da unidade do lote de origem.");
        }
        if (origem.getValidade().isBefore(LocalDate.now())) {
            throw new ResourceUnprocessableEntityException(
                    "O lote " + origem.getNumeroLote() + " venceu em " + origem.getValidade()
                            + ": medicamento vencido não é transferido, registre a perda por vencimento.");
        }

        if (destino == null) {
            destino = loteRepository.save(new Lote(origem.getMedicamento(), unidadeDestino, origem.getNumeroLote(),
                    origem.getValidade(), 0));
        }

        String observacao = dto.getObservacao() == null || dto.getObservacao().isBlank() ? null : dto.getObservacao().trim();
        TransferenciaFarmacia transferencia = transferenciaRepository.save(new TransferenciaFarmacia(null, origem, destino,
                dto.getQuantidade(), profissional, observacao, Instant.now(),
                movimentacaoFarmaciaService.cpfDoUsuarioAutenticado()));

        // A saída vem primeiro: estoque insuficiente responde 422 e desfaz tudo, inclusive o lote de destino criado.
        movimentacaoFarmaciaService.lancar(origem, TipoMovimentacaoFarmacia.TRANSFERENCIA_SAIDA, -dto.getQuantidade(),
                profissional, null, observacao, null, transferencia);
        movimentacaoFarmaciaService.lancar(destino, TipoMovimentacaoFarmacia.TRANSFERENCIA_ENTRADA, dto.getQuantidade(),
                profissional, null, observacao, null, transferencia);

        return TransferenciaFarmaciaResponseDto.fromTransferencia(transferencia);
    }

    /** Da mais recente para a mais antiga. */
    @Transactional(readOnly = true)
    public List<TransferenciaFarmaciaResponseDto> listar() {
        return transferenciaRepository.findAllByOrderByRegistradoEmDesc()
                .stream()
                .map(TransferenciaFarmaciaResponseDto::fromTransferencia)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TransferenciaFarmaciaResponseDto buscarPorId(UUID uuid) {
        return transferenciaRepository.findById(uuid)
                .map(TransferenciaFarmaciaResponseDto::fromTransferencia)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar uma transferência com o id " + uuid + " em nossos registros."));
    }

    private Lote travar(UUID loteId) {
        return loteRepository.findByIdParaMovimentar(loteId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um lote com o id " + loteId + " em nossos registros."));
    }
}
