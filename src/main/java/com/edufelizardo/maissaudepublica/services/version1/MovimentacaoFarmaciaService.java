package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.models.AdministracaoMedicamento;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.Dispensacao;
import com.edufelizardo.maissaudepublica.models.Lote;
import com.edufelizardo.maissaudepublica.models.MovimentacaoFarmacia;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.TransferenciaFarmacia;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.MovimentacaoFarmaciaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.MovimentacaoFarmaciaResponseDto;
import com.edufelizardo.maissaudepublica.models.enuns.MotivoPerda;
import com.edufelizardo.maissaudepublica.models.enuns.TipoMovimentacaoFarmacia;
import com.edufelizardo.maissaudepublica.repositories.LoteRepository;
import com.edufelizardo.maissaudepublica.repositories.MovimentacaoFarmaciaRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Livro de movimentação do estoque de lotes (ADR-0057) — único ponto que altera
 * {@code Lote.quantidade}. {@link LoteService} (entrada), {@link DispensacaoService} (dispensação) e
 * {@link TransferenciaFarmaciaService} (transferência entre unidades, ADR-0059) lançam por
 * {@link #lancar}; perdas e ajustes de inventário chegam por {@link #registrar}.
 */
@Service
public class MovimentacaoFarmaciaService {

    @Autowired
    private MovimentacaoFarmaciaRepository movimentacaoRepository;

    @Autowired
    private ControleDeAcesso controleDeAcesso;

    @Autowired
    private LoteRepository loteRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    /**
     * Aplica a variação ao saldo do lote e registra o lançamento. Precisa rodar dentro da transação
     * de quem chama, com o lote obtido por {@link LoteRepository#findByIdParaMovimentar} (ou recém
     * criado) — é isso que impede duas operações de lerem o mesmo saldo.
     */
    @Transactional
    public MovimentacaoFarmacia lancar(Lote lote, TipoMovimentacaoFarmacia tipo, int variacao, Profissional profissional,
                                       MotivoPerda motivoPerda, String justificativa, Dispensacao dispensacao) {
        return lancar(lote, tipo, variacao, profissional, motivoPerda, justificativa, dispensacao, null);
    }

    /** Lançamento de uma administração ao paciente, ou do seu estorno (ADR-0064). */
    @Transactional
    public MovimentacaoFarmacia lancarAdministracao(Lote lote, TipoMovimentacaoFarmacia tipo, int variacao,
                                                    Profissional profissional, String justificativa,
                                                    AdministracaoMedicamento administracao) {
        return lancar(lote, tipo, variacao, profissional, null, justificativa, null, null, administracao);
    }

    @Transactional
    public MovimentacaoFarmacia lancar(Lote lote, TipoMovimentacaoFarmacia tipo, int variacao, Profissional profissional,
                                       MotivoPerda motivoPerda, String justificativa, Dispensacao dispensacao,
                                       TransferenciaFarmacia transferencia) {
        return lancar(lote, tipo, variacao, profissional, motivoPerda, justificativa, dispensacao, transferencia, null);
    }

    /** Forma completa: liga o lançamento à transferência (ADR-0059) ou à administração (ADR-0064) que o originou. */
    @Transactional
    public MovimentacaoFarmacia lancar(Lote lote, TipoMovimentacaoFarmacia tipo, int variacao, Profissional profissional,
                                       MotivoPerda motivoPerda, String justificativa, Dispensacao dispensacao,
                                       TransferenciaFarmacia transferencia, AdministracaoMedicamento administracao) {
        if (lote.getLoteIncorporador() != null) {
            throw new ResourceUnprocessableEntityException(
                    "O lote " + lote.getNumeroLote() + " foi incorporado ao lote " + lote.getLoteIncorporador().getUuid()
                            + " (mesma remessa na mesma unidade) e não aceita mais movimentação.");
        }
        int saldoApos = lote.getQuantidade() + variacao;
        if (saldoApos < 0) {
            throw new ResourceUnprocessableEntityException(
                    "Estoque insuficiente no lote " + lote.getNumeroLote() + ": disponível " + lote.getQuantidade()
                            + ", solicitado " + (-variacao) + ".");
        }
        lote.setQuantidade(saldoApos);
        loteRepository.save(lote);

        MovimentacaoFarmacia movimentacao = new MovimentacaoFarmacia(null, lote, tipo, variacao, saldoApos,
                motivoPerda, justificativa, profissional, dispensacao, transferencia, administracao, Instant.now(),
                cpfDoUsuarioAutenticado());
        return movimentacaoRepository.save(movimentacao);
    }

    @Transactional
    public MovimentacaoFarmaciaResponseDto registrar(MovimentacaoFarmaciaRequestDto dto) {
        TipoMovimentacaoFarmacia tipo = dto.getTipo();
        if (tipo != TipoMovimentacaoFarmacia.PERDA && tipo != TipoMovimentacaoFarmacia.AJUSTE_INVENTARIO) {
            throw new ResourceBadRequestException(
                    "Só é possível lançar PERDA ou AJUSTE_INVENTARIO: entrada, dispensação, transferência e administração são registradas pelo próprio sistema.");
        }

        Lote lote = loteRepository.findByIdParaMovimentar(dto.getLoteId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um lote com o id " + dto.getLoteId() + " em nossos registros."));
        controleDeAcesso.exigir("FARMACIA.GERENCIAR_ESTOQUE", lote.getUnidade());
        Profissional profissional = profissionalRepository.findByMatricula(dto.getProfissionalMatricula())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um profissional com a matrícula " + dto.getProfissionalMatricula()
                                + " em nossos registros."));
        String justificativa = dto.getJustificativa() == null || dto.getJustificativa().isBlank()
                ? null : dto.getJustificativa().trim();

        MovimentacaoFarmacia movimentacao;
        if (tipo == TipoMovimentacaoFarmacia.PERDA) {
            if (dto.getQuantidade() == null || dto.getMotivoPerda() == null) {
                throw new ResourceBadRequestException("Perda exige quantidade e motivoPerda.");
            }
            if (dto.getMotivoPerda() == MotivoPerda.OUTRO && justificativa == null) {
                throw new ResourceBadRequestException("Perda com motivo OUTRO exige justificativa.");
            }
            movimentacao = lancar(lote, tipo, -dto.getQuantidade(), profissional, dto.getMotivoPerda(), justificativa, null);
        } else {
            if (dto.getSaldoContado() == null || justificativa == null) {
                throw new ResourceBadRequestException("Ajuste de inventário exige saldoContado e justificativa.");
            }
            movimentacao = lancar(lote, tipo, dto.getSaldoContado() - lote.getQuantidade(), profissional, null,
                    justificativa, null);
        }
        return MovimentacaoFarmaciaResponseDto.fromMovimentacao(movimentacao);
    }

    /** Extrato do lote, do lançamento mais antigo ao mais recente. */
    @Transactional(readOnly = true)
    public List<MovimentacaoFarmaciaResponseDto> extratoDoLote(UUID loteId) {
        List<MovimentacaoFarmaciaResponseDto> extrato = controleDeAcesso.filtrar(
                        movimentacaoRepository.findByLote_UuidOrderByRegistradoEmAsc(loteId).stream(),
                        m -> Stream.of(m.getLote().getUnidade()), "FARMACIA.CONSULTAR")
                .map(MovimentacaoFarmaciaResponseDto::fromMovimentacao)
                .collect(Collectors.toList());
        if (extrato.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Não foi possível encontrar movimentações para o lote de id " + loteId + " em nossos registros.");
        }
        return extrato;
    }

    @Transactional(readOnly = true)
    public MovimentacaoFarmaciaResponseDto buscarPorId(UUID uuid) {
        return movimentacaoRepository.findById(uuid)
                .map(m -> {
                    controleDeAcesso.exigirVisivel(m.getLote().getUnidade(), "FARMACIA.CONSULTAR");
                    return MovimentacaoFarmaciaResponseDto.fromMovimentacao(m);
                })
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar uma movimentação com o id " + uuid + " em nossos registros."));
    }

    /** CPF do usuário autenticado — ver {@link UsuarioAutenticado}. */
    String cpfDoUsuarioAutenticado() {
        return UsuarioAutenticado.cpf();
    }
}
