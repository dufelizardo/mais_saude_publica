package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceForbiddenException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.models.AcompanhamentoProgramatico;
import com.edufelizardo.maissaudepublica.models.Microarea;
import com.edufelizardo.maissaudepublica.models.Notificacao;
import com.edufelizardo.maissaudepublica.models.NotificacaoNegativa;
import com.edufelizardo.maissaudepublica.models.ParticipacaoPrograma;
import com.edufelizardo.maissaudepublica.models.Programa;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.IndicadorApuradoDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ProgramaPorUnidadeDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ProgramasNaRedeDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.VigilanciaNaRedeDto;
import com.edufelizardo.maissaudepublica.models.enuns.ClassificacaoCasoEpidemiologico;
import com.edufelizardo.maissaudepublica.models.enuns.IndicadorProgramatico;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoAcompanhamento;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoMetaProgramatica;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoNotificacao;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoPrograma;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.AcompanhamentoProgramaticoRepository;
import com.edufelizardo.maissaudepublica.repositories.NotificacaoNegativaRepository;
import com.edufelizardo.maissaudepublica.repositories.NotificacaoRepository;
import com.edufelizardo.maissaudepublica.repositories.ParticipacaoProgramaRepository;
import com.edufelizardo.maissaudepublica.repositories.ProgramaRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Indicadores da rede, fatia BI3 do domínio Indicadores e BI (ADRs 0142 e 0146): os programas de saúde e a vigilância
 * consolidados por unidade, território e rede. Não recalcula com outra regra: os programas usam a apuração da P6
 * (ADR-0141) e a vigilância, os registros de notificação (ADR-0124). Só números; programa sensível só com
 * PROGRAMA.ACOMPANHAR e agravo sensível, sem o nome, só com VIGILANCIA.INVESTIGAR.
 */
@Service
public class IndicadoresDaRedeService {

    private static final String INVESTIGAR = "VIGILANCIA.INVESTIGAR";
    private static final long MAXIMO_DIAS = 366;
    private static final int SEMANAS = 12;
    private static final ZoneId FUSO = ZoneId.systemDefault();
    /** As esferas e as regionais organizam a rede, mas não atendem nem notificam. */
    private static final Set<TipoUnidadeDeSaude> ADMINISTRATIVAS = EnumSet.of(TipoUnidadeDeSaude.FEDERAL, TipoUnidadeDeSaude.ESTADUAL,
            TipoUnidadeDeSaude.MUNICIPAL, TipoUnidadeDeSaude.REGIONAL);

    @Autowired
    private EscopoDosIndicadores escopoDosIndicadores;

    @Autowired
    private ControleDeAcesso controleDeAcesso;

    @Autowired
    private IndicadorProgramaticoService indicadorProgramaticoService;

    @Autowired
    private ProgramaRepository programaRepository;

    @Autowired
    private ParticipacaoProgramaRepository participacaoRepository;

    @Autowired
    private AcompanhamentoProgramaticoRepository acompanhamentoRepository;

    @Autowired
    private NotificacaoRepository notificacaoRepository;

    @Autowired
    private NotificacaoNegativaRepository negativaRepository;

    @Autowired
    private UnidadeDeSaudeRepository unidadeRepository;

    private record Periodo(LocalDate de, LocalDate ate) {
    }

    // ── Programas ──────────────────────────────────────────────────────────────────────────────

    /**
     * Cada programa ativo: inscritos, novos e saídas no período, faltosos, acompanhados em dia e a situação das metas.
     * Na rede inteira, a apuração da rede (com as metas da rede); nas unidades do acesso, a soma das unidades (com a meta
     * de cada unidade, ou a da rede).
     */
    @Transactional(readOnly = true)
    public ProgramasNaRedeDto programas(LocalDate de, LocalDate ate, UUID unidadeId) {
        Periodo p = periodo(de, ate);
        EscopoDosIndicadores.Escopo e = escopoDosIndicadores.de(unidadeId);
        boolean veSensivel = controleDeAcesso.temAlguma(AcompanhamentoService.ACOMPANHAR);
        int ocultos = 0;
        List<ProgramasNaRedeDto.Programa> lista = new ArrayList<>();
        for (Programa programa : programaRepository.findAllComCategoria()) {
            if (programa.getSituacao() != SituacaoPrograma.ATIVO) {
                continue;
            }
            if (programa.isSensivel() && !veSensivel) {
                ocultos++;
                continue;
            }
            List<UnidadeDeSaude> unidades = unidadesDoPrograma(programa, e);
            List<AcompanhamentoProgramatico> acomp = acompanhamentoRepository.findTodosDoPrograma(programa.getUuid()).stream()
                    .filter(a -> e.contem(a.getUnidade().getUuid())).toList();
            long ativos = acomp.stream().filter(a -> vigenteEm(a, p.ate())).count();
            long faltosos = acomp.stream().filter(a -> a.getSituacao() == SituacaoAcompanhamento.FALTOSO && a.getFim() == null).count();
            long novos = acomp.stream().filter(a -> !a.getInicio().isBefore(p.de()) && !a.getInicio().isAfter(p.ate())).count();
            long saidas = acomp.stream().filter(a -> a.getFim() != null && !a.getFim().isBefore(p.de()) && !a.getFim().isAfter(p.ate())).count();

            List<List<IndicadorApuradoDto>> apuracoes = new ArrayList<>();
            if (e.unidades() == null) {
                apuracoes.add(indicadorProgramaticoService.apurarParaRede(programa, null, p.de(), p.ate()));
            } else {
                for (UnidadeDeSaude u : unidades) {
                    apuracoes.add(indicadorProgramaticoService.apurarParaRede(programa, u, p.de(), p.ate()));
                }
            }
            long num = 0;
            long den = 0;
            boolean temEmDia = false;
            int[] metas = new int[4];
            for (List<IndicadorApuradoDto> ap : apuracoes) {
                for (IndicadorApuradoDto i : ap) {
                    if (i.getIndicador() == IndicadorProgramatico.ACOMPANHADOS_EM_DIA && i.isAplicavel() && i.getDenominador() != null) {
                        temEmDia = true;
                        num += i.getNumerador();
                        den += i.getDenominador();
                    }
                    if (i.getSituacao() != null) {
                        metas[i.getSituacao().ordinal()]++;
                    }
                }
            }
            lista.add(new ProgramasNaRedeDto.Programa(programa.getUuid(), programa.getCodigo(), programa.getNome(), programa.isSensivel(),
                    unidades.size(), ativos, novos, saidas, faltosos, num, temEmDia ? den : null, temEmDia ? percentual(num, den) : null,
                    metas[SituacaoMetaProgramatica.ATINGIDA.ordinal()], metas[SituacaoMetaProgramatica.EM_RISCO.ordinal()],
                    metas[SituacaoMetaProgramatica.ABAIXO.ordinal()], metas[SituacaoMetaProgramatica.SEM_DADOS.ordinal()]));
        }
        lista.sort(Comparator.comparingLong(ProgramasNaRedeDto.Programa::getInscritosAtivos).reversed()
                .thenComparing(ProgramasNaRedeDto.Programa::getNome));
        return new ProgramasNaRedeDto(p.de(), p.ate(), e.unidadeId(), e.abrangencia(), ocultos, lista);
    }

    /** Um programa, unidade por unidade do acesso que o oferece; as que estão abaixo da meta primeiro. */
    @Transactional(readOnly = true)
    public ProgramaPorUnidadeDto programaPorUnidade(UUID programaId, LocalDate de, LocalDate ate) {
        Periodo p = periodo(de, ate);
        EscopoDosIndicadores.Escopo e = escopoDosIndicadores.de(null);
        Programa programa = programaRepository.findById(programaId).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o programa " + programaId + " em nossos registros."));
        if (programa.isSensivel() && !controleDeAcesso.temAlguma(AcompanhamentoService.ACOMPANHAR)) {
            throw new ResourceForbiddenException("Os números deste programa sensível são de quem acompanha os pacientes (PROGRAMA.ACOMPANHAR).");
        }
        Map<UUID, Long> ativos = acompanhamentoRepository.findTodosDoPrograma(programaId).stream()
                .filter(a -> vigenteEm(a, p.ate()))
                .collect(Collectors.groupingBy(a -> a.getUnidade().getUuid(), Collectors.counting()));
        List<ProgramaPorUnidadeDto.Linha> linhas = new ArrayList<>();
        for (UnidadeDeSaude u : unidadesDoPrograma(programa, e)) {
            List<IndicadorApuradoDto> ind = indicadorProgramaticoService.apurarParaRede(programa, u, p.de(), p.ate());
            int abaixo = (int) ind.stream().filter(i -> i.getSituacao() == SituacaoMetaProgramatica.ABAIXO).count();
            int risco = (int) ind.stream().filter(i -> i.getSituacao() == SituacaoMetaProgramatica.EM_RISCO).count();
            linhas.add(new ProgramaPorUnidadeDto.Linha(u.getUuid(), u.getNome(), ativos.getOrDefault(u.getUuid(), 0L), abaixo, risco, ind));
        }
        linhas.sort(Comparator.comparingInt(ProgramaPorUnidadeDto.Linha::getMetasAbaixo).reversed()
                .thenComparing(Comparator.comparingInt(ProgramaPorUnidadeDto.Linha::getMetasEmRisco).reversed())
                .thenComparing(ProgramaPorUnidadeDto.Linha::getUnidadeNome));
        return new ProgramaPorUnidadeDto(programa.getUuid(), programa.getNome(), p.de(), p.ate(), linhas);
    }

    // ── Vigilância ─────────────────────────────────────────────────────────────────────────────

    /**
     * As notificações do período por agravo, com a oportunidade de encerramento; as últimas 12 semanas epidemiológicas;
     * a regularidade da última semana fechada; e o território do caso.
     */
    @Transactional(readOnly = true)
    public VigilanciaNaRedeDto vigilancia(LocalDate de, LocalDate ate, UUID unidadeId) {
        Periodo p = periodo(de, ate);
        EscopoDosIndicadores.Escopo e = escopoDosIndicadores.de(unidadeId);
        boolean veSensivel = controleDeAcesso.temAlguma(INVESTIGAR);
        List<Notificacao> todas = notificacaoRepository.findTodas().stream().filter(n -> e.contem(n.getUnidade().getUuid())).toList();
        List<Notificacao> doPeriodo = todas.stream()
                .filter(n -> !n.getDataNotificacao().isBefore(p.de()) && !n.getDataNotificacao().isAfter(p.ate())).toList();

        long[] total = new long[6];
        long ocultas = 0;
        Map<String, long[]> porAgravo = new TreeMap<>();
        Map<String, String> nomes = new HashMap<>();
        Map<String, long[]> porLocal = new TreeMap<>();
        Map<String, String[]> locais = new HashMap<>();
        for (Notificacao n : doPeriodo) {
            contar(total, n);
            if (n.getAgravo().isSensivel() && !veSensivel) {
                ocultas++;
                continue;
            }
            contar(porAgravo.computeIfAbsent(n.getAgravo().getCodigo(), k -> new long[6]), n);
            nomes.put(n.getAgravo().getCodigo(), n.getAgravo().getNome());
            String[] local = local(n.getMicroarea());
            String chave = String.join("|", local);
            locais.put(chave, local);
            porLocal.computeIfAbsent(chave, k -> new long[1])[0]++;
        }
        List<VigilanciaNaRedeDto.Agravo> agravos = porAgravo.entrySet().stream()
                .map(x -> {
                    long[] c = x.getValue();
                    return new VigilanciaNaRedeDto.Agravo(x.getKey(), nomes.get(x.getKey()), c[0], c[1], c[2], c[3], c[4], c[5], percentual(c[5], c[4]));
                })
                .sorted(Comparator.comparingLong(VigilanciaNaRedeDto.Agravo::getNotificadas).reversed().thenComparing(VigilanciaNaRedeDto.Agravo::getNome))
                .toList();
        List<VigilanciaNaRedeDto.Local> territorio = porLocal.entrySet().stream()
                .map(x -> new VigilanciaNaRedeDto.Local(locais.get(x.getKey())[0], locais.get(x.getKey())[1], locais.get(x.getKey())[2], x.getValue()[0]))
                .sorted(Comparator.comparingLong(VigilanciaNaRedeDto.Local::getNotificacoes).reversed())
                .toList();
        return new VigilanciaNaRedeDto(p.de(), p.ate(), e.unidadeId(), e.abrangencia(), total[0], total[1], total[2], total[3], total[4], total[5],
                percentual(total[5], total[4]), ocultas, agravos, semanas(todas), regularidade(e, todas), territorio);
    }

    /** O resumo em CSV (ponto e vírgula): os programas e a vigilância do período. */
    @Transactional(readOnly = true)
    public String csv(LocalDate de, LocalDate ate, UUID unidadeId) {
        ProgramasNaRedeDto prog = programas(de, ate, unidadeId);
        VigilanciaNaRedeDto vig = vigilancia(de, ate, unidadeId);
        StringBuilder s = new StringBuilder();
        s.append("Indicadores da rede;").append(prog.getAbrangencia()).append('\n');
        s.append("Período;").append(prog.getDe()).append(" a ").append(prog.getAte()).append('\n');
        s.append('\n').append("Programa;Unidades;Inscritos ativos;Novos;Saídas;Faltosos;Em dia (%);Metas atingidas;Em risco;Abaixo").append('\n');
        for (ProgramasNaRedeDto.Programa x : prog.getProgramas()) {
            s.append(x.getNome()).append(';').append(x.getUnidades()).append(';').append(x.getInscritosAtivos()).append(';').append(x.getNovos())
                    .append(';').append(x.getSaidas()).append(';').append(x.getFaltosos()).append(';').append(texto(x.getEmDiaPercentual()))
                    .append(';').append(x.getMetasAtingidas()).append(';').append(x.getMetasEmRisco()).append(';').append(x.getMetasAbaixo()).append('\n');
        }
        s.append('\n').append("Agravo;Notificadas;Confirmadas;Descartadas;Em aberto;Encerradas;No prazo (%)").append('\n');
        for (VigilanciaNaRedeDto.Agravo a : vig.getAgravos()) {
            s.append(a.getNome().replace(';', ',')).append(';').append(a.getNotificadas()).append(';').append(a.getConfirmadas()).append(';')
                    .append(a.getDescartadas()).append(';').append(a.getEmAberto()).append(';').append(a.getEncerradas()).append(';')
                    .append(texto(a.getOportunidadeEncerramento())).append('\n');
        }
        s.append("Total;").append(vig.getNotificadas()).append(';').append(vig.getConfirmadas()).append(';').append(vig.getDescartadas()).append(';')
                .append(vig.getEmAberto()).append(';').append(vig.getEncerradas()).append(';').append(texto(vig.getOportunidadeEncerramento())).append('\n');
        VigilanciaNaRedeDto.Regularidade r = vig.getRegularidade();
        s.append('\n').append("Regularidade da notificação (SE ").append(r.getSemana()).append('/').append(r.getAno()).append(");")
                .append(r.getRegulares()).append(" de ").append(r.getUnidadesAssistenciais()).append(';').append(texto(r.getPercentual())).append('\n');
        return s.toString();
    }

    // ── Apoio ──────────────────────────────────────────────────────────────────────────────────

    private static Periodo periodo(LocalDate de, LocalDate ate) {
        LocalDate fim = ate == null ? LocalDate.now() : ate;
        LocalDate inicio = de == null ? fim.withDayOfMonth(1) : de;
        if (inicio.isAfter(fim)) {
            throw new ResourceBadRequestException("O início do período não pode ser depois do fim.");
        }
        if (ChronoUnit.DAYS.between(inicio, fim) + 1 > MAXIMO_DIAS) {
            throw new ResourceBadRequestException("O período vai até um ano.");
        }
        return new Periodo(inicio, fim);
    }

    /** As unidades do escopo com participação vigente no programa, uma vez cada (a mesma unidade pode ter várias equipes). */
    private List<UnidadeDeSaude> unidadesDoPrograma(Programa programa, EscopoDosIndicadores.Escopo e) {
        Map<UUID, UnidadeDeSaude> m = new LinkedHashMap<>();
        for (ParticipacaoPrograma pp : participacaoRepository.findDoPrograma(programa.getUuid())) {
            if (pp.getFim() == null && e.contem(pp.getUnidade().getUuid())) {
                m.putIfAbsent(pp.getUnidade().getUuid(), pp.getUnidade());
            }
        }
        return m.values().stream().sorted(Comparator.comparing(UnidadeDeSaude::getNome)).toList();
    }

    private static boolean vigenteEm(AcompanhamentoProgramatico a, LocalDate dia) {
        return !a.getInicio().isAfter(dia) && (a.getFim() == null || a.getFim().isAfter(dia))
                && (a.getFim() != null || a.getSituacao().vigente());
    }

    /** [notificadas, confirmadas, descartadas, em aberto, encerradas, encerradas no prazo]. */
    private static void contar(long[] c, Notificacao n) {
        c[0]++;
        if (n.getClassificacaoFinal() == ClassificacaoCasoEpidemiologico.CONFIRMADO) {
            c[1]++;
        }
        if (n.getClassificacaoFinal() == ClassificacaoCasoEpidemiologico.DESCARTADO) {
            c[2]++;
        }
        if (n.getSituacao() == SituacaoNotificacao.ENCERRADA) {
            c[4]++;
            if (n.getEncerradaEm() != null && n.getPrazoEncerramento() != null
                    && !n.getEncerradaEm().atZone(FUSO).toLocalDate().isAfter(n.getPrazoEncerramento())) {
                c[5]++;
            }
        } else {
            c[3]++;
        }
    }

    private static String[] local(Microarea m) {
        if (m == null) {
            return new String[]{"Sem território", "—", "—"};
        }
        String territorio = m.getArea().getTerritorio() != null ? m.getArea().getTerritorio().getNome() : "—";
        return new String[]{territorio, m.getArea().getNome(), m.getNome()};
    }

    /** As últimas 12 semanas epidemiológicas, terminando na atual. */
    private static List<VigilanciaNaRedeDto.Semana> semanas(List<Notificacao> todas) {
        List<SemanaEpidemiologica> lista = new ArrayList<>();
        SemanaEpidemiologica se = SemanaEpidemiologica.de(LocalDate.now());
        for (int i = 0; i < SEMANAS; i++) {
            lista.add(0, se);
            se = se.anterior();
        }
        Map<SemanaEpidemiologica, Long> contagem = todas.stream()
                .collect(Collectors.groupingBy(n -> SemanaEpidemiologica.de(n.getDataNotificacao()), Collectors.counting()));
        return lista.stream().map(s -> new VigilanciaNaRedeDto.Semana(s.ano(), s.semana(), s.inicio(), contagem.getOrDefault(s, 0L))).toList();
    }

    /** Na última semana fechada, as unidades assistenciais ativas do escopo que notificaram ou declararam a negativa. */
    private VigilanciaNaRedeDto.Regularidade regularidade(EscopoDosIndicadores.Escopo e, List<Notificacao> todas) {
        SemanaEpidemiologica se = SemanaEpidemiologica.de(LocalDate.now()).anterior();
        Set<UUID> assistenciais = unidadeRepository.findAll().stream()
                .filter(u -> u.isAtivo() && u.getTipo() != null && !ADMINISTRATIVAS.contains(u.getTipo()) && e.contem(u.getUuid()))
                .map(UnidadeDeSaude::getUuid).collect(Collectors.toSet());
        Set<UUID> notificaram = todas.stream().filter(n -> SemanaEpidemiologica.de(n.getDataNotificacao()).equals(se))
                .map(n -> n.getUnidade().getUuid()).filter(assistenciais::contains).collect(Collectors.toSet());
        Set<UUID> negativa = negativaRepository.findTodas().stream().filter(n -> n.getAno() == se.ano() && n.getSemana() == se.semana())
                .map(NotificacaoNegativa::getUnidade).map(UnidadeDeSaude::getUuid).filter(assistenciais::contains).collect(Collectors.toSet());
        Set<UUID> regulares = new HashSet<>(notificaram);
        regulares.addAll(negativa);
        return new VigilanciaNaRedeDto.Regularidade(se.ano(), se.semana(), assistenciais.size(), notificaram.size(), negativa.size(),
                regulares.size(), percentual(regulares.size(), assistenciais.size()));
    }

    static BigDecimal percentual(long parte, long total) {
        return total == 0 ? null : BigDecimal.valueOf(parte * 100L).divide(BigDecimal.valueOf(total), 1, RoundingMode.HALF_UP);
    }

    private static String texto(BigDecimal v) {
        return v == null ? "" : v.toPlainString();
    }
}
