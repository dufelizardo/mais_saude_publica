package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.AtribuicaoAcesso;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.Usuario;
import com.edufelizardo.maissaudepublica.repositories.AtribuicaoAcessoRepository;
import com.edufelizardo.maissaudepublica.repositories.PapelRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.UsuarioRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import java.time.Instant;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes do fluxo de login e do gate de segurança (ADR-0055). {@code app.security.enabled=true} é
 * ligado só nesta classe via {@code @TestPropertySource} — o Spring Test context-cache trata isso
 * como um contexto separado, então o resto da suíte continua rodando com o toggle desligado
 * (default de {@code application-test.properties}), sem nenhum impacto.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = "app.security.enabled=true")
class AuthControllerTest {

    private static final String LOGIN_URL = "/api/v1/auth/login";
    private static final String STATUS_URL = "/api/v1/auth/status";
    // CPF com dígito verificador válido, exclusivo desta suíte.
    private static final String CPF_TESTE = "52998224725";
    private static final String MATRICULA_TESTE = "AUTHTEST-0001";
    private static final String SENHA_TESTE = "SenhaForte123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PapelRepository papelRepository;

    @Autowired
    private AtribuicaoAcessoRepository atribuicaoAcessoRepository;

    @BeforeEach
    void seed() {
        limpar();

        Usuario usuario = new Usuario(CPF_TESTE, "Usuario Teste Auth", passwordEncoder.encode(SENHA_TESTE));
        usuarioRepository.save(usuario);

        Profissional profissional = new Profissional();
        profissional.setMatricula(MATRICULA_TESTE);
        profissional.setCpf(CPF_TESTE);
        profissional.setNome("Profissional Teste Auth");
        profissional.setAtivo(true);
        profissionalRepository.save(profissional);
    }

    @AfterEach
    void limpar() {
        atribuicaoAcessoRepository.deleteAll(atribuicaoAcessoRepository.findByUsuarioCpf(CPF_TESTE));
        usuarioRepository.findByCpf(CPF_TESTE).ifPresent(usuarioRepository::delete);
        profissionalRepository.findByMatricula(MATRICULA_TESTE).ifPresent(profissionalRepository::delete);
    }

    private String corpoLogin(String tipo, String identificador, String senha) {
        return """
                {
                  "tipo": "%s",
                  "identificador": "%s",
                  "senha": "%s"
                }
                """.formatted(tipo, identificador, senha);
    }

    @Test
    void deveRetornarStatusDoToggleLigado() throws Exception {
        mockMvc.perform(get(STATUS_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.securityEnabled").value(true))
                .andExpect(jsonPath("$.authorizationEnabled").value(false));
    }

    @Test
    void deveLogarComCpf() throws Exception {
        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoLogin("CPF", CPF_TESTE, SENHA_TESTE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.cpf").value(CPF_TESTE));
    }

    @Test
    void deveLogarComMatricula() throws Exception {
        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoLogin("MATRICULA", MATRICULA_TESTE, SENHA_TESTE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cpf").value(CPF_TESTE));
    }

    @Test
    void deveRetornarUnauthorizedComSenhaErrada() throws Exception {
        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoLogin("CPF", CPF_TESTE, "senhaErrada")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveBloquearApos5TentativasErradas() throws Exception {
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post(LOGIN_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(corpoLogin("CPF", CPF_TESTE, "senhaErrada")))
                    .andExpect(status().isUnauthorized());
        }

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoLogin("CPF", CPF_TESTE, SENHA_TESTE)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", containsString("bloqueada")));
    }

    @Test
    void deveNegarAcessoARotaProtegidaSemToken() throws Exception {
        mockMvc.perform(get("/api/v1/medicamento/"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void devePermitirAcessoARotaProtegidaComTokenValido() throws Exception {
        String responseBody = mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoLogin("CPF", CPF_TESTE, SENHA_TESTE)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String token = JsonPath.read(responseBody, "$.token");

        int statusCode = mockMvc.perform(get("/api/v1/medicamento/")
                        .header("Authorization", "Bearer " + token))
                .andReturn().getResponse().getStatus();

        assertThat(statusCode).isNotEqualTo(401);
    }

    @Test
    void deveInformarQuemEstaLogadoComOProfissionalDeMesmoCpf() throws Exception {
        String responseBody = mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoLogin("CPF", CPF_TESTE, SENHA_TESTE)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String token = JsonPath.read(responseBody, "$.token");

        mockMvc.perform(get("/api/v1/auth/eu").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cpf").value(CPF_TESTE))
                .andExpect(jsonPath("$.nome").value("Usuario Teste Auth"))
                .andExpect(jsonPath("$.profissionalMatricula").value(MATRICULA_TESTE))
                .andExpect(jsonPath("$.profissionalNome").value("Profissional Teste Auth"));
    }

    @Test
    void naoDeveDesativarOProprioUsuario() throws Exception {
        String responseBody = mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoLogin("CPF", CPF_TESTE, SENHA_TESTE)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String token = JsonPath.read(responseBody, "$.token");
        String id = usuarioRepository.findByCpf(CPF_TESTE).orElseThrow().getUuid().toString();

        mockMvc.perform(patch("/api/v1/usuario/" + id).header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "Usuario Teste Auth", "ativo": false}
                                """))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void deveRetornarUnauthorizedAoPerguntarQuemEstaLogadoSemToken() throws Exception {
        mockMvc.perform(get("/api/v1/auth/eu"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveInformarPapeisEPermissoesVigentesDeQuemEstaLogado() throws Exception {
        AtribuicaoAcesso atribuicao = new AtribuicaoAcesso();
        atribuicao.setUsuario(usuarioRepository.findByCpf(CPF_TESTE).orElseThrow());
        atribuicao.setPapel(papelRepository.findByCodigo("TECNICO_DE_ENFERMAGEM").orElseThrow());
        atribuicao.setConcedidoEm(Instant.now());
        atribuicaoAcessoRepository.save(atribuicao);

        String responseBody = mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoLogin("CPF", CPF_TESTE, SENHA_TESTE)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String token = JsonPath.read(responseBody, "$.token");

        mockMvc.perform(get("/api/v1/auth/eu").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.acessos[0].papelCodigo").value("TECNICO_DE_ENFERMAGEM"))
                .andExpect(jsonPath("$.acessos[0].unidadeUuid").doesNotExist())
                .andExpect(jsonPath("$.permissoes", hasItem("MEDICACAO.ADMINISTRAR")))
                .andExpect(jsonPath("$.permissoes", not(hasItem("TRIAGEM.REGISTRAR"))));
    }
}
