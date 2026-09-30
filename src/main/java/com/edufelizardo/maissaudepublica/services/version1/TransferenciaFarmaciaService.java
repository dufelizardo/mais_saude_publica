package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceForbiddenException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.Lote;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.TransferenciaFarmacia;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.CancelamentoTransferenciaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.RecebimentoTransferenciaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.TransferenciaFarmaciaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.TransferenciaFarmaciaResponseDto;
import com.edufelizardo.maissaudepublica.models.enuns.StatusTransferenciaFarmacia;
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
import java.util.stream.Stream;

/**
 * Transferência de estoque entre unidades em duas etapas (ADR-0059, revista pela ADR-0061): envio
 * (saída na origem, fica em trânsito), recebimento conferido no destino por outro profissional (entrada
 * do que chegou, com divergência registrada) ou cancelamento antes do recebimento (estorno à origem).
 * Todo movimento de saldo passa pelo livro (ADR-0057).
 *
 * <p>Ordem das travas, a mesma em todo o estoque: transferência → unidade → lotes (em ordem de id).
 */
@Service
public class TransferenciaFarmaciaService {

    @Autowired
    private TransferenciaFarmaciaRepository transferenciaRepository;

    @Autowired
    private ControleDeAcesso controleDeAcesso;

    @Autowired
    private LoteRepository loteRepository;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private MovimentacaoFarmaciaService movimentacaoFarmaciaService;

    /** Envio: tira o saldo do lote de origem e deixa a transferência em trânsito. */
    @Transactional
    public TransferenciaFarmaciaResponseDto enviar(TransferenciaFarmaciaRequestDto dto) {
        UnidadeDeSaude unidadeDestino = unidadeDeSaudeRepository.findById(dto.getUnidadeDestinoId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar uma unidade de saúde com o id " + dto.getUnidadeDestinoId()
                                + " em nossos registros."));
        Profissional profissional = buscarProfissional(dto.getProfissionalMatricula());
        Lote origem = travarLote(dto.getLoteOrigemId());
        controleDeAcesso.exigir("FARMACIA.TRANSFERIR", origem.getUnidade());

        if (origem.getUnidade().getUuid().equals(unidadeDestino.getUuid())) {
            throw new ResourceBadRequestException("A unidade de destino precisa ser diferente da unidade do lote de origem.");
        }
        if (origem.getValidade().isBefore(LocalDate.now())) {
            throw new ResourceUnprocessableEntityException(
                    "O lote " + origem.getNumeroLote() + " venceu em " + origem.getValidade()
                            + ": medicamento vencido não é transferido, registre a perda por vencimento.");
        }

        String observacao = textoOuNulo(dto.getObservacao());
        TransferenciaFarmacia transferencia = transferenciaRepository.save(new TransferenciaFarmacia(origem,
                unidadeDestino, dto.getQuantidade(), profissional, observacao, Instant.now(),
                movimentacaoFarmaciaService.cpfDoUsuarioAutenticado()));

        // Estoque insuficiente responde 422 e desfaz tudo, inclusive a transferência.
        movimentacaoFarmaciaService.lancar(origem, TipoMovimentacaoFarmacia.TRANSFERENCIA_SAIDA, -dto.getQuantidade(),
                profissional, null, observacao, null, transferencia);
        return TransferenciaFarmaciaResponseDto.fromTransferencia(transferencia);
    }

    /**
     * Recebimento conferido no destino: a quantidade que chegou entra no lote da mesma remessa na
     * unidade de destino (criado se ainda não existir, ADR-0060). Chegar menos exige motivo e
     * justificativa; a diferença fica registrada na transferência.
     */
    @Transactional
    public TransferenciaFarmaciaResponseDto receber(UUID uuid, RecebimentoTransferenciaRequestDto dto) {
        TransferenciaFarmacia transferencia = travarEmTransito(uuid, "receber");
        // Só quem responde pela unidade de destino confere o que chegou (ADR-0061, ADR-0067).
        controleDeAcesso.exigir("FARMACIA.TRANSFERIR", transferencia.getUnidadeDestino());
        Profissional recebedor = buscarProfissional(dto.getProfissionalMatricula());
        if (recebedor.getUuid().equals(transferencia.getProfissional().getUuid())) {
            throw new ResourceUnprocessableEntityException(
                    "O recebimento precisa ser conferido por outro profissional, não por quem enviou a transferência.");
        }

        int recebida = dto.getQuantidadeRecebida();
        if (recebida > transferencia.getQuantidade()) {
            throw new ResourceUnprocessableEntityException("Foram enviadas " + transferencia.getQuantidade()
                    + " unidades; não é possível receber " + recebida + ".");
        }
        String justificativa = textoOuNulo(dto.getJustificativaDivergencia());
        boolean divergente = recebida < transferencia.getQuantidade();
        if (divergente && (dto.getMotivoDivergencia() == null || justificativa == null)) {
            throw new ResourceBadRequestException(
                    "Recebimento com quantidade menor que a enviada exige motivoDivergencia e justificativaDivergencia.");
        }

        if (recebida > 0) {
            Lote origem = transferencia.getLoteOrigem();
            UnidadeDeSaude unidadeDestino = unidadeDeSaudeRepository
                    .findByIdParaMovimentarEstoque(transferencia.getUnidadeDestino().getUuid())
                    .orElseThrow();
            UUID destinoId = loteRepository.findIdsDaRemessaNaUnidade(origem.getMedicamento().getUuid(),
                    unidadeDestino.getUuid(), origem.getNumeroLote(), origem.getValidade()).stream().findFirst().orElse(null);
            Lote destino = destinoId != null
                    ? travarLote(destinoId)
                    : loteRepository.save(new Lote(origem.getMedicamento(), unidadeDestino, origem.getNumeroLote(),
                    origem.getValidade(), 0));
            String registro = divergente
                    ? "Recebida com divergência: " + (transferencia.getQuantidade() - recebida) + " de "
                    + transferencia.getQuantidade() + " não chegaram (" + dto.getMotivoDivergencia() + ")."
                    : transferencia.getObservacao();
            movimentacaoFarmaciaService.lancar(destino, TipoMovimentacaoFarmacia.TRANSFERENCIA_ENTRADA, recebida,
                    recebedor, null, registro, null, transferencia);
            transferencia.setLoteDestino(destino);
        }

        transferencia.setStatus(divergente ? StatusTransferenciaFarmacia.RECEBIDA_COM_DIVERGENCIA
                : StatusTransferenciaFarmacia.RECEBIDA);
        transferencia.setQuantidadeRecebida(recebida);
        transferencia.setProfissionalRecebimento(recebedor);
        transferencia.setMotivoDivergencia(divergente ? dto.getMotivoDivergencia() : null);
        transferencia.setJustificativaDivergencia(divergente ? justificativa : null);
        transferencia.setRecebidoEm(Instant.now());
        transferencia.setRecebidoPorCpf(movimentacaoFarmaciaService.cpfDoUsuarioAutenticado());
        return TransferenciaFarmaciaResponseDto.fromTransferencia(transferenciaRepository.save(transferencia));
    }

    /** Cancelamento antes do recebimento: o saldo volta ao lote de origem. */
    @Transactional
    public TransferenciaFarmaciaResponseDto cancelar(UUID uuid, CancelamentoTransferenciaRequestDto dto) {
        TransferenciaFarmacia transferencia = travarEmTransito(uuid, "cancelar");
        controleDeAcesso.exigir("FARMACIA.TRANSFERIR", transferencia.getLoteOrigem().getUnidade());
        Profissional profissional = buscarProfissional(dto.getProfissionalMatricula());
        String motivo = dto.getMotivo().trim();

        Lote origem = travarLote(transferencia.getLoteOrigem().getUuid());
        movimentacaoFarmaciaService.lancar(origem, TipoMovimentacaoFarmacia.TRANSFERENCIA_ESTORNO,
                transferencia.getQuantidade(), profissional, null, "Transferência cancelada: " + motivo, null,
                transferencia);

        transferencia.setStatus(StatusTransferenciaFarmacia.CANCELADA);
        transferencia.setProfissionalCancelamento(profissional);
        transferencia.setMotivoCancelamento(motivo);
        transferencia.setCanceladoEm(Instant.now());
        transferencia.setCanceladoPorCpf(movimentacaoFarmaciaService.cpfDoUsuarioAutenticado());
        return TransferenciaFarmaciaResponseDto.fromTransferencia(transferenciaRepository.save(transferencia));
    }

    /** Da mais recente para a mais antiga, com filtros opcionais por status e unidade. */
    @Transactional(readOnly = true)
    public List<TransferenciaFarmaciaResponseDto> listar(StatusTransferenciaFarmacia status, UUID unidadeOrigemId,
                                                         UUID unidadeDestinoId) {
        return controleDeAcesso.filtrar(transferenciaRepository.findAllByOrderByRegistradoEmDesc().stream(),
                        t -> Stream.of(t.getLoteOrigem().getUnidade(), t.getUnidadeDestino()), "FARMACIA.CONSULTAR", "FARMACIA.TRANSFERIR")
                .filter(t -> status == null || t.getStatus() == status)
                .filter(t -> unidadeOrigemId == null || t.getLoteOrigem().getUnidade().getUuid().equals(unidadeOrigemId))
                .filter(t -> unidadeDestinoId == null
                        || (t.getUnidadeDestino() != null && t.getUnidadeDestino().getUuid().equals(unidadeDestinoId)))
                .map(TransferenciaFarmaciaResponseDto::fromTransferencia)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TransferenciaFarmaciaResponseDto buscarPorId(UUID uuid) {
        return transferenciaRepository.findById(uuid)
                .map(t -> {
                    controleDeAcesso.filtrar(Stream.of(t), x -> Stream.of(x.getLoteOrigem().getUnidade(), x.getUnidadeDestino()),
                                    "FARMACIA.CONSULTAR", "FARMACIA.TRANSFERIR")
                            .findAny()
                            .orElseThrow(() -> new ResourceForbiddenException("Esta transferência é de unidades fora do seu acesso."));
                    return TransferenciaFarmaciaResponseDto.fromTransferencia(t);
                })
                .orElseThrow(() -> naoEncontrada(uuid));
    }

    private TransferenciaFarmacia travarEmTransito(UUID uuid, String acao) {
        TransferenciaFarmacia transferencia = transferenciaRepository.findByIdParaAtualizar(uuid)
                .orElseThrow(() -> naoEncontrada(uuid));
        if (transferencia.getStatus() != StatusTransferenciaFarmacia.EM_TRANSITO) {
            throw new ResourceUnprocessableEntityException("Só é possível " + acao
                    + " uma transferência em trânsito; esta está " + transferencia.getStatus() + ".");
        }
        return transferencia;
    }

    private ResourceNotFoundException naoEncontrada(UUID uuid) {
        return new ResourceNotFoundException(
                "Não foi possível encontrar uma transferência com o id " + uuid + " em nossos registros.");
    }

    private Profissional buscarProfissional(String matricula) {
        return profissionalRepository.findByMatricula(matricula)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um profissional com a matrícula " + matricula + " em nossos registros."));
    }

    private Lote travarLote(UUID loteId) {
        return loteRepository.findByIdParaMovimentar(loteId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um lote com o id " + loteId + " em nossos registros."));
    }

    private static String textoOuNulo(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }
}
