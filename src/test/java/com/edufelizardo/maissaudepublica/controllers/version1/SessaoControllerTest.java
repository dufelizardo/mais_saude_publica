package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.AtribuicaoAcesso;
import com.edufelizardo.maissaudepublica.models.Usuario;
import com.edufelizardo.maissaudepublica.repositories.AtribuicaoAcessoRepository;
import com.edufelizardo.maissaudepublica.repositories.PapelRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Sessões encerradas na hora (ADR-0078): o token carrega a versão das sessões do usuário, e o filtro
 * confere usuário ativo e versão a cada requisição. Desativar, redefinir a senha, trocar a própria senha
 * e o encerramento pela administração invalidam os tokens já emitidos.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"app.security.enabled=true", "app.security.authorization.enabled=true"})
class SessaoControllerTest {

    private static final String LOGIN = "/api/v1/auth/login";
    private static final String EU = "/api/v1/auth/eu";
    // CPFs com dígito verificador válido, exclusivos desta suíte.
    private static final String ADMIN = "50621038199";
    private static final String PESSOA = "04966117470";
    private static final String SENHA_ADMIN = "AdminSessao123";
    private static final String SENHA = "PessoaSessao123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PapelRepository papelRepository;

    @Autowired
    private AtribuicaoAcessoRepository atribuicaoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @BeforeEach
    void seed() {
        limpar();
        Usuario admin = usuarioRepository.save(new Usuario(ADMIN, "Admin Sessao", passwordEncoder.encode(SENHA_ADMIN)));
        AtribuicaoAcesso a = new AtribuicaoAcesso();
        a.setUsuario(admin);
        a.setPapel(papelRepository.findByCodigo("ADMINISTRADOR_PLATAFORMA").orElseThrow());
        a.setConcedidoEm(Instant.now());
        atribuicaoRepository.save(a);
        usuarioRepository.save(new Usuario(PESSOA, "Pessoa Sessao", passwordEncoder.encode(SENHA)));
    }

    @AfterEach
    void limpar() {
        for (String cpf : new String[]{ADMIN, PESSOA}) {
            atribuicaoRepository.deleteAll(atribuicaoRepository.findByUsuarioCpf(cpf));
            usuarioRepository.findByCpf(cpf).ifPresent(usuarioRepository::delete);
        }
    }

    // ── Apoio ──────────────────────────────────────────────────────────────────────────────────

    private String tokenDe(String cpf, String senha) throws Exception {
        String body = mockMvc.perform(post(LOGIN).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tipo": "CPF", "identificador": "%s", "senha": "%s"}
                                """.formatted(cpf, senha)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.token");
    }

    private int statusDoEu(String token) throws Exception {
        return mockMvc.perform(get(EU).header("Authorization", "Bearer " + token)).andReturn().getResponse().getStatus();
    }

    private UUID idDa(String cpf) {
        return usuarioRepository.findByCpf(cpf).orElseThrow().getUuid();
    }

    // ── Casos ──────────────────────────────────────────────────────────────────────────────────

    @Test
    void desativarDerrubaASessaoNaHora() throws Exception {
        String token = tokenDe(PESSOA, SENHA);
        assertThat(statusDoEu(token)).isEqualTo(200);

        mockMvc.perform(patch("/api/v1/usuario/" + idDa(PESSOA)).header("Authorization", "Bearer " + tokenDe(ADMIN, SENHA_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "Pessoa Sessao", "ativo": false}
                                """))
                .andExpect(status().isOk());

        assertThat(statusDoEu(token)).isEqualTo(401);
    }

    @Test
    void reativarNaoRessuscitaOTokenAntigo() throws Exception {
        String token = tokenDe(PESSOA, SENHA);
        String admin = tokenDe(ADMIN, SENHA_ADMIN);
        for (boolean ativo : new boolean[]{false, true}) {
            mockMvc.perform(patch("/api/v1/usuario/" + idDa(PESSOA)).header("Authorization", "Bearer " + admin)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"nome": "Pessoa Sessao", "ativo": %s}
                                    """.formatted(ativo)))
                    .andExpect(status().isOk());
        }
        assertThat(statusDoEu(token)).isEqualTo(401);
        assertThat(statusDoEu(tokenDe(PESSOA, SENHA))).isEqualTo(200);
    }

    @Test
    void redefinirASenhaDerrubaASessaoAntiga() throws Exception {
        String token = tokenDe(PESSOA, SENHA);

        mockMvc.perform(post("/api/v1/usuario/" + idDa(PESSOA) + "/redefinicao-senha")
                        .header("Authorization", "Bearer " + tokenDe(ADMIN, SENHA_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"senhaProvisoria": "Provisoria987"}
                                """))
                .andExpect(status().isOk());

        assertThat(statusDoEu(token)).isEqualTo(401);
    }

    @Test
    void trocarAPropriaSenhaDerrubaAsOutrasSessoesEMantemANova() throws Exception {
        String computador = tokenDe(PESSOA, SENHA);
        String celular = tokenDe(PESSOA, SENHA);

        String body = mockMvc.perform(post("/api/v1/auth/senha").header("Authorization", "Bearer " + computador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"senhaAtual": "%s", "novaSenha": "OutraSenha456"}
                                """.formatted(SENHA)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String novo = JsonPath.read(body, "$.token");

        assertThat(statusDoEu(celular)).isEqualTo(401);
        assertThat(statusDoEu(computador)).isEqualTo(401);
        assertThat(statusDoEu(novo)).isEqualTo(200);
    }

    @Test
    void administracaoEncerraAsSessoesSemMudarASenha() throws Exception {
        String token = tokenDe(PESSOA, SENHA);
        String admin = tokenDe(ADMIN, SENHA_ADMIN);

        mockMvc.perform(post("/api/v1/usuario/" + idDa(PESSOA) + "/encerramento-de-sessoes").header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk());

        assertThat(statusDoEu(token)).isEqualTo(401);
        assertThat(statusDoEu(tokenDe(PESSOA, SENHA))).isEqualTo(200);
        // As próprias sessões se encerram trocando a senha.
        mockMvc.perform(post("/api/v1/usuario/" + idDa(ADMIN) + "/encerramento-de-sessoes").header("Authorization", "Bearer " + admin))
                .andExpect(status().isUnprocessableEntity());
        mockMvc.perform(post("/api/v1/usuario/" + UUID.randomUUID() + "/encerramento-de-sessoes").header("Authorization", "Bearer " + admin))
                .andExpect(status().isNotFound());
    }

    @Test
    void encerrarSessoesExigeUsuarioGerenciar() throws Exception {
        mockMvc.perform(post("/api/v1/usuario/" + idDa(ADMIN) + "/encerramento-de-sessoes")
                        .header("Authorization", "Bearer " + tokenDe(PESSOA, SENHA)))
                .andExpect(status().isForbidden());
    }

    @Test
    void tokenSemVersaoEmitidoAntesContinuaValendoEUsuarioInexistenteNao() throws Exception {
        // Token como os emitidos antes da ADR-0078 (sem a versão): vale enquanto ninguém encerrou as sessões.
        assertThat(statusDoEu(jwtService.gerarToken(PESSOA, "Pessoa Sessao", false))).isEqualTo(200);
        assertThat(statusDoEu(jwtService.gerarToken("87368253011", "Ninguem", false))).isEqualTo(401);
    }
}
