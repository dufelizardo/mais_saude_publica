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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Território com login e autorização ligados (ADR-0125): ler é de quem está logado; cadastrar é do gestor, na unidade
 * de gestão do segmento (sem unidade, só na rede inteira); o enfermeiro não cadastra.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"app.security.enabled=true", "app.security.authorization.enabled=true"})
class TerritorioAutorizacaoControllerTest {

    private static final String PREFIXO = "TerAut Teste ";
    private static final String CODIGO = "TSTAUT";
    private static final String ENF = "94701867370";
    private static final String GESTOR = "94701867371";
    private static final List<String> CPFS = List.of(ENF, GESTOR);

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

    @BeforeEach
    void seed() {
        limpar();
        ubs = new UnidadeDeSaude();
        ubs.setNome(PREFIXO + "UBS");
        ubs.setTipo(TipoUnidadeDeSaude.UBS);
        ubs.setAtivo(true);
        ubs = unidadeDeSaudeRepository.save(ubs);
        atribuir(ENF, "ENFERMEIRO");
        atribuir(GESTOR, "GESTOR");
    }

    @AfterEach
    void limpar() {
        for (String cpf : CPFS) {
            atribuicaoRepository.deleteAll(atribuicaoRepository.findByUsuarioCpf(cpf));
            usuarioRepository.findByCpf(cpf).ifPresent(usuarioRepository::delete);
            jdbc.update("delete from tb_evento_auditoria where usuario_cpf = ?", cpf);
        }
        TerritorioControllerTest.limparDados(jdbc, PREFIXO, CODIGO);
        unidadeDeSaudeRepository.deleteAll(unidadeDeSaudeRepository.findAll().stream()
                .filter(u -> u.getNome() != null && u.getNome().startsWith(PREFIXO)).toList());
    }

    private void atribuir(String cpf, String papel) {
        Usuario usuario = usuarioRepository.save(new Usuario(cpf, PREFIXO + cpf, "hash-irrelevante"));
        AtribuicaoAcesso a = new AtribuicaoAcesso();
        a.setUsuario(usuario);
        a.setPapel(papelRepository.findByCodigo(papel).orElseThrow());
        a.setUnidade(ubs);
        a.setConcedidoEm(Instant.now());
        atribuicaoRepository.save(a);
    }

    private MockHttpServletRequestBuilder como(String cpf, MockHttpServletRequestBuilder r) {
        return r.header("Authorization", "Bearer " + jwtService.gerarToken(cpf, PREFIXO + cpf, false));
    }

    @Test
    void gestorCadastraNaSuaUnidadeEnfermeiroSoLe() throws Exception {
        String naUnidade = "{\"codigo\": \"%s1\", \"nome\": \"Distrito\", \"tipo\": \"DISTRITO_SANITARIO\", \"unidadeGestaoId\": \"%s\"}"
                .formatted(CODIGO, ubs.getUuid());
        String naRede = "{\"codigo\": \"%s2\", \"nome\": \"Distrito\", \"tipo\": \"DISTRITO_SANITARIO\"}".formatted(CODIGO);
        mockMvc.perform(como(ENF, post("/api/v1/territorio/segmento")).contentType(MediaType.APPLICATION_JSON).content(naUnidade))
                .andExpect(status().isForbidden());
        // O gestor é da UBS: sem unidade de gestão, o segmento seria da rede inteira.
        mockMvc.perform(como(GESTOR, post("/api/v1/territorio/segmento")).contentType(MediaType.APPLICATION_JSON).content(naRede))
                .andExpect(status().isForbidden());
        mockMvc.perform(como(GESTOR, post("/api/v1/territorio/segmento")).contentType(MediaType.APPLICATION_JSON).content(naUnidade))
                .andExpect(status().isCreated());
        mockMvc.perform(como(ENF, get("/api/v1/territorio/arvore"))).andExpect(status().isOk());
        mockMvc.perform(como(ENF, get("/api/v1/territorio/unidades"))).andExpect(status().isOk());
        // Coordenadas da unidade: ORGANIZACAO.GERENCIAR, que o gestor não tem.
        mockMvc.perform(como(GESTOR, org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put(
                        "/api/v1/territorio/unidade/" + ubs.getUuid() + "/coordenadas"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"latitude\": -22.9, \"longitude\": -47.06}"))
                .andExpect(status().isForbidden());
    }
}
