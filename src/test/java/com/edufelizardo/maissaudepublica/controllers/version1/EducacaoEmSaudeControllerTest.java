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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

/**
 * Educação em saúde, fatia C3 (ADR-0195): o detalhe educativo da ação coletiva (conteúdo só revisado, o vencido marcado, material retirado
 * ou repetido, quantidade, campanha encerrada, ação individual ou de outro tipo, ação cancelada), a avaliação na realizada, as ações na
 * campanha de comunicação e o uso dos conteúdos.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EducacaoEmSaudeControllerTest {

    private static final String PREFIXO = "EduSaude Teste ";

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private UnidadeDeSaudeRepository unidadeRepository;

    private UnidadeDeSaude ubs;
    private String grupo;
    private String vacina;
    private String aprovado;
    private String vencido;
    private String rascunho;
    private String cartilha;
    private String retirado;
    private String campanha;
    private String encerrada;
    private String microarea;

    @BeforeEach
    void seed() {
        limpar();
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + "UBS");
        u.setTipo(TipoUnidadeDeSaude.UBS);
        u.setAtivo(true);
        ubs = unidadeRepository.save(u);
        grupo = acao("GRUPO", "PENDENTE");
        vacina = acao("VACINA", "PENDENTE");
        aprovado = conteudo("Dengue", "APROVADO", LocalDate.now().plusYears(1));
        vencido = conteudo("Aleitamento", "PUBLICADO", LocalDate.now().minusDays(1));
        rascunho = conteudo("Rascunho", "RASCUNHO", LocalDate.now().plusYears(1));
        cartilha = material(aprovado, "Cartilha da dengue", false);
        retirado = material(aprovado, "Folder antigo", true);
        campanha = campanha("Dengue no verão", "EM_ANDAMENTO");
        encerrada = campanha("Gripe passada", "ENCERRADA");
        String segmento = jdbc.queryForObject("insert into tb_segmento_territorial (uuid, codigo, nome, tipo) values (gen_random_uuid(), 'EDS-SEG', 'Seg', "
                + "'DISTRITO_SANITARIO') returning cast(uuid as varchar)", String.class);
        String territorio = jdbc.queryForObject("insert into tb_territorio (uuid, segmento_id, nome, finalidade, ativo) values (gen_random_uuid(), cast(? as uuid), ?, "
                + "'ATENCAO_PRIMARIA', true) returning cast(uuid as varchar)", String.class, segmento, PREFIXO + "Território");
        String area = jdbc.queryForObject("insert into tb_area (uuid, territorio_id, codigo, nome) values (gen_random_uuid(), cast(? as uuid), 'EDS-AR', 'Área') "
                + "returning cast(uuid as varchar)", String.class, territorio);
        microarea = jdbc.queryForObject("insert into tb_microarea (uuid, area_id, codigo) values (gen_random_uuid(), cast(? as uuid), 'EDS-01') "
                + "returning cast(uuid as varchar)", String.class, area);
    }

    @AfterEach
    void limpar() {
        String acoes = "(select uuid from tb_acao_programatica where descricao like '" + PREFIXO + "%')";
        String educativas = "(select uuid from tb_acao_educativa where acao_id in " + acoes + ")";
        jdbc.update("delete from tb_acao_educativa_material where acao_educativa_id in " + educativas);
        jdbc.update("delete from tb_acao_educativa_microarea where acao_educativa_id in " + educativas);
        jdbc.update("delete from tb_acao_educativa_conteudo where acao_educativa_id in " + educativas);
        jdbc.update("delete from tb_acao_educativa where acao_id in " + acoes);
        jdbc.update("delete from tb_acao_programatica where descricao like '" + PREFIXO + "%'");
        jdbc.update("delete from tb_material_educativo where titulo in ('Cartilha da dengue', 'Folder antigo')");
        jdbc.update("delete from tb_conteudo_educativo where titulo like '" + PREFIXO + "%'");
        jdbc.update("delete from tb_campanha_comunicacao where nome like '" + PREFIXO + "%'");
        jdbc.update("delete from tb_microarea where codigo = 'EDS-01'");
        jdbc.update("delete from tb_area where codigo = 'EDS-AR'");
        jdbc.update("delete from tb_territorio where nome = '" + PREFIXO + "Território'");
        jdbc.update("delete from tb_segmento_territorial where codigo = 'EDS-SEG'");
        jdbc.update("delete from tb_unidade_de_saude where nome like '" + PREFIXO + "%'");
    }

    private String acao(String tipo, String situacao) {
        return jdbc.queryForObject("insert into tb_acao_programatica (uuid, programa_id, unidade_id, tipo, situacao, data_prevista, descricao, local, criada_em) "
                + "select gen_random_uuid(), uuid, ?, ?, ?, ?, ?, 'Escola Municipal', now() from tb_programa where codigo = 'SAUDE_DA_FAMILIA' returning cast(uuid as varchar)",
                String.class, ubs.getUuid(), tipo, situacao, LocalDate.now().plusDays(3), PREFIXO + tipo);
    }

    private String conteudo(String titulo, String situacao, LocalDate validade) {
        return jdbc.queryForObject("insert into tb_conteudo_educativo (uuid, grupo_id, versao, titulo, resumo, texto, tema, publico, validade, situacao, autor_cpf) "
                + "values (gen_random_uuid(), gen_random_uuid(), 1, ?, 'Resumo', 'Texto', 'dengue', 'POPULACAO', ?, ?, '00000000000') returning cast(grupo_id as varchar)",
                String.class, PREFIXO + titulo, validade, situacao);
    }

    private String material(String grupoId, String titulo, boolean retiradoDoCatalogo) {
        return jdbc.queryForObject("insert into tb_material_educativo (uuid, conteudo_grupo_id, formato, titulo, registrado_em, retirado_em) values "
                + "(gen_random_uuid(), cast(? as uuid), 'CARTILHA', ?, now(), ?) returning cast(uuid as varchar)", String.class, grupoId, titulo,
                retiradoDoCatalogo ? java.sql.Timestamp.valueOf(java.time.LocalDateTime.now()) : null);
    }

    private String campanha(String nome, String situacao) {
        return jdbc.queryForObject("insert into tb_campanha_comunicacao (uuid, nome, objetivo, inicio, fim, situacao, publico_alvo, registrada_em) values "
                + "(gen_random_uuid(), ?, 'Informar', ?, ?, ?, 'POPULACAO_GERAL', now()) returning cast(uuid as varchar)", String.class, PREFIXO + nome,
                LocalDate.now().minusDays(5), LocalDate.now().plusDays(30), situacao);
    }

    private String enviar(MockHttpServletRequestBuilder r, String corpo, int esperado) throws Exception {
        if (corpo != null) {
            r = r.contentType(MediaType.APPLICATION_JSON).content(corpo);
        }
        var resposta = mockMvc.perform(r).andReturn().getResponse();
        assertThat(resposta.getStatus()).as(resposta.getContentAsString(StandardCharsets.UTF_8)).isEqualTo(esperado);
        return resposta.getContentAsString(StandardCharsets.UTF_8);
    }

    private String corpo(String conteudos, String materiais, String campanhaId) {
        return "{\"tema\": \"Dengue\", \"conteudoGrupoIds\": [%s], \"materiais\": [%s], \"microareaIds\": [\"%s\"]%s}"
                .formatted(conteudos, materiais, microarea, campanhaId == null ? "" : ", \"campanhaComunicacaoId\": \"" + campanhaId + "\"");
    }

    @Test
    void detalheEducativoDaAcaoColetiva() throws Exception {
        String url = "/api/v1/acao-programatica/" + grupo + "/educacao";
        assertThat((Object) JsonPath.read(enviar(get(url), null, 200), "$.tema")).isNull();
        String mat = "{\"materialId\": \"" + cartilha + "\", \"quantidade\": 50}";

        // As regras: tema obrigatório, só conteúdo revisado, material retirado ou repetido, quantidade, campanha encerrada, outro tipo.
        enviar(put(url), "{\"tema\": \"\"}", 400);
        enviar(put(url), corpo("\"" + rascunho + "\"", "", null), 422);
        enviar(put(url), corpo("", "{\"materialId\": \"" + retirado + "\", \"quantidade\": 5}", null), 422);
        enviar(put(url), corpo("", mat + ", " + mat, null), 400);
        enviar(put(url), corpo("", "{\"materialId\": \"" + cartilha + "\", \"quantidade\": 0}", null), 400);
        enviar(put(url), corpo("", "", encerrada), 422);
        enviar(put("/api/v1/acao-programatica/" + vacina + "/educacao"), corpo("", "", null), 422);

        String r = enviar(put(url), corpo("\"" + aprovado + "\", \"" + vencido + "\"", mat, campanha), 200);
        assertThat((String) JsonPath.read(r, "$.tema")).isEqualTo("Dengue");
        assertThat((List<Boolean>) JsonPath.read(r, "$.conteudos[*].vencido")).containsExactlyInAnyOrder(false, true);
        assertThat((Integer) JsonPath.read(r, "$.materiais[0].quantidade")).isEqualTo(50);
        assertThat((String) JsonPath.read(r, "$.microareas[0].codigo")).isEqualTo("EDS-01");
        assertThat((String) JsonPath.read(r, "$.campanhaComunicacaoNome")).isEqualTo(PREFIXO + "Dengue no verão");

        // A avaliação só depois de realizada; a cancelada não muda mais.
        enviar(post(url + "/avaliacao"), "{\"participacao\": \"ALTA\"}", 422);
        jdbc.update("update tb_acao_programatica set situacao = 'REALIZADA', realizada_em = ?, quantidade_atendida = 35 where uuid = cast(? as uuid)",
                LocalDate.now(), grupo);
        enviar(post(url + "/avaliacao"), "{}", 400);
        r = enviar(post(url + "/avaliacao"), "{\"participacao\": \"ALTA\", \"avaliacao\": \"Muitas perguntas sobre os focos.\"}", 200);
        assertThat((String) JsonPath.read(r, "$.participacao")).isEqualTo("ALTA");
        assertThat((Integer) JsonPath.read(r, "$.publicoAlcancado")).isEqualTo(35);

        // A campanha mostra a ação; o uso dos conteúdos conta a ação e o público.
        String campanhas = enviar(get("/api/v1/comunicacao/campanha"), null, 200);
        List<Integer> publico = JsonPath.read(campanhas, "$[?(@.uuid == '" + campanha + "')].acoesEducativas[0].publicoAlcancado");
        assertThat(publico).containsExactly(35);
        String uso = enviar(get("/api/v1/educacao-permanente/uso-dos-conteudos"), null, 200);
        assertThat(JsonPath.parse(uso).read("$[?(@.grupoId == '" + aprovado + "')].acoesEducativas", List.class)).containsExactly(1);
        assertThat(JsonPath.parse(uso).read("$[?(@.grupoId == '" + aprovado + "')].pessoasAlcancadas", List.class)).containsExactly(35);

        jdbc.update("update tb_acao_programatica set situacao = 'CANCELADA' where uuid = cast(? as uuid)", grupo);
        enviar(put(url), corpo("", "", null), 422);
    }
}
