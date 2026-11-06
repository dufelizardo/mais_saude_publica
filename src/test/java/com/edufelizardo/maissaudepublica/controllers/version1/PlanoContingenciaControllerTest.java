package com.edufelizardo.maissaudepublica.controllers.version1;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Emergências, fatia E2 (ADR-0157): o plano de contingência versionado (rascunho, publicação, substituição, revisão,
 * revogação) e a ativação na emergência (tipo, nível coberto, 409), com o passo automático na mudança de nível, as ações
 * previstas do nível atual e a desativação no encerramento.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PlanoContingenciaControllerTest {

    private static final String PREFIXO = "Plano Teste ";

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;

    private String inundacao;
    private String epidemia;
    private String nivel1;
    private String nivel2;
    private final LocalDate hoje = LocalDate.now();

    @BeforeEach
    void seed() {
        limpar();
        inundacao = jdbc.queryForObject("select cast(uuid as varchar) from tb_tipo_emergencia where codigo = 'INUNDACAO'", String.class);
        epidemia = jdbc.queryForObject("select cast(uuid as varchar) from tb_tipo_emergencia where codigo = 'EPIDEMIA'", String.class);
        nivel1 = jdbc.queryForObject("select cast(uuid as varchar) from tb_nivel_emergencia where ordem = 1", String.class);
        nivel2 = jdbc.queryForObject("select cast(uuid as varchar) from tb_nivel_emergencia where ordem = 2", String.class);
    }

    @AfterEach
    void limpar() {
        String planos = "(select uuid from tb_plano_contingencia where codigo like 'PLT_%')";
        String emergencias = "(select e.uuid from tb_emergencia e join tb_evento_emergencial v on v.uuid = e.evento_id where v.descricao like '" + PREFIXO + "%')";
        jdbc.update("delete from tb_registro_ativacao_plano where ativacao_id in (select uuid from tb_ativacao_plano where emergencia_id in " + emergencias + ")");
        jdbc.update("delete from tb_ativacao_plano where emergencia_id in " + emergencias);
        jdbc.update("delete from tb_mudanca_nivel_emergencia where emergencia_id in " + emergencias);
        jdbc.update("delete from tb_emergencia where uuid in " + emergencias);
        jdbc.update("delete from tb_evento_emergencial where descricao like '" + PREFIXO + "%'");
        for (String t : List.of("tb_criterio_ativacao", "tb_acao_plano", "tb_recurso_plano", "tb_contato_plano", "tb_plano_contingencia_nivel",
                "tb_plano_contingencia_unidade")) {
            jdbc.update("delete from " + t + " where plano_id in " + planos);
        }
        jdbc.update("delete from tb_plano_contingencia where codigo like 'PLT_%'");
    }

    private String enviar(MockHttpServletRequestBuilder r, String corpo, int esperado) throws Exception {
        return mockMvc.perform(r.contentType(MediaType.APPLICATION_JSON).content(corpo)).andExpect(status().is(esperado)).andReturn().getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
    }

    private String ler(String url) throws Exception {
        return mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    /** O corpo do plano: os níveis, um critério e uma ação (prazo de 2 h) em cada nível, se pedidos. */
    private String corpo(String codigo, String tipo, List<String> niveis, boolean comAcoes) {
        StringBuilder criterios = new StringBuilder();
        StringBuilder acoes = new StringBuilder();
        for (String n : niveis) {
            criterios.append(criterios.length() > 0 ? "," : "").append("{\"nivelId\": \"").append(n).append("\", \"descricao\": \"Rio acima de 4 m\"}");
            acoes.append(acoes.length() > 0 ? "," : "").append("{\"nivelId\": \"").append(n)
                    .append("\", \"descricao\": \"Abrir o COE\", \"responsavel\": \"Coordenação de vigilância\", \"prazoHoras\": 2}");
        }
        return "{" + (codigo != null ? "\"codigo\": \"" + codigo + "\", " : "") + "\"nome\": \"Plano de enchentes\", \"tipoId\": \"" + tipo
                + "\", \"objetivo\": \"Responder às enchentes\", \"vigenciaInicio\": \"" + hoje.minusDays(1) + "\", \"nivelIds\": [\""
                + String.join("\", \"", niveis) + "\"], \"criterios\": [" + criterios + "], \"acoes\": [" + (comAcoes ? acoes : "")
                + "], \"recursos\": [{\"descricao\": \"Kits de hidratação\", \"quantidade\": 500}], \"contatos\": [{\"nome\": \"Defesa Civil\", "
                + "\"telefone\": \"199\"}]}";
    }

    private String vigente(String codigo, String tipo, List<String> niveis) throws Exception {
        String id = JsonPath.read(enviar(post("/api/v1/emergencias/plano"), corpo(codigo, tipo, niveis, true), 201), "$.uuid");
        enviar(post("/api/v1/emergencias/plano/" + id + "/publicacao"), "", 200);
        return id;
    }

    @Test
    void versionamentoDoPlano() throws Exception {
        enviar(post("/api/v1/emergencias/plano"), corpo(null, inundacao, List.of(nivel1), true), 400);
        String v1 = JsonPath.read(enviar(post("/api/v1/emergencias/plano"), corpo("plt_enchente", inundacao, List.of(nivel1, nivel2), true), 201), "$.uuid");
        String r = ler("/api/v1/emergencias/plano/" + v1);
        assertThat((String) JsonPath.read(r, "$.codigo")).isEqualTo("PLT_ENCHENTE");
        assertThat((String) JsonPath.read(r, "$.situacao")).isEqualTo("RASCUNHO");
        assertThat((List<Object>) JsonPath.read(r, "$.acoes")).hasSize(2);
        enviar(post("/api/v1/emergencias/plano"), corpo("PLT_ENCHENTE", inundacao, List.of(nivel1), true), 409);
        // Critério num nível que o plano não cobre; fim antes do início.
        String fora = corpo(null, inundacao, List.of(nivel1), true).replace("\"criterios\": [{\"nivelId\": \"" + nivel1, "\"criterios\": [{\"nivelId\": \"" + nivel2);
        enviar(put("/api/v1/emergencias/plano/" + v1), fora, 422);
        enviar(put("/api/v1/emergencias/plano/" + v1), corpo(null, inundacao, List.of(nivel1), true).replace("\"vigenciaInicio\"",
                "\"vigenciaFim\": \"" + hoje.minusDays(5) + "\", \"vigenciaInicio\""), 400);
        enviar(put("/api/v1/emergencias/plano/" + v1), corpo("OUTRO", inundacao, List.of(nivel1), true), 422);

        // Sem ação prevista não publica.
        String semAcao = JsonPath.read(enviar(post("/api/v1/emergencias/plano"), corpo("PLT_VAZIO", inundacao, List.of(nivel1), false), 201), "$.uuid");
        enviar(post("/api/v1/emergencias/plano/" + semAcao + "/publicacao"), "", 422);

        r = enviar(post("/api/v1/emergencias/plano/" + v1 + "/publicacao"), "", 200);
        assertThat((String) JsonPath.read(r, "$.situacao")).isEqualTo("VIGENTE");
        enviar(put("/api/v1/emergencias/plano/" + v1), corpo(null, inundacao, List.of(nivel1), true), 422);
        enviar(post("/api/v1/emergencias/plano/" + semAcao + "/revisao"), "", 422);

        r = enviar(post("/api/v1/emergencias/plano/" + v1 + "/revisao"), "", 201);
        String v2 = JsonPath.read(r, "$.uuid");
        assertThat((Integer) JsonPath.read(r, "$.versao")).isEqualTo(2);
        assertThat((List<Object>) JsonPath.read(r, "$.acoes")).hasSize(2);
        assertThat((List<Object>) JsonPath.read(r, "$.contatos")).hasSize(1);
        enviar(post("/api/v1/emergencias/plano/" + v1 + "/revisao"), "", 409);
        enviar(post("/api/v1/emergencias/plano/" + v2 + "/publicacao"), "", 200);
        r = ler("/api/v1/emergencias/plano/" + v1);
        assertThat((String) JsonPath.read(r, "$.situacao")).isEqualTo("SUBSTITUIDA");
        assertThat((List<Integer>) JsonPath.read(r, "$.versoes[*].versao")).containsExactly(2, 1);
        List<String> vigentes = JsonPath.read(ler("/api/v1/emergencias/planos?vigentes=true&tipoId=" + inundacao), "$[*].uuid");
        assertThat(vigentes).contains(v2).doesNotContain(v1);

        r = enviar(post("/api/v1/emergencias/plano/" + v2 + "/revogacao"), "{\"motivo\": \"Substituído pelo plano regional\"}", 200);
        assertThat((String) JsonPath.read(r, "$.situacao")).isEqualTo("REVOGADA");
        enviar(post("/api/v1/emergencias/plano/" + v2 + "/revogacao"), "{\"motivo\": \"De novo\"}", 422);
    }

    private String emergencia() throws Exception {
        String evento = JsonPath.read(enviar(post("/api/v1/evento-emergencial"), "{\"tipoId\": \"" + inundacao + "\", \"fonte\": \"DEFESA_CIVIL\", "
                + "\"descricao\": \"" + PREFIXO + "Rio transbordou\"}", 201), "$.uuid");
        return JsonPath.read(enviar(post("/api/v1/evento-emergencial/" + evento + "/emergencia"), "{\"nome\": \"Enchente\", \"nivelId\": \"" + nivel1
                + "\", \"justificativa\": \"Cota de alerta\"}", 201), "$.uuid");
    }

    @Test
    void ativacaoNaEmergencia() throws Exception {
        String em = emergencia();
        String soNivel1 = vigente("PLT_N1", inundacao, List.of(nivel1));
        String deEpidemia = vigente("PLT_EPI", epidemia, List.of(nivel1));
        String soNivel2 = vigente("PLT_N2", inundacao, List.of(nivel2));
        String rascunho = JsonPath.read(enviar(post("/api/v1/emergencias/plano"), corpo("PLT_RASC", inundacao, List.of(nivel1), true), 201), "$.uuid");
        String ativar = "/api/v1/emergencia/" + em + "/plano-ativo";
        String corpo = "\", \"autoridade\": \"Secretário de Saúde\", \"justificativa\": \"Cota de alerta atingida\"}";
        enviar(post(ativar), "{\"planoId\": \"" + deEpidemia + corpo, 422);
        enviar(post(ativar), "{\"planoId\": \"" + soNivel2 + corpo, 422);
        enviar(post(ativar), "{\"planoId\": \"" + rascunho + corpo, 422);

        String r = enviar(post(ativar), "{\"planoId\": \"" + soNivel1 + corpo, 201);
        assertThat((String) JsonPath.read(r, "$.planos[0].situacao")).isEqualTo("ATIVA");
        assertThat((Boolean) JsonPath.read(r, "$.planos[0].cobreNivelAtual")).isTrue();
        assertThat((String) JsonPath.read(r, "$.planos[0].acoesDoNivel[0].descricao")).isEqualTo("Abrir o COE");
        assertThat((String) JsonPath.read(r, "$.planos[0].acoesDoNivel[0].prazoEm")).isNotNull();
        assertThat((String) JsonPath.read(r, "$.planos[0].registros[0].autoridade")).isEqualTo("Secretário de Saúde");
        enviar(post(ativar), "{\"planoId\": \"" + soNivel1 + corpo, 409);

        // A mudança de nível entra no histórico do plano ativo, que não cobre o nível 2.
        r = enviar(post("/api/v1/emergencia/" + em + "/nivel"), "{\"nivelId\": \"" + nivel2 + "\", \"justificativa\": \"Hospital atingido\"}", 200);
        assertThat((String) JsonPath.read(r, "$.planos[0].registros[0].tipo")).isEqualTo("NIVEL_ALTERADO");
        assertThat((Boolean) JsonPath.read(r, "$.planos[0].registros[0].nivelCoberto")).isFalse();
        assertThat((Boolean) JsonPath.read(r, "$.planos[0].cobreNivelAtual")).isFalse();
        assertThat((List<Object>) JsonPath.read(r, "$.planos[0].acoesDoNivel")).isEmpty();

        String ativacao = JsonPath.read(r, "$.planos[0].ativacaoId");
        r = enviar(post(ativar + "/" + ativacao + "/desativacao"), "{\"justificativa\": \"Plano não cobre o nível 2\"}", 200);
        assertThat((String) JsonPath.read(r, "$.planos[0].situacao")).isEqualTo("DESATIVADA");
        enviar(post(ativar + "/" + ativacao + "/desativacao"), "{\"justificativa\": \"De novo\"}", 422);

        // O plano do nível 2 entra; encerrar a emergência o desativa.
        enviar(post(ativar), "{\"planoId\": \"" + soNivel2 + corpo, 201);
        r = enviar(post("/api/v1/emergencia/" + em + "/encerramento"), "{\"motivo\": \"Águas baixaram\"}", 200);
        assertThat((List<String>) JsonPath.read(r, "$.planos[*].situacao")).containsOnly("DESATIVADA");
        List<String> ultimo = JsonPath.read(r, "$.planos[?(@.codigo == 'PLT_N2')].registros[0].justificativa");
        assertThat(ultimo).containsExactly("Emergência encerrada: Águas baixaram");
    }
}
