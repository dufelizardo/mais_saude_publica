package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AgendaProducaoDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ContagemProducaoDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ProducaoAssistencialDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ProducaoUnidadeDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.SerieProducaoDto;
import com.edufelizardo.maissaudepublica.models.enuns.FaixaEtariaProducao;
import com.edufelizardo.maissaudepublica.models.enuns.GranularidadeSerie;
import com.edufelizardo.maissaudepublica.models.enuns.Sexo;
import com.edufelizardo.maissaudepublica.models.enuns.StatusAgendamento;
import com.edufelizardo.maissaudepublica.models.enuns.TipoAtendimento;
import com.edufelizardo.maissaudepublica.repositories.AgendamentoRepository;
import com.edufelizardo.maissaudepublica.repositories.AtendimentoRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Produção assistencial por unidade, fatia BI1 do domínio Indicadores e BI (ADRs 0142 e 0143). Camada de leitura:
 * agrega na hora o que Atendimento e Agenda já registram, e só devolve números. Sem unidade, soma as unidades do acesso
 * (a rede inteira para quem tem acesso na rede).
 */
@Service
public class ProducaoAssistencialService {

    public static final String CONSULTAR = EscopoDosIndicadores.CONSULTAR;

    /** Período máximo de uma consulta: um ano (e um dia, para o ano bissexto). */
    private static final long MAXIMO_DIAS = 366;

    private static final Map<String, String[]> CATEGORIAS = new LinkedHashMap<>();

    static {
        // As mesmas categorias da tela Profissionais (ADR-0072), pelo conselho de classe.
        CATEGORIAS.put("CRM", new String[]{"MED", "Médicos"});
        CATEGORIAS.put("COREN", new String[]{"ENF", "Enfermagem"});
        CATEGORIAS.put("CRO", new String[]{"ODO", "Odontologia"});
        CATEGORIAS.put("CRF", new String[]{"FAR", "Farmácia"});
    }

    @Autowired
    private AtendimentoRepository atendimentoRepository;

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @Autowired
    private UnidadeDeSaudeRepository unidadeRepository;

    @Autowired
    private EscopoDosIndicadores escopoDosIndicadores;

    private record Periodo(LocalDate de, LocalDate ate) {
        long dias() {
            return ChronoUnit.DAYS.between(de, ate) + 1;
        }

        Periodo anterior() {
            return new Periodo(de.minusDays(dias()), de.minusDays(1));
        }
    }

    @Transactional(readOnly = true)
    public ProducaoAssistencialDto producao(LocalDate de, LocalDate ate, UUID unidadeId) {
        Periodo p = periodo(de, ate);
        EscopoDosIndicadores.Escopo e = escopoDosIndicadores.de(unidadeId);
        Periodo ant = p.anterior();
        List<Object[]> atendimentos = atendimentos(e, p);
        long anterior = atendimentos(e, ant).size();
        long total = atendimentos.size();

        Map<String, Long> tipos = new LinkedHashMap<>();
        for (TipoAtendimento t : TipoAtendimento.values()) {
            tipos.put(t.name(), 0L);
        }
        Map<String, Long> especialidades = new TreeMap<>();
        Map<String, Long> categorias = new LinkedHashMap<>();
        Map<String, Long> faixas = new LinkedHashMap<>();
        for (FaixaEtariaProducao f : FaixaEtariaProducao.values()) {
            faixas.put(f.name(), 0L);
        }
        Map<String, Long> sexos = new LinkedHashMap<>();
        for (Sexo s : Sexo.values()) {
            sexos.put(s.name(), 0L);
        }
        sexos.put("SEM_REGISTRO", 0L);
        Long[] horas = new Long[24];
        Arrays.fill(horas, 0L);
        Map<String, String> rotulosCategoria = new HashMap<>();

        for (Object[] l : atendimentos) {
            LocalDateTime quando = (LocalDateTime) l[1];
            tipos.merge(l[2] == null ? "SEM_TIPO" : ((TipoAtendimento) l[2]).name(), 1L, Long::sum);
            especialidades.merge(l[3] == null ? "Sem especialidade" : (String) l[3], 1L, Long::sum);
            String[] categoria = categoria((String) l[4]);
            rotulosCategoria.put(categoria[0], categoria[1]);
            categorias.merge(categoria[0], 1L, Long::sum);
            faixas.merge(FaixaEtariaProducao.de((LocalDate) l[5], quando.toLocalDate()).name(), 1L, Long::sum);
            sexos.merge(l[6] == null ? "SEM_REGISTRO" : ((Sexo) l[6]).name(), 1L, Long::sum);
            horas[quando.getHour()]++;
        }

        List<ContagemProducaoDto> porTipo = tipos.entrySet().stream()
                .map(x -> new ContagemProducaoDto(x.getKey(), rotuloTipo(x.getKey()), x.getValue())).toList();
        List<ContagemProducaoDto> porEspecialidade = especialidades.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(x -> new ContagemProducaoDto(x.getKey(), x.getKey(), x.getValue())).toList();
        List<ContagemProducaoDto> porCategoria = categorias.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(x -> new ContagemProducaoDto(x.getKey(), rotulosCategoria.get(x.getKey()), x.getValue())).toList();
        List<ContagemProducaoDto> porFaixa = faixas.entrySet().stream()
                .map(x -> {
                    FaixaEtariaProducao f = FaixaEtariaProducao.valueOf(x.getKey());
                    return new ContagemProducaoDto(f.name(), f.rotulo() + " (" + f.faixa() + ")", x.getValue());
                }).toList();
        List<ContagemProducaoDto> porSexo = sexos.entrySet().stream()
                .map(x -> new ContagemProducaoDto(x.getKey(), rotuloSexo(x.getKey()), x.getValue())).toList();

        BigDecimal media = BigDecimal.valueOf(total).divide(BigDecimal.valueOf(p.dias()), 1, RoundingMode.HALF_UP);
        return new ProducaoAssistencialDto(p.de(), p.ate(), ant.de(), ant.ate(),
                e.unidadeId(), e.unidade() == null ? null : e.unidade().getNome(), e.abrangencia(),
                total, anterior, variacao(total, anterior), media, porTipo, porEspecialidade, porCategoria, porFaixa, porSexo,
                List.of(horas), agenda(e, p), agenda(e, ant));
    }

    /** A série terminando hoje: por dia (padrão 30, até 90) ou por mês (padrão 12, até 24). */
    @Transactional(readOnly = true)
    public SerieProducaoDto serie(GranularidadeSerie granularidade, UUID unidadeId, Integer pontos) {
        GranularidadeSerie g = granularidade == null ? GranularidadeSerie.MES : granularidade;
        int padrao = g == GranularidadeSerie.DIA ? 30 : 12;
        int maximo = g == GranularidadeSerie.DIA ? 90 : 24;
        int n = pontos == null ? padrao : pontos;
        if (n < 1 || n > maximo) {
            throw new ResourceBadRequestException("A série por " + (g == GranularidadeSerie.DIA ? "dia" : "mês") + " vai de 1 a " + maximo + " pontos.");
        }
        EscopoDosIndicadores.Escopo e = escopoDosIndicadores.de(unidadeId);
        LocalDate hoje = LocalDate.now();
        LocalDate inicio = g == GranularidadeSerie.DIA ? hoje.minusDays(n - 1L) : YearMonth.from(hoje).minusMonths(n - 1L).atDay(1);
        Periodo p = new Periodo(inicio, hoje);
        Function<LocalDateTime, String> chave = g == GranularidadeSerie.DIA
                ? d -> d.toLocalDate().toString()
                : d -> YearMonth.from(d).toString();
        Map<String, long[]> pontosMap = new LinkedHashMap<>();
        for (int i = 0; i < n; i++) {
            pontosMap.put(g == GranularidadeSerie.DIA ? inicio.plusDays(i).toString() : YearMonth.from(inicio).plusMonths(i).toString(), new long[2]);
        }
        for (Object[] l : atendimentos(e, p)) {
            long[] v = pontosMap.get(chave.apply((LocalDateTime) l[1]));
            if (v != null) {
                v[0]++;
            }
        }
        for (Object[] l : agendamentos(e, p)) {
            long[] v = pontosMap.get(chave.apply((LocalDateTime) l[1]));
            if (v != null && l[2] == StatusAgendamento.FALTOU) {
                v[1]++;
            }
        }
        List<SerieProducaoDto.Ponto> lista = pontosMap.entrySet().stream()
                .map(x -> new SerieProducaoDto.Ponto(x.getKey(), x.getValue()[0], x.getValue()[1])).toList();
        return new SerieProducaoDto(g, e.unidadeId(), lista);
    }

    /** As unidades do acesso com produção no período ou no anterior, da maior para a menor. */
    @Transactional(readOnly = true)
    public List<ProducaoUnidadeDto> unidades(LocalDate de, LocalDate ate) {
        Periodo p = periodo(de, ate);
        EscopoDosIndicadores.Escopo e = escopoDosIndicadores.de(null);
        Map<UUID, Long> atual = contarPorUnidade(atendimentos(e, p));
        Map<UUID, Long> anterior = contarPorUnidade(atendimentos(e, p.anterior()));
        Set<UUID> ids = new HashSet<>(atual.keySet());
        ids.addAll(anterior.keySet());
        Map<UUID, UnidadeDeSaude> unidades = unidadeRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(UnidadeDeSaude::getUuid, u -> u));
        return ids.stream().filter(unidades::containsKey).map(id -> {
                    UnidadeDeSaude u = unidades.get(id);
                    long a = atual.getOrDefault(id, 0L);
                    long b = anterior.getOrDefault(id, 0L);
                    return new ProducaoUnidadeDto(id, u.getNome(), u.getTipo(), a, b, variacao(a, b));
                })
                .sorted(Comparator.comparingLong(ProducaoUnidadeDto::getAtendimentos).reversed().thenComparing(ProducaoUnidadeDto::getUnidadeNome))
                .toList();
    }

    /** O resumo do período em CSV (ponto e vírgula), para a planilha. */
    @Transactional(readOnly = true)
    public String csv(LocalDate de, LocalDate ate, UUID unidadeId) {
        ProducaoAssistencialDto d = producao(de, ate, unidadeId);
        StringBuilder s = new StringBuilder();
        s.append("Produção assistencial;").append(d.getAbrangencia()).append('\n');
        s.append("Período;").append(d.getDe()).append(" a ").append(d.getAte()).append('\n');
        s.append('\n').append("Indicador;Período;Anterior").append('\n');
        s.append("Atendimentos concluídos;").append(d.getAtendimentos()).append(';').append(d.getAtendimentosAnterior()).append('\n');
        s.append("Média diária;").append(d.getMediaDiaria()).append(";\n");
        linhaAgenda(s, "Marcações", d.getAgenda().getMarcados(), d.getAgendaAnterior().getMarcados());
        linhaAgenda(s, "Realizadas", d.getAgenda().getRealizados(), d.getAgendaAnterior().getRealizados());
        linhaAgenda(s, "Faltas", d.getAgenda().getFaltas(), d.getAgendaAnterior().getFaltas());
        linhaAgenda(s, "Canceladas", d.getAgenda().getCancelados(), d.getAgendaAnterior().getCancelados());
        linhaAgenda(s, "Encaixes", d.getAgenda().getEncaixes(), d.getAgendaAnterior().getEncaixes());
        s.append("Absenteísmo (%);").append(texto(d.getAgenda().getAbsenteismo())).append(';')
                .append(texto(d.getAgendaAnterior().getAbsenteismo())).append('\n');
        secao(s, "Por tipo", d.getPorTipo());
        secao(s, "Por especialidade", d.getPorEspecialidade());
        secao(s, "Por categoria profissional", d.getPorCategoria());
        secao(s, "Por faixa etária", d.getPorFaixaEtaria());
        secao(s, "Por sexo", d.getPorSexo());
        s.append('\n').append("Hora;Atendimentos").append('\n');
        for (int h = 0; h < 24; h++) {
            s.append(String.format("%02dh", h)).append(';').append(d.getPorHora().get(h)).append('\n');
        }
        return s.toString();
    }

    // ── Apoio ──────────────────────────────────────────────────────────────────────────────────

    private static Periodo periodo(LocalDate de, LocalDate ate) {
        LocalDate fim = ate == null ? LocalDate.now() : ate;
        LocalDate inicio = de == null ? fim.withDayOfMonth(1) : de;
        if (inicio.isAfter(fim)) {
            throw new ResourceBadRequestException("O início do período não pode ser depois do fim.");
        }
        Periodo p = new Periodo(inicio, fim);
        if (p.dias() > MAXIMO_DIAS) {
            throw new ResourceBadRequestException("O período vai até um ano; para mais, use a série por mês.");
        }
        return p;
    }

    private List<Object[]> atendimentos(EscopoDosIndicadores.Escopo e, Periodo p) {
        return atendimentoRepository.findConcluidosParaProducao(p.de().atStartOfDay(), p.ate().plusDays(1).atStartOfDay()).stream()
                .filter(l -> e.contem((UUID) l[0])).toList();
    }

    private List<Object[]> agendamentos(EscopoDosIndicadores.Escopo e, Periodo p) {
        return agendamentoRepository.findParaProducao(p.de().atStartOfDay(), p.ate().plusDays(1).atStartOfDay()).stream()
                .filter(l -> e.contem((UUID) l[0])).toList();
    }

    private AgendaProducaoDto agenda(EscopoDosIndicadores.Escopo e, Periodo p) {
        long marcados = 0, realizados = 0, faltas = 0, cancelados = 0, encaixes = 0;
        for (Object[] l : agendamentos(e, p)) {
            marcados++;
            StatusAgendamento s = (StatusAgendamento) l[2];
            if (s == StatusAgendamento.REALIZADO) {
                realizados++;
            } else if (s == StatusAgendamento.FALTOU) {
                faltas++;
            } else if (s == StatusAgendamento.CANCELADO) {
                cancelados++;
            }
            if (Boolean.TRUE.equals(l[3]) && s != StatusAgendamento.CANCELADO) {
                encaixes++;
            }
        }
        BigDecimal absenteismo = realizados + faltas == 0 ? null
                : BigDecimal.valueOf(faltas * 100).divide(BigDecimal.valueOf(realizados + faltas), 1, RoundingMode.HALF_UP);
        return new AgendaProducaoDto(marcados, realizados, faltas, cancelados, encaixes, absenteismo);
    }

    private static Map<UUID, Long> contarPorUnidade(List<Object[]> linhas) {
        Map<UUID, Long> m = new HashMap<>();
        linhas.forEach(l -> m.merge((UUID) l[0], 1L, Long::sum));
        return m;
    }

    static BigDecimal variacao(long atual, long anterior) {
        if (anterior == 0) {
            return null;
        }
        return BigDecimal.valueOf((atual - anterior) * 100).divide(BigDecimal.valueOf(anterior), 1, RoundingMode.HALF_UP);
    }

    /** CRM-SP, "crm", "CRM/SP" → Médicos; sem conselho → Apoio e administrativo; outro conselho → Outros conselhos. */
    static String[] categoria(String conselho) {
        String sigla = conselho == null ? "" : conselho.trim().toUpperCase(Locale.ROOT).split("[\\s\\-/]")[0];
        if (sigla.isEmpty()) {
            return new String[]{"APO", "Apoio e administrativo"};
        }
        return CATEGORIAS.getOrDefault(sigla, new String[]{"OUT", "Outros conselhos"});
    }

    private static String rotuloTipo(String tipo) {
        return switch (tipo) {
            case "CONSULTA" -> "Consulta";
            case "URGENCIA" -> "Urgência";
            case "INTERNACAO" -> "Internação";
            default -> "Sem tipo";
        };
    }

    private static String rotuloSexo(String sexo) {
        return switch (sexo) {
            case "MASCULINO" -> "Masculino";
            case "FEMININO" -> "Feminino";
            case "IGNORADO" -> "Ignorado";
            default -> "Sem registro";
        };
    }

    private static void linhaAgenda(StringBuilder s, String nome, long atual, long anterior) {
        s.append(nome).append(';').append(atual).append(';').append(anterior).append('\n');
    }

    private static void secao(StringBuilder s, String titulo, List<ContagemProducaoDto> linhas) {
        s.append('\n').append(titulo).append(";Atendimentos").append('\n');
        for (ContagemProducaoDto c : linhas) {
            s.append(c.getRotulo().replace(';', ',')).append(';').append(c.getQuantidade()).append('\n');
        }
    }

    private static String texto(BigDecimal v) {
        return v == null ? "" : v.toPlainString();
    }
}
