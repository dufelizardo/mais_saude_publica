package com.edufelizardo.maissaudepublica.services.version1;

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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

    /** Mesmo que o anterior, ligando o lançamento à transferência que o originou (ADR-0059). */
    @Transactional
    public MovimentacaoFarmacia lancar(Lote lote, TipoMovimentacaoFarmacia tipo, int variacao, Profissional profissional,
                                       MotivoPerda motivoPerda, String justificativa, Dispensacao dispensacao,
                                       TransferenciaFarmacia transferencia) {
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
                motivoPerda, justificativa, profissional, dispensacao, transferencia, Instant.now(), cpfDoUsuarioAutenticado());
        return movimentacaoRepository.save(movimentacao);
    }

    @Transactional
    public MovimentacaoFarmaciaResponseDto registrar(MovimentacaoFarmaciaRequestDto dto) {
        TipoMovimentacaoFarmacia tipo = dto.getTipo();
        if (tipo != TipoMovimentacaoFarmacia.PERDA && tipo != TipoMovimentacaoFarmacia.AJUSTE_INVENTARIO) {
            throw new ResourceBadRequestException(
                    "Só é possível lançar PERDA ou AJUSTE_INVENTARIO: entrada, dispensação e transferência são registradas pelo próprio sistema.");
        }

        Lote lote = loteRepository.findByIdParaMovimentar(dto.getLoteId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um lote com o id " + dto.getLoteId() + " em nossos registros."));
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
        List<MovimentacaoFarmaciaResponseDto> extrato = movimentacaoRepository.findByLote_UuidOrderByRegistradoEmAsc(loteId)
                .stream()
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
                .map(MovimentacaoFarmaciaResponseDto::fromMovimentacao)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar uma movimentação com o id " + uuid + " em nossos registros."));
    }

    /** Principal colocado pelo JwtAuthenticationFilter; com o toggle desligado a requisição é anônima. */
    String cpfDoUsuarioAutenticado() {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacao instanceof UsernamePasswordAuthenticationToken && autenticacao.getPrincipal() instanceof String cpf) {
            return cpf;
        }
        return null;
    }
}
