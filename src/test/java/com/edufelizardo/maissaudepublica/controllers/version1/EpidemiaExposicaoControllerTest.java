package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
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

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Emergências, fatia E6b (ADR-0161): o painel da epidemia, lido das notificações da Vigilância (agravo escolhido ou do
 * surto, semanas, classificação, óbitos), e a exposição ambiental do desastre; os números no relatório.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EpidemiaExposicaoControllerTest {

    private static final String PREFIXO = "EpiExp Teste ";
    private static final String CODIGO = "EPIEXP";

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private UnidadeDeSaudeRepository unidadeRepository;
    @Autowired private PacienteRepository pacienteRepository;

    private UnidadeDeSaude ubs;
    private String nivel1;
    private final LocalDate hoje = LocalDate.now();

    @BeforeEach
    void seed() {
        limpar();
        ubs = new UnidadeDeSaude();
        ubs.setNome(PREFIXO + "UBS");
        ubs.setTipo(TipoUnidadeDeSaude.UBS);
        ubs.setAtivo(true);
        ubs = unidadeRepository.save(ubs);
        nivel1 = jdbc.queryForObject("select cast(uuid as varchar) from tb_nivel_emergencia where ordem = 1", String.class);
    }

    @AfterEach
    void limpar() {
        String emergencias = "(select e.uuid from tb_emergencia e join tb_evento_emergencial v on v.uuid = e.evento_id where v.descricao like '" + PREFIXO + "%')";
        jdbc.update("delete from tb_exposicao_ambiental_unidade where exposicao_id in (select uuid from tb_exposicao_ambiental where emergencia_id in "
                + emergencias + ")");
        jdbc.update("delete from tb_exposicao_ambiental where emergencia_id in " + emergencias);
        jdbc.update("delete from tb_desmobilizacao where emergencia_id in " + emergencias);
        jdbc.update("delete from tb_mudanca_nivel_emergencia where emergencia_id in " + emergencias);
        jdbc.update("delete from tb_emergencia where uuid in " + emergencias);
        jdbc.update("delete from tb_evento_emergencial where descricao like '" + PREFIXO + "%'");
        VigilanciaControllerTest.limparDados(jdbc, PREFIXO, CODIGO);
    }

    private String enviar(MockHttpServletRequestBuilder r, String corpo, int esperado) throws Exception {
        return mockMvc.perform(r.contentType(MediaType.APPLICATION_JSON).content(corpo)).andExpect(status().is(esperado)).andReturn().getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
    }

    private String ler(String url, int esperado) throws Exception {
        return mockMvc.perform(get(url)).andExpect(status().is(esperado)).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    private String tipo(String codigo) {
        return jdbc.queryForObject("select cast(uuid as varchar) from tb_tipo_emergencia where codigo = ?", String.class, codigo);
    }

    private String emergencia(String tipo, String descricao) throws Exception {
        String evento = JsonPath.read(enviar(post("/api/v1/evento-emergencial"), "{\"tipoId\": \"" + tipo(tipo) + "\", \"fonte\": \"VIGILANCIA\", "
                + "\"descricao\": \"" + PREFIXO + descricao + "\"}", 201), "$.uuid");
        return JsonPath.read(enviar(post("/api/v1/evento-emergencial/" + evento + "/emergencia"), "{\"nome\": \"" + descricao + "\", \"nivelId\": \""
                + nivel1 + "\", \"justificativa\": \"Limiar\"}", 201), "$.uuid");
    }

    private String agravo(String sufixo, boolean sensivel) throws Exception {
        return JsonPath.read(enviar(post("/api/v1/vigilancia/agravo"), "{\"codigo\": \"" + CODIGO + sufixo + "\", \"nome\": \"" + PREFIXO + sufixo
                + "\", \"periodicidade\": \"SEMANAL\", \"prazoEncerramentoDias\": 30, \"criterios\": [\"CLINICO\"], \"sensivel\": " + sensivel + "}", 201), "$.uuid");
    }

    private String notificar(String agravo, String cpf, LocalDate sintomas) throws Exception {
        Paciente p = new Paciente();
        p.setNome(PREFIXO + cpf);
        p.setCpf(cpf);
        p.setAtivo(true);
        p = pacienteRepository.save(p);
        return JsonPath.read(enviar(post("/api/v1/vigilancia/notificacao"), "{\"pacienteId\": \"%s\", \"agravoId\": \"%s\", \"unidadeId\": \"%s\", \"origem\": \"ATENDIMENTO\", \"dataPrimeirosSintomas\": \"%s\"}"
                .formatted(p.getUuid(), agravo, ubs.getUuid(), sintomas), 201), "$.uuid");
    }

    @Test
    void painelDaEpidemia() throws Exception {
        String febre = agravo("FEBRE", false);
        String sensivel = agravo("SENS", true);
        String em = emergencia("EPIDEMIA", "Epidemia de febre");
        String base = "/api/v1/emergencia/" + em;
        // Sem agravo (nem surto), 422; sensível, 422; em outro perfil, 422.
        ler(base + "/painel-epidemia", 422);
        enviar(put(base + "/agravo"), "{\"agravoId\": \"" + sensivel + "\"}", 422);
        enviar(put("/api/v1/emergencia/" + emergencia("INUNDACAO", "Cheia") + "/agravo"), "{\"agravoId\": \"" + febre + "\"}", 422);
        enviar(put(base + "/agravo"), "{\"agravoId\": \"" + UUID.randomUUID() + "\"}", 404);
        String r = enviar(put(base + "/agravo"), "{\"agravoId\": \"" + febre + "\"}", 200);
        assertThat((String) JsonPath.read(r, "$.agravoNome")).isEqualTo(PREFIXO + "FEBRE");

        String confirmado = notificar(febre, "93970000001", hoje.minusDays(10));
        String descartado = notificar(febre, "93970000002", hoje);
        notificar(febre, "93970000003", hoje);
        notificar(febre, "93970000004", hoje.minusDays(90));
        jdbc.update("update tb_notificacao set classificacao_final = 'CONFIRMADO', evolucao = 'OBITO_PELO_AGRAVO' where uuid = cast(? as uuid)", confirmado);
        jdbc.update("update tb_notificacao set classificacao_final = 'DESCARTADO' where uuid = cast(? as uuid)", descartado);

        String p = ler(base + "/painel-epidemia", 200);
        assertThat((Integer) JsonPath.read(p, "$.notificados")).isEqualTo(3);
        assertThat((Integer) JsonPath.read(p, "$.confirmados")).isEqualTo(1);
        assertThat((Integer) JsonPath.read(p, "$.descartados")).isEqualTo(1);
        assertThat((Integer) JsonPath.read(p, "$.emInvestigacao")).isEqualTo(1);
        assertThat((Integer) JsonPath.read(p, "$.obitos")).isEqualTo(1);
        assertThat((Integer) JsonPath.read(p, "$.semanaAtual")).isEqualTo(2);
        assertThat((Boolean) JsonPath.read(p, "$.doSurto")).isFalse();
        assertThat((Boolean) JsonPath.read(p, "$.naAreaAfetada")).isFalse();
        assertThat(((List<Integer>) JsonPath.read(p, "$.curva[*].casos")).stream().mapToInt(Integer::intValue).sum()).isEqualTo(3);

        // No relatório, os números da epidemia.
        enviar(post(base + "/desmobilizacao"), "{\"responsavel\": \"Vigilância\", \"justificativa\": \"Queda\"}", 200);
        String rel = ler(base + "/relatorio", 200);
        assertThat((Integer) JsonPath.read(rel, "$.indicadores.casosNotificados")).isEqualTo(3);
        assertThat((Integer) JsonPath.read(rel, "$.indicadores.obitosAgravo")).isEqualTo(1);
        // Na desmobilização, o agravo não muda.
        enviar(put(base + "/agravo"), "{\"agravoId\": \"" + febre + "\"}", 422);
    }

    @Test
    void exposicaoAmbiental() throws Exception {
        String em = emergencia("PRODUTO_PERIGOSO", "Vazamento no rio");
        String url = "/api/v1/emergencia/" + em + "/exposicao-ambiental";
        enviar(post(url), "{\"meio\": \"AGUA\", \"agente\": \"\"}", 400);
        enviar(post(url), "{\"meio\": \"AGUA\", \"agente\": \"Benzeno\", \"unidadeIds\": [\"" + UUID.randomUUID() + "\"]}", 404);
        enviar(post(url), "{\"meio\": \"AGUA\", \"agente\": \"Benzeno\", \"fonte\": \"Tanque da indústria\", \"populacaoExposta\": 1200, "
                + "\"unidadeIds\": [\"" + ubs.getUuid() + "\"], \"medidas\": \"Suspender a captação\", \"recomendacao\": \"Não usar a água da torneira\"}", 201);
        String r = enviar(post(url), "{\"meio\": \"AR\", \"agente\": \"Fumaça\", \"populacaoExposta\": 800}", 201);
        assertThat((List<String>) JsonPath.read(r, "$.exposicoes[*].meio")).containsExactlyInAnyOrder("AGUA", "AR");
        assertThat((List<List<String>>) JsonPath.read(r, "$.exposicoes[?(@.meio == 'AGUA')].unidades")).containsExactly(List.of(PREFIXO + "UBS"));
        // Em outro perfil, 422.
        enviar(post("/api/v1/emergencia/" + emergencia("EPIDEMIA", "Epidemia") + "/exposicao-ambiental"), "{\"meio\": \"AR\", \"agente\": \"X\"}", 422);

        String rel = ler("/api/v1/emergencia/" + em + "/relatorio", 200);
        assertThat((Integer) JsonPath.read(rel, "$.indicadores.exposicoes")).isEqualTo(2);
        assertThat((Integer) JsonPath.read(rel, "$.indicadores.populacaoExpostaMaxima")).isEqualTo(1200);
        assertThat((Object) JsonPath.read(rel, "$.indicadores.casosNotificados")).isNull();
    }
}
