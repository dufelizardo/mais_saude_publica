package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.CamaraRefrigeracao;
import com.edufelizardo.maissaudepublica.models.FrascoAberto;
import com.edufelizardo.maissaudepublica.models.LoteImunobiologico;
import com.edufelizardo.maissaudepublica.models.RemanejamentoImunobiologico;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.MotivoRemanejamentoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.RecebimentoRemanejamentoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.RemanejamentoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ImunizacaoRedeDtos;
import com.edufelizardo.maissaudepublica.models.enuns.MotivoRemanejamento;
import com.edufelizardo.maissaudepublica.models.enuns.MotivoSuspeitaLote;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoLoteImunobiologico;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoRemanejamento;
import com.edufelizardo.maissaudepublica.models.enuns.TipoMovimentacaoImunobiologico;
import com.edufelizardo.maissaudepublica.repositories.CamaraRefrigeracaoRepository;
import com.edufelizardo.maissaudepublica.repositories.FrascoAbertoRepository;
import com.edufelizardo.maissaudepublica.repositories.LoteImunobiologicoRepository;
import com.edufelizardo.maissaudepublica.repositories.RemanejamentoImunobiologicoRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Remanejamento de imunobiológico entre salas de vacina, I6 da Imunização (ADR-0206), em duas etapas como a transferência da
 * Farmácia (ADR-0061): a coordenação da origem envia (as doses saem do lote e ficam em trânsito); a sala do destino recebe, com a
 * temperatura da caixa na chegada (fora de 2 a 8 °C, o lote chega sob suspeita), ou recusa; a origem pode cancelar enquanto enviado.
 */
@Service
public class RemanejamentoVacinaService {

    static final BigDecimal MINIMA = new BigDecimal("2.0");
    static final BigDecimal MAXIMA = new BigDecimal("8.0");

    @Autowired private RemanejamentoImunobiologicoRepository repository;
    @Autowired private LoteImunobiologicoRepository loteRepository;
    @Autowired private FrascoAbertoRepository frascoRepository;
    @Autowired private CamaraRefrigeracaoRepository camaraRepository;
    @Autowired private UnidadeDeSaudeRepository unidadeRepository;
    @Autowired private EstoqueVacinaService estoqueService;
    @Autowired private ControleDeAcesso controleDeAcesso;

    @Transactional(readOnly = true)
    public List<ImunizacaoRedeDtos.Remanejamento> daUnidade(UUID unidadeId) {
        UnidadeDeSaude u = unidade(unidadeId);
        estoqueService.exigirLeitura(u);
        return repository.findDaUnidade(unidadeId).stream().map(RemanejamentoVacinaService::dto).toList();
    }

    /** O envio: lote disponível, na validade, com as doses fora dos frascos abertos; o destino com sala de vacina. */
    @Transactional
    public ImunizacaoRedeDtos.Remanejamento enviar(RemanejamentoRequestDto dto) {
        LoteImunobiologico l = estoqueService.loteComTrava(dto.getLoteId());
        controleDeAcesso.exigir(ImunizacaoCatalogoService.GERENCIAR, l.getUnidade());
        UnidadeDeSaude destino = unidade(dto.getUnidadeDestinoId());
        if (destino.getUuid().equals(l.getUnidade().getUuid())) {
            throw new ResourceBadRequestException("O destino é a própria unidade do lote.");
        }
        if (dto.getMotivo() == MotivoRemanejamento.OUTRO && texto(dto.getObservacao()) == null) {
            throw new ResourceBadRequestException("No motivo \"outro\", descreva na observação.");
        }
        estoqueService.exigirSala(destino);
        if (l.getSituacao() != SituacaoLoteImunobiologico.DISPONIVEL) {
            throw new ResourceUnprocessableEntityException("O lote " + l.getNumeroLote() + " não está disponível: lote sob suspeita ou inutilizado não sai.");
        }
        if (l.getValidade().isBefore(LocalDate.now())) {
            throw new ResourceUnprocessableEntityException("O lote " + l.getNumeroLote() + " venceu em " + l.getValidade() + ".");
        }
        int noFrasco = frascoRepository.findByLote_UuidAndEncerradoEmIsNullOrderByAbertoEmAsc(l.getUuid()).stream()
                .mapToInt(f -> f.getDosesNoFrasco() - f.getDosesUsadas()).sum();
        int livres = l.getDoses() - noFrasco;
        if (dto.getDoses() > livres) {
            throw new ResourceUnprocessableEntityException("O lote tem " + livres + " doses fora dos frascos abertos: o remanejamento não passa disso.");
        }
        RemanejamentoImunobiologico r = new RemanejamentoImunobiologico();
        r.setLoteOrigem(l);
        r.setUnidadeDestino(destino);
        r.setDoses(dto.getDoses());
        r.setMotivo(dto.getMotivo());
        r.setObservacao(texto(dto.getObservacao()));
        r.setSituacao(SituacaoRemanejamento.ENVIADO);
        r.setEnviadoEm(Instant.now());
        r.setEnviadoPorCpf(UsuarioAutenticado.cpf());
        r = repository.save(r);
        l.setDoses(l.getDoses() - dto.getDoses());
        loteRepository.save(l);
        estoqueService.lancar(l, TipoMovimentacaoImunobiologico.REMANEJAMENTO_SAIDA, -dto.getDoses(), null,
                "Para " + destino.getNome() + ".", null, null, r);
        ContextoAuditoria.registro(r.getUuid());
        return dto(r);
    }

    /**
     * O recebimento no destino: o lote é criado ou somado (o mesmo número, com a mesma validade); a caixa fora de 2 a 8 °C deixa o
     * lote sob suspeita pela excursão; o número suspenso na rede chega sob suspeita pelo ESAVI.
     */
    @Transactional
    public ImunizacaoRedeDtos.Remanejamento receber(UUID id, RecebimentoRemanejamentoRequestDto dto) {
        RemanejamentoImunobiologico r = enviado(id);
        UnidadeDeSaude destino = r.getUnidadeDestino();
        controleDeAcesso.exigir(VacinacaoService.REGISTRAR, destino);
        LoteImunobiologico origem = r.getLoteOrigem();
        if (origem.getValidade().isBefore(LocalDate.now())) {
            throw new ResourceUnprocessableEntityException("O lote " + origem.getNumeroLote() + " venceu em trânsito: recuse o remanejamento.");
        }
        CamaraRefrigeracao camara = null;
        if (dto.getCamaraId() != null) {
            camara = camaraRepository.findById(dto.getCamaraId()).orElseThrow(() -> new ResourceNotFoundException(
                    "Não foi possível encontrar a câmara " + dto.getCamaraId() + " em nossos registros."));
            if (!camara.getUnidade().getUuid().equals(destino.getUuid()) || !camara.isAtiva()) {
                throw new ResourceUnprocessableEntityException("A câmara " + camara.getNome() + " não está ativa na unidade de destino.");
            }
        }
        Optional<LoteImunobiologico> existente = loteRepository.findByUnidade_UuidAndImunobiologico_UuidAndNumeroLote(destino.getUuid(),
                origem.getImunobiologico().getUuid(), origem.getNumeroLote());
        LoteImunobiologico l;
        if (existente.isPresent()) {
            l = estoqueService.loteComTrava(existente.get().getUuid());
            if (l.getSituacao() == SituacaoLoteImunobiologico.INUTILIZADO) {
                throw new ResourceUnprocessableEntityException("O lote " + l.getNumeroLote() + " foi inutilizado nesta unidade: recuse o remanejamento.");
            }
            if (!l.getValidade().equals(origem.getValidade())) {
                throw new ResourceUnprocessableEntityException("O lote " + l.getNumeroLote() + " já está no estoque com outra validade: recuse e confira.");
            }
            if (camara != null) {
                l.setCamara(camara);
            }
        } else {
            l = new LoteImunobiologico();
            l.setImunobiologico(origem.getImunobiologico());
            l.setUnidade(destino);
            l.setNumeroLote(origem.getNumeroLote());
            l.setFabricante(origem.getFabricante());
            l.setValidade(origem.getValidade());
            l.setCamara(camara);
            l.setSituacao(SituacaoLoteImunobiologico.DISPONIVEL);
        }
        l.setDoses(l.getDoses() + r.getDoses());
        boolean fora = foraDaFaixa(dto.getTemperaturaChegada());
        if (fora && l.getSituacao() == SituacaoLoteImunobiologico.DISPONIVEL) {
            l.setSituacao(SituacaoLoteImunobiologico.SOB_SUSPEITA);
            l.setMotivoSuspeita(MotivoSuspeitaLote.EXCURSAO_TEMPERATURA);
            l.setSobSuspeitaDesde(Instant.now());
            l.setParecer(null);
            l.setAvaliadoEm(null);
            l.setAvaliadoPorCpf(null);
        } else if (fora && l.getMotivoSuspeita() == MotivoSuspeitaLote.ESAVI_LOTE) {
            l.setMotivoSuspeita(MotivoSuspeitaLote.EXCURSAO_TEMPERATURA);
        }
        estoqueService.aplicarSuspensaoSeVigente(l);
        l = loteRepository.save(l);
        r.setLoteDestino(l);
        r.setCamaraDestino(camara);
        r.setTemperaturaChegada(dto.getTemperaturaChegada());
        concluir(r, SituacaoRemanejamento.RECEBIDO);
        estoqueService.lancar(l, TipoMovimentacaoImunobiologico.REMANEJAMENTO_ENTRADA, r.getDoses(), null,
                "De " + origem.getUnidade().getNome() + (fora ? "; caixa a " + dto.getTemperaturaChegada().toPlainString() + " °C na chegada." : "."), null, null, r);
        ContextoAuditoria.registro(r.getUuid());
        return dto(repository.save(r));
    }

    /** O destino recusa, com o motivo: as doses voltam ao lote de origem. */
    @Transactional
    public ImunizacaoRedeDtos.Remanejamento recusar(UUID id, MotivoRemanejamentoRequestDto dto) {
        RemanejamentoImunobiologico r = enviado(id);
        controleDeAcesso.exigir(VacinacaoService.REGISTRAR, r.getUnidadeDestino());
        r.setMotivoRecusa(dto.getMotivo().trim());
        devolver(r, "Recusado por " + r.getUnidadeDestino().getNome() + ": " + dto.getMotivo().trim());
        concluir(r, SituacaoRemanejamento.RECUSADO);
        ContextoAuditoria.registro(r.getUuid());
        return dto(repository.save(r));
    }

    /** A origem cancela enquanto enviado: as doses voltam ao lote. */
    @Transactional
    public ImunizacaoRedeDtos.Remanejamento cancelar(UUID id) {
        RemanejamentoImunobiologico r = enviado(id);
        controleDeAcesso.exigir(ImunizacaoCatalogoService.GERENCIAR, r.getLoteOrigem().getUnidade());
        devolver(r, "Remanejamento cancelado pela origem.");
        concluir(r, SituacaoRemanejamento.CANCELADO);
        ContextoAuditoria.registro(r.getUuid());
        return dto(repository.save(r));
    }

    // ── Apoio ──────────────────────────────────────────────────────────────────────────────────

    static boolean foraDaFaixa(BigDecimal t) {
        return t != null && (t.compareTo(MINIMA) < 0 || t.compareTo(MAXIMA) > 0);
    }

    private void devolver(RemanejamentoImunobiologico r, String justificativa) {
        LoteImunobiologico l = estoqueService.loteComTrava(r.getLoteOrigem().getUuid());
        l.setDoses(l.getDoses() + r.getDoses());
        loteRepository.save(l);
        estoqueService.lancar(l, TipoMovimentacaoImunobiologico.ESTORNO_REMANEJAMENTO, r.getDoses(), null, justificativa, null, null, r);
    }

    private static void concluir(RemanejamentoImunobiologico r, SituacaoRemanejamento s) {
        r.setSituacao(s);
        r.setConcluidoEm(Instant.now());
        r.setConcluidoPorCpf(UsuarioAutenticado.cpf());
    }

    private RemanejamentoImunobiologico enviado(UUID id) {
        RemanejamentoImunobiologico r = repository.findComTrava(id).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o remanejamento " + id + " em nossos registros."));
        if (r.getSituacao() != SituacaoRemanejamento.ENVIADO) {
            throw new ResourceUnprocessableEntityException("O remanejamento já foi " + r.getSituacao().name().toLowerCase() + ".");
        }
        return r;
    }

    private UnidadeDeSaude unidade(UUID id) {
        return unidadeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar uma unidade de saúde com o id " + id + " em nossos registros."));
    }

    private static String texto(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    static ImunizacaoRedeDtos.Remanejamento dto(RemanejamentoImunobiologico r) {
        LoteImunobiologico l = r.getLoteOrigem();
        return new ImunizacaoRedeDtos.Remanejamento(r.getUuid(), l.getUuid(), l.getUnidade().getUuid(), l.getUnidade().getNome(),
                r.getUnidadeDestino().getUuid(), r.getUnidadeDestino().getNome(), l.getImunobiologico().getUuid(), l.getImunobiologico().getSigla(),
                l.getNumeroLote(), l.getValidade(), r.getDoses(), r.getMotivo(), r.getObservacao(), r.getSituacao(), r.getEnviadoEm(), r.getConcluidoEm(),
                r.getTemperaturaChegada(), foraDaFaixa(r.getTemperaturaChegada()), r.getMotivoRecusa(),
                r.getLoteDestino() != null ? r.getLoteDestino().getUuid() : null);
    }
}
