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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

/**
 * Saúde do Trabalhador com login e autorização ligados (ADR-0207): o enfermeiro notifica o acidente com o bloco do trabalho, vê o
 * painel e registra o acompanhamento, mas não corrige o bloco; a vigilância da unidade corrige; a lista dos acidentes dos servidores
 * é só da vigilância com acesso à rede inteira.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"app.security.enabled=true", "app.security.authorization.enabled=true"})
class SaudeTrabalhadorAutorizacaoControllerTest {

    private static final String PREFIXO = "TrabAut Teste ";
    private static final String API = "/api/v1/vigilancia/";
    private static final String ENF = "94944867411";
    private static final String VIG = "94944867412";
    private static final String VIG_REDE = "94944867413";
    private static final List<String> CPFS = List.of(ENF, VIG, VIG_REDE);

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
    private Paciente trabalhadora;

    @BeforeEach
    void seed() {
        limpar();
        ubs = new UnidadeDeSaude();
        ubs.setNome(PREFIXO + "UBS");
        ubs.setTipo(TipoUnidadeDeSaude.UBS);
        ubs.setAtivo(true);
        ubs = unidadeDeSaudeRepository.save(ubs);
        atribuir(ENF, "ENFERMEIRO", ubs);
        atribuir(VIG, "VIGILANCIA_EPIDEMIOLOGICA", ubs);
        atribuir(VIG_REDE, "VIGILANCIA_EPIDEMIOLOGICA", null);
        Paciente p = new Paciente();
        p.setNome(PREFIXO + "Trabalhadora");
        p.setCpf("90077700001");
        p.setDataNascimento(LocalDate.now().minusYears(30));
        p.setSexo(Sexo.FEMININO);
        p.setAtivo(true);
        trabalhadora = pacienteRepository.save(p);
    }

    @AfterEach
    void limpar() {
        for (String cpf : CPFS) {
            atribuicaoRepository.deleteAll(atribuicaoRepository.findByUsuarioCpf(cpf));
            usuarioRepository.findByCpf(cpf).ifPresent(usuarioRepository::delete);
            jdbc.update("delete from tb_evento_auditoria where usuario_cpf = ?", cpf);
        }
        VigilanciaControllerTest.limparDados(jdbc, PREFIXO, "TSTV5A");
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

    private int status(String cpf, MockHttpServletRequestBuilder r, String corpo) throws Exception {
        if (corpo != null) {
            r = r.contentType(MediaType.APPLICATION_JSON).content(corpo);
        }
        return mockMvc.perform(como(cpf, r)).andReturn().getResponse().getStatus();
    }

    @Test
    void enfermeiroNotificaEAVigilanciaCorrige() throws Exception {
        String agravo = agravoRepository.findByCodigo("ACIDENTE_TRABALHO_MATERIAL_BIOLOGICO").orElseThrow().getUuid().toString();
        LocalDate acidente = LocalDate.now().minusDays(10);
        String bloco = "{\"ocupacaoCbo\": \"322205\", \"ocupacaoDescricao\": \"Técnica de enfermagem\", \"situacaoTrabalho\": \"EMPREGADO_REGISTRADO\"}";
        String corpo = "{\"pacienteId\": \"" + trabalhadora.getUuid() + "\", \"agravoId\": \"" + agravo + "\", \"unidadeId\": \"" + ubs.getUuid()
                + "\", \"origem\": \"ATENDIMENTO\", \"dataPrimeirosSintomas\": \"" + acidente + "\", \"respostas\": {\"tipoExposicao\": [\"Mucosa (oral ou ocular)\"], "
                + "\"materialOrganico\": \"Sangue\", \"vacinadoHepatiteB\": true, \"fonteConhecida\": true}, \"trabalho\": " + bloco + "}";
        var resposta = mockMvc.perform(como(ENF, post(API + "notificacao")).contentType(MediaType.APPLICATION_JSON).content(corpo)).andReturn().getResponse();
        assertThat(resposta.getStatus()).isEqualTo(201);
        String n = JsonPath.read(resposta.getContentAsString(StandardCharsets.UTF_8), "$.uuid");

        // O enfermeiro vê o painel e registra o acompanhamento, mas não corrige o bloco.
        assertThat(status(ENF, get(API + "saude-trabalhador/painel"), null)).isEqualTo(200);
        assertThat(status(ENF, post(API + "notificacao/" + n + "/trabalho/acompanhamento"),
                "{\"etapa\": \"SEIS_SEMANAS\", \"realizadoEm\": \"" + LocalDate.now() + "\"}")).isEqualTo(200);
        assertThat(status(ENF, put(API + "notificacao/" + n + "/trabalho"), bloco)).isEqualTo(403);
        assertThat(status(VIG, put(API + "notificacao/" + n + "/trabalho"), bloco)).isEqualTo(200);

        // Os acidentes dos servidores: só a vigilância da rede inteira.
        assertThat(status(ENF, get(API + "saude-trabalhador/acidentes-sem-notificacao"), null)).isEqualTo(403);
        assertThat(status(VIG, get(API + "saude-trabalhador/acidentes-sem-notificacao"), null)).isEqualTo(403);
        assertThat(status(VIG_REDE, get(API + "saude-trabalhador/acidentes-sem-notificacao"), null)).isEqualTo(200);
        assertThat(mockMvc.perform(get(API + "saude-trabalhador/painel")).andReturn().getResponse().getStatus()).isEqualTo(401);
    }
}
