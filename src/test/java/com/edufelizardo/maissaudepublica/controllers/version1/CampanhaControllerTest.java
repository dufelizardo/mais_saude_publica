package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Campanhas, fatia P3 (ADR-0133): cadastro com as regras de período e público, situação pelas datas, publicação na
 * consulta pública, locais da rede e temporários, ações da campanha com o andamento da meta e o cancelamento.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CampanhaControllerTest {

    private static final String PREFIXO = "Campanha Teste ";
    private static final String URL = "/api/v1/";

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    private UnidadeDeSaude ubs;

    @BeforeEach
    void seed() {
        limpar();
        ubs = new UnidadeDeSaude();
        ubs.setNome(PREFIXO + "UBS");
        ubs.setTipo(TipoUnidadeDeSaude.UBS);
        ubs.setAtivo(true);
        ubs = unidadeDeSaudeRepository.save(ubs);
    }

    @AfterEach
    void limpar() {
        String campanhas = "(select uuid from tb_campanha where nome like '" + PREFIXO + "%')";
        jdbc.update("delete from tb_acao_programatica where campanha_id in " + campanhas);
        jdbc.update("delete from tb_local_campanha where campanha_id in " + campanhas);
        jdbc.update("delete from tb_campanha_condicao where campanha_id in " + campanhas);
        jdbc.update("delete from tb_campanha where nome like '" + PREFIXO + "%'");
        jdbc.update("delete from tb_unidade_de_saude where nome like '" + PREFIXO + "%'");
    }

    private String enviar(MockHttpServletRequestBuilder r, String corpo, int esperado) throws Exception {
        return mockMvc.perform(r.contentType(MediaType.APPLICATION_JSON).content(corpo)).andExpect(status().is(esperado))
                .andReturn().getResponse().getContentAsString();
    }

    private String ler(String url) throws Exception {
        return mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
    }

    private String campanha(String nome, LocalDate inicio, LocalDate fim, String extra, int esperado) throws Exception {
        return campanha("VACINACAO", nome, inicio, fim, extra, esperado);
    }

    private String campanha(String tipo, String nome, LocalDate inicio, LocalDate fim, String extra, int esperado) throws Exception {
        return enviar(post(URL + "campanha"), "{\"nome\": \"%s\", \"tipo\": \"%s\", \"inicio\": \"%s\", \"fim\": \"%s\"%s}"
                .formatted(PREFIXO + nome, tipo, inicio, fim, extra), esperado);
    }

    @Test
    void cadastroPublicacaoELocais() throws Exception {
        LocalDate hoje = LocalDate.now();
        campanha("Gripe", hoje, hoje.minusDays(1), "", 400);
        campanha("Gripe", hoje, hoje.plusDays(30), ", \"sexo\": \"IGNORADO\"", 400);
        campanha("Gripe", hoje, hoje.plusDays(30), ", \"condicoes\": [\"xyz\"]", 400);
        String r = campanha("Gripe", hoje.minusDays(1), hoje.plusDays(30), ", \"idadeMinima\": 60, \"metaQuantidade\": 100", 201);
        String id = JsonPath.read(r, "$.uuid");
        assertThat(JsonPath.<String>read(r, "$.situacao")).isEqualTo("EM_ANDAMENTO");
        assertThat(JsonPath.<String>read(r, "$.publico")).isEqualTo("60 anos ou mais");
        String planejada = campanha("Outubro Rosa", hoje.plusDays(10), hoje.plusDays(40), ", \"publicoDescricao\": \"Mulheres de 50 a 69 anos\"", 201);
        assertThat(JsonPath.<String>read(planejada, "$.situacao")).isEqualTo("PLANEJADA");
        assertThat((List<String>) JsonPath.read(ler(URL + "campanhas?situacao=PLANEJADA"), "$[*].nome")).contains(PREFIXO + "Outubro Rosa");

        // Só a publicada aparece na consulta pública.
        assertThat((List<String>) JsonPath.read(ler(URL + "publico/campanha"), "$[*].nome")).doesNotContain(PREFIXO + "Gripe");
        enviar(post(URL + "campanha/" + id + "/publicacao"), "{\"publicada\": true}", 200);
        String publico = ler(URL + "publico/campanha");
        assertThat((List<String>) JsonPath.read(publico, "$[*].nome")).contains(PREFIXO + "Gripe");

        // Locais: a unidade da rede uma vez só; o temporário pede nome e fica no período.
        r = enviar(post(URL + "campanha/" + id + "/local"), "{\"unidadeId\": \"%s\", \"funcionamento\": \"seg a sex, 8h às 17h\"}".formatted(ubs.getUuid()), 201);
        assertThat((List<?>) JsonPath.read(r, "$.locais")).hasSize(1);
        enviar(post(URL + "campanha/" + id + "/local"), "{\"unidadeId\": \"%s\"}".formatted(ubs.getUuid()), 409);
        enviar(post(URL + "campanha/" + id + "/local"), "{\"unidadeId\": \"%s\", \"temporario\": true}".formatted(ubs.getUuid()), 400);
        enviar(post(URL + "campanha/" + id + "/local"), "{\"unidadeId\": \"%s\", \"temporario\": true, \"nome\": \"Praça\", \"fim\": \"%s\"}"
                .formatted(ubs.getUuid(), hoje.plusDays(60)), 422);
        r = enviar(post(URL + "campanha/" + id + "/local"), "{\"unidadeId\": \"%s\", \"temporario\": true, \"nome\": \"Escola Municipal\", \"endereco\": \"Rua A, 10\"}"
                .formatted(ubs.getUuid()), 201);
        assertThat((List<String>) JsonPath.read(r, "$.locais[*].nome")).containsExactly(PREFIXO + "UBS", "Escola Municipal");
        publico = ler(URL + "publico/campanha");
        assertThat((List<String>) JsonPath.read(publico, "$[?(@.nome == '" + PREFIXO + "Gripe')].locais[*].nome")).contains("Escola Municipal");
    }

    @Test
    void acoesDaCampanhaAndamentoECancelamento() throws Exception {
        LocalDate hoje = LocalDate.now();
        // O mutirão soma as ações no andamento; na de vacinação, o andamento vem das doses (ADR-0149).
        String id = JsonPath.read(campanha("MUTIRAO", "Mutirão", hoje.minusDays(2), hoje.plusDays(10), ", \"metaQuantidade\": 300", 201), "$.uuid");
        enviar(post(URL + "campanha/" + id + "/publicacao"), "{\"publicada\": true}", 200);
        String r = enviar(post(URL + "campanha/" + id + "/local"), "{\"unidadeId\": \"%s\"}".formatted(ubs.getUuid()), 201);
        String local = JsonPath.read(r, "$.locais[0].uuid");
        String acao = "{\"campanhaId\": \"%s\", \"tipo\": \"%s\", \"dataPrevista\": \"%s\"%s}";
        enviar(post(URL + "acao-programatica"), acao.formatted(id, "CONSULTA_PROGRAMADA", hoje, ", \"localCampanhaId\": \"" + local + "\""), 400);
        enviar(post(URL + "acao-programatica"), acao.formatted(id, "VACINA", hoje, ""), 400);
        enviar(post(URL + "acao-programatica"), acao.formatted(id, "VACINA", hoje.plusDays(30), ", \"localCampanhaId\": \"" + local + "\""), 422);
        enviar(post(URL + "acao-programatica"), "{\"tipo\": \"VACINA\", \"dataPrevista\": \"%s\"}".formatted(hoje), 400);
        r = enviar(post(URL + "acao-programatica"), acao.formatted(id, "VACINA", hoje, ", \"localCampanhaId\": \"" + local + "\""), 201);
        String vacina = JsonPath.read(r, "$.uuid");
        assertThat(JsonPath.<String>read(r, "$.campanhaNome")).isEqualTo(PREFIXO + "Mutirão");
        assertThat(JsonPath.<Boolean>read(r, "$.coletiva")).isTrue();
        enviar(post(URL + "acao-programatica/" + vacina + "/realizacao"), "{\"quantidadeAtendida\": -1}", 400);
        enviar(post(URL + "acao-programatica/" + vacina + "/realizacao"), "{\"quantidadeAtendida\": 40}", 200);
        r = ler(URL + "campanha/" + id);
        assertThat(JsonPath.<Integer>read(r, "$.realizado")).isEqualTo(40);
        assertThat(JsonPath.<Integer>read(r, "$.acoesRealizadas")).isEqualTo(1);
        assertThat((List<Integer>) JsonPath.read(ler(URL + "publico/campanha"), "$[?(@.nome == '" + PREFIXO + "Mutirão')].realizado")).containsExactly(40);

        String mobilizacao = JsonPath.read(enviar(post(URL + "acao-programatica"), acao.formatted(id, "MOBILIZACAO", hoje.plusDays(1),
                ", \"localCampanhaId\": \"" + local + "\""), 201), "$.uuid");
        assertThat((List<?>) JsonPath.read(ler(URL + "acoes-programaticas?campanhaId=" + id + "&de=" + hoje.minusDays(7) + "&ate=" + hoje.plusDays(7)),
                "$.lista")).hasSize(2);
        enviar(delete(URL + "campanha-local/" + local), "", 409);

        // Cancelar: pede motivo, tira da consulta pública e cancela as ações pendentes.
        enviar(post(URL + "campanha/" + id + "/cancelamento"), "{}", 400);
        r = enviar(post(URL + "campanha/" + id + "/cancelamento"), "{\"motivo\": \"Falta de insumos\"}", 200);
        assertThat(JsonPath.<String>read(r, "$.situacao")).isEqualTo("CANCELADA");
        assertThat(JsonPath.<Boolean>read(r, "$.publicada")).isFalse();
        enviar(post(URL + "campanha/" + id + "/publicacao"), "{\"publicada\": true}", 422);
        assertThat((List<String>) JsonPath.read(ler(URL + "publico/campanha"), "$[*].nome")).doesNotContain(PREFIXO + "Mutirão");
        String lista = ler(URL + "acoes-programaticas?campanhaId=" + id + "&de=" + hoje.minusDays(7) + "&ate=" + hoje.plusDays(7));
        assertThat((List<String>) JsonPath.read(lista, "$.lista[?(@.uuid == '" + mobilizacao + "')].situacao")).containsExactly("CANCELADA");
        enviar(post(URL + "acao-programatica"), acao.formatted(id, "VACINA", hoje, ", \"localCampanhaId\": \"" + local + "\""), 422);
    }
}
