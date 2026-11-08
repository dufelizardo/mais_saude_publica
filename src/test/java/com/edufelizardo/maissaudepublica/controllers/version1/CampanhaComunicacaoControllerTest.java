package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.ConteudoEducativo;
import com.edufelizardo.maissaudepublica.models.enuns.PublicoComunicacao;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoConteudo;
import com.edufelizardo.maissaudepublica.repositories.ConteudoEducativoRepository;
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

import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Campanhas de comunicação, fatia C2 (ADR-0174): ciclo, validações, comunicado da campanha, portal e material educativo. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CampanhaComunicacaoControllerTest {

    private static final String URL = "/api/v1/comunicacao/";
    private static final String PREFIXO = "CampCom Teste ";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private ConteudoEducativoRepository conteudoRepository;

    private String portal;
    private String email;
    private String territorio;
    private String inativo;
    private UUID publicado;
    private UUID emRevisao;
    private final LocalDate hoje = LocalDate.now();

    @BeforeEach
    void seed() {
        limpar();
        portal = jdbc.queryForObject("select cast(uuid as varchar) from tb_canal_comunicacao where tipo = 'PORTAL' order by nome limit 1", String.class);
        email = jdbc.queryForObject("select cast(uuid as varchar) from tb_canal_comunicacao where tipo = 'EMAIL' order by nome limit 1", String.class);
        String segmento = jdbc.queryForObject("insert into tb_segmento_territorial (uuid, codigo, nome, tipo) values (gen_random_uuid(), 'C2-SEG', ?, "
                + "'DISTRITO_SANITARIO') returning cast(uuid as varchar)", String.class, PREFIXO + "Segmento");
        territorio = jdbc.queryForObject("insert into tb_territorio (uuid, segmento_id, nome, finalidade, ativo) values (gen_random_uuid(), cast(? as uuid), ?, "
                + "'ATENCAO_PRIMARIA', true) returning cast(uuid as varchar)", String.class, segmento, PREFIXO + "Território");
        inativo = jdbc.queryForObject("insert into tb_territorio (uuid, segmento_id, nome, finalidade, ativo) values (gen_random_uuid(), cast(? as uuid), ?, "
                + "'ATENCAO_PRIMARIA', false) returning cast(uuid as varchar)", String.class, segmento, PREFIXO + "Inativo");
        publicado = conteudo("Autoexame", SituacaoConteudo.PUBLICADO);
        emRevisao = conteudo("Rascunho", SituacaoConteudo.EM_REVISAO);
    }

    @AfterEach
    void limpar() {
        String camp = "(select uuid from tb_campanha_comunicacao where nome like '" + PREFIXO + "%')";
        String com = "(select uuid from tb_comunicado where campanha_comunicacao_id in " + camp + ")";
        jdbc.update("delete from tb_publicacao where comunicado_id in " + com);
        jdbc.update("delete from tb_comunicado_canal where comunicado_id in " + com);
        jdbc.update("delete from tb_comunicado where uuid in " + com);
        for (String t : new String[]{"territorio", "tema", "canal", "conteudo"}) {
            jdbc.update("delete from tb_campanha_comunicacao_" + t + " where campanha_id in " + camp);
        }
        jdbc.update("delete from tb_campanha_comunicacao where nome like ?", PREFIXO + "%");
        jdbc.update("delete from tb_material_educativo where titulo like ?", PREFIXO + "%");
        jdbc.update("delete from tb_conteudo_educativo where titulo like ?", PREFIXO + "%");
        jdbc.update("delete from tb_territorio where nome like ?", PREFIXO + "%");
        jdbc.update("delete from tb_segmento_territorial where nome like ?", PREFIXO + "%");
    }

    private UUID conteudo(String titulo, SituacaoConteudo situacao) {
        ConteudoEducativo c = new ConteudoEducativo();
        c.setGrupoId(UUID.randomUUID());
        c.setVersao(1);
        c.setTitulo(PREFIXO + titulo);
        c.setResumo("Resumo de " + titulo);
        c.setTexto("Texto");
        c.setTema("Câncer de mama");
        c.setPublico(PublicoComunicacao.POPULACAO);
        c.setValidade(hoje.plusDays(60));
        c.setSituacao(situacao);
        c.setAutorCpf("00000000000");
        return conteudoRepository.save(c).getGrupoId();
    }

    private static String q(Object id) {
        return "\"" + id + "\"";
    }

    private String corpo(String nome, String publico, String canais, String conteudos, String territorios, LocalDate inicio, LocalDate fim) {
        return ("{\"nome\": \"%s\", \"objetivo\": \"Estimular a mamografia\", \"inicio\": \"%s\", \"fim\": \"%s\", \"publicoAlvo\": \"%s\", "
                + "\"temas\": [\"Câncer de mama\", \" Câncer de mama \", \"Mamografia\"], \"canalIds\": [%s], \"conteudoGrupoIds\": [%s], \"territorioIds\": [%s]}")
                .formatted(PREFIXO + nome, inicio, fim, publico, canais, conteudos, territorios);
    }

    private ResultActions criar(String corpo) throws Exception {
        return mockMvc.perform(post(URL + "campanha").contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    private String criada(String nome, String canais) throws Exception {
        String r = criar(corpo(nome, "POPULACAO_GERAL", canais, q(publicado), q(territorio), hoje, hoje.plusDays(30)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return JsonPath.read(r, "$.uuid");
    }

    @Test
    void criaComTemasSemRepetirEOConteudoEmVigor() throws Exception {
        criar(corpo("Outubro Rosa", "POPULACAO_GERAL", q(portal), q(publicado), q(territorio), hoje, hoje.plusDays(30))).andExpect(status().isCreated())
                .andExpect(jsonPath("$.situacao").value("PLANEJADA"))
                .andExpect(jsonPath("$.temas.length()").value(2))
                .andExpect(jsonPath("$.territorios[0].nome").value(PREFIXO + "Território"))
                .andExpect(jsonPath("$.conteudos[0].titulo").value(PREFIXO + "Autoexame"));
    }

    @Test
    void validacoes() throws Exception {
        criar(corpo("Datas", "POPULACAO_GERAL", "", "", "", hoje, hoje.minusDays(1))).andExpect(status().isBadRequest());
        criar(corpo("Sem revisão", "POPULACAO_GERAL", "", q(emRevisao), "", hoje, hoje.plusDays(1))).andExpect(status().isUnprocessableEntity());
        criar(corpo("Inativo", "POPULACAO_GERAL", "", "", q(inativo), hoje, hoje.plusDays(1))).andExpect(status().isUnprocessableEntity());
        criar(corpo("Portal", "PROFISSIONAIS_SAUDE", q(portal), "", "", hoje, hoje.plusDays(1))).andExpect(status().isUnprocessableEntity());
        criar(corpo("Conteúdo", "POPULACAO_GERAL", "", q(UUID.randomUUID()), "", hoje, hoje.plusDays(1))).andExpect(status().isNotFound());
        criar(corpo("Programa", "POPULACAO_GERAL", "", "", "", hoje, hoje.plusDays(1)).replace("}", ", \"programaId\": " + q(UUID.randomUUID()) + "}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void cicloDaCampanha() throws Exception {
        String sem = criada("Sem canal", "");
        mockMvc.perform(post(URL + "campanha/" + sem + "/inicio")).andExpect(status().isUnprocessableEntity());
        mockMvc.perform(post(URL + "campanha/" + sem + "/cancelamento").contentType(MediaType.APPLICATION_JSON).content("{\"motivo\": \"Desistência\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.situacao").value("CANCELADA")).andExpect(jsonPath("$.motivoCancelamento").value("Desistência"));

        String id = criada("Ciclo", q(portal));
        mockMvc.perform(patch(URL + "campanha/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content(corpo("Ciclo editada", "POPULACAO_GERAL", q(portal), "", "", hoje, hoje.plusDays(30))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.nome").value(PREFIXO + "Ciclo editada"));
        mockMvc.perform(post(URL + "campanha/" + id + "/inicio")).andExpect(status().isOk()).andExpect(jsonPath("$.situacao").value("EM_ANDAMENTO"));
        mockMvc.perform(patch(URL + "campanha/" + id).contentType(MediaType.APPLICATION_JSON)
                .content(corpo("Ciclo", "POPULACAO_GERAL", q(portal), "", "", hoje, hoje.plusDays(30)))).andExpect(status().isUnprocessableEntity());
        mockMvc.perform(post(URL + "campanha/" + id + "/encerramento").contentType(MediaType.APPLICATION_JSON).content("{\"balanco\": \"Alcançou as UBS\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.situacao").value("ENCERRADA")).andExpect(jsonPath("$.balanco").value("Alcançou as UBS"));
        mockMvc.perform(post(URL + "campanha/" + id + "/cancelamento").contentType(MediaType.APPLICATION_JSON).content("{\"motivo\": \"x\"}"))
                .andExpect(status().isUnprocessableEntity());
        mockMvc.perform(post(URL + "campanha/" + UUID.randomUUID() + "/inicio")).andExpect(status().isNotFound());
    }

    @Test
    void comunicadoDaCampanhaContaNoAlcance() throws Exception {
        String id = criada("Alcance", q(portal));
        String fora = "{\"titulo\": \"%sAviso\", \"mensagem\": \"M\", \"prioridade\": \"NORMAL\", \"publico\": \"POPULACAO\", \"inicio\": \"%s\", \"fim\": \"%s\", \"canalIds\": [%s], \"campanhaId\": \"%s\"}";
        mockMvc.perform(post(URL + "comunicado").contentType(MediaType.APPLICATION_JSON).content(fora.formatted(PREFIXO, hoje, hoje.plusDays(40), q(portal), id)))
                .andExpect(status().isUnprocessableEntity());
        String r = mockMvc.perform(post(URL + "comunicado").contentType(MediaType.APPLICATION_JSON).content(fora.formatted(PREFIXO, hoje, hoje.plusDays(5), q(portal), id)))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.campanhaNome").value(PREFIXO + "Alcance"))
                .andReturn().getResponse().getContentAsString();
        mockMvc.perform(post(URL + "comunicado/" + JsonPath.read(r, "$.uuid") + "/publicacao")).andExpect(status().isOk());
        mockMvc.perform(get(URL + "campanha")).andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.uuid == '" + id + "')].comunicados[0].situacao").value(hasItem("PUBLICADO")))
                .andExpect(jsonPath("$[?(@.uuid == '" + id + "')].alcance[0].publicacoes").value(hasItem(1)));
    }

    @Test
    void portalMostraACampanhaEmAndamentoComOsMateriais() throws Exception {
        String id = criada("Portal", q(portal));
        criada("Planejada", q(portal));
        String material = "{\"conteudoGrupoId\": \"%s\", \"formato\": \"CARTILHA\", \"titulo\": \"%sCartilha\", \"link\": \"https://gov.br/cartilha.pdf\"}";
        mockMvc.perform(post(URL + "material").contentType(MediaType.APPLICATION_JSON).content(material.formatted(publicado, PREFIXO))).andExpect(status().isCreated());
        mockMvc.perform(post(URL + "campanha/" + id + "/inicio")).andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/publico/comunicacao/campanhas")).andExpect(status().isOk())
                .andExpect(jsonPath("$[*].nome").value(hasItem(PREFIXO + "Portal")))
                .andExpect(jsonPath("$[*].nome").value(not(hasItem(PREFIXO + "Planejada"))))
                .andExpect(jsonPath("$[?(@.nome == '" + PREFIXO + "Portal')].conteudos[0].materiais[0].link").value(hasItem("https://gov.br/cartilha.pdf")));
    }

    @Test
    void materialEducativo() throws Exception {
        String material = "{\"conteudoGrupoId\": \"%s\", \"formato\": \"VIDEO\", \"titulo\": \"%sVídeo\", \"link\": \"%s\"}";
        mockMvc.perform(post(URL + "material").contentType(MediaType.APPLICATION_JSON).content(material.formatted(publicado, PREFIXO, "http://inseguro"))).andExpect(status().isBadRequest());
        mockMvc.perform(post(URL + "material").contentType(MediaType.APPLICATION_JSON).content(material.formatted(UUID.randomUUID(), PREFIXO, "https://x"))).andExpect(status().isNotFound());
        String r = mockMvc.perform(post(URL + "material").contentType(MediaType.APPLICATION_JSON).content(material.formatted(publicado, PREFIXO, "https://youtu.be/x")))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String id = JsonPath.read(r, "$.uuid");
        mockMvc.perform(post(URL + "material/" + id + "/retirada").contentType(MediaType.APPLICATION_JSON).content("{\"motivo\": \"Vídeo fora do ar\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.motivoRetirada").value("Vídeo fora do ar"));
        mockMvc.perform(post(URL + "material/" + id + "/retirada").contentType(MediaType.APPLICATION_JSON).content("{\"motivo\": \"x\"}")).andExpect(status().isUnprocessableEntity());
        mockMvc.perform(get(URL + "material").param("conteudoGrupoId", publicado.toString())).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].retiradoEm").exists());
    }
}
