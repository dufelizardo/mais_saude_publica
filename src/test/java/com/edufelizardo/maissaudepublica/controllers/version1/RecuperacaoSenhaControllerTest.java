package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.EventoAuditoria;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.RedefinicaoSenha;
import com.edufelizardo.maissaudepublica.models.Usuario;
import com.edufelizardo.maissaudepublica.models.enuns.AcaoAuditoria;
import com.edufelizardo.maissaudepublica.models.enuns.ResultadoAuditoria;
import com.edufelizardo.maissaudepublica.repositories.EventoAuditoriaRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.RedefinicaoSenhaRepository;
import com.edufelizardo.maissaudepublica.repositories.UsuarioRepository;
import com.edufelizardo.maissaudepublica.services.version1.EnvioDeEmail;
import com.edufelizardo.maissaudepublica.services.version1.RecuperacaoSenhaService;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Recuperação de senha por e-mail (ADR-0081), com o login ligado e o envio de e-mail simulado: o link vai para o
 * e-mail do profissional de mesmo CPF, vale uma vez e por 30 minutos, e a troca encerra as sessões abertas.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"app.security.enabled=true",
        "app.security.recuperacao-senha.url-frontend=http://frontend.teste"})
class RecuperacaoSenhaControllerTest {

    // CPFs com dígito verificador válido, exclusivos desta suíte.
    private static final String COM_EMAIL = "88791937400";
    private static final String SEM_EMAIL = "16579384093";
    private static final String SENHA = "SenhaAntiga123";
    private static final String EMAIL = "pessoa.recuperacao@prefeitura.teste";
    private static final String MATRICULA = "RECSENHA-0001";
    private static final Pattern TOKEN = Pattern.compile("redefinir-senha\\?token=([A-Za-z0-9_-]+)");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EnvioDeEmail envioDeEmail;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private RedefinicaoSenhaRepository redefinicaoRepository;

    @Autowired
    private EventoAuditoriaRepository auditoriaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void seed() {
        limpar();
        when(envioDeEmail.disponivel()).thenReturn(true);
        when(envioDeEmail.enviar(anyString(), anyString(), anyString())).thenReturn(true);
        usuarioRepository.save(new Usuario(COM_EMAIL, "Pessoa Recuperacao", passwordEncoder.encode(SENHA)));
        usuarioRepository.save(new Usuario(SEM_EMAIL, "Pessoa Sem Email", passwordEncoder.encode(SENHA)));
        Profissional p = new Profissional();
        p.setMatricula(MATRICULA);
        p.setCpf(COM_EMAIL);
        p.setNome("Profissional Recuperacao");
        p.setEmail(EMAIL);
        p.setAtivo(true);
        profissionalRepository.save(p);
    }

    @AfterEach
    void limpar() {
        for (String cpf : new String[]{COM_EMAIL, SEM_EMAIL}) {
            usuarioRepository.findByCpf(cpf).ifPresent(u -> {
                redefinicaoRepository.deleteAll(redefinicaoRepository.findByUsuario_Uuid(u.getUuid()));
                usuarioRepository.delete(u);
            });
        }
        profissionalRepository.findByMatricula(MATRICULA).ifPresent(profissionalRepository::delete);
    }

    // ── Apoio ──────────────────────────────────────────────────────────────────────────────────

    private ResultActions pedir(String cpf) throws Exception {
        return mockMvc.perform(post("/api/v1/auth/senha/recuperacao").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"cpf": "%s"}
                        """.formatted(cpf)));
    }

    private ResultActions redefinir(String token, String novaSenha) throws Exception {
        return mockMvc.perform(post("/api/v1/auth/senha/redefinicao").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"token": "%s", "novaSenha": "%s"}
                        """.formatted(token, novaSenha)));
    }

    /** O token do último e-mail enviado. */
    private String tokenDoUltimoEmail(int totalDeEnvios) {
        ArgumentCaptor<String> texto = ArgumentCaptor.forClass(String.class);
        verify(envioDeEmail, times(totalDeEnvios)).enviar(eq(EMAIL), anyString(), texto.capture());
        Matcher m = TOKEN.matcher(texto.getValue());
        assertThat(m.find()).isTrue();
        assertThat(texto.getValue()).startsWith("Olá, Pessoa Recuperacao.").contains("http://frontend.teste/redefinir-senha?token=");
        return m.group(1);
    }

    private String tokenDeLogin(String senha) throws Exception {
        String body = mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tipo": "CPF", "identificador": "%s", "senha": "%s"}
                                """.formatted(COM_EMAIL, senha)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.token");
    }

    // ── Casos ──────────────────────────────────────────────────────────────────────────────────

    @Test
    void statusInformaQueARecuperacaoEstaDisponivel() throws Exception {
        mockMvc.perform(get("/api/v1/auth/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recuperacaoDeSenha").value(true));
    }

    @Test
    void linkPorEmailTrocaASenhaUmaVezEEncerraAsSessoes() throws Exception {
        String sessaoAntiga = tokenDeLogin(SENHA);

        pedir("887.919.374-00")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(RecuperacaoSenhaService.MENSAGEM_PEDIDO));
        String token = tokenDoUltimoEmail(1);

        redefinir(token, "SenhaNova456").andExpect(status().isOk());

        // A senha nova entra; a antiga não; a sessão aberta antes caiu (ADR-0078).
        tokenDeLogin("SenhaNova456");
        mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tipo": "CPF", "identificador": "%s", "senha": "%s"}
                                """.formatted(COM_EMAIL, SENHA)))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/auth/eu").header("Authorization", "Bearer " + sessaoAntiga))
                .andExpect(status().isUnauthorized());

        // O link vale uma vez só.
        redefinir(token, "OutraSenha789").andExpect(status().isUnprocessableEntity());
    }

    @Test
    void respostaEAMesmaComOuSemCadastroESoEnviaQuandoHaEmail() throws Exception {
        pedir("87368253011").andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(RecuperacaoSenhaService.MENSAGEM_PEDIDO));
        pedir(SEM_EMAIL).andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(RecuperacaoSenhaService.MENSAGEM_PEDIDO));
        verify(envioDeEmail, never()).enviar(anyString(), anyString(), anyString());
    }

    @Test
    void pedidoNovoInvalidaOAnteriorEHaLimitePorHora() throws Exception {
        pedir(COM_EMAIL).andExpect(status().isOk());
        String primeiro = tokenDoUltimoEmail(1);
        pedir(COM_EMAIL).andExpect(status().isOk());
        String segundo = tokenDoUltimoEmail(2);

        redefinir(primeiro, "SenhaNova456").andExpect(status().isUnprocessableEntity());

        pedir(COM_EMAIL).andExpect(status().isOk());
        pedir(COM_EMAIL).andExpect(status().isOk());
        // 3 por hora: o quarto não envia.
        verify(envioDeEmail, times(3)).enviar(eq(EMAIL), anyString(), anyString());
        assertThat(segundo).isNotEqualTo(primeiro);
    }

    @Test
    void linkVencidoNaoValeESenhaIgualAoCpfNaoEntra() throws Exception {
        Usuario usuario = usuarioRepository.findByCpf(COM_EMAIL).orElseThrow();
        RedefinicaoSenha vencido = new RedefinicaoSenha();
        vencido.setUsuario(usuario);
        vencido.setTokenHash(RecuperacaoSenhaService.hash("token-vencido-de-teste"));
        vencido.setCriadaEm(Instant.now().minus(Duration.ofHours(2)));
        vencido.setExpiraEm(Instant.now().minus(Duration.ofHours(1)));
        redefinicaoRepository.save(vencido);
        redefinir("token-vencido-de-teste", "SenhaNova456").andExpect(status().isUnprocessableEntity());
        redefinir("token-que-nunca-existiu", "SenhaNova456").andExpect(status().isUnprocessableEntity());

        pedir(COM_EMAIL).andExpect(status().isOk());
        String token = tokenDoUltimoEmail(1);
        redefinir(token, "887.919.374-00").andExpect(status().isBadRequest());
        redefinir(token, "curta").andExpect(status().isBadRequest());
    }

    @Test
    void pedidoEntraNaTrilhaSemOToken() throws Exception {
        Instant inicio = Instant.now().minus(1, ChronoUnit.SECONDS);
        pedir(COM_EMAIL).andExpect(status().isOk());
        String token = tokenDoUltimoEmail(1);

        List<EventoAuditoria> eventos = auditoriaRepository
                .findByUsuarioCpfAndOcorridoEmGreaterThanEqualOrderByOcorridoEmAsc(COM_EMAIL, inicio);
        EventoAuditoria pedido = eventos.stream().filter(e -> e.getAcao() == AcaoAuditoria.RECUPERACAO_DE_SENHA)
                .findFirst().orElseThrow();
        assertThat(pedido.getResultado()).isEqualTo(ResultadoAuditoria.PERMITIDO);
        assertThat(pedido.getDetalhe()).contains("p***@prefeitura.teste").doesNotContain(token).doesNotContain(EMAIL);
    }

    @Test
    void semSmtpARecuperacaoFicaIndisponivel() throws Exception {
        when(envioDeEmail.disponivel()).thenReturn(false);
        mockMvc.perform(get("/api/v1/auth/status")).andExpect(jsonPath("$.recuperacaoDeSenha").value(false));
        pedir(COM_EMAIL).andExpect(status().isUnprocessableEntity());
    }
}
