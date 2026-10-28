package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.AtribuicaoAcesso;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.Usuario;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.AtribuicaoAcessoRepository;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
import com.edufelizardo.maissaudepublica.repositories.PapelRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
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
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Imunização com login e autorização ligados (ADR-0147): o técnico de enfermagem registra a dose na própria unidade,
 * não em outra; a recepção não registra; o catálogo é da gestão; a retificação é do autor ou da supervisão.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"app.security.enabled=true", "app.security.authorization.enabled=true"})
class VacinacaoAutorizacaoControllerTest {

    private static final String PREFIXO = "VacinaAut Teste ";
    private static final String TECNICO = "94501867370";
    private static final String OUTRO_TECNICO = "94501867371";
    private static final String RECEPCAO = "94501867372";
    private static final String GESTOR = "94501867373";
    private static final String COORDENADOR = "94501867374";
    private static final List<String> CPFS = List.of(TECNICO, OUTRO_TECNICO, RECEPCAO, GESTOR, COORDENADOR);

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private JwtService jwtService;
    @Autowired private UnidadeDeSaudeRepository unidadeDeSaudeRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private AtribuicaoAcessoRepository atribuicaoRepository;
    @Autowired private PapelRepository papelRepository;
    @Autowired private PacienteRepository pacienteRepository;
    @Autowired private ProfissionalRepository profissionalRepository;

    private UnidadeDeSaude ubs;
    private UnidadeDeSaude outra;
    private Paciente paciente;

    @BeforeEach
    void seed() {
        limpar();
        ubs = unidade("UBS");
        outra = unidade("Outra");
        atribuir(TECNICO, "TECNICO_DE_ENFERMAGEM", ubs);
        atribuir(OUTRO_TECNICO, "TECNICO_DE_ENFERMAGEM", ubs);
        atribuir(RECEPCAO, "RECEPCAO", ubs);
        atribuir(GESTOR, "GESTOR", ubs);
        atribuir(COORDENADOR, "COORDENADOR_DE_ENFERMAGEM", ubs);
        Profissional p = new Profissional();
        p.setMatricula("P-IMUAUT");
        p.setCpf(TECNICO);
        p.setNome(PREFIXO + "Técnico");
        p.setAtivo(true);
        profissionalRepository.save(p);
        paciente = new Paciente();
        paciente.setNome(PREFIXO + "Paciente");
        paciente.setCpf("94501867398");
        paciente.setDataNascimento(LocalDate.now().minusMonths(3));
        paciente.setAtivo(true);
        paciente = pacienteRepository.save(paciente);
    }

    @AfterEach
    void limpar() {
        jdbc.update("update tb_registro_vacinacao set retificacao_de_id = null where paciente_id in (select uuid from tb_paciente where nome like ?)", PREFIXO + "%");
        jdbc.update("delete from tb_registro_vacinacao where paciente_id in (select uuid from tb_paciente where nome like ?)", PREFIXO + "%");
        jdbc.update("delete from tb_paciente where nome like ?", PREFIXO + "%");
        jdbc.update("delete from tb_profissional where matricula = 'P-IMUAUT'");
        jdbc.update("delete from tb_imunobiologico where codigo = 'IMU_AUT'");
        for (String cpf : CPFS) {
            atribuicaoRepository.deleteAll(atribuicaoRepository.findByUsuarioCpf(cpf));
            usuarioRepository.findByCpf(cpf).ifPresent(usuarioRepository::delete);
            jdbc.update("delete from tb_evento_auditoria where usuario_cpf = ?", cpf);
        }
        unidadeDeSaudeRepository.deleteAllById(unidadeDeSaudeRepository.findAll().stream()
                .filter(u -> u.getNome() != null && u.getNome().startsWith(PREFIXO)).map(UnidadeDeSaude::getUuid).toList());
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

    private String dose(UnidadeDeSaude u, String extra) {
        String vip = jdbc.queryForObject("select cast(uuid as varchar) from tb_imunobiologico where codigo = 'VIP'", String.class);
        return ("{\"pacienteId\": \"%s\", \"imunobiologicoId\": \"%s\", \"dose\": \"PRIMEIRA\", \"unidadeId\": \"%s\", \"origem\": \"APLICADA\", "
                + "\"profissionalMatricula\": \"P-IMUAUT\", \"lote\": \"L1\"%s}").formatted(paciente.getUuid(), vip, u.getUuid(), extra);
    }

    @Test
    void registroNaUnidadeDoAcessoECatalogoDaGestao() throws Exception {
        mockMvc.perform(post("/api/v1/vacinacao").contentType(MediaType.APPLICATION_JSON).content(dose(ubs, ""))).andExpect(status().isUnauthorized());
        mockMvc.perform(como(RECEPCAO, post("/api/v1/vacinacao")).contentType(MediaType.APPLICATION_JSON).content(dose(ubs, "")))
                .andExpect(status().isForbidden());
        mockMvc.perform(como(TECNICO, post("/api/v1/vacinacao")).contentType(MediaType.APPLICATION_JSON).content(dose(outra, "")))
                .andExpect(status().isForbidden());
        String r = mockMvc.perform(como(TECNICO, post("/api/v1/vacinacao")).contentType(MediaType.APPLICATION_JSON).content(dose(ubs, "")))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String id = JsonPath.read(r, "$.uuid");

        mockMvc.perform(como(TECNICO, get("/api/v1/paciente/" + paciente.getUuid() + "/vacinacao"))).andExpect(status().isOk());
        mockMvc.perform(como(TECNICO, get("/api/v1/vacinacao/dia?unidadeId=" + ubs.getUuid()))).andExpect(status().isOk());
        mockMvc.perform(como(TECNICO, get("/api/v1/vacinacao/dia?unidadeId=" + outra.getUuid()))).andExpect(status().isForbidden());
        mockMvc.perform(como(RECEPCAO, get("/api/v1/vacinacao/atrasados?unidadeId=" + ubs.getUuid()))).andExpect(status().isForbidden());

        // Retificação: o outro técnico não (nem é o autor, nem supervisiona); a coordenação sim.
        String retificacao = dose(ubs, ", \"motivoRetificacao\": \"Lote errado\"").replace("\"L1\"", "\"L2\"");
        mockMvc.perform(como(OUTRO_TECNICO, post("/api/v1/vacinacao/" + id + "/retificacao")).contentType(MediaType.APPLICATION_JSON).content(retificacao))
                .andExpect(status().isForbidden());
        mockMvc.perform(como(COORDENADOR, post("/api/v1/vacinacao/" + id + "/retificacao")).contentType(MediaType.APPLICATION_JSON).content(retificacao))
                .andExpect(status().isCreated());

        // O catálogo: lido por qualquer logado, mantido pela gestão.
        String novo = "{\"codigo\": \"IMU_AUT\", \"nome\": \"Teste\", \"sigla\": \"T\", \"tipo\": \"VACINA\", \"via\": \"ORAL\", \"dosesPorFrasco\": 1}";
        mockMvc.perform(como(RECEPCAO, get("/api/v1/calendario-vacinal"))).andExpect(status().isOk());
        mockMvc.perform(como(TECNICO, post("/api/v1/imunobiologico")).contentType(MediaType.APPLICATION_JSON).content(novo)).andExpect(status().isForbidden());
        mockMvc.perform(como(GESTOR, post("/api/v1/imunobiologico")).contentType(MediaType.APPLICATION_JSON).content(novo)).andExpect(status().isCreated());
    }
}
