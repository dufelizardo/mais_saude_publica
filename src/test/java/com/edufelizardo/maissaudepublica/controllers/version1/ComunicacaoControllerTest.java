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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Comunicação básica, fatia C1 (ADR-0168): canais, comunicado, conteúdo com revisão e versão, resultado do envio e portal. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ComunicacaoControllerTest {

    private static final String URL = "/api/v1/comunicacao/";
    private static final String PUBLICO = "/api/v1/publico/comunicacao/";
    private static final String PREFIXO = "ComTeste ";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    private String portal;
    private String email;

    @BeforeEach
    void seed() throws Exception {
        limpar();
        portal = canal("PORTAL");
        email = canal("EMAIL");
    }

    @AfterEach
    void limpar() {
        jdbc.update("delete from tb_publicacao where comunicado_id in (select uuid from tb_comunicado where titulo like ?)", PREFIXO + "%");
        jdbc.update("delete from tb_publicacao where conteudo_id in (select uuid from tb_conteudo_educativo where titulo like ?)", PREFIXO + "%");
        jdbc.update("delete from tb_comunicado_canal where comunicado_id in (select uuid from tb_comunicado where titulo like ?)", PREFIXO + "%");
        jdbc.update("delete from tb_conteudo_canal where conteudo_id in (select uuid from tb_conteudo_educativo where titulo like ?)", PREFIXO + "%");
        jdbc.update("delete from tb_comunicado where titulo like ?", PREFIXO + "%");
        jdbc.update("delete from tb_conteudo_educativo where titulo like ?", PREFIXO + "%");
        jdbc.update("delete from tb_canal_comunicacao where nome like ?", PREFIXO + "%");
        jdbc.update("delete from tb_unidade_de_saude where nome like ?", PREFIXO + "%");
    }

    private String canal(String tipo) throws Exception {
        String r = mockMvc.perform(get(URL + "canal")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        List<String> ids = JsonPath.read(r, "$[?(@.tipo == '" + tipo + "')].uuid");
        return ids.get(0);
    }

    private String enviar(MockHttpServletRequestBuilder r, String corpo, int esperado) throws Exception {
        return mockMvc.perform(r.contentType(MediaType.APPLICATION_JSON).content(corpo)).andExpect(status().is(esperado))
                .andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
    }

    private String corpoComunicado(String titulo, String prioridade, String publico, String canal, LocalDate inicio, LocalDate fim) {
        return "{\"titulo\": \"%s%s\", \"mensagem\": \"Aviso\", \"prioridade\": \"%s\", \"publico\": \"%s\", \"inicio\": \"%s\", \"fim\": \"%s\", \"canalIds\": [%s]}"
                .formatted(PREFIXO, titulo, prioridade, publico, inicio, fim, canal == null ? "" : "\"" + canal + "\"");
    }

    private String comunicado(String titulo, String prioridade, String publico, String canal) throws Exception {
        String r = enviar(post(URL + "comunicado"), corpoComunicado(titulo, prioridade, publico, canal, LocalDate.now(), LocalDate.now().plusDays(7)), 201);
        return JsonPath.read(r, "$.uuid");
    }

    private ResultActions publicar(String id) throws Exception {
        return mockMvc.perform(post(URL + "comunicado/" + id + "/publicacao"));
    }

    private String conteudo(String titulo, LocalDate validade) throws Exception {
        String r = enviar(post(URL + "conteudo"), "{\"titulo\": \"%s%s\", \"resumo\": \"Resumo\", \"texto\": \"Texto\", \"tema\": \"Dengue\", \"publico\": \"POPULACAO\", \"validade\": \"%s\"}"
                .formatted(PREFIXO, titulo, validade), 201);
        return JsonPath.read(r, "$.uuid");
    }

    private void aprovar(String id) throws Exception {
        mockMvc.perform(post(URL + "conteudo/" + id + "/envio-revisao")).andExpect(status().isOk());
        mockMvc.perform(post(URL + "conteudo/" + id + "/aprovacao")).andExpect(status().isOk());
    }

    private String canais(String canal) {
        return "{\"canalIds\": [\"%s\"]}".formatted(canal);
    }

    @Test
    void cargaTrazPortalEEmail() throws Exception {
        mockMvc.perform(get(URL + "canal")).andExpect(jsonPath("$[*].tipo", org.hamcrest.Matchers.hasItems("PORTAL", "EMAIL")));
    }

    @Test
    void canalNomeRepetidoE409ETipoNaoMuda() throws Exception {
        String corpo = "{\"tipo\": \"PAINEL\", \"nome\": \"" + PREFIXO + "TV\"}";
        String r = enviar(post(URL + "canal"), corpo, 201);
        enviar(post(URL + "canal"), corpo, 409);
        enviar(patch(URL + "canal/" + JsonPath.read(r, "$.uuid")), "{\"tipo\": \"SMS\", \"nome\": \"" + PREFIXO + "TV\"}", 422);
    }

    @Test
    void comunicadoPublicadoApareceNoPortalESoEle() throws Exception {
        String id = comunicado("Obra", "NORMAL", "POPULACAO", portal);
        mockMvc.perform(get(PUBLICO + "avisos")).andExpect(jsonPath("$[*].titulo", not(hasItem(PREFIXO + "Obra"))));

        publicar(id).andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("PUBLICADO"))
                .andExpect(jsonPath("$.vigente").value(true))
                .andExpect(jsonPath("$.publicacoes[0].situacao").value("PUBLICADA"));
        mockMvc.perform(get(PUBLICO + "avisos")).andExpect(jsonPath("$[*].titulo", hasItem(PREFIXO + "Obra")));

        publicar(id).andExpect(status().isUnprocessableEntity());
        enviar(patch(URL + "comunicado/" + id), corpoComunicado("Outro", "NORMAL", "POPULACAO", portal, LocalDate.now(), LocalDate.now()), 422);
        enviar(post(URL + "comunicado/" + id + "/encerramento"), "{\"motivo\": \"Obra acabou\"}", 200);
        mockMvc.perform(get(PUBLICO + "avisos")).andExpect(jsonPath("$[*].titulo", not(hasItem(PREFIXO + "Obra"))));
    }

    @Test
    void comunicadoForaDoPeriodoSaiDoPortalSozinho() throws Exception {
        String id = comunicado("Vencido", "NORMAL", "POPULACAO", portal);
        publicar(id).andExpect(status().isOk());
        jdbc.update("update tb_comunicado set inicio = ?, fim = ? where uuid = ?::uuid", LocalDate.now().minusDays(5), LocalDate.now().minusDays(1), id);
        mockMvc.perform(get(PUBLICO + "avisos")).andExpect(jsonPath("$[*].titulo", not(hasItem(PREFIXO + "Vencido"))));
    }

    @Test
    void emergencialApareceAntesDosDemais() throws Exception {
        publicar(comunicado("A normal", "NORMAL", "POPULACAO", portal)).andExpect(status().isOk());
        publicar(comunicado("Z emergencial", "EMERGENCIAL", "POPULACAO", portal)).andExpect(status().isOk());
        mockMvc.perform(get(PUBLICO + "avisos")).andExpect(jsonPath("$[0].titulo").value(PREFIXO + "Z emergencial"));
    }

    @Test
    void populacaoSoPeloPortalEProfissionaisForaDele() throws Exception {
        enviar(post(URL + "comunicado"), corpoComunicado("X", "NORMAL", "POPULACAO", email, LocalDate.now(), LocalDate.now()), 422);
        enviar(post(URL + "comunicado"), corpoComunicado("X", "NORMAL", "PROFISSIONAIS", portal, LocalDate.now(), LocalDate.now()), 422);
    }

    @Test
    void fimAntesDoInicioE400SemCanalNaoPublicaEPeriodoEncerradoE422() throws Exception {
        enviar(post(URL + "comunicado"), corpoComunicado("X", "NORMAL", "POPULACAO", portal, LocalDate.now(), LocalDate.now().minusDays(1)), 400);
        publicar(comunicado("Sem canal", "NORMAL", "POPULACAO", null)).andExpect(status().isBadRequest());
        String r = enviar(post(URL + "comunicado"), corpoComunicado("Antigo", "NORMAL", "POPULACAO", portal, LocalDate.now().minusDays(9), LocalDate.now().minusDays(1)), 201);
        publicar(JsonPath.read(r, "$.uuid")).andExpect(status().isUnprocessableEntity());
    }

    @Test
    void semSmtpAPublicacaoNaoFalhaEORegistroFicaComoNaoEnviada() throws Exception {
        publicar(comunicado("Interno", "NORMAL", "PROFISSIONAIS", email)).andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("PUBLICADO"))
                .andExpect(jsonPath("$.publicacoes[0].situacao").value("NAO_ENVIADA"))
                .andExpect(jsonPath("$.publicacoes[0].detalhe").isNotEmpty());
    }

    @Test
    void comunicadoGuardaAUnidade() throws Exception {
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + "UBS");
        u.setTipo(TipoUnidadeDeSaude.UBS);
        u.setAtivo(true);
        u = unidadeDeSaudeRepository.save(u);
        String corpo = corpoComunicado("Da unidade", "ALTA", "POPULACAO", portal, LocalDate.now(), LocalDate.now()).replace("\"canalIds\"", "\"unidadeId\": \"" + u.getUuid() + "\", \"canalIds\"");
        String r = enviar(post(URL + "comunicado"), corpo, 201);
        publicar(JsonPath.read(r, "$.uuid")).andExpect(status().isOk());
        mockMvc.perform(get(PUBLICO + "avisos")).andExpect(jsonPath("$[?(@.titulo == '" + PREFIXO + "Da unidade')].unidadeNome").value(PREFIXO + "UBS"));
    }

    @Test
    void conteudoSoPublicaDepoisDaRevisao() throws Exception {
        String id = conteudo("Dengue", LocalDate.now().plusMonths(6));
        enviar(post(URL + "conteudo/" + id + "/publicacao"), canais(portal), 422);
        mockMvc.perform(post(URL + "conteudo/" + id + "/aprovacao")).andExpect(status().isUnprocessableEntity());
        aprovar(id);
        enviar(post(URL + "conteudo/" + id + "/publicacao"), canais(portal), 200);
        mockMvc.perform(get(PUBLICO + "orientacoes")).andExpect(jsonPath("$[*].titulo", hasItem(PREFIXO + "Dengue")));
    }

    @Test
    void devolverVoltaAoRascunhoComOMotivo() throws Exception {
        String id = conteudo("Dengue", LocalDate.now().plusMonths(6));
        mockMvc.perform(post(URL + "conteudo/" + id + "/envio-revisao")).andExpect(status().isOk());
        String r = enviar(post(URL + "conteudo/" + id + "/devolucao"), "{\"motivo\": \"Rever a dose\"}", 200);
        org.assertj.core.api.Assertions.assertThat(JsonPath.<String>read(r, "$.situacao")).isEqualTo("RASCUNHO");
        org.assertj.core.api.Assertions.assertThat(JsonPath.<String>read(r, "$.motivoDevolucao")).isEqualTo("Rever a dose");
    }

    @Test
    void conteudoVencidoNaoPublicaESaiDoPortal() throws Exception {
        String vencido = conteudo("Vencido", LocalDate.now().minusDays(1));
        aprovar(vencido);
        enviar(post(URL + "conteudo/" + vencido + "/publicacao"), canais(portal), 422);

        String id = conteudo("No ar", LocalDate.now().plusDays(1));
        aprovar(id);
        enviar(post(URL + "conteudo/" + id + "/publicacao"), canais(portal), 200);
        jdbc.update("update tb_conteudo_educativo set validade = ? where uuid = ?::uuid", LocalDate.now().minusDays(1), id);
        mockMvc.perform(get(PUBLICO + "orientacoes")).andExpect(jsonPath("$[*].titulo", not(hasItem(PREFIXO + "No ar"))));
        mockMvc.perform(get(URL + "conteudo")).andExpect(jsonPath("$[?(@.uuid == '" + id + "')].situacao").value("EXPIRADO"));
    }

    @Test
    void novaVersaoSubstituiAAnteriorAoPublicar() throws Exception {
        String v1 = conteudo("Versionado", LocalDate.now().plusMonths(6));
        aprovar(v1);
        enviar(post(URL + "conteudo/" + v1 + "/publicacao"), canais(portal), 200);

        String r = mockMvc.perform(post(URL + "conteudo/" + v1 + "/nova-versao")).andExpect(status().isOk())
                .andExpect(jsonPath("$.versao").value(2)).andExpect(jsonPath("$.situacao").value("RASCUNHO"))
                .andReturn().getResponse().getContentAsString();
        mockMvc.perform(post(URL + "conteudo/" + v1 + "/nova-versao")).andExpect(status().isConflict());
        String v2 = JsonPath.read(r, "$.uuid");
        aprovar(v2);
        enviar(post(URL + "conteudo/" + v2 + "/publicacao"), canais(portal), 200);

        mockMvc.perform(get(URL + "conteudo"))
                .andExpect(jsonPath("$[?(@.uuid == '" + v1 + "')].situacao").value("SUBSTITUIDO"))
                .andExpect(jsonPath("$[?(@.uuid == '" + v2 + "')].situacao").value("PUBLICADO"));
        mockMvc.perform(get(PUBLICO + "orientacoes")).andExpect(jsonPath("$[?(@.titulo == '" + PREFIXO + "Versionado')].versao").value(2));
    }

    @Test
    void conteudoPublicadoNaoSeEdita() throws Exception {
        String id = conteudo("Dengue", LocalDate.now().plusMonths(6));
        aprovar(id);
        enviar(patch(URL + "conteudo/" + id), "{\"titulo\": \"%sOutro\", \"resumo\": \"R\", \"texto\": \"T\", \"tema\": \"D\", \"publico\": \"POPULACAO\", \"validade\": \"%s\"}"
                .formatted(PREFIXO, LocalDate.now().plusMonths(6)), 422);
    }
}
