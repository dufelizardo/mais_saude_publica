package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.models.Afastamento;
import com.edufelizardo.maissaudepublica.models.TurnoEscala;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AlertaSituacaoDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.EsperaAtendimentoDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.EstatisticaEsperaDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.OcorrenciaEscalaDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.OcupacaoAgendaDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.SituacaoAgoraDto;
import com.edufelizardo.maissaudepublica.models.enuns.FuncaoEquipe;
import com.edufelizardo.maissaudepublica.models.enuns.StatusAfastamento;
import com.edufelizardo.maissaudepublica.repositories.AfastamentoRepository;
import com.edufelizardo.maissaudepublica.repositories.AtendimentoRepository;
import com.edufelizardo.maissaudepublica.repositories.ConsultaRepository;
import com.edufelizardo.maissaudepublica.repositories.TriagemRepository;
import com.edufelizardo.maissaudepublica.repositories.TurnoEscalaRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Situação operacional, fatia BI2 do domínio Indicadores e BI (ADRs 0142 e 0144): quem está em atendimento e esperando
 * agora, o tempo de espera no período (pela chegada registrada), a ocupação das agendas, as ocorrências da escala e os
 * alertas que saem disso. Camada de leitura, só números; nomes só os dos profissionais na escala, e licença médica não
 * é nomeada (ADR-0072).
 */
@Service
public class SituacaoOperacionalService {

    private static final Set<StatusAfastamento> AFASTA = Set.of(StatusAfastamento.APROVADO, StatusAfastamento.EM_ANDAMENTO);
    private static final long MAXIMO_DIAS_PERIODO = 366;
    /** Espera acima de um dia é erro de registro (atendimento esquecido aberto), não espera. */
    private static final long ESPERA_MAXIMA_VALIDA = 24 * 60;

    private static final Map<FuncaoEquipe, String> FUNCOES = new EnumMap<>(FuncaoEquipe.class);

    static {
        FUNCOES.put(FuncaoEquipe.MEDICO, "Médico");
        FUNCOES.put(FuncaoEquipe.ENFERMEIRO, "Enfermeiro");
        FUNCOES.put(FuncaoEquipe.TECNICO_ENFERMAGEM, "Técnico de enfermagem");
        FUNCOES.put(FuncaoEquipe.AUXILIAR_ENFERMAGEM, "Auxiliar de enfermagem");
        FUNCOES.put(FuncaoEquipe.ACS, "Agente comunitário de saúde");
        FUNCOES.put(FuncaoEquipe.CIRURGIAO_DENTISTA, "Cirurgião-dentista");
        FUNCOES.put(FuncaoEquipe.TECNICO_SAUDE_BUCAL, "Técnico em saúde bucal");
        FUNCOES.put(FuncaoEquipe.AUXILIAR_SAUDE_BUCAL, "Auxiliar em saúde bucal");
        FUNCOES.put(FuncaoEquipe.PSICOLOGO, "Psicólogo");
        FUNCOES.put(FuncaoEquipe.ASSISTENTE_SOCIAL, "Assistente social");
        FUNCOES.put(FuncaoEquipe.OUTRO, "Outra função");
    }

    @Autowired
    private EscopoDosIndicadores escopoDosIndicadores;

    @Autowired
    private AtendimentoRepository atendimentoRepository;

    @Autowired
    private TriagemRepository triagemRepository;

    @Autowired
    private ConsultaRepository consultaRepository;

    @Autowired
    private TurnoEscalaRepository turnoRepository;

    @Autowired
    private AfastamentoRepository afastamentoRepository;

    @Autowired
    private UnidadeDeSaudeRepository unidadeRepository;

    @Autowired
    private AgendaService agendaService;

    @Value("${app.indicadores.alertas.fila-aguardando:20}")
    private int limiteFila;

    @Value("${app.indicadores.alertas.espera-minutos:120}")
    private int limiteEspera;

    @Value("${app.indicadores.alertas.ocupacao-agenda-percentual:95}")
    private int limiteOcupacao;

    /** Um atendimento em andamento: unidade, chegada e se já teve triagem ou consulta. */
    private record EmAndamento(UUID unidadeId, LocalDateTime chegada, boolean triado, boolean consultado) {
        boolean aguardando() {
            return !consultado;
        }
    }

    // ── Agora ──────────────────────────────────────────────────────────────────────────────────

    /**
     * Em andamento nas últimas 24 horas: aguardando a triagem (sem triagem nem consulta), aguardando a consulta (com
     * triagem, sem consulta) e a maior espera de quem aguarda. Mais os alertas.
     */
    @Transactional(readOnly = true)
    public SituacaoAgoraDto agora(UUID unidadeId) {
        EscopoDosIndicadores.Escopo e = escopoDosIndicadores.de(unidadeId);
        LocalDateTime agora = LocalDateTime.now();
        List<EmAndamento> lista = emAndamento(e, agora);
        long aguardandoTriagem = lista.stream().filter(a -> !a.triado() && !a.consultado()).count();
        long aguardandoConsulta = lista.stream().filter(a -> a.triado() && !a.consultado()).count();
        Long maior = lista.stream().filter(EmAndamento::aguardando).map(a -> minutos(a.chegada(), agora)).max(Long::compare).orElse(null);
        return new SituacaoAgoraDto(agora, e.unidadeId(), e.abrangencia(), lista.size(), aguardandoTriagem, aguardandoConsulta, maior,
                alertas(e, lista, agora));
    }

    // ── Espera ─────────────────────────────────────────────────────────────────────────────────

    /** Da chegada ao primeiro contato (triagem ou consulta) e à consulta, dos que chegaram no período. */
    @Transactional(readOnly = true)
    public EsperaAtendimentoDto espera(LocalDate de, LocalDate ate, UUID unidadeId) {
        LocalDate fim = ate == null ? LocalDate.now() : ate;
        LocalDate inicio = de == null ? fim.withDayOfMonth(1) : de;
        if (inicio.isAfter(fim)) {
            throw new ResourceBadRequestException("O início do período não pode ser depois do fim.");
        }
        if (ChronoUnit.DAYS.between(inicio, fim) + 1 > MAXIMO_DIAS_PERIODO) {
            throw new ResourceBadRequestException("O período vai até um ano.");
        }
        EscopoDosIndicadores.Escopo e = escopoDosIndicadores.de(unidadeId);
        Map<UUID, LocalDateTime> chegadas = new HashMap<>();
        for (Object[] l : atendimentoRepository.findComChegadaNoIntervalo(inicio.atStartOfDay(), fim.plusDays(1).atStartOfDay())) {
            if (e.contem((UUID) l[1])) {
                chegadas.put((UUID) l[0], (LocalDateTime) l[2]);
            }
        }
        Map<UUID, LocalDateTime> triagens = primeiras(chegadas.keySet(), () -> triagemRepository.findPrimeiraPorAtendimento(chegadas.keySet()));
        Map<UUID, LocalDateTime> consultas = primeiras(chegadas.keySet(), () -> consultaRepository.findPrimeiraPorAtendimento(chegadas.keySet()));
        List<Long> contato = new ArrayList<>();
        List<Long> consulta = new ArrayList<>();
        long descartados = 0;
        for (Map.Entry<UUID, LocalDateTime> c : chegadas.entrySet()) {
            LocalDateTime t = triagens.get(c.getKey());
            LocalDateTime q = consultas.get(c.getKey());
            LocalDateTime primeiro = t == null ? q : q == null ? t : (t.isBefore(q) ? t : q);
            if (primeiro != null) {
                long m = minutos(c.getValue(), primeiro);
                if (valida(m)) {
                    contato.add(m);
                } else {
                    descartados++;
                }
            }
            if (q != null) {
                long m = minutos(c.getValue(), q);
                if (valida(m)) {
                    consulta.add(m);
                }
            }
        }
        return new EsperaAtendimentoDto(inicio, fim, e.unidadeId(), e.abrangencia(), chegadas.size(), descartados,
                estatistica(contato), estatistica(consulta));
    }

    // ── Ocupação das agendas ───────────────────────────────────────────────────────────────────

    /** As vagas da agenda a partir de hoje (padrão 7 dias, até 31), por especialidade. */
    @Transactional(readOnly = true)
    public OcupacaoAgendaDto ocupacao(UUID unidadeId, Integer dias) {
        int n = dias(dias);
        EscopoDosIndicadores.Escopo e = escopoDosIndicadores.de(unidadeId);
        LocalDate de = LocalDate.now();
        LocalDate ate = de.plusDays(n - 1L);
        Map<String, long[]> m = agendaService.ocupacaoPorEspecialidade(e.unidades(), de, ate);
        List<OcupacaoAgendaDto.Linha> linhas = m.entrySet().stream()
                .map(x -> new OcupacaoAgendaDto.Linha(x.getKey(), x.getValue()[0], x.getValue()[1], percentual(x.getValue()[1], x.getValue()[0])))
                .sorted(Comparator.comparing((OcupacaoAgendaDto.Linha l) -> l.getPercentual() == null ? -1 : l.getPercentual()).reversed()
                        .thenComparing(OcupacaoAgendaDto.Linha::getEspecialidade))
                .toList();
        long ofertadas = linhas.stream().mapToLong(OcupacaoAgendaDto.Linha::getOfertadas).sum();
        long ocupadas = linhas.stream().mapToLong(OcupacaoAgendaDto.Linha::getOcupadas).sum();
        return new OcupacaoAgendaDto(de, ate, e.unidadeId(), e.abrangencia(), ofertadas, ocupadas, percentual(ocupadas, ofertadas), linhas);
    }

    // ── Escala ─────────────────────────────────────────────────────────────────────────────────

    /** Turnos a partir de agora (padrão 7 dias, até 31) sem profissional, ou com o profissional afastado. */
    @Transactional(readOnly = true)
    public List<OcorrenciaEscalaDto> escala(UUID unidadeId, Integer dias) {
        int n = dias(dias);
        EscopoDosIndicadores.Escopo e = escopoDosIndicadores.de(unidadeId);
        return ocorrencias(e, LocalDateTime.now(), n);
    }

    // ── Apoio ──────────────────────────────────────────────────────────────────────────────────

    private List<EmAndamento> emAndamento(EscopoDosIndicadores.Escopo e, LocalDateTime agora) {
        Map<UUID, Object[]> linhas = new LinkedHashMap<>();
        for (Object[] l : atendimentoRepository.findEmAndamentoDesde(agora.minusHours(24))) {
            if (e.contem((UUID) l[1])) {
                linhas.put((UUID) l[0], l);
            }
        }
        Set<UUID> ids = linhas.keySet();
        Map<UUID, LocalDateTime> triagens = primeiras(ids, () -> triagemRepository.findPrimeiraPorAtendimento(ids));
        Map<UUID, LocalDateTime> consultas = primeiras(ids, () -> consultaRepository.findPrimeiraPorAtendimento(ids));
        return linhas.values().stream()
                .map(l -> new EmAndamento((UUID) l[1], (LocalDateTime) l[2], triagens.containsKey((UUID) l[0]), consultas.containsKey((UUID) l[0])))
                .toList();
    }

    private List<AlertaSituacaoDto> alertas(EscopoDosIndicadores.Escopo e, List<EmAndamento> lista, LocalDateTime agora) {
        List<AlertaSituacaoDto> alertas = new ArrayList<>();
        Map<UUID, List<EmAndamento>> porUnidade = lista.stream().filter(EmAndamento::aguardando)
                .collect(Collectors.groupingBy(EmAndamento::unidadeId, LinkedHashMap::new, Collectors.toList()));
        Map<UUID, String> nomes = nomes(porUnidade.keySet());
        for (Map.Entry<UUID, List<EmAndamento>> u : porUnidade.entrySet()) {
            int aguardando = u.getValue().size();
            if (aguardando > limiteFila) {
                alertas.add(new AlertaSituacaoDto("FILA", "ALTA", "Fila alta na " + nomes.get(u.getKey()),
                        aguardando + " pacientes aguardando; o limite configurado é " + limiteFila + ".", u.getKey(), nomes.get(u.getKey()),
                        "/assistencia/atendimentos"));
            }
            long maior = u.getValue().stream().mapToLong(a -> minutos(a.chegada(), agora)).max().orElse(0);
            if (maior > limiteEspera) {
                alertas.add(new AlertaSituacaoDto("ESPERA", maior > 2L * limiteEspera ? "ALTA" : "MEDIA", "Espera longa na " + nomes.get(u.getKey()),
                        "Há paciente aguardando há " + maior + " min; o limite configurado é " + limiteEspera + " min.", u.getKey(),
                        nomes.get(u.getKey()), "/assistencia/atendimentos"));
            }
        }
        LocalDate hoje = agora.toLocalDate();
        Map<String, long[]> ocupacao = agendaService.ocupacaoPorEspecialidade(e.unidades(), hoje, hoje.plusDays(6));
        for (Map.Entry<String, long[]> o : ocupacao.entrySet()) {
            Integer p = percentual(o.getValue()[1], o.getValue()[0]);
            if (p != null && p >= limiteOcupacao) {
                alertas.add(new AlertaSituacaoDto("AGENDA", "MEDIA", "Agenda saturada em " + o.getKey(),
                        p + "% das " + o.getValue()[0] + " vagas dos próximos 7 dias já estão ocupadas (" + e.abrangencia() + ").",
                        e.unidadeId(), e.unidade() == null ? null : e.unidade().getNome(), "/assistencia/agenda"));
            }
        }
        Map<UUID, List<OcorrenciaEscalaDto>> escala = ocorrencias(e, agora, 1).stream()
                .collect(Collectors.groupingBy(OcorrenciaEscalaDto::getUnidadeId, LinkedHashMap::new, Collectors.toList()));
        for (Map.Entry<UUID, List<OcorrenciaEscalaDto>> u : escala.entrySet()) {
            long sem = u.getValue().stream().filter(o -> "SEM_COBERTURA".equals(o.getSituacao())).count();
            long afastados = u.getValue().size() - sem;
            String nome = u.getValue().get(0).getUnidadeNome();
            alertas.add(new AlertaSituacaoDto("ESCALA", sem > 0 ? "ALTA" : "MEDIA", "Escala descoberta na " + nome,
                    "Nas próximas 24 horas: " + sem + " turno(s) sem profissional e " + afastados + " com o profissional afastado.",
                    u.getKey(), nome, "/rh/escalas"));
        }
        alertas.sort(Comparator.comparing((AlertaSituacaoDto a) -> "ALTA".equals(a.getGravidade()) ? 0 : 1));
        return alertas;
    }

    private List<OcorrenciaEscalaDto> ocorrencias(EscopoDosIndicadores.Escopo e, LocalDateTime de, int dias) {
        LocalDateTime ate = de.plusDays(dias);
        List<TurnoEscala> turnos = turnoRepository.entre(de, ate).stream().filter(t -> e.contem(t.getUnidade().getUuid())).toList();
        if (turnos.isEmpty()) {
            return List.of();
        }
        Map<UUID, List<Afastamento>> afastamentos = afastamentoRepository.findQueTocam(de.toLocalDate(), ate.toLocalDate(), AFASTA).stream()
                .collect(Collectors.groupingBy(a -> a.getProfissional().getUuid()));
        List<OcorrenciaEscalaDto> lista = new ArrayList<>();
        for (TurnoEscala t : turnos) {
            UnidadeDeSaude u = t.getUnidade();
            String funcao = t.getFuncao() == null ? null : FUNCOES.get(t.getFuncao());
            if (t.getProfissional() == null) {
                lista.add(new OcorrenciaEscalaDto(t.getUuid(), u.getUuid(), u.getNome(), null, funcao, t.getTipo() == null ? null : t.getTipo().name(),
                        t.getInicioEm(), t.getFimEm(), "SEM_COBERTURA", "Sem profissional designado"));
                continue;
            }
            Optional<Afastamento> a = afastamentos.getOrDefault(t.getProfissional().getUuid(), List.of()).stream()
                    .filter(x -> !x.getDataInicio().isAfter(t.getFimEm().toLocalDate()) && !x.getDataFim().isBefore(t.getInicioEm().toLocalDate()))
                    .findFirst();
            if (a.isPresent()) {
                lista.add(new OcorrenciaEscalaDto(t.getUuid(), u.getUuid(), u.getNome(), t.getProfissional().getNome(), funcao,
                        t.getTipo() == null ? null : t.getTipo().name(), t.getInicioEm(), t.getFimEm(), "AFASTADO", motivo(a.get())));
            }
        }
        return lista;
    }

    /** Licença médica é motivo de saúde, dado sensível (ADR-0072): aparece só "Afastado". */
    static String motivo(Afastamento a) {
        return switch (a.getTipo()) {
            case FERIAS -> "Férias";
            case LICENCA_PESSOAL -> "Licença pessoal";
            default -> "Afastado";
        };
    }

    /** Sem atendimento, nem consulta: o "in ()" vazio não vai ao banco. */
    private static Map<UUID, LocalDateTime> primeiras(Set<UUID> ids, java.util.function.Supplier<List<Object[]>> consulta) {
        Map<UUID, LocalDateTime> m = new HashMap<>();
        if (!ids.isEmpty()) {
            consulta.get().forEach(l -> m.put((UUID) l[0], (LocalDateTime) l[1]));
        }
        return m;
    }

    private Map<UUID, String> nomes(Set<UUID> ids) {
        return unidadeRepository.findAllById(ids).stream().collect(Collectors.toMap(UnidadeDeSaude::getUuid, UnidadeDeSaude::getNome));
    }

    private static long minutos(LocalDateTime de, LocalDateTime ate) {
        return Duration.between(de, ate).toMinutes();
    }

    private static boolean valida(long minutos) {
        return minutos >= 0 && minutos <= ESPERA_MAXIMA_VALIDA;
    }

    private static int dias(Integer dias) {
        int n = dias == null ? 7 : dias;
        if (n < 1 || n > 31) {
            throw new ResourceBadRequestException("O horizonte vai de 1 a 31 dias.");
        }
        return n;
    }

    static Integer percentual(long parte, long total) {
        return total == 0 ? null : (int) Math.round(parte * 100.0 / total);
    }

    /** Média, mediana e percentil 90 (posto mais próximo), em minutos com uma casa. */
    static EstatisticaEsperaDto estatistica(List<Long> valores) {
        if (valores.isEmpty()) {
            return new EstatisticaEsperaDto(0, null, null, null);
        }
        List<Long> v = new ArrayList<>(valores);
        Collections.sort(v);
        int n = v.size();
        BigDecimal media = BigDecimal.valueOf(v.stream().mapToLong(Long::longValue).sum()).divide(BigDecimal.valueOf(n), 1, RoundingMode.HALF_UP);
        BigDecimal mediana = n % 2 == 1 ? BigDecimal.valueOf(v.get(n / 2)).setScale(1)
                : BigDecimal.valueOf(v.get(n / 2 - 1) + v.get(n / 2)).divide(BigDecimal.valueOf(2), 1, RoundingMode.HALF_UP);
        BigDecimal p90 = BigDecimal.valueOf(v.get((int) Math.ceil(0.9 * n) - 1)).setScale(1);
        return new EstatisticaEsperaDto(n, media, mediana, p90);
    }
}
