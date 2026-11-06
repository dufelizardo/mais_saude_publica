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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Vigilância, fatia V4c (ADR-0164): a qualidade da água (pontos, amostras e a conformidade pela Portaria GM/MS 888/2021,
 * com a medida da fora do padrão) e o risco ambiental permanente.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class VigilanciaAmbientalControllerTest {

    private static final String PREFIXO = "Ambiental Teste ";

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;

    private final LocalDate hoje = LocalDate.now();

    @BeforeEach
    @AfterEach
    void limpar() {
        jdbc.update("delete from tb_amostra_agua where ponto_id in (select uuid from tb_ponto_coleta_agua where nome like ?)", PREFIXO + "%");
        jdbc.update("delete from tb_ponto_coleta_agua where nome like ?", PREFIXO + "%");
        jdbc.update("delete from tb_risco_ambiental where local like ?", PREFIXO + "%");
    }

    private String enviar(MockHttpServletRequestBuilder r, String corpo, int esperado) throws Exception {
        return mockMvc.perform(r.contentType(MediaType.APPLICATION_JSON).content(corpo)).andExpect(status().is(esperado)).andReturn().getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
    }

    private String ler(String url) throws Exception {
        return mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    private String ponto(String nome, String tipo) throws Exception {
        return JsonPath.read(enviar(post("/api/v1/ambiental/ponto-agua"), "{\"nome\": \"" + PREFIXO + nome + "\", \"tipo\": \"" + tipo
                + "\", \"local\": \"Praça Central\", \"latitude\": -22.9, \"longitude\": -47.06}", 201), "$.uuid");
    }

    @Test
    void qualidadeDaAgua() throws Exception {
        enviar(post("/api/v1/ambiental/ponto-agua"), "{\"nome\": \"" + PREFIXO + "X\", \"tipo\": \"REDE\", \"local\": \"L\", \"latitude\": -22.9}", 400);
        String rede = ponto("Escola", "REDE");
        String poco = ponto("Poço", "SOLUCAO_ALTERNATIVA_INDIVIDUAL");
        String url = "/api/v1/ambiental/amostra-agua";
        enviar(post(url), "{\"pontoId\": \"" + rede + "\"}", 400);
        enviar(post(url), "{\"pontoId\": \"" + rede + "\", \"turbidez\": 1, \"dataColeta\": \"" + hoje.plusDays(1) + "\"}", 400);

        // Dentro do padrão.
        String r = enviar(post(url), "{\"pontoId\": \"" + rede + "\", \"coliformesTotais\": false, \"ecoli\": false, \"cloroResidual\": 0.8, "
                + "\"turbidez\": 0.6, \"fluor\": 0.7}", 201);
        assertThat((Boolean) JsonPath.read(r, "$.conforme")).isTrue();
        String conforme = JsonPath.read(r, "$.uuid");
        enviar(post(url + "/" + conforme + "/medida"), "{\"medida\": \"x\"}", 422);

        // Fora do padrão: cloro baixo, turbidez e flúor altos, E. coli.
        r = enviar(post(url), "{\"pontoId\": \"" + rede + "\", \"ecoli\": true, \"cloroResidual\": 0.1, \"turbidez\": 7, \"fluor\": 2}", 201);
        assertThat((Boolean) JsonPath.read(r, "$.conforme")).isFalse();
        assertThat((List<String>) JsonPath.read(r, "$.naoConformidades")).containsExactly("Escherichia coli presente",
                "cloro residual livre abaixo de 0,2 mg/L", "turbidez acima de 5 uT", "fluoreto acima de 1,5 mg/L");
        String fora = JsonPath.read(r, "$.uuid");
        r = enviar(post(url + "/" + fora + "/medida"), "{\"medida\": \"Comunicada a concessionária; recoleta em 48 h\"}", 200);
        assertThat((String) JsonPath.read(r, "$.medida")).startsWith("Comunicada");

        // A solução individual não é tratada: sem cloro e com coliformes totais, ainda conforme; o E. coli não.
        r = enviar(post(url), "{\"pontoId\": \"" + poco + "\", \"coliformesTotais\": true, \"cloroResidual\": 0}", 201);
        assertThat((Boolean) JsonPath.read(r, "$.conforme")).isTrue();
        r = enviar(post(url), "{\"pontoId\": \"" + poco + "\", \"ecoli\": true}", 201);
        assertThat((Boolean) JsonPath.read(r, "$.conforme")).isFalse();

        assertThat((List<String>) JsonPath.read(ler("/api/v1/ambiental/amostras-agua?naoConformes=true&desde=" + hoje), "$[*].pontoId"))
                .contains(rede, poco);
        String pontos = ler("/api/v1/ambiental/pontos-agua");
        assertThat((List<Integer>) JsonPath.read(pontos, "$[?(@.uuid == '" + rede + "')].amostras")).containsExactly(2);
        assertThat((List<Boolean>) JsonPath.read(pontos, "$[?(@.uuid == '" + rede + "')].ultimaConforme")).hasSize(1);
    }

    @Test
    void riscoAmbiental() throws Exception {
        String corpo = "{\"tipo\": \"DESCARTE_IRREGULAR\", \"descricao\": \"Lixo e entulho às margens do córrego\", \"local\": \"" + PREFIXO
                + "Córrego do Moinho\", \"populacaoExposta\": 350";
        enviar(post("/api/v1/ambiental/risco"), corpo + ", \"longitude\": -47}", 400);
        String r = enviar(post("/api/v1/ambiental/risco"), corpo + ", \"latitude\": -22.9, \"longitude\": -47}", 201);
        String id = JsonPath.read(r, "$.uuid");
        assertThat((String) JsonPath.read(r, "$.situacao")).isEqualTo("ATIVO");
        assertThat((List<String>) JsonPath.read(ler("/api/v1/ambiental/riscos?situacao=ATIVO"), "$[*].uuid")).contains(id);
        enviar(post("/api/v1/ambiental/risco/" + id + "/controle"), "{\"motivo\": \"Limpeza e cerca pela prefeitura\"}", 200);
        enviar(post("/api/v1/ambiental/risco/" + id + "/controle"), "{\"motivo\": \"De novo\"}", 422);
        assertThat((List<String>) JsonPath.read(ler("/api/v1/ambiental/riscos?situacao=CONTROLADO"), "$[*].uuid")).contains(id);
    }
}
