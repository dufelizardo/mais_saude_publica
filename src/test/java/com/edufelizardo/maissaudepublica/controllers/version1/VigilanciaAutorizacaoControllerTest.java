package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.AtribuicaoAcesso;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.Usuario;
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

import java.time.Instant;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Vigilância com login e autorização ligados (ADR-0124): o enfermeiro notifica e o gestor não; o agravo sensível só
 * aparece para a vigilância e para quem notificou; investigar é da vigilância.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"app.security.enabled=true", "app.security.authorization.enabled=true"})
class VigilanciaAutorizacaoControllerTest {

    private static final String PREFIXO = "VigAut Teste ";
    private static final String ENF = "94701867360";
    private static final String ENF_OUTRO = "94701867361";
    private static final String VIG = "94701867362";
    private static final String GESTOR = "94701867363";
    private static final List<String> CPFS = List.of(ENF, ENF_OUTRO, VIG, GESTOR);

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
    private PacienteRepository pacienteRepository;

    @Autowired
    private AgravoRepository agravoRepository;

    private UnidadeDeSaude ubs;
    private Paciente paciente;

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
        atribuir(GESTOR, "GESTOR");
        Paciente p = new Paciente();
        p.setNome(PREFIXO + "Paciente");
        p.setCpf("90011122233");
        p.setAtivo(true);
        paciente = pacienteRepository.save(p);
    }

    @AfterEach
    void limpar() {
        for (String cpf : CPFS) {
            atribuicaoRepository.deleteAll(atribuicaoRepository.findByUsuarioCpf(cpf));
            usuarioRepository.findByCpf(cpf).ifPresent(usuarioRepository::delete);
            jdbc.update("delete from tb_evento_auditoria where usuario_cpf = ?", cpf);
        }
        VigilanciaControllerTest.limparDados(jdbc, PREFIXO, "TESTE_VA_");
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
    void agravoSensivelSoParaAVigilanciaEQuemNotificou() throws Exception {
        String hiv = agravoRepository.findByCodigo("HIV_AIDS").orElseThrow().getUuid().toString();
        String corpo = "{\"pacienteId\": \"%s\", \"agravoId\": \"%s\", \"unidadeId\": \"%s\", \"origem\": \"ATENDIMENTO\"}"
                .formatted(paciente.getUuid(), hiv, ubs.getUuid());
        mockMvc.perform(como(GESTOR, post("/api/v1/vigilancia/notificacao")).contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isForbidden());
        String r = mockMvc.perform(como(ENF, post("/api/v1/vigilancia/notificacao")).contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String n = "/api/v1/vigilancia/notificacao/" + JsonPath.read(r, "$.uuid");

        mockMvc.perform(como(ENF, get(n))).andExpect(status().isOk());
        mockMvc.perform(como(ENF_OUTRO, get(n))).andExpect(status().isForbidden());
        mockMvc.perform(como(ENF_OUTRO, get("/api/v1/vigilancia/notificacoes"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.lista.length()").value(0));
        mockMvc.perform(como(VIG, get(n))).andExpect(status().isOk()).andExpect(jsonPath("$.agravoCodigo").value("HIV_AIDS"));
        mockMvc.perform(como(VIG, get("/api/v1/vigilancia/notificacoes"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.lista.length()").value(1));

        // Investigar é da vigilância.
        mockMvc.perform(como(ENF, post(n + "/investigacao")).contentType(MediaType.APPLICATION_JSON).content("{\"exposicao\": \"x\"}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(como(VIG, post(n + "/investigacao")).contentType(MediaType.APPLICATION_JSON).content("{\"exposicao\": \"Relação sem preservativo.\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.situacao").value("EM_INVESTIGACAO"));
        // O catálogo é de quem gerencia: o enfermeiro não cadastra agravo.
        mockMvc.perform(como(ENF, post("/api/v1/vigilancia/agravo")).contentType(MediaType.APPLICATION_JSON)
                .content("{\"codigo\": \"TESTE_VA_X\", \"nome\": \"x\", \"periodicidade\": \"SEMANAL\", \"prazoEncerramentoDias\": 30, \"criterios\": [\"CLINICO\"]}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void mapaSemPontosParaQuemSoNotificaEContatosDaVigilancia() throws Exception {
        mockMvc.perform(como(ENF, get("/api/v1/vigilancia/mapa"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.pontosVisiveis").value(false));
        mockMvc.perform(como(VIG, get("/api/v1/vigilancia/mapa"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.pontosVisiveis").value(true));
        mockMvc.perform(como(GESTOR, get("/api/v1/vigilancia/mapa"))).andExpect(status().isForbidden());

        String sarampo = agravoRepository.findByCodigo("SARAMPO").orElseThrow().getUuid().toString();
        String corpo = "{\"pacienteId\": \"%s\", \"agravoId\": \"%s\", \"unidadeId\": \"%s\", \"origem\": \"ATENDIMENTO\"}"
                .formatted(paciente.getUuid(), sarampo, ubs.getUuid());
        String r = mockMvc.perform(como(ENF, post("/api/v1/vigilancia/notificacao")).contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String contato = "/api/v1/vigilancia/notificacao/" + JsonPath.read(r, "$.uuid") + "/contato";
        String dados = "{\"nome\": \"Vizinho\", \"tipoExposicao\": \"COMUNITARIO\"}";
        mockMvc.perform(como(ENF, post(contato)).contentType(MediaType.APPLICATION_JSON).content(dados)).andExpect(status().isForbidden());
        mockMvc.perform(como(VIG, post(contato)).contentType(MediaType.APPLICATION_JSON).content(dados)).andExpect(status().isCreated());
    }
}
