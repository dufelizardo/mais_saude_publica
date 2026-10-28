package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceForbiddenException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.CamaraRefrigeracao;
import com.edufelizardo.maissaudepublica.models.FrascoAberto;
import com.edufelizardo.maissaudepublica.models.Imunobiologico;
import com.edufelizardo.maissaudepublica.models.LoteImunobiologico;
import com.edufelizardo.maissaudepublica.models.MovimentacaoImunobiologico;
import com.edufelizardo.maissaudepublica.models.RegistroVacinacao;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AjusteLoteRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EntradaLoteRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.PerdaLoteRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.EstoqueSalaVacinaDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.FrascoAbertoDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.LoteImunobiologicoDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.MovimentacaoImunobiologicoDto;
import com.edufelizardo.maissaudepublica.models.enuns.MotivoEncerramentoFrasco;
import com.edufelizardo.maissaudepublica.models.enuns.MotivoPerdaImunobiologico;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoLoteImunobiologico;
import com.edufelizardo.maissaudepublica.models.enuns.TipoMovimentacaoImunobiologico;
import com.edufelizardo.maissaudepublica.repositories.CamaraRefrigeracaoRepository;
import com.edufelizardo.maissaudepublica.repositories.FrascoAbertoRepository;
import com.edufelizardo.maissaudepublica.repositories.ImunobiologicoRepository;
import com.edufelizardo.maissaudepublica.repositories.LoteImunobiologicoRepository;
import com.edufelizardo.maissaudepublica.repositories.MovimentacaoImunobiologicoRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeCapacidadeAssistencialRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * Estoque da sala de vacina, fatia I2 da Imunização (ADR-0148): o lote de imunobiológico por unidade, em doses, com o
 * livro de movimentação imutável (como o da Farmácia, ADR-0057). Só a unidade com a capacidade {@value #SALA_VACINA}
 * controla estoque; nela, a dose aplicada sai de um lote, com a baixa automática, e o frasco multidose aberto tem prazo:
 * o que sobra no vencimento é perda técnica, apurada na próxima consulta ou aplicação.
 */
@Service
public class EstoqueVacinaService {

    public static final String SALA_VACINA = "SALA_VACINA";

    @Autowired private LoteImunobiologicoRepository loteRepository;
    @Autowired private MovimentacaoImunobiologicoRepository movimentacaoRepository;
    @Autowired private FrascoAbertoRepository frascoRepository;
    @Autowired private CamaraRefrigeracaoRepository camaraRepository;
    @Autowired private ImunobiologicoRepository imunobiologicoRepository;
    @Autowired private UnidadeDeSaudeRepository unidadeRepository;
    @Autowired private UnidadeCapacidadeAssistencialRepository capacidadeRepository;
    @Autowired private ControleDeAcesso controleDeAcesso;

    public boolean temSalaDeVacina(UUID unidadeId) {
        return capacidadeRepository.existsByUnidade_UuidAndCapacidade_CodigoAndFimIsNull(unidadeId, SALA_VACINA);
    }

    // ── Painel ─────────────────────────────────────────────────────────────────────────────────

    /** O saldo aplicável por vacina, os lotes, os frascos abertos e o mês (aplicadas e perdas). Apura os frascos vencidos. */
    @Transactional
    public EstoqueSalaVacinaDto estoque(UUID unidadeId) {
        UnidadeDeSaude u = unidade(unidadeId);
        exigirLeitura(u);
        boolean sala = temSalaDeVacina(unidadeId);
        if (!sala) {
            return new EstoqueSalaVacinaDto(u.getUuid(), u.getNome(), false, List.of(), List.of(), List.of(), 0, 0, 0);
        }
        LocalDateTime agora = LocalDateTime.now();
        frascoRepository.findAbertosDaUnidade(unidadeId).stream().filter(f -> !f.getValidoAte().isAfter(agora))
                .forEach(f -> encerrarVencido(f, agora));
        LocalDate hoje = agora.toLocalDate();
        List<LoteImunobiologico> lotes = loteRepository.findDaUnidade(unidadeId);
        Map<UUID, EstoqueSalaVacinaDto.Saldo> porVacina = new LinkedHashMap<>();
        lotes.stream().filter(l -> aplicavel(l, hoje))
                .sorted(Comparator.comparing((LoteImunobiologico l) -> l.getImunobiologico().getNome()))
                .forEach(l -> {
                    Imunobiologico i = l.getImunobiologico();
                    EstoqueSalaVacinaDto.Saldo s = porVacina.computeIfAbsent(i.getUuid(),
                            k -> new EstoqueSalaVacinaDto.Saldo(i.getUuid(), i.getSigla(), i.getNome(), 0, 0));
                    s.setDoses(s.getDoses() + l.getDoses());
                    s.setLotes(s.getLotes() + 1);
                });
        List<FrascoAbertoDto> frascos = frascoRepository.findAbertosDaUnidade(unidadeId).stream().map(f -> dto(f, agora)).toList();
        Instant inicioMes = hoje.withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
        long aplicadas = 0, tecnica = 0, fisica = 0;
        for (MovimentacaoImunobiologico m : movimentacaoRepository.findDaUnidadeDesde(unidadeId, inicioMes)) {
            switch (m.getTipo()) {
                case APLICACAO -> aplicadas -= m.getDoses();
                case ESTORNO_APLICACAO -> aplicadas -= m.getDoses();
                case PERDA_TECNICA -> tecnica -= m.getDoses();
                case PERDA_FISICA -> fisica -= m.getDoses();
                default -> { }
            }
        }
        return new EstoqueSalaVacinaDto(u.getUuid(), u.getNome(), true, new ArrayList<>(porVacina.values()),
                lotes.stream().filter(l -> l.getSituacao() != SituacaoLoteImunobiologico.INUTILIZADO && l.getDoses() > 0)
                        .map(l -> dto(l, hoje)).toList(),
                frascos, aplicadas, tecnica, fisica);
    }

    @Transactional(readOnly = true)
    public List<MovimentacaoImunobiologicoDto> movimentacoes(UUID loteId) {
        LoteImunobiologico l = lote(loteId);
        exigirLeitura(l.getUnidade());
        return movimentacaoRepository.findByLote_UuidOrderByRegistradoEmAsc(loteId).stream().map(EstoqueVacinaService::dto).toList();
    }

    // ── Entrada, perda e ajuste ────────────────────────────────────────────────────────────────

    /** Recebimento: o lote é criado ou somado (o mesmo número com a mesma validade). Só na unidade com sala de vacina (422). */
    @Transactional
    public LoteImunobiologicoDto entrada(EntradaLoteRequestDto dto) {
        UnidadeDeSaude u = unidade(dto.getUnidadeId());
        controleDeAcesso.exigir(VacinacaoService.REGISTRAR, u);
        exigirSala(u);
        Imunobiologico imuno = imunobiologicoRepository.findById(dto.getImunobiologicoId()).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o imunobiológico " + dto.getImunobiologicoId() + " em nossos registros."));
        if (!imuno.isAtivo()) {
            throw new ResourceUnprocessableEntityException("O imunobiológico " + imuno.getNome() + " está inativo no catálogo.");
        }
        if (dto.getValidade().isBefore(LocalDate.now())) {
            throw new ResourceUnprocessableEntityException("O lote está vencido: não entra no estoque.");
        }
        CamaraRefrigeracao camara = null;
        if (dto.getCamaraId() != null) {
            camara = camaraRepository.findById(dto.getCamaraId()).orElseThrow(() -> new ResourceNotFoundException(
                    "Não foi possível encontrar a câmara " + dto.getCamaraId() + " em nossos registros."));
            if (!camara.getUnidade().getUuid().equals(u.getUuid()) || !camara.isAtiva()) {
                throw new ResourceUnprocessableEntityException("A câmara " + camara.getNome() + " não está ativa nesta unidade.");
            }
        }
        String numero = dto.getNumeroLote().trim().toUpperCase(Locale.ROOT);
        Optional<LoteImunobiologico> existente = loteRepository.findByUnidade_UuidAndImunobiologico_UuidAndNumeroLote(u.getUuid(), imuno.getUuid(), numero);
        LoteImunobiologico l;
        if (existente.isPresent()) {
            l = loteRepository.findComTrava(existente.get().getUuid()).orElseThrow();
            if (l.getSituacao() == SituacaoLoteImunobiologico.INUTILIZADO) {
                throw new ResourceUnprocessableEntityException("O lote " + numero + " foi inutilizado nesta unidade.");
            }
            if (!l.getValidade().equals(dto.getValidade())) {
                throw new ResourceUnprocessableEntityException("O lote " + numero + " já está no estoque com a validade " + l.getValidade()
                        + ": confira a validade informada.");
            }
            if (camara != null) {
                l.setCamara(camara);
            }
        } else {
            l = new LoteImunobiologico();
            l.setImunobiologico(imuno);
            l.setUnidade(u);
            l.setNumeroLote(numero);
            l.setFabricante(texto(dto.getFabricante()));
            l.setValidade(dto.getValidade());
            l.setCamara(camara);
            l.setSituacao(SituacaoLoteImunobiologico.DISPONIVEL);
        }
        l.setDoses(l.getDoses() + dto.getDoses());
        l = loteRepository.save(l);
        lancar(l, TipoMovimentacaoImunobiologico.ENTRADA, dto.getDoses(), null, texto(dto.getJustificativa()), null, null);
        ContextoAuditoria.registro(l.getUuid());
        return dto(l, LocalDate.now());
    }

    /** Perda física (quebra, falha de equipamento, vencimento do lote...). A perda técnica do frasco aberto é apurada sozinha. */
    @Transactional
    public LoteImunobiologicoDto perda(UUID loteId, PerdaLoteRequestDto dto) {
        LoteImunobiologico l = loteComTrava(loteId);
        controleDeAcesso.exigir(VacinacaoService.REGISTRAR, l.getUnidade());
        if (dto.getMotivo() == MotivoPerdaImunobiologico.FRASCO_ABERTO_VENCIDO) {
            throw new ResourceBadRequestException("A perda do frasco aberto vencido é apurada pelo sistema.");
        }
        if (dto.getMotivo() == MotivoPerdaImunobiologico.OUTRO && texto(dto.getJustificativa()) == null) {
            throw new ResourceBadRequestException("Informe a justificativa da perda.");
        }
        if (dto.getDoses() > l.getDoses()) {
            throw new ResourceUnprocessableEntityException("O lote tem " + l.getDoses() + " doses: a perda não pode passar do saldo.");
        }
        l.setDoses(l.getDoses() - dto.getDoses());
        lancar(l, TipoMovimentacaoImunobiologico.PERDA_FISICA, -dto.getDoses(), dto.getMotivo(), texto(dto.getJustificativa()), null, null);
        ContextoAuditoria.registro(l.getUuid());
        return dto(loteRepository.save(l), LocalDate.now());
    }

    /** Ajuste de inventário (a contagem física), pela coordenação, com a justificativa. */
    @Transactional
    public LoteImunobiologicoDto ajuste(UUID loteId, AjusteLoteRequestDto dto) {
        LoteImunobiologico l = loteComTrava(loteId);
        controleDeAcesso.exigir(ImunizacaoCatalogoService.GERENCIAR, l.getUnidade());
        int delta = dto.getSaldo() - l.getDoses();
        if (delta == 0) {
            throw new ResourceUnprocessableEntityException("O saldo contado é igual ao do sistema: não há o que ajustar.");
        }
        l.setDoses(dto.getSaldo());
        lancar(l, TipoMovimentacaoImunobiologico.AJUSTE_INVENTARIO, delta, null, dto.getJustificativa().trim(), null, null);
        ContextoAuditoria.registro(l.getUuid());
        return dto(loteRepository.save(l), LocalDate.now());
    }

    // ── A dose aplicada (chamado pelo registro de vacinação) ───────────────────────────────────

    /**
     * Antes de gravar a dose aplicada na unidade com sala de vacina: confere o lote (da unidade, do mesmo imunobiológico,
     * disponível, na validade e com saldo; senão 422), tira uma dose e, no multidose, usa o frasco aberto ou abre outro.
     * Liga o lote e o frasco à dose; o lançamento sai em {@link #lancarAplicacao}, depois de gravada.
     */
    public void baixar(RegistroVacinacao r, UUID loteId) {
        LoteImunobiologico l = loteComTrava(loteId);
        LocalDateTime quando = r.getAplicadaEm();
        if (!l.getUnidade().getUuid().equals(r.getUnidade().getUuid())) {
            throw new ResourceUnprocessableEntityException("O lote " + l.getNumeroLote() + " é do estoque de outra unidade.");
        }
        if (!l.getImunobiologico().getUuid().equals(r.getImunobiologico().getUuid())) {
            throw new ResourceUnprocessableEntityException("O lote " + l.getNumeroLote() + " é de " + l.getImunobiologico().getSigla() + ".");
        }
        if (l.getSituacao() == SituacaoLoteImunobiologico.SOB_SUSPEITA) {
            throw new ResourceUnprocessableEntityException("O lote " + l.getNumeroLote() + " está sob suspeita (excursão de temperatura): "
                    + "não se aplica até a avaliação da coordenação.");
        }
        if (l.getSituacao() == SituacaoLoteImunobiologico.INUTILIZADO) {
            throw new ResourceUnprocessableEntityException("O lote " + l.getNumeroLote() + " foi inutilizado.");
        }
        if (l.getValidade().isBefore(quando.toLocalDate())) {
            throw new ResourceUnprocessableEntityException("O lote " + l.getNumeroLote() + " venceu em " + l.getValidade() + ".");
        }
        LocalDateTime agora = LocalDateTime.now();
        List<FrascoAberto> abertos = frascoRepository.findByLote_UuidAndEncerradoEmIsNullOrderByAbertoEmAsc(l.getUuid());
        abertos.stream().filter(f -> !f.getValidoAte().isAfter(agora)).forEach(f -> encerrarVencido(f, agora));
        if (l.getDoses() < 1) {
            throw new ResourceUnprocessableEntityException("O lote " + l.getNumeroLote() + " está sem saldo.");
        }
        Imunobiologico i = l.getImunobiologico();
        FrascoAberto frasco = null;
        if (i.getDosesPorFrasco() > 1 && i.getValidadeAposAbertoHoras() != null) {
            frasco = abertos.stream().filter(FrascoAberto::aberto).findFirst().orElse(null);
            if (frasco == null) {
                frasco = new FrascoAberto();
                frasco.setLote(l);
                frasco.setAbertoEm(agora);
                frasco.setValidoAte(agora.plusHours(i.getValidadeAposAbertoHoras()));
                frasco.setDosesNoFrasco(Math.min(i.getDosesPorFrasco(), l.getDoses()));
            }
            frasco.setDosesUsadas(frasco.getDosesUsadas() + 1);
            if (frasco.getDosesUsadas() >= frasco.getDosesNoFrasco()) {
                frasco.setEncerradoEm(agora);
                frasco.setMotivoEncerramento(MotivoEncerramentoFrasco.ESGOTADO);
            }
            frasco = frascoRepository.save(frasco);
        }
        l.setDoses(l.getDoses() - 1);
        loteRepository.save(l);
        r.setLoteEstoque(l);
        r.setFrasco(frasco);
        r.setLote(l.getNumeroLote());
        if (r.getFabricante() == null) {
            r.setFabricante(l.getFabricante());
        }
    }

    public void lancarAplicacao(RegistroVacinacao r) {
        lancar(r.getLoteEstoque(), TipoMovimentacaoImunobiologico.APLICACAO, -1, null, null, r, r.getFrasco());
    }

    /**
     * A dose anulada (ou trocada de lote na retificação) volta ao lote. No frasco ainda no prazo, a dose volta ao frasco
     * (que reabre se tinha esgotado); no frasco vencido, volta e sai de novo como perda técnica.
     */
    public void estornar(RegistroVacinacao r) {
        if (r.getLoteEstoque() == null) {
            return;
        }
        LoteImunobiologico l = loteComTrava(r.getLoteEstoque().getUuid());
        FrascoAberto f = r.getFrasco();
        LocalDateTime agora = LocalDateTime.now();
        l.setDoses(l.getDoses() + 1);
        lancar(l, TipoMovimentacaoImunobiologico.ESTORNO_APLICACAO, 1, null, null, r, f);
        if (f != null) {
            if (f.getValidoAte().isAfter(agora) && f.getMotivoEncerramento() != MotivoEncerramentoFrasco.VENCIDO) {
                f.setDosesUsadas(f.getDosesUsadas() - 1);
                f.setEncerradoEm(null);
                f.setMotivoEncerramento(null);
                frascoRepository.save(f);
            } else {
                l.setDoses(l.getDoses() - 1);
                lancar(l, TipoMovimentacaoImunobiologico.PERDA_TECNICA, -1, MotivoPerdaImunobiologico.FRASCO_ABERTO_VENCIDO,
                        "Dose estornada de frasco já vencido.", null, f);
            }
        }
        loteRepository.save(l);
    }

    // ── Apoio ──────────────────────────────────────────────────────────────────────────────────

    /** O frasco vencido se encerra; as doses que sobraram saem do lote como perda técnica. */
    void encerrarVencido(FrascoAberto f, LocalDateTime agora) {
        LoteImunobiologico l = loteComTrava(f.getLote().getUuid());
        int sobra = Math.min(f.getDosesNoFrasco() - f.getDosesUsadas(), l.getDoses());
        f.setEncerradoEm(agora);
        f.setMotivoEncerramento(MotivoEncerramentoFrasco.VENCIDO);
        frascoRepository.save(f);
        if (sobra > 0) {
            l.setDoses(l.getDoses() - sobra);
            lancar(l, TipoMovimentacaoImunobiologico.PERDA_TECNICA, -sobra, MotivoPerdaImunobiologico.FRASCO_ABERTO_VENCIDO,
                    "Frasco aberto em " + f.getAbertoEm().withNano(0) + ", vencido em " + f.getValidoAte().withNano(0) + ".", null, f);
            loteRepository.save(l);
        }
    }

    /** Lançamento no livro, com o saldo depois. */
    void lancar(LoteImunobiologico l, TipoMovimentacaoImunobiologico tipo, int doses, MotivoPerdaImunobiologico motivo, String justificativa,
                RegistroVacinacao registro, FrascoAberto frasco) {
        MovimentacaoImunobiologico m = new MovimentacaoImunobiologico();
        m.setLote(l);
        m.setTipo(tipo);
        m.setDoses(doses);
        m.setSaldoApos(l.getDoses());
        m.setMotivoPerda(motivo);
        m.setJustificativa(justificativa);
        m.setRegistroVacinacao(registro);
        m.setFrasco(frasco);
        m.setProfissional(registro != null ? registro.getProfissional() : null);
        m.setRegistradoEm(Instant.now());
        m.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        movimentacaoRepository.save(m);
    }

    /** Leitura do estoque e da cadeia de frio: quem vacina na unidade, ou quem gerencia a imunização nela. */
    void exigirLeitura(UnidadeDeSaude u) {
        try {
            controleDeAcesso.exigir(VacinacaoService.REGISTRAR, u);
        } catch (ResourceForbiddenException e) {
            controleDeAcesso.exigir(ImunizacaoCatalogoService.GERENCIAR, u);
        }
    }

    void exigirSala(UnidadeDeSaude u) {
        if (!temSalaDeVacina(u.getUuid())) {
            throw new ResourceUnprocessableEntityException("A unidade " + u.getNome() + " não tem sala de vacina (capacidade "
                    + "\"Sala de vacina\" do modelo operacional).");
        }
    }

    static boolean aplicavel(LoteImunobiologico l, LocalDate hoje) {
        return l.getSituacao() == SituacaoLoteImunobiologico.DISPONIVEL && !l.getValidade().isBefore(hoje) && l.getDoses() > 0;
    }

    LoteImunobiologico lote(UUID uuid) {
        return loteRepository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o lote " + uuid + " em nossos registros."));
    }

    LoteImunobiologico loteComTrava(UUID uuid) {
        return loteRepository.findComTrava(uuid).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o lote " + uuid + " em nossos registros."));
    }

    private UnidadeDeSaude unidade(UUID id) {
        return unidadeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar uma unidade de saúde com o id " + id + " em nossos registros."));
    }

    private static String texto(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    static LoteImunobiologicoDto dto(LoteImunobiologico l, LocalDate hoje) {
        Imunobiologico i = l.getImunobiologico();
        CamaraRefrigeracao c = l.getCamara();
        return new LoteImunobiologicoDto(l.getUuid(), l.getUnidade().getUuid(), i.getUuid(), i.getSigla(), i.getNome(), l.getNumeroLote(),
                l.getFabricante(), l.getValidade(), l.getDoses(), c != null ? c.getUuid() : null, c != null ? c.getNome() : null, l.getSituacao(),
                l.getSobSuspeitaDesde(), l.getParecer(), l.getValidade().isBefore(hoje),
                !l.getValidade().isBefore(hoje) && !l.getValidade().isAfter(hoje.plusDays(30)), aplicavel(l, hoje));
    }

    static FrascoAbertoDto dto(FrascoAberto f, LocalDateTime agora) {
        LoteImunobiologico l = f.getLote();
        return new FrascoAbertoDto(f.getUuid(), l.getUuid(), l.getImunobiologico().getSigla(), l.getNumeroLote(), f.getAbertoEm(), f.getValidoAte(),
                f.getDosesNoFrasco(), f.getDosesUsadas(), f.getDosesNoFrasco() - f.getDosesUsadas(),
                Math.max(0, Duration.between(agora, f.getValidoAte()).toMinutes()));
    }

    static MovimentacaoImunobiologicoDto dto(MovimentacaoImunobiologico m) {
        return new MovimentacaoImunobiologicoDto(m.getUuid(), m.getTipo(), m.getDoses(), m.getSaldoApos(), m.getMotivoPerda(), m.getJustificativa(),
                m.getRegistroVacinacao() != null ? m.getRegistroVacinacao().getUuid() : null, m.getFrasco() != null ? m.getFrasco().getUuid() : null,
                m.getRegistradoEm(), m.getRegistradoPorCpf());
    }
}
