package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.AtribuicaoAcesso;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.Usuario;
import com.edufelizardo.maissaudepublica.models.enuns.Sexo;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
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
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * ESAVI com login e autorização ligados (ADR-0150): o técnico de enfermagem da sala de vacina notifica na própria unidade,
 * não em outra, e não encerra; a recepção não notifica; a vigilância encerra com a causalidade e a conduta.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"app.security.enabled=true", "app.security.authorization.enabled=true"})
class EsaviAutorizacaoControllerTest {

    private static final String PREFIXO = "EsaviAut Teste ";
    private static final String TECNICO = "94701867380";
    private static final String RECEPCAO = "94701867381";
    private static final String VIGILANCIA = "94701867382";
    private static final List<String> CPFS = List.of(TECNICO, RECEPCAO, VIGILANCIA);

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private JwtService jwtService;
    @Autowired private UnidadeDeSaudeRepository unidadeDeSaudeRepository;
    @Autowired private PacienteRepository pacienteRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private AtribuicaoAcessoRepository atribuicaoRepository;
    @Autowired private PapelRepository papelRepository;

    private UnidadeDeSaude ubs;
    private UnidadeDeSaude outra;
    private Paciente crianca;

    @BeforeEach
    void seed() {
        limpar();
        ubs = unidade("UBS");
        outra = unidade("Outra");
        Paciente p = new Paciente();
        p.setNome(PREFIXO + "Criança");
        p.setCpf("90055500001");
        p.setSexo(Sexo.MASCULINO);
        p.setDataNascimento(LocalDate.now().minusMonths(4));
        p.setAtivo(true);
        crianca = pacienteRepository.save(p);
        atribuir(TECNICO, "TECNICO_DE_ENFERMAGEM", ubs);
        atribuir(RECEPCAO, "RECEPCAO", ubs);
        atribuir(VIGILANCIA, "VIGILANCIA_EPIDEMIOLOGICA", ubs);
    }

    @AfterEach
    void limpar() {
        String pacientesSql = "(select uuid from tb_paciente where nome like '" + PREFIXO + "%')";
        String notificacoes = "(select uuid from tb_notificacao where paciente_id in " + pacientesSql + ")";
        jdbc.update("delete from tb_contraindicacao_vacinal where paciente_id in " + pacientesSql);
        jdbc.update("delete from tb_notificacao_dose where notificacao_id in " + notificacoes);
        jdbc.update("delete from tb_notificacao where paciente_id in " + pacientesSql);
        jdbc.update("delete from tb_registro_vacinacao where paciente_id in " + pacientesSql);
        for (String cpf : CPFS) {
            atribuicaoRepository.deleteAll(atribuicaoRepository.findByUsuarioCpf(cpf));
            usuarioRepository.findByCpf(cpf).ifPresent(usuarioRepository::delete);
            jdbc.update("delete from tb_evento_auditoria where usuario_cpf = ?", cpf);
        }
        jdbc.update("delete from tb_paciente where nome like '" + PREFIXO + "%'");
        jdbc.update("delete from tb_unidade_de_saude where nome like '" + PREFIXO + "%'");
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

    private org.springframework.mock.web.MockHttpServletResponse enviar(String cpf, MockHttpServletRequestBuilder r, String corpo) throws Exception {
        return mockMvc.perform(como(cpf, r).contentType(MediaType.APPLICATION_JSON).content(corpo)).andReturn().getResponse();
    }

    private String esavi(UnidadeDeSaude u, String dose) {
        String agravo = jdbc.queryForObject("select cast(uuid as varchar) from tb_agravo where codigo = 'ESAVI'", String.class);
        return ("{\"pacienteId\": \"%s\", \"agravoId\": \"%s\", \"unidadeId\": \"%s\", \"origem\": \"VACINACAO\", \"gravidade\": \"NAO_GRAVE\", "
                + "\"doseIds\": [\"%s\"], \"respostas\": {\"manifestacoes\": [\"Reação local\"]}}").formatted(crianca.getUuid(), agravo, u.getUuid(), dose);
    }

    @Test
    void notificarEEncerrarPorPapelEUnidade() throws Exception {
        String penta = jdbc.queryForObject("select cast(uuid as varchar) from tb_imunobiologico where codigo = 'PENTA'", String.class);
        String transcrita = "{\"pacienteId\": \"%s\", \"imunobiologicoId\": \"%s\", \"dose\": \"PRIMEIRA\", \"unidadeId\": \"%s\", \"origem\": \"TRANSCRITA\", "
                + "\"aplicadaEm\": \"%s\"}";
        var d = enviar(TECNICO, post("/api/v1/vacinacao"), transcrita.formatted(crianca.getUuid(), penta, ubs.getUuid(),
                LocalDate.now().minusDays(60).atTime(LocalTime.of(9, 0))));
        assertThat(d.getStatus()).isEqualTo(201);
        String dose = JsonPath.read(d.getContentAsString(), "$.uuid");

        // Notificar: o técnico na própria unidade; não em outra; a recepção não.
        assertThat(enviar(RECEPCAO, post("/api/v1/vigilancia/notificacao"), esavi(ubs, dose)).getStatus()).isEqualTo(403);
        assertThat(enviar(TECNICO, post("/api/v1/vigilancia/notificacao"), esavi(outra, dose)).getStatus()).isEqualTo(403);
        var n = enviar(TECNICO, post("/api/v1/vigilancia/notificacao"), esavi(ubs, dose));
        assertThat(n.getStatus()).isEqualTo(201);
        String id = JsonPath.read(n.getContentAsString(), "$.uuid");

        // Encerrar é da vigilância.
        String encerramento = "{\"evolucao\": \"CURA\", \"causalidade\": \"C_COINCIDENTE\", \"conduta\": \"MANTER\"}";
        assertThat(enviar(TECNICO, post("/api/v1/vigilancia/notificacao/" + id + "/encerramento"), encerramento).getStatus()).isEqualTo(403);
        var e = enviar(VIGILANCIA, post("/api/v1/vigilancia/notificacao/" + id + "/encerramento"), encerramento);
        assertThat(e.getStatus()).isEqualTo(200);
        assertThat((String) JsonPath.read(e.getContentAsString(), "$.classificacaoFinal")).isEqualTo("DESCARTADO");
    }
}
