package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Atendimento;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.Sexo;
import com.edufelizardo.maissaudepublica.models.enuns.StatusAtendimento;
import com.edufelizardo.maissaudepublica.models.enuns.TipoAtendimento;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.AtendimentoRepository;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import com.edufelizardo.maissaudepublica.services.version1.SemanaEpidemiologica;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Indicadores da rede, fatia BI3 (ADR-0146): o programa consolidado igual à apuração da P6 na unidade, a meta da
 * unidade antes da meta da rede, a lista por unidade com as abaixo da meta primeiro, e a vigilância (oportunidade de
 * encerramento, regularidade com a notificação negativa, semanas e território).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class IndicadoresDaRedeControllerTest {

    private static final String PREFIXO = "Rede Teste ";

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private UnidadeDeSaudeRepository unidadeDeSaudeRepository;
    @Autowired private PacienteRepository pacienteRepository;
    @Autowired private ProfissionalRepository profissionalRepository;
    @Autowired private AtendimentoRepository atendimentoRepository;

    private final LocalDate hoje = LocalDate.now();
    private UnidadeDeSaude ubsA;
    private UnidadeDeSaude ubsB;
    private Profissional medico;
    private String hiperdia;
    private int pacientes;

    @BeforeEach
    void seed() {
        limpar();
        ubsA = unidade("UBS A");
        ubsB = unidade("UBS B");
        medico = new Profissional();
        medico.setMatricula("P-BI3-MED");
        medico.setCpf("90077700001");
        medico.setNome(PREFIXO + "Médico");
        medico.setConselhoClasse("CRM");
        medico.setAtivo(true);
        medico = profissionalRepository.save(medico);
        hiperdia = jdbc.queryForObject("select cast(uuid as varchar) from tb_programa where codigo = 'HIPERDIA'", String.class);
        for (UnidadeDeSaude u : List.of(ubsA, ubsB)) {
            jdbc.update("insert into tb_participacao_programa (uuid, programa_id, unidade_id, inicio) values (gen_random_uuid(), cast(? as uuid), ?, ?)",
                    hiperdia, u.getUuid(), hoje.minusYears(1));
        }
    }

    @AfterEach
    void limpar() {
        String unidades = "(select uuid from tb_unidade_de_saude where nome like '" + PREFIXO + "%')";
        String pacientesSql = "(select uuid from tb_paciente where nome like '" + PREFIXO + "%')";
        jdbc.update("delete from tb_meta_programatica where registrado_por_cpf = 'BI3-TESTE'");
        jdbc.update("delete from tb_notificacao where unidade_id in " + unidades);
        jdbc.update("delete from tb_notificacao_negativa where unidade_id in " + unidades);
        jdbc.update("delete from tb_microarea where codigo = 'BI3-MA'");
        jdbc.update("delete from tb_area where codigo = 'BI3-AR'");
        jdbc.update("delete from tb_territorio where nome = '" + PREFIXO + "Território'");
        jdbc.update("delete from tb_segmento_territorial where codigo = 'BI3-SEG'");
        jdbc.update("delete from tb_atendimento where paciente_id in " + pacientesSql);
        jdbc.update("delete from tb_evento_acompanhamento where acompanhamento_id in (select uuid from tb_acompanhamento_programatico where paciente_id in "
                + pacientesSql + ")");
        jdbc.update("delete from tb_acompanhamento_programatico where paciente_id in " + pacientesSql);
        jdbc.update("delete from tb_participacao_programa where unidade_id in " + unidades);
        jdbc.update("delete from tb_paciente where nome like '" + PREFIXO + "%'");
        jdbc.update("delete from tb_profissional where matricula = 'P-BI3-MED'");
        jdbc.update("delete from tb_unidade_de_saude where nome like '" + PREFIXO + "%'");
    }

    private UnidadeDeSaude unidade(String nome) {
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + nome);
        u.setTipo(TipoUnidadeDeSaude.UBS);
        u.setAtivo(true);
        return unidadeDeSaudeRepository.save(u);
    }

    private Paciente paciente() {
        pacientes++;
        Paciente p = new Paciente();
        p.setNome(PREFIXO + "Paciente " + pacientes);
        p.setCpf(String.format("900777%05d", pacientes));
        p.setSexo(Sexo.FEMININO);
        p.setDataNascimento(LocalDate.of(1960, 1, 1));
        p.setAtivo(true);
        return pacienteRepository.save(p);
    }

    private Paciente inscrito(UnidadeDeSaude u, boolean atendidoRecentemente) throws Exception {
        Paciente p = paciente();
        mockMvc.perform(post("/api/v1/programa/" + hiperdia + "/acompanhamento").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pacienteId\": \"%s\", \"unidadeId\": \"%s\", \"inicio\": \"%s\"}".formatted(p.getUuid(), u.getUuid(), hoje.minusDays(10))))
                .andExpect(status().isCreated());
        if (atendidoRecentemente) {
            atendimentoRepository.save(new Atendimento(p, medico, u, null, null, TipoAtendimento.CONSULTA, StatusAtendimento.CONCLUIDO,
                    LocalDateTime.now().minusDays(2)));
        }
        return p;
    }

    private void meta(UnidadeDeSaude u, String indicador, int alvo) {
        jdbc.update("insert into tb_meta_programatica (uuid, programa_id, unidade_id, indicador, alvo, inicio, fim, criada_em, registrado_por_cpf) "
                + "values (gen_random_uuid(), cast(? as uuid), ?, ?, ?, ?, ?, now(), 'BI3-TESTE')", hiperdia, u == null ? null : u.getUuid(), indicador, alvo,
                hoje.minusDays(60), hoje.plusDays(60));
    }

    private String ler(String url) throws Exception {
        return mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    private static double numero(Object v) {
        return ((Number) v).doubleValue();
    }

    @Test
    void programaNaUnidadeIgualAApuracaoDaP6() throws Exception {
        inscrito(ubsA, true);
        inscrito(ubsA, false);
        String periodo = "de=" + hoje.minusDays(29) + "&ate=" + hoje;
        String r = ler("/api/v1/indicadores-rede/programas?" + periodo + "&unidadeId=" + ubsA.getUuid());
        Map<String, Object> h = ((List<Map<String, Object>>) JsonPath.read(r, "$.programas[?(@.codigo == 'HIPERDIA')]")).get(0);
        assertThat(h).containsEntry("unidades", 1).containsEntry("inscritosAtivos", 2).containsEntry("novos", 2).containsEntry("saidas", 0);
        // A mesma apuração do detalhe do programa (P6) na unidade.
        String p6 = ler("/api/v1/programa/" + hiperdia + "/indicadores?" + periodo + "&unidadeId=" + ubsA.getUuid());
        double emDiaP6 = numero(((List<Object>) JsonPath.read(p6, "$[?(@.indicador == 'ACOMPANHADOS_EM_DIA')].valor")).get(0));
        assertThat(numero(h.get("emDiaPercentual"))).isEqualTo(emDiaP6).isEqualTo(50.0);
        assertThat((Integer) JsonPath.read(r, "$.sensiveisOcultos")).isZero();
        mockMvc.perform(get("/api/v1/indicadores-rede/programas?de=" + hoje + "&ate=" + hoje.minusDays(1))).andExpect(status().isBadRequest());
    }

    @Test
    void porUnidadeComAMetaDaUnidadeAntesDaRedeEAbaixoPrimeiro() throws Exception {
        inscrito(ubsA, true);
        inscrito(ubsA, false);
        inscrito(ubsB, false);
        // Rede: em dia com alvo 80 (A, com 50%, fica abaixo). B tem a própria meta, alvo 0 (atingida).
        meta(null, "ACOMPANHADOS_EM_DIA", 80);
        meta(ubsB, "ACOMPANHADOS_EM_DIA", 0);
        String r = ler("/api/v1/indicadores-rede/programa/" + hiperdia + "/unidades?de=" + hoje.minusDays(29) + "&ate=" + hoje);
        List<Map<String, Object>> linhas = JsonPath.read(r, "$.unidades[?(@.unidadeNome =~ /" + PREFIXO + ".*/)]");
        assertThat(linhas).extracting(l -> l.get("unidadeNome")).containsExactly(PREFIXO + "UBS A", PREFIXO + "UBS B");
        String a = "$.unidades[?(@.unidadeId == '" + ubsA.getUuid() + "')].indicadores[?(@.indicador == 'ACOMPANHADOS_EM_DIA')]";
        String b = "$.unidades[?(@.unidadeId == '" + ubsB.getUuid() + "')].indicadores[?(@.indicador == 'ACOMPANHADOS_EM_DIA')]";
        assertThat((List<Object>) JsonPath.read(r, a + ".alvo")).extracting(v -> numero(v)).containsExactly(80.0);
        assertThat((List<String>) JsonPath.read(r, a + ".situacao")).containsExactly("ABAIXO");
        assertThat((List<Object>) JsonPath.read(r, b + ".alvo")).extracting(v -> numero(v)).containsExactly(0.0);
        assertThat((List<String>) JsonPath.read(r, b + ".situacao")).containsExactly("ATINGIDA");
        assertThat((List<Integer>) JsonPath.read(r, "$.unidades[?(@.unidadeId == '" + ubsA.getUuid() + "')].metasAbaixo")).containsExactly(1);
        assertThat(r).doesNotContain(PREFIXO + "Paciente");
    }

    @Test
    void vigilanciaComOportunidadeRegularidadeSemanasETerritorio() throws Exception {
        String agravo = jdbc.queryForObject("select cast(uuid as varchar) from tb_agravo where not sensivel order by codigo limit 1", String.class);
        String sensivel = jdbc.queryForObject("select cast(uuid as varchar) from tb_agravo where sensivel order by codigo limit 1", String.class);
        String segmento = jdbc.queryForObject("insert into tb_segmento_territorial (uuid, codigo, nome, tipo) values (gen_random_uuid(), 'BI3-SEG', 'Segmento Teste', "
                + "'DISTRITO_SANITARIO') returning cast(uuid as varchar)", String.class);
        String territorio = jdbc.queryForObject("insert into tb_territorio (uuid, segmento_id, nome, finalidade, ativo) values (gen_random_uuid(), cast(? as uuid), ?, "
                + "'ATENCAO_PRIMARIA', true) returning cast(uuid as varchar)", String.class, segmento, PREFIXO + "Território");
        String area = jdbc.queryForObject("insert into tb_area (uuid, territorio_id, codigo, nome) values (gen_random_uuid(), cast(? as uuid), 'BI3-AR', 'Área Teste') "
                + "returning cast(uuid as varchar)", String.class, territorio);
        String microarea = jdbc.queryForObject("insert into tb_microarea (uuid, area_id, codigo, nome) values (gen_random_uuid(), cast(? as uuid), 'BI3-MA', 'Microárea Teste') "
                + "returning cast(uuid as varchar)", String.class, area);

        String n = "insert into tb_notificacao (uuid, agravo_id, paciente_id, unidade_id, data_notificacao, prazo_encerramento, registrada_em, origem, situacao, "
                + "classificacao_final, encerrada_em, microarea_id) values (gen_random_uuid(), cast(? as uuid), ?, ?, ?, ?, now(), 'ATENDIMENTO', ?, ?, ?, cast(? as uuid))";
        // Encerrada no prazo (confirmada), encerrada fora do prazo (descartada), aberta na microárea, e uma de agravo sensível.
        jdbc.update(n, agravo, paciente().getUuid(), ubsA.getUuid(), hoje.minusDays(10), hoje, "ENCERRADA", "CONFIRMADO", Timestamp.valueOf(LocalDateTime.now().minusDays(3)), null);
        jdbc.update(n, agravo, paciente().getUuid(), ubsA.getUuid(), hoje.minusDays(9), hoje.minusDays(5), "ENCERRADA", "DESCARTADO", Timestamp.valueOf(LocalDateTime.now().minusDays(1)), null);
        jdbc.update(n, agravo, paciente().getUuid(), ubsA.getUuid(), hoje.minusDays(2), hoje.plusDays(30), "NOTIFICADA", null, null, microarea);
        jdbc.update(n, sensivel, paciente().getUuid(), ubsA.getUuid(), hoje.minusDays(1), hoje.plusDays(30), "NOTIFICADA", null, null, null);
        SemanaEpidemiologica anterior = SemanaEpidemiologica.de(hoje).anterior();
        jdbc.update("insert into tb_notificacao_negativa (uuid, unidade_id, ano, semana, declarada_em) values (gen_random_uuid(), ?, ?, ?, now())",
                ubsA.getUuid(), anterior.ano(), anterior.semana());

        String r = ler("/api/v1/indicadores-rede/vigilancia?de=" + hoje.minusDays(29) + "&ate=" + hoje + "&unidadeId=" + ubsA.getUuid());
        assertThat((Integer) JsonPath.read(r, "$.notificadas")).isEqualTo(4);
        assertThat((Integer) JsonPath.read(r, "$.confirmadas")).isEqualTo(1);
        assertThat((Integer) JsonPath.read(r, "$.descartadas")).isEqualTo(1);
        assertThat((Integer) JsonPath.read(r, "$.emAberto")).isEqualTo(2);
        assertThat((Integer) JsonPath.read(r, "$.encerradas")).isEqualTo(2);
        assertThat((Integer) JsonPath.read(r, "$.encerradasNoPrazo")).isEqualTo(1);
        assertThat(numero(JsonPath.read(r, "$.oportunidadeEncerramento"))).isEqualTo(50.0);
        // Com a autorização desligada, o agravo sensível aparece com o nome.
        assertThat((Integer) JsonPath.read(r, "$.sensiveisOcultas")).isZero();
        assertThat((List<?>) JsonPath.read(r, "$.agravos")).hasSize(2);
        assertThat((List<?>) JsonPath.read(r, "$.semanas")).hasSize(12);
        assertThat((Integer) JsonPath.read(r, "$.semanas[11].notificacoes")).isGreaterThanOrEqualTo(1);
        assertThat((Integer) JsonPath.read(r, "$.regularidade.semana")).isEqualTo(anterior.semana());
        assertThat((Integer) JsonPath.read(r, "$.regularidade.unidadesAssistenciais")).isEqualTo(1);
        assertThat((Integer) JsonPath.read(r, "$.regularidade.comNegativa")).isEqualTo(1);
        assertThat(numero(JsonPath.read(r, "$.regularidade.percentual"))).isEqualTo(100.0);
        assertThat((List<Integer>) JsonPath.read(r, "$.territorio[?(@.microarea == 'Microárea Teste')].notificacoes")).containsExactly(1);
        assertThat((List<String>) JsonPath.read(r, "$.territorio[?(@.microarea == 'Microárea Teste')].territorio")).containsExactly(PREFIXO + "Território");
        assertThat((List<Integer>) JsonPath.read(r, "$.territorio[?(@.territorio == 'Sem território')].notificacoes")).containsExactly(3);
        assertThat(r).doesNotContain(PREFIXO + "Paciente");

        String csv = ler("/api/v1/indicadores-rede/exportacao?de=" + hoje.minusDays(29) + "&ate=" + hoje + "&unidadeId=" + ubsA.getUuid());
        assertThat(csv).contains("Total;4;1;1;2;2;50.0").doesNotContain(PREFIXO + "Paciente");
    }
}
