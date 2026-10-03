package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.AtribuicaoAcesso;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.Setor;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.Usuario;
import com.edufelizardo.maissaudepublica.models.enuns.Sexo;
import com.edufelizardo.maissaudepublica.models.enuns.TipoSetor;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.AtribuicaoAcessoRepository;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
import com.edufelizardo.maissaudepublica.repositories.PapelRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.SetorRepository;
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
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Leitos e internação com login e autorização ligados (ADR-0098): o enfermeiro interna mas não dá alta; o médico dá alta;
 * a recepção vê o mapa e não interna; o gestor de leitos de outro hospital não vê nem mexe.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"app.security.enabled=true", "app.security.authorization.enabled=true"})
class InternacaoAutorizacaoControllerTest {

    private static final String LEITO = "/api/v1/leito/";
    private static final String INTERNACAO = "/api/v1/internacao/";
    private static final String PREFIXO = "IntAut Teste ";
    private static final String MEDICO = "92301847560";
    private static final String ENFERMEIRO = "92301847561";
    private static final String RECEPCAO = "92301847562";
    private static final String NIR_OUTRO = "92301847563";
    private static final List<String> CPFS = List.of(MEDICO, ENFERMEIRO, RECEPCAO, NIR_OUTRO);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @Autowired
    private SetorRepository setorRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AtribuicaoAcessoRepository atribuicaoRepository;

    @Autowired
    private PapelRepository papelRepository;

    private UnidadeDeSaude hospital;
    private Setor enfermaria;
    private Paciente paciente;

    @BeforeEach
    void seed() {
        limpar();
        hospital = unidade("Hospital");
        UnidadeDeSaude outro = unidade("Outro hospital");
        enfermaria = setorRepository.save(new Setor(hospital, PREFIXO + "Clínica", "IA-CL", TipoSetor.ASSISTENCIAL, true, null));
        atribuir(MEDICO, "MEDICO", hospital);
        atribuir(ENFERMEIRO, "ENFERMEIRO", hospital);
        atribuir(RECEPCAO, "RECEPCAO", hospital);
        atribuir(NIR_OUTRO, "GESTOR_DE_LEITOS", outro);
        for (String cpf : List.of(MEDICO, ENFERMEIRO)) {
            Profissional p = new Profissional();
            p.setMatricula("IA-" + cpf.substring(7));
            p.setCpf(cpf);
            p.setNome(PREFIXO + cpf);
            p.setAtivo(true);
            profissionalRepository.save(p);
        }
        paciente = new Paciente();
        paciente.setNome(PREFIXO + "Paciente");
        paciente.setSexo(Sexo.FEMININO);
        paciente.setAtivo(true);
        paciente = pacienteRepository.save(paciente);
    }

    @AfterEach
    void limpar() {
        List<UUID> unidades = unidadeDeSaudeRepository.findAll().stream()
                .filter(u -> u.getNome() != null && u.getNome().startsWith(PREFIXO)).map(UnidadeDeSaude::getUuid).toList();
        for (UUID u : unidades) {
            jdbc.update("delete from tb_evento_leito where leito_id in (select uuid from tb_leito where unidade_id = ?)", u);
            jdbc.update("delete from tb_internacao where unidade_id = ?", u);
            jdbc.update("delete from tb_leito where unidade_id = ?", u);
            jdbc.update("delete from tb_setor where unidade_id = ?", u);
        }
        for (String cpf : CPFS) {
            atribuicaoRepository.deleteAll(atribuicaoRepository.findByUsuarioCpf(cpf));
            usuarioRepository.findByCpf(cpf).ifPresent(usuarioRepository::delete);
            jdbc.update("delete from tb_evento_auditoria where usuario_cpf = ?", cpf);
        }
        unidadeDeSaudeRepository.deleteAllById(unidades);
        pacienteRepository.deleteAll(pacienteRepository.findAll().stream()
                .filter(p -> p.getNome() != null && p.getNome().startsWith(PREFIXO)).toList());
        profissionalRepository.deleteAll(profissionalRepository.findAll().stream()
                .filter(p -> p.getMatricula() != null && p.getMatricula().startsWith("IA-")).toList());
    }

    private UnidadeDeSaude unidade(String nome) {
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + nome);
        u.setTipo(TipoUnidadeDeSaude.HOSPITAL);
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

    private MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder r, String corpo) {
        return r.contentType(MediaType.APPLICATION_JSON).content(corpo);
    }

    @Test
    void enfermeiroInternaMedicoDaAltaRecepcaoSoVeEOutroHospitalNaoAlcanca() throws Exception {
        String leitoJson = mockMvc.perform(como(ENFERMEIRO, json(post(LEITO), """
                        {"unidadeId": "%s", "setorId": "%s", "identificacao": "Leito 01", "tipo": "OBSTETRICO", "sexo": "FEMININO"}
                        """.formatted(hospital.getUuid(), enfermaria.getUuid()))))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        UUID leito = UUID.fromString(JsonPath.read(leitoJson, "$.uuid"));

        String pedido = """
                {"pacienteId": "%s", "leitoId": "%s", "medicoMatricula": "IA-%s", "cid": "O80", "motivo": "Trabalho de parto.", "carater": "URGENCIA"}
                """.formatted(paciente.getUuid(), leito, MEDICO.substring(7));
        mockMvc.perform(como(RECEPCAO, json(post(INTERNACAO), pedido))).andExpect(status().isForbidden());
        String internacaoJson = mockMvc.perform(como(ENFERMEIRO, json(post(INTERNACAO), pedido)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        UUID internacao = UUID.fromString(JsonPath.read(internacaoJson, "$.uuid"));

        mockMvc.perform(como(RECEPCAO, get(LEITO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.uuid == '%s')].pacienteNome".formatted(leito)).value(PREFIXO + "Paciente"));
        mockMvc.perform(como(NIR_OUTRO, get(LEITO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.uuid == '%s')]".formatted(leito)).isEmpty());
        mockMvc.perform(como(NIR_OUTRO, json(post(LEITO + leito + "/bloqueio"), "{\"motivo\": \"Teste\"}"))).andExpect(status().isForbidden());
        mockMvc.perform(como(NIR_OUTRO, get(INTERNACAO + internacao))).andExpect(status().isForbidden());

        String alta = "{\"tipoAlta\": \"MELHORADO\", \"sumario\": \"Parto normal sem intercorrências.\", \"medicoMatricula\": \"IA-%s\"}";
        mockMvc.perform(como(ENFERMEIRO, json(post(INTERNACAO + internacao + "/alta"), alta.formatted(ENFERMEIRO.substring(7)))))
                .andExpect(status().isForbidden());
        mockMvc.perform(como(MEDICO, json(post(INTERNACAO + internacao + "/alta"), alta.formatted(MEDICO.substring(7)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ALTA"));
        mockMvc.perform(como(ENFERMEIRO, post(LEITO + leito + "/liberacao"))).andExpect(status().isOk());
    }
}
