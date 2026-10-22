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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Metas e indicadores programáticos, fatia P6 (ADR-0141): cada indicador calculado a partir dos registros, as metas com
 * a situação, a série mensal e o relatório em CSV. Só números.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class IndicadorProgramaticoControllerTest {

    private static final String PREFIXO = "Indicador Teste ";
    private static final String MATRICULA = "P6-MEDICO";
    private static final String URL = "/api/v1/";

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private UnidadeDeSaudeRepository unidadeDeSaudeRepository;
    @Autowired private PacienteRepository pacienteRepository;
    @Autowired private ProfissionalRepository profissionalRepository;
    @Autowired private AtendimentoRepository atendimentoRepository;

    private UnidadeDeSaude ubs;
    private Profissional medico;
    private final LocalDate hoje = LocalDate.now();

    @BeforeEach
    void seed() {
        limpar();
        ubs = new UnidadeDeSaude();
        ubs.setNome(PREFIXO + "UBS");
        ubs.setTipo(TipoUnidadeDeSaude.UBS);
        ubs.setAtivo(true);
        ubs = unidadeDeSaudeRepository.save(ubs);
        jdbc.update("insert into tb_participacao_programa (uuid, programa_id, unidade_id, inicio) select gen_random_uuid(), uuid, ?, ? from tb_programa "
                + "where codigo in ('HIPERDIA', 'PRE_NATAL', 'SAUDE_BUCAL')", ubs.getUuid(), hoje.minusYears(1));
        medico = new Profissional();
        medico.setMatricula(MATRICULA);
        medico.setCpf("90044400009");
        medico.setNome(PREFIXO + "Médico");
        medico.setAtivo(true);
        medico = profissionalRepository.save(medico);
    }

    @AfterEach
    void limpar() {
        String pacientes = "(select uuid from tb_paciente where nome like '" + PREFIXO + "%')";
        String unidades = "(select uuid from tb_unidade_de_saude where nome like '" + PREFIXO + "%')";
        String acoes = "(select uuid from tb_acao_programatica where unidade_id in " + unidades + ")";
        jdbc.update("delete from tb_meta_programatica where unidade_id in " + unidades + " or registrado_por_cpf = 'P6-TESTE'");
        jdbc.update("delete from tb_acao_saude_bucal_atividade where detalhe_id in (select uuid from tb_acao_saude_bucal where acao_id in " + acoes + ")");
        jdbc.update("delete from tb_acao_saude_bucal where acao_id in " + acoes);
        jdbc.update("delete from tb_acao_programatica_participante where acao_id in " + acoes);
        jdbc.update("delete from tb_acao_programatica where uuid in " + acoes);
        jdbc.update("delete from tb_tratamento_protetico where paciente_id in " + pacientes);
        jdbc.update("delete from tb_plano_tratamento where paciente_id in " + pacientes);
        jdbc.update("delete from tb_triagem where atendimento_id in (select uuid from tb_atendimento where paciente_id in " + pacientes + ")");
        jdbc.update("delete from tb_atendimento where paciente_id in " + pacientes);
        jdbc.update("delete from tb_evento_acompanhamento where acompanhamento_id in (select uuid from tb_acompanhamento_programatico where paciente_id in "
                + pacientes + ")");
        jdbc.update("delete from tb_acompanhamento_programatico where paciente_id in " + pacientes);
        jdbc.update("delete from tb_participacao_programa where unidade_id in " + unidades);
        jdbc.update("delete from tb_paciente where nome like '" + PREFIXO + "%'");
        jdbc.update("delete from tb_profissional where matricula = '" + MATRICULA + "'");
        jdbc.update("delete from tb_unidade_de_saude where nome like '" + PREFIXO + "%'");
    }

    private Paciente paciente(String nome, String cpf, Sexo sexo) {
        Paciente p = new Paciente();
        p.setNome(PREFIXO + nome);
        p.setCpf(cpf);
        p.setSexo(sexo);
        p.setAtivo(true);
        return pacienteRepository.save(p);
    }

    private String enviar(MockHttpServletRequestBuilder r, String corpo, int esperado) throws Exception {
        return mockMvc.perform(r.contentType(MediaType.APPLICATION_JSON).content(corpo)).andExpect(status().is(esperado))
                .andReturn().getResponse().getContentAsString();
    }

    private String ler(String url) throws Exception {
        return mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
    }

    private String programa(String codigo) {
        return jdbc.queryForObject("select cast(uuid as varchar) from tb_programa where codigo = ?", String.class, codigo);
    }

    private void inscrever(String programa, Paciente p, LocalDate inicio) throws Exception {
        enviar(post(URL + "programa/" + programa + "/acompanhamento"), "{\"pacienteId\": \"%s\", \"unidadeId\": \"%s\", \"inicio\": \"%s\"}"
                .formatted(p.getUuid(), ubs.getUuid(), inicio), 201);
    }

    private Atendimento atendimento(Paciente p, LocalDateTime quando) {
        return atendimentoRepository.save(new Atendimento(p, medico, ubs, null, null, TipoAtendimento.CONSULTA, StatusAtendimento.CONCLUIDO, quando));
    }

    private void triagem(Atendimento a, String pa) {
        jdbc.update("insert into tb_triagem (uuid, classificacao_risco, data_hora, atendimento_id, profissional_id, pressao_arterial) "
                + "values (gen_random_uuid(), 'VERDE', ?, ?, ?, ?)", a.getDataHora(), a.getUuid(), medico.getUuid(), pa);
    }

    private static List<Double> valor(String json, String indicador) {
        return ((List<Object>) JsonPath.read(json, "$[?(@.indicador == '" + indicador + "')].valor")).stream()
                .map(v -> Double.valueOf(String.valueOf(v))).toList();
    }

    @Test
    void hiperdiaEmDiaPaControladaEMetas() throws Exception {
        String hiperdia = programa("HIPERDIA");
        Paciente ana = paciente("Ana", "90044400001", Sexo.FEMININO);
        Paciente bruno = paciente("Bruno", "90044400002", Sexo.MASCULINO);
        Paciente caio = paciente("Caio", "90044400003", Sexo.MASCULINO);
        for (Paciente p : List.of(ana, bruno, caio)) {
            inscrever(hiperdia, p, hoje.minusDays(60));
        }
        triagem(atendimento(ana, LocalDateTime.now().minusDays(2)), "130/85");
        triagem(atendimento(bruno, LocalDateTime.now().minusDays(3)), "150x95");

        String url = URL + "programa/" + hiperdia + "/indicadores?de=" + hoje.minusDays(29) + "&ate=" + hoje + "&unidadeId=" + ubs.getUuid();
        String r = ler(url);
        assertThat((List<String>) JsonPath.read(r, "$[*].indicador")).contains("ACOMPANHADOS_EM_DIA", "PA_CONTROLADA", "EXAME_NO_ALVO")
                .doesNotContain("PRE_NATAL_6_CONSULTAS", "PROTESES_ENTREGUES");
        assertThat(valor(r, "ACOMPANHADOS_EM_DIA")).containsExactly(66.67d);
        assertThat(valor(r, "PA_CONTROLADA")).containsExactly(50.00d);
        // HiperDia é por condição: a cobertura não tem denominador; o exame no alvo pede a meta.
        assertThat((List<Boolean>) JsonPath.read(r, "$[?(@.indicador == 'COBERTURA')].aplicavel")).containsExactly(false);
        assertThat((List<Boolean>) JsonPath.read(r, "$[?(@.indicador == 'EXAME_NO_ALVO')].aplicavel")).containsExactly(false);

        // Metas: validação, repetição e situação.
        String meta = "{\"indicador\": \"PA_CONTROLADA\", \"unidadeId\": \"%s\", \"alvo\": %s, \"inicio\": \"%s\", \"fim\": \"%s\"}";
        enviar(post(URL + "programa/" + hiperdia + "/meta"), meta.formatted(ubs.getUuid(), "120", hoje.minusDays(29), hoje.plusDays(30)), 400);
        enviar(post(URL + "programa/" + hiperdia + "/meta"), meta.formatted(ubs.getUuid(), "70", hoje.plusDays(1), hoje), 400);
        enviar(post(URL + "programa/" + hiperdia + "/meta"), "{\"indicador\": \"PRE_NATAL_6_CONSULTAS\", \"unidadeId\": \"%s\", \"alvo\": 60, \"inicio\": \"%s\", \"fim\": \"%s\"}"
                .formatted(ubs.getUuid(), hoje.minusDays(29), hoje), 422);
        enviar(post(URL + "programa/" + hiperdia + "/meta"), "{\"indicador\": \"EXAME_NO_ALVO\", \"unidadeId\": \"%s\", \"alvo\": 60, \"inicio\": \"%s\", \"fim\": \"%s\"}"
                .formatted(ubs.getUuid(), hoje.minusDays(29), hoje), 400);
        String criada = enviar(post(URL + "programa/" + hiperdia + "/meta"), meta.formatted(ubs.getUuid(), "70", hoje.minusDays(29), hoje.plusDays(30)), 201);
        enviar(post(URL + "programa/" + hiperdia + "/meta"), meta.formatted(ubs.getUuid(), "80", hoje.minusDays(29), hoje.plusDays(30)), 409);
        // Período que se cruza também é repetição (a meta vigente tem que ser uma só); o seguinte, não.
        enviar(post(URL + "programa/" + hiperdia + "/meta"), meta.formatted(ubs.getUuid(), "80", hoje.plusDays(30), hoje.plusDays(60)), 409);
        String seguinte = enviar(post(URL + "programa/" + hiperdia + "/meta"), meta.formatted(ubs.getUuid(), "80", hoje.plusDays(31), hoje.plusDays(60)), 201);
        enviar(put(URL + "meta-programatica/" + JsonPath.read(seguinte, "$.uuid")), meta.formatted(ubs.getUuid(), "80", hoje.plusDays(10), hoje.plusDays(60)), 409);
        r = ler(url);
        assertThat((List<String>) JsonPath.read(r, "$[?(@.indicador == 'PA_CONTROLADA')].situacao")).containsExactly("ABAIXO");
        // Com o alvo em 60, os 50% ficam em risco (a partir de 80% do alvo).
        enviar(put(URL + "meta-programatica/" + JsonPath.read(criada, "$.uuid")), meta.formatted(ubs.getUuid(), "60", hoje.minusDays(29), hoje.plusDays(30)), 200);
        assertThat((List<String>) JsonPath.read(ler(url), "$[?(@.indicador == 'PA_CONTROLADA')].situacao")).containsExactly("EM_RISCO");
        assertThat((List<?>) JsonPath.read(ler(URL + "programa/" + hiperdia + "/metas"), "$[?(@.indicador == 'PA_CONTROLADA')]")).hasSize(2);

        // A série: 12 meses, o último com os 50%.
        String serie = ler(URL + "programa/" + hiperdia + "/indicador/PA_CONTROLADA/serie?unidadeId=" + ubs.getUuid());
        assertThat((List<?>) JsonPath.read(serie, "$.pontos")).hasSize(12);
        assertThat(String.valueOf((Object) JsonPath.read(serie, "$.pontos[11].valor"))).isEqualTo("50.0");
        mockMvc.perform(get(URL + "programa/" + hiperdia + "/indicador/PROTESES_ENTREGUES/serie")).andExpect(status().isUnprocessableEntity());

        // O CSV, sem nenhum paciente.
        String csv = mockMvc.perform(get(url.replace("/indicadores?", "/indicadores.csv?"))).andExpect(status().isOk()).andReturn().getResponse()
                .getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        assertThat(csv).contains("PA controlada").contains("50.00").doesNotContain(PREFIXO);
    }

    @Test
    void preNatalComSeisConsultas() throws Exception {
        String preNatal = programa("PRE_NATAL");
        Paciente dora = paciente("Dora", "90044400004", Sexo.FEMININO);
        Paciente eva = paciente("Eva", "90044400005", Sexo.FEMININO);
        inscrever(preNatal, dora, hoje.minusDays(120));
        inscrever(preNatal, eva, hoje.minusDays(90));
        for (int i = 0; i < 6; i++) {
            atendimento(dora, LocalDateTime.now().minusDays(100 - i * 15L));
        }
        atendimento(eva, LocalDateTime.now().minusDays(30));
        // Antes da inscrição não conta.
        atendimento(eva, LocalDateTime.now().minusDays(200));
        String r = ler(URL + "programa/" + preNatal + "/indicadores?unidadeId=" + ubs.getUuid());
        assertThat(valor(r, "PRE_NATAL_6_CONSULTAS")).containsExactly(50.00d);
    }

    @Test
    void saudeBucalTratamentoProteseEEscovacao() throws Exception {
        String bucal = programa("SAUDE_BUCAL");
        Paciente fabio = paciente("Fábio", "90044400006", Sexo.MASCULINO);
        Atendimento at = atendimento(fabio, LocalDateTime.now().minusDays(10));
        String plano = "insert into tb_plano_tratamento (uuid, aberto_em, atendimento_id, paciente_id, profissional_id, unidade_id, prioridade, situacao, encerrado_em) "
                + "values (gen_random_uuid(), now() - interval '5 days', ?, ?, ?, ?, 'ELETIVA', ?, ?)";
        jdbc.update(plano, at.getUuid(), fabio.getUuid(), medico.getUuid(), ubs.getUuid(), "CONCLUIDO", java.sql.Timestamp.valueOf(LocalDateTime.now().minusDays(1)));
        jdbc.update(plano, at.getUuid(), fabio.getUuid(), medico.getUuid(), ubs.getUuid(), "ABANDONADO", java.sql.Timestamp.valueOf(LocalDateTime.now().minusDays(1)));
        jdbc.update("insert into tb_tratamento_protetico (uuid, aberto_em, atendimento_id, paciente_id, profissional_id, unidade_id, arcada, etapa_atual, situacao, tipo, data_entrega) "
                + "values (gen_random_uuid(), now() - interval '20 days', ?, ?, ?, ?, 'SUPERIOR', 'ENTREGA', 'EM_ANDAMENTO', 'TOTAL', ?)",
                at.getUuid(), fabio.getUuid(), medico.getUuid(), ubs.getUuid(), hoje.minusDays(2));

        String acao = JsonPath.read(enviar(post(URL + "acao-programatica"), ("{\"programaId\": \"%s\", \"tipo\": \"GRUPO\", \"unidadeId\": \"%s\", "
                + "\"dataPrevista\": \"%s\"}").formatted(bucal, ubs.getUuid(), hoje), 201), "$.uuid");
        enviar(post(URL + "acao-programatica/" + acao + "/realizacao"), "{\"quantidadeAtendida\": 25}", 200);
        enviar(put(URL + "acao-programatica/" + acao + "/saude-bucal"), "{\"atividades\": [\"ESCOVACAO_SUPERVISIONADA\"], \"localTipo\": \"ESCOLA\"}", 200);

        String r = ler(URL + "programa/" + bucal + "/indicadores?de=" + hoje.minusDays(29) + "&ate=" + hoje + "&unidadeId=" + ubs.getUuid());
        assertThat(valor(r, "TRATAMENTO_ODONTOLOGICO_CONCLUIDO")).containsExactly(50.00d);
        assertThat(valor(r, "PROTESES_ENTREGUES")).containsExactly(1d);
        assertThat(valor(r, "ESCOVACAO_SUPERVISIONADA")).containsExactly(25d);
        assertThat(valor(r, "ACOES_REALIZADAS")).containsExactly(100.00d);
        assertThat((List<String>) JsonPath.read(r, "$[?(@.indicador == 'PROTESES_ENTREGUES')].tipo")).containsExactly("QUANTIDADE");
    }

    @Test
    void periodoInvalidoEProgramaInexistente() throws Exception {
        String hiperdia = programa("HIPERDIA");
        mockMvc.perform(get(URL + "programa/" + hiperdia + "/indicadores?de=" + hoje + "&ate=" + hoje.minusDays(1))).andExpect(status().isBadRequest());
        mockMvc.perform(get(URL + "programa/" + UUID.randomUUID() + "/indicadores")).andExpect(status().isNotFound());
        mockMvc.perform(get(URL + "programa/" + hiperdia + "/indicador/PA_CONTROLADA/serie?meses=30")).andExpect(status().isBadRequest());
    }
}
