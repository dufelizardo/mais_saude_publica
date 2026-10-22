package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.AtribuicaoAcesso;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.Usuario;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.AtribuicaoAcessoRepository;
import com.edufelizardo.maissaudepublica.repositories.PapelRepository;
import com.edufelizardo.maissaudepublica.repositories.PerfilOperacionalRepository;
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
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Modelo operacional com login e autorização ligados (ADR-0117): quem tem ORGANIZACAO.GERENCIAR na unidade inclui; o gestor,
 * com ADMINISTRATIVO.CONSULTAR, só lê; o gestor de outra unidade nem lê.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"app.security.enabled=true", "app.security.authorization.enabled=true"})
class ModeloOperacionalAutorizacaoControllerTest {

    private static final String PREFIXO = "ModeloOpAut Teste ";
    private static final String ADMIN_A = "94501867340";
    private static final String GESTOR_A = "94501867341";
    private static final String GESTOR_B = "94501867342";
    private static final List<String> CPFS = List.of(ADMIN_A, GESTOR_A, GESTOR_B);

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

    @Autowired
    private PerfilOperacionalRepository perfilRepository;

    private UnidadeDeSaude ubsA;

    @BeforeEach
    void seed() {
        limpar();
        ubsA = unidade("UBS A");
        UnidadeDeSaude ubsB = unidade("UBS B");
        atribuir(ADMIN_A, "ADMINISTRADOR_PLATAFORMA", ubsA);
        atribuir(GESTOR_A, "GESTOR", ubsA);
        atribuir(GESTOR_B, "GESTOR", ubsB);
    }

    @AfterEach
    void limpar() {
        List<UUID> unidades = unidadeDeSaudeRepository.findAll().stream()
                .filter(u -> u.getNome() != null && u.getNome().startsWith(PREFIXO)).map(UnidadeDeSaude::getUuid).toList();
        for (UUID u : unidades) {
            jdbc.update("delete from tb_unidade_perfil_operacional where unidade_id = ?", u);
        }
        for (String cpf : CPFS) {
            atribuicaoRepository.deleteAll(atribuicaoRepository.findByUsuarioCpf(cpf));
            usuarioRepository.findByCpf(cpf).ifPresent(usuarioRepository::delete);
            jdbc.update("delete from tb_evento_auditoria where usuario_cpf = ?", cpf);
        }
        unidadeDeSaudeRepository.deleteAllById(unidades);
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

    @Test
    void quemGerenciaAOrganizacaoIncluiGestorSoLeOutroGestorNaoAlcanca() throws Exception {
        String url = "/api/v1/unidade-saude/id/" + ubsA.getUuid() + "/modelo-operacional";
        String corpo = "{\"perfilId\": \"%s\"}".formatted(perfilRepository.findByCodigo("ATENCAO_PRIMARIA").orElseThrow().getUuid());

        mockMvc.perform(como(GESTOR_A, post(url + "/perfil")).contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isForbidden());
        mockMvc.perform(como(ADMIN_A, post(url + "/perfil")).contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isOk());
        mockMvc.perform(como(GESTOR_A, get(url))).andExpect(status().isOk());
        mockMvc.perform(como(GESTOR_B, get(url))).andExpect(status().isForbidden());
        mockMvc.perform(como(GESTOR_A, post("/api/v1/perfil-operacional/")).contentType(MediaType.APPLICATION_JSON)
                .content("{\"codigo\": \"TESTE_MO_AUT\", \"nome\": \"Sem permissão\"}")).andExpect(status().isForbidden());
    }
}
