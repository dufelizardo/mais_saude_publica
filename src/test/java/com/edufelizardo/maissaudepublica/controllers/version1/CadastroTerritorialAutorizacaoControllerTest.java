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

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Cadastro territorial com login e autorização ligados (ADR-0127): o ACS cadastra na unidade da equipe da área; o
 * enfermeiro de outra unidade não; o gestor lê (gestão do território) e não cadastra; a leitura do imóvel é auditada.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"app.security.enabled=true", "app.security.authorization.enabled=true"})
class CadastroTerritorialAutorizacaoControllerTest {

    private static final String PREFIXO = "CadTerAut Teste ";
    private static final String CODIGO = "TSTF3AUT";
    private static final String ACS = "94701867380";
    private static final String ENF_OUTRA = "94701867381";
    private static final String GESTOR = "94701867382";
    private static final List<String> CPFS = List.of(ACS, ENF_OUTRA, GESTOR);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AtribuicaoAcessoRepository atribuicaoRepository;

    @Autowired
    private PapelRepository papelRepository;

    private UnidadeDeSaude ubs;
    private String microarea;

    @BeforeEach
    void seed() throws Exception {
        limpar();
        ubs = unidade("UBS");
        UnidadeDeSaude outra = unidade("UBS Outra");
        atribuir(ACS, "AGENTE_COMUNITARIO_SAUDE", ubs);
        atribuir(ENF_OUTRA, "ENFERMEIRO", outra);
        atribuir(GESTOR, "GESTOR", ubs);
        // O recorte e a equipe pelo gestor, na unidade dele.
        String s = enviar(GESTOR, post("/api/v1/territorio/segmento"), "{\"codigo\": \"%s\", \"nome\": \"D\", \"tipo\": \"OUTRO\", \"unidadeGestaoId\": \"%s\"}"
                .formatted(CODIGO, ubs.getUuid()), 201);
        String t = enviar(GESTOR, post("/api/v1/territorio/territorio"), "{\"segmentoId\": \"%s\", \"nome\": \"APS\", \"finalidade\": \"ATENCAO_PRIMARIA\"}"
                .formatted(JsonPath.<String>read(s, "$.uuid")), 201);
        String a = enviar(GESTOR, post("/api/v1/territorio/area"), "{\"territorioId\": \"%s\", \"codigo\": \"01\", \"nome\": \"A\"}"
                .formatted(JsonPath.<String>read(t, "$.uuid")), 201);
        String area = JsonPath.read(a, "$.area.uuid");
        String m = enviar(GESTOR, post("/api/v1/territorio/microarea"), "{\"areaId\": \"%s\", \"codigo\": \"07\"}".formatted(area), 201);
        microarea = JsonPath.read(m, "$.area.microareas[0].uuid");
        String e = enviar(GESTOR, post("/api/v1/equipe/"), "{\"unidadeId\": \"%s\", \"tipo\": \"ESF\", \"nome\": \"%sSF\"}".formatted(ubs.getUuid(), PREFIXO), 201);
        enviar(GESTOR, post("/api/v1/territorio/area/" + area + "/responsabilidade"), "{\"equipeId\": \"%s\"}".formatted(JsonPath.<String>read(e, "$.resumo.uuid")), 200);
    }

    @AfterEach
    void limpar() {
        for (String cpf : CPFS) {
            atribuicaoRepository.deleteAll(atribuicaoRepository.findByUsuarioCpf(cpf));
            usuarioRepository.findByCpf(cpf).ifPresent(usuarioRepository::delete);
            jdbc.update("delete from tb_evento_auditoria where usuario_cpf = ?", cpf);
        }
        TerritorioControllerTest.limparDados(jdbc, PREFIXO, CODIGO);
        jdbc.update("delete from tb_equipe where unidade_id in (select uuid from tb_unidade_de_saude where nome like ?)", PREFIXO + "%");
        unidadeDeSaudeRepository.deleteAll(unidadeDeSaudeRepository.findAll().stream()
                .filter(u -> u.getNome() != null && u.getNome().startsWith(PREFIXO)).toList());
    }

    private UnidadeDeSaude unidade(String nome) {
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + nome);
        u.setTipo(TipoUnidadeDeSaude.UBS);
        u.setAtivo(true);
        return unidadeDeSaudeRepository.save(u);
    }

    private void atribuir(String cpf, String papel, UnidadeDeSaude unidade) {
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

    private String enviar(String cpf, MockHttpServletRequestBuilder r, String corpo, int esperado) throws Exception {
        return mockMvc.perform(como(cpf, r).contentType(MediaType.APPLICATION_JSON).content(corpo)).andExpect(status().is(esperado))
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    void acsCadastraNaUnidadeDaEquipeOutrosNao() throws Exception {
        String imovel = "{\"microareaId\": \"%s\", \"tipo\": \"DOMICILIO\", \"endereco\": %s}".formatted(microarea,
                CadastroTerritorialControllerTest.endereco("Rua A", "1"));
        enviar(ENF_OUTRA, post("/api/v1/territorio/imovel"), imovel, 403);
        enviar(GESTOR, post("/api/v1/territorio/imovel"), imovel, 403);
        String r = enviar(ACS, post("/api/v1/territorio/imovel"), imovel, 201);
        String id = JsonPath.read(r, "$.imovel.uuid");

        mockMvc.perform(como(ENF_OUTRA, get("/api/v1/territorio/imovel/" + id))).andExpect(status().isForbidden());
        mockMvc.perform(como(GESTOR, get("/api/v1/territorio/imovel/" + id))).andExpect(status().isOk());
        mockMvc.perform(como(ACS, get("/api/v1/territorio/imovel/" + id))).andExpect(status().isOk());
        assertThat(jdbc.queryForObject("select count(*) from tb_evento_auditoria where usuario_cpf = ? and registro_id = ?::uuid",
                Integer.class, ACS, id)).isGreaterThanOrEqualTo(1);
        // A árvore, só com contagens, é de quem está logado.
        mockMvc.perform(como(ENF_OUTRA, get("/api/v1/territorio/arvore"))).andExpect(status().isOk());
    }
}
