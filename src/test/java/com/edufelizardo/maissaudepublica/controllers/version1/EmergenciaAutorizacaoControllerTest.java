package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.AtribuicaoAcesso;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.Usuario;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.AtribuicaoAcessoRepository;
import com.edufelizardo.maissaudepublica.repositories.PapelRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import com.edufelizardo.maissaudepublica.repositories.UsuarioRepository;
import com.edufelizardo.maissaudepublica.services.version1.JwtService;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Emergências com login e autorização ligados (ADR-0156): a vigilância registra o evento e o impacto; ativar e mudar o
 * nível é do gestor; a recepção e a administração da plataforma não leem.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"app.security.enabled=true", "app.security.authorization.enabled=true"})
class EmergenciaAutorizacaoControllerTest {

    private static final String PREFIXO = "EmergAut Teste ";
    private static final String VIGILANCIA = "94901867401";
    private static final String GESTOR = "94901867402";
    private static final String RECEPCAO = "94901867403";
    private static final String ADMIN = "94901867404";
    private static final List<String> CPFS = List.of(VIGILANCIA, GESTOR, RECEPCAO, ADMIN);

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private JwtService jwtService;
    @Autowired private UnidadeDeSaudeRepository unidadeDeSaudeRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private AtribuicaoAcessoRepository atribuicaoRepository;
    @Autowired private PapelRepository papelRepository;

    private UnidadeDeSaude unidade;

    @BeforeEach
    void seed() {
        limpar();
        unidade = new UnidadeDeSaude();
        unidade.setNome(PREFIXO + "Secretaria");
        unidade.setTipo(TipoUnidadeDeSaude.UBS);
        unidade.setAtivo(true);
        unidade = unidadeDeSaudeRepository.save(unidade);
        atribuir(VIGILANCIA, "VIGILANCIA_EPIDEMIOLOGICA");
        atribuir(GESTOR, "GESTOR");
        atribuir(RECEPCAO, "RECEPCAO");
        atribuir(ADMIN, "ADMINISTRADOR_PLATAFORMA");
    }

    @AfterEach
    void limpar() {
        String emergencias = "(select e.uuid from tb_emergencia e join tb_evento_emergencial v on v.uuid = e.evento_id where v.descricao like '" + PREFIXO + "%')";
        jdbc.update("delete from tb_registro_ativacao_plano where ativacao_id in (select uuid from tb_ativacao_plano where emergencia_id in "
                + emergencias + ")");
        jdbc.update("delete from tb_ativacao_plano where emergencia_id in " + emergencias);
        jdbc.update("delete from tb_mudanca_nivel_emergencia where emergencia_id in " + emergencias);
        jdbc.update("delete from tb_emergencia where uuid in " + emergencias);
        jdbc.update("delete from tb_evento_emergencial where descricao like '" + PREFIXO + "%'");
        String planos = "(select uuid from tb_plano_contingencia where codigo like 'EMGAUT_%')";
        jdbc.update("delete from tb_acao_plano where plano_id in " + planos);
        jdbc.update("delete from tb_plano_contingencia_nivel where plano_id in " + planos);
        jdbc.update("delete from tb_plano_contingencia where codigo like 'EMGAUT_%'");
        for (String cpf : CPFS) {
            atribuicaoRepository.deleteAll(atribuicaoRepository.findByUsuarioCpf(cpf));
            usuarioRepository.findByCpf(cpf).ifPresent(usuarioRepository::delete);
            jdbc.update("delete from tb_evento_auditoria where usuario_cpf = ?", cpf);
        }
        jdbc.update("delete from tb_unidade_de_saude where nome like '" + PREFIXO + "%'");
    }

    private void atribuir(String cpf, String papel) {
        Usuario usuario = usuarioRepository.save(new Usuario(cpf, PREFIXO + cpf, "hash-irrelevante"));
        AtribuicaoAcesso a = new AtribuicaoAcesso();
        a.setUsuario(usuario);
        a.setPapel(papelRepository.findByCodigo(papel).orElseThrow());
        a.setUnidade(unidade);
        a.setConcedidoEm(Instant.now());
        atribuicaoRepository.save(a);
    }

    private MockHttpServletRequestBuilder como(String cpf, MockHttpServletRequestBuilder r) {
        return r.header("Authorization", "Bearer " + jwtService.gerarToken(cpf, PREFIXO + cpf, false));
    }

    private int status(String cpf, MockHttpServletRequestBuilder r) throws Exception {
        return mockMvc.perform(como(cpf, r)).andReturn().getResponse().getStatus();
    }

    private static MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder r, String corpo) {
        return r.contentType(MediaType.APPLICATION_JSON).content(corpo);
    }

    @Test
    void registrarAtivarELerPorPapel() throws Exception {
        String tipo = jdbc.queryForObject("select cast(uuid as varchar) from tb_tipo_emergencia where codigo = 'EPIDEMIA'", String.class);
        String nivel = jdbc.queryForObject("select cast(uuid as varchar) from tb_nivel_emergencia where ordem = 1", String.class);
        String corpo = "{\"tipoId\": \"" + tipo + "\", \"fonte\": \"VIGILANCIA\", \"descricao\": \"" + PREFIXO + "Casos de dengue acima do esperado\"}";
        // Registrar: a vigilância sim; a recepção não.
        assertThat(status(RECEPCAO, json(post("/api/v1/evento-emergencial"), corpo))).isEqualTo(403);
        String r = mockMvc.perform(como(VIGILANCIA, json(post("/api/v1/evento-emergencial"), corpo))).andReturn().getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        String evento = JsonPath.read(r, "$.uuid");

        // Ativar: a autoridade (gestor); a vigilância registra, mas não ativa.
        String conversao = "{\"nome\": \"Epidemia de dengue\", \"nivelId\": \"" + nivel + "\", \"justificativa\": \"Incidência acima do limiar\"}";
        String url = "/api/v1/evento-emergencial/" + evento + "/emergencia";
        assertThat(status(VIGILANCIA, json(post(url), conversao))).isEqualTo(403);
        assertThat(status(GESTOR, json(post(url), conversao))).isEqualTo(201);

        // Ler: quem registra, ativa ou consulta indicadores; a recepção e a administração da plataforma não.
        assertThat(status(VIGILANCIA, get("/api/v1/emergencias"))).isEqualTo(200);
        assertThat(status(GESTOR, get("/api/v1/emergencias"))).isEqualTo(200);
        assertThat(status(RECEPCAO, get("/api/v1/emergencias"))).isEqualTo(403);
        assertThat(status(ADMIN, get("/api/v1/emergencias"))).isEqualTo(403);

        // O catálogo: o gestor mantém; a vigilância não.
        String tipoNovo = "{\"codigo\": \"EMGAUT_X\", \"nome\": \"X\", \"categoria\": \"SAUDE\"}";
        assertThat(status(VIGILANCIA, json(post("/api/v1/emergencias/tipo"), tipoNovo))).isEqualTo(403);
    }

    @Test
    void planosPorPapel() throws Exception {
        String tipo = jdbc.queryForObject("select cast(uuid as varchar) from tb_tipo_emergencia where codigo = 'EPIDEMIA'", String.class);
        String nivel = jdbc.queryForObject("select cast(uuid as varchar) from tb_nivel_emergencia where ordem = 1", String.class);
        String plano = "{\"codigo\": \"EMGAUT_DENGUE\", \"nome\": \"Plano da dengue\", \"tipoId\": \"" + tipo + "\", \"objetivo\": \"Responder\", "
                + "\"vigenciaInicio\": \"" + java.time.LocalDate.now().minusDays(1) + "\", \"nivelIds\": [\"" + nivel + "\"], \"acoes\": [{\"nivelId\": \""
                + nivel + "\", \"descricao\": \"Mutirão\", \"responsavel\": \"Vigilância\"}]}";
        // Manter o plano: o gestor; a vigilância lê, mas não mantém.
        assertThat(status(VIGILANCIA, json(post("/api/v1/emergencias/plano"), plano))).isEqualTo(403);
        String r = mockMvc.perform(como(GESTOR, json(post("/api/v1/emergencias/plano"), plano))).andReturn().getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        String id = JsonPath.read(r, "$.uuid");
        assertThat(status(GESTOR, post("/api/v1/emergencias/plano/" + id + "/publicacao"))).isEqualTo(200);
        assertThat(status(VIGILANCIA, get("/api/v1/emergencias/planos"))).isEqualTo(200);
        assertThat(status(RECEPCAO, get("/api/v1/emergencias/planos"))).isEqualTo(403);

        // Ativar na emergência: a autoridade; a vigilância não.
        String evento = JsonPath.read(mockMvc.perform(como(VIGILANCIA, json(post("/api/v1/evento-emergencial"), "{\"tipoId\": \"" + tipo
                + "\", \"fonte\": \"VIGILANCIA\", \"descricao\": \"" + PREFIXO + "Dengue\"}"))).andReturn().getResponse()
                .getContentAsString(StandardCharsets.UTF_8), "$.uuid");
        String em = JsonPath.read(mockMvc.perform(como(GESTOR, json(post("/api/v1/evento-emergencial/" + evento + "/emergencia"), "{\"nome\": \"Dengue\", "
                + "\"nivelId\": \"" + nivel + "\", \"justificativa\": \"Limiar\"}"))).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8), "$.uuid");
        String ativar = "{\"planoId\": \"" + id + "\", \"autoridade\": \"Secretário\", \"justificativa\": \"Limiar\"}";
        assertThat(status(VIGILANCIA, json(post("/api/v1/emergencia/" + em + "/plano-ativo"), ativar))).isEqualTo(403);
        assertThat(status(GESTOR, json(post("/api/v1/emergencia/" + em + "/plano-ativo"), ativar))).isEqualTo(201);
    }
}
