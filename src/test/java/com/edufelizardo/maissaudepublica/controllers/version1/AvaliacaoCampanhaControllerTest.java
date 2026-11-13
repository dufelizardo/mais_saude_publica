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

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Avaliação da comunicação e da educação, fatia C6 (ADR-0196): as metas da campanha, a apuração (canais, ações educativas, cobertura por
 * território, metas e efetividade), a avaliação só na encerrada com os números congelados e os dois relatórios do #19.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AvaliacaoCampanhaControllerTest {

    private static final String PREFIXO = "AvalCamp Teste ";

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private UnidadeDeSaudeRepository unidadeRepository;

    private UnidadeDeSaude ubs;
    private String campanha;
    private String realizada;
    private String a1;

    @BeforeEach
    void seed() {
        limpar();
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + "UBS");
        u.setTipo(TipoUnidadeDeSaude.UBS);
        u.setAtivo(true);
        ubs = unidadeRepository.save(u);
        String segmento = jdbc.queryForObject("insert into tb_segmento_territorial (uuid, codigo, nome, tipo) values (gen_random_uuid(), 'AVC-SEG', 'Seg', "
                + "'DISTRITO_SANITARIO') returning cast(uuid as varchar)", String.class);
        String t1 = territorio(segmento, "Norte");
        String t2 = territorio(segmento, "Sul");
        a1 = microarea(area(t1, "AVC-AN"), "AVC-N1");
        microarea(jdbc.queryForObject("select cast(uuid as varchar) from tb_area where codigo = 'AVC-AN'", String.class), "AVC-N2");
        String b1 = microarea(area(t2, "AVC-AS"), "AVC-S1");
        String programa = jdbc.queryForObject("select cast(uuid as varchar) from tb_programa where codigo = 'SAUDE_DA_FAMILIA'", String.class);
        String operacional = jdbc.queryForObject("insert into tb_campanha (uuid, nome, tipo, programa_id, inicio, fim, gestante, publicada, meta_quantidade, criada_em) "
                + "values (gen_random_uuid(), ?, 'CONSCIENTIZACAO', cast(? as uuid), ?, ?, false, false, 100, now()) returning cast(uuid as varchar)", String.class,
                PREFIXO + "Mutirão", programa, LocalDate.now().minusDays(5), LocalDate.now().plusDays(30));
        campanha = jdbc.queryForObject("insert into tb_campanha_comunicacao (uuid, nome, objetivo, inicio, fim, situacao, publico_alvo, unidade_id, programa_id, "
                + "campanha_operacional_id, meta_pessoas, meta_acoes, registrada_em) values (gen_random_uuid(), ?, 'Informar', ?, ?, 'EM_ANDAMENTO', 'POPULACAO_GERAL', ?, "
                + "cast(? as uuid), cast(? as uuid), 50, 3, now()) returning cast(uuid as varchar)", String.class, PREFIXO + "Dengue",
                LocalDate.now().minusDays(5), LocalDate.now().plusDays(30), ubs.getUuid(), programa, operacional);
        jdbc.update("insert into tb_campanha_comunicacao_territorio (campanha_id, territorio_id) values (cast(? as uuid), cast(? as uuid)), (cast(? as uuid), cast(? as uuid))",
                campanha, t1, campanha, t2);
        String email = canal("EMAIL");
        String portal = canal("PORTAL");
        jdbc.update("insert into tb_campanha_comunicacao_canal (campanha_id, canal_id) values (cast(? as uuid), cast(? as uuid)), (cast(? as uuid), cast(? as uuid))",
                campanha, email, campanha, portal);
        String comunicado = jdbc.queryForObject("insert into tb_comunicado (uuid, titulo, mensagem, prioridade, publico, situacao, inicio, fim, campanha_comunicacao_id) "
                + "values (gen_random_uuid(), ?, 'M', 'NORMAL', 'POPULACAO', 'PUBLICADO', ?, ?, cast(? as uuid)) returning cast(uuid as varchar)", String.class,
                PREFIXO + "Aviso", LocalDate.now(), LocalDate.now().plusDays(5), campanha);
        publicacao(comunicado, email, 30, 10);
        publicacao(comunicado, portal, 0, 0);
        // A ação operacional realizada dá o andamento da meta da campanha operacional.
        jdbc.update("insert into tb_acao_programatica (uuid, unidade_id, tipo, situacao, data_prevista, realizada_em, quantidade_atendida, descricao, "
                + "campanha_id, criada_em) values (gen_random_uuid(), ?, 'OUTRA', 'REALIZADA', ?, ?, 40, ?, cast(? as uuid), now())",
                ubs.getUuid(), LocalDate.now(), LocalDate.now(), PREFIXO + "Operacional", operacional);
        String cartilha = jdbc.queryForObject("insert into tb_material_educativo (uuid, conteudo_grupo_id, formato, titulo, registrado_em) values "
                + "(gen_random_uuid(), gen_random_uuid(), 'CARTILHA', ?, now()) returning cast(uuid as varchar)", String.class, PREFIXO + "Cartilha");
        realizada = educativa(programa, "REALIZADA", 35, "ALTA", a1);
        material(realizada, cartilha, 20);
        educativa(programa, "PENDENTE", null, null, b1);
        educativa(programa, "REALIZADA", 5, null, null);
    }

    @AfterEach
    void limpar() {
        String acoes = "(select uuid from tb_acao_programatica where descricao like '" + PREFIXO + "%')";
        String educativas = "(select uuid from tb_acao_educativa where acao_id in " + acoes + ")";
        String campanhas = "(select uuid from tb_campanha_comunicacao where nome like '" + PREFIXO + "%')";
        jdbc.update("delete from tb_acao_educativa_material where acao_educativa_id in " + educativas);
        jdbc.update("delete from tb_acao_educativa_microarea where acao_educativa_id in " + educativas);
        jdbc.update("delete from tb_acao_educativa where acao_id in " + acoes);
        jdbc.update("delete from tb_acao_programatica where descricao like '" + PREFIXO + "%'");
        jdbc.update("delete from tb_material_educativo where titulo like '" + PREFIXO + "%'");
        jdbc.update("delete from tb_publicacao where comunicado_id in (select uuid from tb_comunicado where titulo like '" + PREFIXO + "%')");
        jdbc.update("delete from tb_comunicado where titulo like '" + PREFIXO + "%'");
        jdbc.update("delete from tb_avaliacao_campanha where campanha_id in " + campanhas);
        jdbc.update("delete from tb_campanha_comunicacao_territorio where campanha_id in " + campanhas);
        jdbc.update("delete from tb_campanha_comunicacao_canal where campanha_id in " + campanhas);
        jdbc.update("delete from tb_campanha_comunicacao where nome like '" + PREFIXO + "%'");
        jdbc.update("delete from tb_campanha where nome like '" + PREFIXO + "%'");
        jdbc.update("delete from tb_microarea where codigo like 'AVC-%'");
        jdbc.update("delete from tb_area where codigo like 'AVC-%'");
        jdbc.update("delete from tb_territorio where nome like '" + PREFIXO + "%'");
        jdbc.update("delete from tb_segmento_territorial where codigo = 'AVC-SEG'");
        jdbc.update("delete from tb_unidade_de_saude where nome like '" + PREFIXO + "%'");
    }

    private String territorio(String segmento, String nome) {
        return jdbc.queryForObject("insert into tb_territorio (uuid, segmento_id, nome, finalidade, ativo) values (gen_random_uuid(), cast(? as uuid), ?, "
                + "'ATENCAO_PRIMARIA', true) returning cast(uuid as varchar)", String.class, segmento, PREFIXO + nome);
    }

    private String area(String territorio, String codigo) {
        return jdbc.queryForObject("insert into tb_area (uuid, territorio_id, codigo, nome) values (gen_random_uuid(), cast(? as uuid), ?, 'Área') "
                + "returning cast(uuid as varchar)", String.class, territorio, codigo);
    }

    private String microarea(String area, String codigo) {
        return jdbc.queryForObject("insert into tb_microarea (uuid, area_id, codigo) values (gen_random_uuid(), cast(? as uuid), ?) returning cast(uuid as varchar)",
                String.class, area, codigo);
    }

    private String canal(String tipo) {
        return jdbc.queryForObject("select cast(uuid as varchar) from tb_canal_comunicacao where tipo = ? order by nome limit 1", String.class, tipo);
    }

    private void publicacao(String comunicado, String canal, int enviados, int falhas) {
        jdbc.update("insert into tb_publicacao (uuid, comunicado_id, canal_id, publicada_em, situacao, enviados, falhas) values (gen_random_uuid(), cast(? as uuid), "
                + "cast(? as uuid), now(), 'ENVIADA', ?, ?)", comunicado, canal, enviados, falhas);
    }

    private String educativa(String programa, String situacao, Integer quantidade, String participacao, String microarea) {
        String acao = jdbc.queryForObject("insert into tb_acao_programatica (uuid, programa_id, unidade_id, tipo, situacao, data_prevista, realizada_em, quantidade_atendida, "
                        + "descricao, criada_em) values (gen_random_uuid(), cast(? as uuid), ?, 'GRUPO', ?, ?, ?, ?, ?, now()) returning cast(uuid as varchar)", String.class,
                programa, ubs.getUuid(), situacao, LocalDate.now(), "REALIZADA".equals(situacao) ? LocalDate.now() : null, quantidade,
                PREFIXO + situacao + quantidade);
        String e = jdbc.queryForObject("insert into tb_acao_educativa (uuid, acao_id, tema, campanha_comunicacao_id, participacao, atualizado_em) values "
                + "(gen_random_uuid(), cast(? as uuid), 'Dengue', cast(? as uuid), ?, now()) returning cast(uuid as varchar)", String.class, acao, campanha, participacao);
        if (microarea != null) {
            jdbc.update("insert into tb_acao_educativa_microarea (acao_educativa_id, microarea_id) values (cast(? as uuid), cast(? as uuid))", e, microarea);
        }
        return acao;
    }

    private void material(String acao, String material, int quantidade) {
        jdbc.update("insert into tb_acao_educativa_material (uuid, acao_educativa_id, material_id, quantidade) select gen_random_uuid(), uuid, cast(? as uuid), ? "
                + "from tb_acao_educativa where acao_id = cast(? as uuid)", material, quantidade, acao);
    }

    private String enviar(MockHttpServletRequestBuilder r, String corpo, int esperado) throws Exception {
        if (corpo != null) {
            r = r.contentType(MediaType.APPLICATION_JSON).content(corpo);
        }
        var resposta = mockMvc.perform(r).andReturn().getResponse();
        assertThat(resposta.getStatus()).as(resposta.getContentAsString(StandardCharsets.UTF_8)).isEqualTo(esperado);
        return resposta.getContentAsString(StandardCharsets.UTF_8);
    }

    private static Object ler(String json, String caminho) {
        return JsonPath.read(json, caminho);
    }

    @Test
    void metasDaCampanha() throws Exception {
        String corpo = "{\"nome\": \"%sMetas\", \"objetivo\": \"O\", \"inicio\": \"%s\", \"fim\": \"%s\", \"publicoAlvo\": \"POPULACAO_GERAL\", \"metaPessoas\": %d, \"metaAcoes\": 2}";
        enviar(post("/api/v1/comunicacao/campanha"), corpo.formatted(PREFIXO, LocalDate.now(), LocalDate.now().plusDays(9), 0), 400);
        String r = enviar(post("/api/v1/comunicacao/campanha"), corpo.formatted(PREFIXO, LocalDate.now(), LocalDate.now().plusDays(9), 120), 201);
        assertThat(ler(r, "$.metaPessoas")).isEqualTo(120);
        assertThat(ler(r, "$.metaAcoes")).isEqualTo(2);
    }

    @Test
    void apuracaoDaCampanha() throws Exception {
        String r = enviar(get("/api/v1/comunicacao/campanha/" + campanha + "/avaliacao"), null, 200);
        // Canais: o e-mail entregou 30 de 40 e é o destaque; o Portal não conta pessoas.
        assertThat(ler(r, "$.enviados")).isEqualTo(30);
        assertThat((List<Object>) ler(r, "$.canais[?(@.tipo == 'EMAIL')].taxaEntrega")).containsExactly(75.0);
        assertThat((List<Object>) ler(r, "$.canais[?(@.tipo == 'EMAIL')].destaque")).containsExactly(true);
        assertThat((List<Object>) ler(r, "$.canais[?(@.tipo == 'PORTAL')].semContagem")).containsExactly(true);
        // Ações: uma prevista, duas realizadas com 40 pessoas, a participação e o material.
        assertThat(ler(r, "$.acoes.previstas")).isEqualTo(1);
        assertThat(ler(r, "$.acoes.realizadas")).isEqualTo(2);
        assertThat(ler(r, "$.acoes.pessoas")).isEqualTo(40);
        assertThat(ler(r, "$.acoes.participacaoAlta")).isEqualTo(1);
        assertThat(ler(r, "$.acoes.semAvaliacao")).isEqualTo(1);
        assertThat(ler(r, "$.acoes.materiais[0].quantidade")).isEqualTo(20);
        // Cobertura: o Sul sem ação vem primeiro, em destaque; o Norte com uma de duas microáreas; a ação sem microárea à parte.
        assertThat(ler(r, "$.territorios[0].nome")).isEqualTo(PREFIXO + "Sul");
        assertThat(ler(r, "$.territorios[0].cobertura")).isEqualTo(0.0);
        assertThat(ler(r, "$.territorios[0].menor")).isEqualTo(true);
        assertThat(ler(r, "$.territorios[1].cobertura")).isEqualTo(50.0);
        assertThat(ler(r, "$.territorios[1].pessoas")).isEqualTo(35);
        assertThat(ler(r, "$.cobertura")).isEqualTo(33.3);
        assertThat(ler(r, "$.semTerritorio.acoes")).isEqualTo(1);
        assertThat(ler(r, "$.semTerritorio.pessoas")).isEqualTo(5);
        // Metas em andamento; efetividade pela campanha operacional e pelo programa.
        assertThat(ler(r, "$.metaPessoas.situacao")).isEqualTo("EM_ANDAMENTO");
        assertThat(ler(r, "$.metaAcoes.valor")).isEqualTo(2);
        assertThat(ler(r, "$.efetividade.realizadoOperacional")).isEqualTo(40);
        assertThat(ler(r, "$.efetividade.percentualOperacional")).isEqualTo(40.0);
        assertThat(ler(r, "$.efetividade.programa")).isNotNull();
        assertThat(ler(r, "$.efetividade.novosNoPeriodo")).isNotNull();
        assertThat(ler(r, "$.avaliacao")).isNull();
    }

    @Test
    void avaliacaoSoNaEncerradaComNumerosCongelados() throws Exception {
        String url = "/api/v1/comunicacao/campanha/" + campanha + "/avaliacao";
        String corpo = "{\"publicoAtingido\": \"PARCIAL\", \"resultados\": \"Escolas alcançadas.\", \"conclusoes\": \"Faltou o Sul.\", \"recomendacoes\": \"Agentes no Sul.\"}";
        enviar(post(url), corpo, 422);
        jdbc.update("update tb_campanha_comunicacao set situacao = 'ENCERRADA', encerrada_em = now(), balanco = 'B' where uuid = cast(? as uuid)", campanha);
        enviar(post(url), "{\"conclusoes\": \"x\"}", 400);
        enviar(post(url), "{\"publicoAtingido\": \"SIM\"}", 400);
        String r = enviar(post(url), corpo, 200);
        assertThat(ler(r, "$.avaliacao.publicoAtingido")).isEqualTo("PARCIAL");
        assertThat(ler(r, "$.avaliacao.pessoasAlcancadas")).isEqualTo(40);
        assertThat(ler(r, "$.avaliacao.situacaoMetaPessoas")).isEqualTo("ABAIXO");
        assertThat(ler(r, "$.avaliacao.cobertura")).isEqualTo(33.3);

        // Mexer numa ação depois não muda o julgamento registrado; a apuração ao vivo muda.
        jdbc.update("update tb_acao_programatica set quantidade_atendida = 100 where uuid = cast(? as uuid)", realizada);
        r = enviar(get(url), null, 200);
        assertThat(ler(r, "$.acoes.pessoas")).isEqualTo(105);
        assertThat(ler(r, "$.metaPessoas.situacao")).isEqualTo("ATINGIDA");
        assertThat(ler(r, "$.avaliacao.pessoasAlcancadas")).isEqualTo(40);
        assertThat(ler(r, "$.avaliacao.situacaoMetaPessoas")).isEqualTo("ABAIXO");
    }

    @Test
    void relatoriosDoIndicadores() throws Exception {
        String periodo = "?de=" + LocalDate.now().minusDays(10) + "&ate=" + LocalDate.now();
        String r = enviar(get("/api/v1/relatorios/CAMPANHAS_COMUNICACAO" + periodo + "&unidadeId=" + ubs.getUuid()), null, 200);
        assertThat(ler(r, "$.categoria")).isEqualTo("Comunicação e educação");
        assertThat((List<Object>) ler(r, "$.secoes[0].linhas[?(@[0] == '" + PREFIXO + "Dengue')][5]")).containsExactly("40");

        r = enviar(get("/api/v1/relatorios/EDUCACAO_EM_SAUDE" + periodo + "&unidadeId=" + ubs.getUuid()), null, 200);
        assertThat(ler(r, "$.resumo[0].valor")).isEqualTo("2");
        assertThat(ler(r, "$.resumo[1].valor")).isEqualTo("40");
        assertThat((List<Object>) ler(r, "$.secoes[2].linhas[?(@[0] == '" + PREFIXO + "Cartilha')][1]")).containsExactly("20");
        enviar(get("/api/v1/relatorios/EDUCACAO_EM_SAUDE/exportacao" + periodo + "&unidadeId=" + ubs.getUuid()), null, 200);
    }
}
