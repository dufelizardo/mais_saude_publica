package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.AtribuicaoAcesso;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.Usuario;
import com.edufelizardo.maissaudepublica.repositories.AtribuicaoAcessoRepository;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * As pessoas na emergência com a segurança ligada (ADR-0193): a coordenação (coordenador de enfermagem) vê os vulneráveis e registra o
 * deslocado; a recepção, que consulta paciente mas não coordena, não vê a lista nem registra, e lê o aviso do prontuário; o almoxarife
 * não lê nada disso.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"app.security.enabled=true", "app.security.authorization.enabled=true"})
class PessoasEmergenciaAutorizacaoControllerTest {

    private static final String PREFIXO = "PessEmergAut Teste ";
    private static final String GESTOR = "94901867730";
    private static final String COORD = "94901867731";
    private static final String RECEPCAO = "94901867732";
    private static final String ALMOX = "94901867733";
    private static final List<String> CPFS = List.of(GESTOR, COORD, RECEPCAO, ALMOX);

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private JwtService jwtService;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private AtribuicaoAcessoRepository atribuicaoRepository;
    @Autowired private PapelRepository papelRepository;
    @Autowired private PacienteRepository pacienteRepository;

    private Paciente paciente;

    @BeforeEach
    void seed() {
        limpar();
        atribuir(GESTOR, "GESTOR");
        atribuir(COORD, "COORDENADOR_DE_ENFERMAGEM");
        atribuir(RECEPCAO, "RECEPCAO");
        atribuir(ALMOX, "ALMOXARIFE");
        Paciente p = new Paciente();
        p.setNome(PREFIXO + "Paciente");
        p.setCpf("90199800001");
        p.setDataNascimento(LocalDate.now().minusYears(70));
        p.setAtivo(true);
        paciente = pacienteRepository.save(p);
    }

    @AfterEach
    void limpar() {
        String emergencias = "(select e.uuid from tb_emergencia e join tb_evento_emergencial v on v.uuid = e.evento_id where v.descricao like '" + PREFIXO + "%')";
        jdbc.update("delete from tb_paciente_deslocado where emergencia_id in " + emergencias);
        jdbc.update("delete from tb_mudanca_nivel_emergencia where emergencia_id in " + emergencias);
        jdbc.update("delete from tb_emergencia where uuid in " + emergencias);
        jdbc.update("delete from tb_evento_emergencial where descricao like '" + PREFIXO + "%'");
        for (String cpf : CPFS) {
            atribuicaoRepository.deleteAll(atribuicaoRepository.findByUsuarioCpf(cpf));
            usuarioRepository.findByCpf(cpf).ifPresent(usuarioRepository::delete);
            jdbc.update("delete from tb_evento_auditoria where usuario_cpf = ?", cpf);
        }
        jdbc.update("delete from tb_paciente where nome like ?", PREFIXO + "%");
    }

    private void atribuir(String cpf, String papel) {
        Usuario usuario = usuarioRepository.save(new Usuario(cpf, PREFIXO + cpf, "hash-irrelevante"));
        AtribuicaoAcesso a = new AtribuicaoAcesso();
        a.setUsuario(usuario);
        a.setPapel(papelRepository.findByCodigo(papel).orElseThrow());
        a.setConcedidoEm(Instant.now());
        atribuicaoRepository.save(a);
    }

    private MockHttpServletRequestBuilder como(String cpf, MockHttpServletRequestBuilder r) {
        return r.header("Authorization", "Bearer " + jwtService.gerarToken(cpf, PREFIXO + cpf, false)).contentType(MediaType.APPLICATION_JSON);
    }

    @Test
    void cadaPapelNoSeuLugar() throws Exception {
        String tipo = jdbc.queryForObject("select cast(uuid as varchar) from tb_tipo_emergencia where codigo = 'INUNDACAO'", String.class);
        String nivel = jdbc.queryForObject("select cast(uuid as varchar) from tb_nivel_emergencia where ordem = 1", String.class);
        String evento = JsonPath.read(mockMvc.perform(como(GESTOR, post("/api/v1/evento-emergencial")).content("{\"tipoId\": \"" + tipo
                + "\", \"fonte\": \"DEFESA_CIVIL\", \"descricao\": \"" + PREFIXO + "Cheia\"}")).andExpect(status().isCreated()).andReturn().getResponse()
                .getContentAsString(StandardCharsets.UTF_8), "$.uuid");
        String em = JsonPath.read(mockMvc.perform(como(GESTOR, post("/api/v1/evento-emergencial/" + evento + "/emergencia")).content(
                "{\"nome\": \"Cheia Aut\", \"nivelId\": \"" + nivel + "\", \"justificativa\": \"J\"}")).andExpect(status().isCreated()).andReturn()
                .getResponse().getContentAsString(StandardCharsets.UTF_8), "$.uuid");
        String deslocado = "{\"pacienteId\": \"" + paciente.getUuid() + "\", \"destinoTipo\": \"OUTRO\", \"destinoDescricao\": \"Escola Municipal\"}";

        // A coordenação vê a lista e registra; a recepção (só paciente) e o almoxarife, não.
        mockMvc.perform(como(COORD, get("/api/v1/emergencia/" + em + "/vulneraveis"))).andExpect(status().isOk());
        mockMvc.perform(como(RECEPCAO, get("/api/v1/emergencia/" + em + "/vulneraveis"))).andExpect(status().isForbidden());
        mockMvc.perform(como(ALMOX, get("/api/v1/emergencia/" + em + "/deslocados"))).andExpect(status().isForbidden());
        mockMvc.perform(como(RECEPCAO, post("/api/v1/emergencia/" + em + "/deslocado")).content(deslocado)).andExpect(status().isForbidden());
        mockMvc.perform(como(COORD, post("/api/v1/emergencia/" + em + "/deslocado")).content(deslocado)).andExpect(status().isCreated());

        // O aviso do prontuário: quem consulta paciente lê; o almoxarife, não.
        mockMvc.perform(como(RECEPCAO, get("/api/v1/emergencias/deslocamentos?pacienteId=" + paciente.getUuid()))).andExpect(status().isOk());
        mockMvc.perform(como(ALMOX, get("/api/v1/emergencias/deslocamentos?pacienteId=" + paciente.getUuid()))).andExpect(status().isForbidden());
    }
}
