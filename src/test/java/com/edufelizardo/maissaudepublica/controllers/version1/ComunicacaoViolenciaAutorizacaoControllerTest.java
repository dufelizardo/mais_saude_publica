package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.AtribuicaoAcesso;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.Usuario;
import com.edufelizardo.maissaudepublica.models.enuns.Sexo;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.AgravoRepository;
import com.edufelizardo.maissaudepublica.repositories.AtribuicaoAcessoRepository;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
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
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * A comunicação obrigatória com login e autorização ligados (ADR-0201): quem notificou comunica; outro profissional da unidade não vê a
 * notificação sensível nem comunica; a vigilância vê as pendentes; o enfermeiro não vê a lista.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"app.security.enabled=true", "app.security.authorization.enabled=true"})
class ComunicacaoViolenciaAutorizacaoControllerTest {

    private static final String PREFIXO = "ViolAut Teste ";
    private static final String API = "/api/v1/vigilancia/";
    private static final String ENF = "94944867401";
    private static final String ENF_OUTRO = "94944867402";
    private static final String VIG = "94944867403";
    private static final List<String> CPFS = List.of(ENF, ENF_OUTRO, VIG);

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private JwtService jwtService;
    @Autowired private UnidadeDeSaudeRepository unidadeDeSaudeRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private AtribuicaoAcessoRepository atribuicaoRepository;
    @Autowired private PapelRepository papelRepository;
    @Autowired private PacienteRepository pacienteRepository;
    @Autowired private AgravoRepository agravoRepository;

    private UnidadeDeSaude ubs;
    private Paciente crianca;

    @BeforeEach
    void seed() {
        limpar();
        ubs = new UnidadeDeSaude();
        ubs.setNome(PREFIXO + "UBS");
        ubs.setTipo(TipoUnidadeDeSaude.UBS);
        ubs.setAtivo(true);
        ubs = unidadeDeSaudeRepository.save(ubs);
        atribuir(ENF, "ENFERMEIRO");
        atribuir(ENF_OUTRO, "ENFERMEIRO");
        atribuir(VIG, "VIGILANCIA_EPIDEMIOLOGICA");
        Paciente p = new Paciente();
        p.setNome(PREFIXO + "Criança");
        p.setCpf("90055500001");
        p.setDataNascimento(LocalDate.now().minusYears(9));
        p.setSexo(Sexo.MASCULINO);
        p.setAtivo(true);
        crianca = pacienteRepository.save(p);
    }

    @AfterEach
    void limpar() {
        for (String cpf : CPFS) {
            atribuicaoRepository.deleteAll(atribuicaoRepository.findByUsuarioCpf(cpf));
            usuarioRepository.findByCpf(cpf).ifPresent(usuarioRepository::delete);
            jdbc.update("delete from tb_evento_auditoria where usuario_cpf = ?", cpf);
        }
        ComunicacaoViolenciaControllerTest.limparDados(jdbc, PREFIXO);
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

    private int status(String cpf, MockHttpServletRequestBuilder r) throws Exception {
        return mockMvc.perform(como(cpf, r)).andReturn().getResponse().getStatus();
    }

    @Test
    void quemNotificouComunica() throws Exception {
        String violencia = agravoRepository.findByCodigo("VIOLENCIA_INTERPESSOAL_AUTOPROVOCADA").orElseThrow().getUuid().toString();
        String ct = ComunicacaoViolenciaControllerTest.instituicao(jdbc, PREFIXO, "Conselho Tutelar", "GARANTIA_DE_DIREITOS", "CONSELHO_TUTELAR");
        String corpo = "{\"pacienteId\": \"" + crianca.getUuid() + "\", \"agravoId\": \"" + violencia + "\", \"unidadeId\": \"" + ubs.getUuid()
                + "\", \"origem\": \"ATENDIMENTO\", \"respostas\": {\"tipoViolencia\": \"Física\", \"lesaoAutoprovocada\": false}}";
        String r = mockMvc.perform(como(ENF, post(API + "notificacao")).contentType(MediaType.APPLICATION_JSON).content(corpo)).andReturn().getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        String n = API + "notificacao/" + JsonPath.read(r, "$.uuid") + "/comunicacao";

        // Agravo sensível: só quem notificou e a vigilância.
        assertThat(status(ENF_OUTRO, get(n))).isEqualTo(403);
        assertThat(status(ENF_OUTRO, post(n).contentType(MediaType.APPLICATION_JSON).content("{\"instituicaoId\": \"" + ct + "\"}"))).isEqualTo(403);
        assertThat(status(ENF, get(n))).isEqualTo(200);
        assertThat(status(ENF, get(API + "comunicacoes-pendentes"))).isEqualTo(403);
        r = mockMvc.perform(como(VIG, get(API + "comunicacoes-pendentes"))).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat((List<String>) JsonPath.read(r, "$[*].pacienteNome")).contains(PREFIXO + "Criança");
        assertThat(status(ENF, post(n).contentType(MediaType.APPLICATION_JSON).content("{\"instituicaoId\": \"" + ct + "\"}"))).isEqualTo(201);
        assertThat(mockMvc.perform(get(n)).andReturn().getResponse().getStatus()).isEqualTo(401);
    }
}
