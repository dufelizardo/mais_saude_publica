package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ContagemProducaoDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.CoberturaVacinalDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.EsperaAtendimentoDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.EstatisticaEsperaDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.OcorrenciaEscalaDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.OcupacaoAgendaDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ProducaoAssistencialDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ProgramasNaRedeDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.RelatorioCatalogoDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.RelatorioDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.VacinacaoNaRedeDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.VigilanciaNaRedeDto;
import com.edufelizardo.maissaudepublica.models.enuns.TipoRelatorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Function;

/**
 * Relatórios, fatia BI4 de Indicadores e BI (ADR-0167): um catálogo fixo sobre o que os domínios já calculam. Nada é recalculado
 * aqui, pela regra da ADR-0142; o serviço só organiza os números em resumo e tabelas. O escopo, o período e a ocultação de
 * sensíveis vêm dos serviços de cada domínio.
 */
@Service
public class RelatorioService {

    @Autowired private ProducaoAssistencialService producaoService;
    @Autowired private SituacaoOperacionalService situacaoService;
    @Autowired private IndicadoresDaRedeService redeService;
    @Autowired private CoberturaVacinalService coberturaService;

    public List<RelatorioCatalogoDto> catalogo() {
        return Arrays.stream(TipoRelatorio.values()).map(t -> new RelatorioCatalogoDto(t.name(), t.getNome(), t.getCategoria(), t.getDescricao())).toList();
    }

    public RelatorioDto gerar(String codigo, LocalDate de, LocalDate ate, UUID unidadeId) {
        return switch (tipo(codigo)) {
            case PRODUCAO -> producao(de, ate, unidadeId);
            case SITUACAO -> situacao(de, ate, unidadeId);
            case PROGRAMAS -> programas(de, ate, unidadeId);
            case VACINACAO -> vacinacao(de, ate, unidadeId);
            case VIGILANCIA -> vigilancia(de, ate, unidadeId);
        };
    }

    /** O relatório em CSV com ponto e vírgula, para a planilha. */
    public String csv(String codigo, LocalDate de, LocalDate ate, UUID unidadeId) {
        RelatorioDto r = gerar(codigo, de, ate, unidadeId);
        StringBuilder s = new StringBuilder();
        linha(s, List.of(r.getNome(), r.getAbrangencia()));
        linha(s, List.of("Período", r.getDe() + " a " + r.getAte()));
        s.append('\n');
        linha(s, List.of("Indicador", "Valor"));
        r.getResumo().forEach(i -> linha(s, List.of(i.getRotulo(), i.getValor())));
        for (RelatorioDto.Secao x : r.getSecoes()) {
            s.append('\n');
            linha(s, List.of(x.getTitulo()));
            linha(s, x.getColunas());
            x.getLinhas().forEach(l -> linha(s, l));
        }
        if (!r.getNotas().isEmpty()) {
            s.append('\n');
            r.getNotas().forEach(n -> linha(s, List.of(n)));
        }
        return s.toString();
    }

    // ── Relatórios ─────────────────────────────────────────────────────────────────────────────────

    private RelatorioDto producao(LocalDate de, LocalDate ate, UUID unidadeId) {
        ProducaoAssistencialDto d = producaoService.producao(de, ate, unidadeId);
        var a = d.getAgenda();
        var b = d.getAgendaAnterior();
        List<RelatorioDto.Secao> secoes = new ArrayList<>();
        secoes.add(new RelatorioDto.Secao("Agenda", List.of("Indicador", "Período", "Período anterior"), List.of(
                List.of("Marcações", n(a.getMarcados()), n(b.getMarcados())),
                List.of("Realizadas", n(a.getRealizados()), n(b.getRealizados())),
                List.of("Faltas", n(a.getFaltas()), n(b.getFaltas())),
                List.of("Canceladas", n(a.getCancelados()), n(b.getCancelados())),
                List.of("Encaixes", n(a.getEncaixes()), n(b.getEncaixes())),
                List.of("Absenteísmo (%)", n(a.getAbsenteismo()), n(b.getAbsenteismo())))));
        contagens(secoes, "Por tipo", d.getPorTipo());
        contagens(secoes, "Por especialidade", d.getPorEspecialidade());
        contagens(secoes, "Por categoria profissional", d.getPorCategoria());
        contagens(secoes, "Por faixa etária", d.getPorFaixaEtaria());
        contagens(secoes, "Por sexo", d.getPorSexo());
        return relatorio(TipoRelatorio.PRODUCAO, d.getDe(), d.getAte(), d.getAbrangencia(), List.of(
                new RelatorioDto.Indicador("Atendimentos concluídos", n(d.getAtendimentos())),
                new RelatorioDto.Indicador("Período anterior", n(d.getAtendimentosAnterior())),
                new RelatorioDto.Indicador("Média diária", n(d.getMediaDiaria())),
                new RelatorioDto.Indicador("Absenteísmo (%)", n(a.getAbsenteismo()))), secoes, List.of());
    }

    private RelatorioDto situacao(LocalDate de, LocalDate ate, UUID unidadeId) {
        EsperaAtendimentoDto e = situacaoService.espera(de, ate, unidadeId);
        OcupacaoAgendaDto o = situacaoService.ocupacao(unidadeId, null);
        List<OcorrenciaEscalaDto> escala = situacaoService.escala(unidadeId, null);
        Map<String, Long> ocorrencias = new TreeMap<>();
        for (OcorrenciaEscalaDto x : escala) {
            ocorrencias.merge(x.getUnidadeNome() + "|" + ("SEM_COBERTURA".equals(x.getSituacao()) ? "Sem cobertura" : "Profissional afastado"), 1L, Long::sum);
        }
        List<RelatorioDto.Secao> secoes = new ArrayList<>();
        secoes.add(new RelatorioDto.Secao("Tempo de espera (minutos)", List.of("Etapa", "Amostra", "Média", "Mediana", "Percentil 90"), List.of(
                espera("Até o primeiro contato", e.getPrimeiroContato()), espera("Até a consulta", e.getConsulta()))));
        secoes.add(new RelatorioDto.Secao("Ocupação das agendas, de " + o.getDe() + " a " + o.getAte(), List.of("Especialidade", "Ofertadas", "Ocupadas", "Ocupação (%)"),
                o.getEspecialidades().stream().map(l -> List.of(l.getEspecialidade(), n(l.getOfertadas()), n(l.getOcupadas()), n(l.getPercentual()))).toList()));
        secoes.add(new RelatorioDto.Secao("Ocorrências da escala nos próximos dias", List.of("Unidade", "Situação", "Turnos"),
                ocorrencias.entrySet().stream().map(x -> {
                    String[] c = x.getKey().split("\\|", 2);
                    return List.of(c[0], c[1], n(x.getValue()));
                }).toList()));
        return relatorio(TipoRelatorio.SITUACAO, e.getDe(), e.getAte(), e.getAbrangencia(), List.of(
                new RelatorioDto.Indicador("Atendimentos com chegada", n(e.getComChegada())),
                new RelatorioDto.Indicador("Mediana até a consulta (min)", n(e.getConsulta().getMedianaMinutos())),
                new RelatorioDto.Indicador("Ocupação das agendas (%)", n(o.getPercentual())),
                new RelatorioDto.Indicador("Turnos com ocorrência", n(escala.size()))), secoes,
                List.of("A ocupação e a escala olham para frente, a partir de hoje; o período vale para o tempo de espera."));
    }

    private RelatorioDto programas(LocalDate de, LocalDate ate, UUID unidadeId) {
        ProgramasNaRedeDto d = redeService.programas(de, ate, unidadeId);
        long inscritos = d.getProgramas().stream().mapToLong(ProgramasNaRedeDto.Programa::getInscritosAtivos).sum();
        long faltosos = d.getProgramas().stream().mapToLong(ProgramasNaRedeDto.Programa::getFaltosos).sum();
        List<String> notas = new ArrayList<>();
        if (d.getSensiveisOcultos() > 0) {
            notas.add(d.getSensiveisOcultos() + " programa(s) sensível(is) oculto(s) pelo seu acesso.");
        }
        return relatorio(TipoRelatorio.PROGRAMAS, d.getDe(), d.getAte(), d.getAbrangencia(), List.of(
                new RelatorioDto.Indicador("Programas", n(d.getProgramas().size())),
                new RelatorioDto.Indicador("Inscritos ativos", n(inscritos)),
                new RelatorioDto.Indicador("Faltosos", n(faltosos))),
                List.of(new RelatorioDto.Secao("Programas", List.of("Programa", "Unidades", "Inscritos ativos", "Novos", "Saídas", "Faltosos", "Em dia (%)", "Metas atingidas", "Em risco", "Abaixo"),
                        d.getProgramas().stream().map(p -> List.of(p.getNome(), n(p.getUnidades()), n(p.getInscritosAtivos()), n(p.getNovos()), n(p.getSaidas()),
                                n(p.getFaltosos()), n(p.getEmDiaPercentual()), n(p.getMetasAtingidas()), n(p.getMetasEmRisco()), n(p.getMetasAbaixo()))).toList())), notas);
    }

    private RelatorioDto vacinacao(LocalDate de, LocalDate ate, UUID unidadeId) {
        VacinacaoNaRedeDto d = coberturaService.naRede(de, ate, unidadeId);
        List<RelatorioDto.Secao> secoes = new ArrayList<>();
        secoes.add(new RelatorioDto.Secao("Cobertura hoje, pelo cadastro", List.of("Vacina", "Dose", "Meta (%)", "Coorte", "Vacinados", "Cobertura (%)", "Meta atingida"),
                d.getCobertura().stream().map(RelatorioService::cobertura).toList()));
        contagens(secoes, "Doses por vacina", d.getPorVacina());
        contagens(secoes, "Doses por estratégia", d.getPorEstrategia());
        secoes.add(new RelatorioDto.Secao("Campanhas", List.of("Campanha", "Início", "Fim", "Situação", "Meta", "Realizado"),
                d.getCampanhas().stream().map(c -> List.of(c.getNome(), String.valueOf(c.getInicio()), String.valueOf(c.getFim()), c.getSituacao().name(),
                        c.getMetaQuantidade() == null ? "" : n(c.getMetaQuantidade()), n(c.getRealizado()))).toList()));
        return relatorio(TipoRelatorio.VACINACAO, d.getDe(), d.getAte(), d.getAbrangencia(), List.of(
                new RelatorioDto.Indicador("Doses aplicadas", n(d.getDosesAplicadas())),
                new RelatorioDto.Indicador("Vacinas na meta", d.getVacinasNaMeta() + " de " + d.getVacinasAvaliadas()),
                new RelatorioDto.Indicador("Homogeneidade (%)", n(d.getHomogeneidade())),
                new RelatorioDto.Indicador("ESAVI notificados", n(d.getEsaviNotificados()))), secoes, List.of());
    }

    private RelatorioDto vigilancia(LocalDate de, LocalDate ate, UUID unidadeId) {
        VigilanciaNaRedeDto d = redeService.vigilancia(de, ate, unidadeId);
        VigilanciaNaRedeDto.Regularidade r = d.getRegularidade();
        List<String> notas = new ArrayList<>();
        notas.add("Regularidade da notificação na semana " + r.getSemana() + "/" + r.getAno() + ": " + r.getRegulares() + " de " + r.getUnidadesAssistenciais() + " unidades.");
        if (d.getSensiveisOcultas() > 0) {
            notas.add(d.getSensiveisOcultas() + " notificação(ões) sensível(is) oculta(s) pelo seu acesso.");
        }
        return relatorio(TipoRelatorio.VIGILANCIA, d.getDe(), d.getAte(), d.getAbrangencia(), List.of(
                new RelatorioDto.Indicador("Notificadas", n(d.getNotificadas())),
                new RelatorioDto.Indicador("Confirmadas", n(d.getConfirmadas())),
                new RelatorioDto.Indicador("Em aberto", n(d.getEmAberto())),
                new RelatorioDto.Indicador("Encerradas no prazo (%)", n(d.getOportunidadeEncerramento()))),
                List.of(new RelatorioDto.Secao("Por agravo", List.of("Agravo", "Notificadas", "Confirmadas", "Descartadas", "Em aberto", "Encerradas", "No prazo (%)"),
                                d.getAgravos().stream().map(a -> List.of(a.getNome(), n(a.getNotificadas()), n(a.getConfirmadas()), n(a.getDescartadas()), n(a.getEmAberto()),
                                        n(a.getEncerradas()), n(a.getOportunidadeEncerramento()))).toList()),
                        new RelatorioDto.Secao("Por semana epidemiológica", List.of("Ano", "Semana", "Início", "Notificações"),
                                d.getSemanas().stream().map(x -> List.of(String.valueOf(x.getAno()), String.valueOf(x.getSemana()), String.valueOf(x.getInicio()), n(x.getNotificacoes()))).toList()),
                        new RelatorioDto.Secao("Por território", List.of("Território", "Área", "Microárea", "Notificações"),
                                d.getTerritorio().stream().map(x -> List.of(vazio(x.getTerritorio()), vazio(x.getArea()), vazio(x.getMicroarea()), n(x.getNotificacoes()))).toList())), notas);
    }

    // ── Apoio ──────────────────────────────────────────────────────────────────────────────────────

    private static TipoRelatorio tipo(String codigo) {
        return Arrays.stream(TipoRelatorio.values()).filter(t -> t.name().equalsIgnoreCase(codigo)).findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Não foi possível encontrar o relatório " + codigo + "."));
    }

    private static RelatorioDto relatorio(TipoRelatorio t, LocalDate de, LocalDate ate, String abrangencia, List<RelatorioDto.Indicador> resumo,
                                          List<RelatorioDto.Secao> secoes, List<String> notas) {
        return new RelatorioDto(t.name(), t.getNome(), t.getCategoria(), de, ate, abrangencia, resumo, secoes, notas);
    }

    private static void contagens(List<RelatorioDto.Secao> secoes, String titulo, List<ContagemProducaoDto> lista) {
        if (lista != null && !lista.isEmpty()) {
            secoes.add(new RelatorioDto.Secao(titulo, List.of("Item", "Quantidade"), lista.stream().map(c -> List.of(c.getRotulo(), n(c.getQuantidade()))).toList()));
        }
    }

    private static List<String> espera(String etapa, EstatisticaEsperaDto e) {
        return List.of(etapa, n(e.getAmostra()), n(e.getMediaMinutos()), n(e.getMedianaMinutos()), n(e.getP90Minutos()));
    }

    private static List<String> cobertura(CoberturaVacinalDto.Vacina v) {
        return List.of(v.getNome(), vazio(v.getDoseRotulo()), n(v.getMeta()), n(v.getCoorte()), n(v.getVacinados()), n(v.getCobertura()), v.isAtingida() ? "Sim" : "Não");
    }

    private static String vazio(String s) {
        return s == null ? "" : s;
    }

    private static String n(long v) {
        return Long.toString(v);
    }

    private static String n(Number v) {
        if (v == null) {
            return "";
        }
        return (v instanceof BigDecimal b ? b.toPlainString() : v.toString()).replace('.', ',');
    }

    private static void linha(StringBuilder s, List<String> celulas) {
        s.append(String.join(";", celulas.stream().map(CELULA).toList())).append('\n');
    }

    /** Tira o separador e as quebras, e neutraliza o texto que a planilha leria como fórmula. */
    private static final Function<String, String> CELULA = c -> {
        String t = c.replace(';', ',').replace('\n', ' ').replace('\r', ' ');
        return !t.isEmpty() && "=+-@".indexOf(t.charAt(0)) >= 0 && !t.matches("-?[0-9][0-9.,]*") ? "'" + t : t;
    };
}
