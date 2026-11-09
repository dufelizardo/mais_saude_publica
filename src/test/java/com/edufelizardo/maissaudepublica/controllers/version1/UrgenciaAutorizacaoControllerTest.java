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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Regulação de urgência com a segurança ligada (ADR-0176): o atendente abre só na central dele e não regula; o médico regulador
 * regula; o gestor vê o andamento sem dado clínico; a recepção do destino recebe e vê a queixa, a de outra unidade não; a
 * enfermagem aciona o SAMU pela crise.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"app.security.enabled=true", "app.security.authorization.enabled=true"})
class UrgenciaAutorizacaoControllerTest {

    private static final String PREFIXO = "UrgenciaAut Teste ";
    private static final String URL = "/api/v1/urgencia/";
    private static final String TARM = "94701867700";
    private static final String TARM_OUTRA = "94701867701";
    private static final String REGULADOR = "94701867702";
    private static final String GESTOR = "94701867703";
    private static final String RECEPCAO_HOSPITAL = "94701867704";
    private static final String RECEPCAO_UBS = "94701867705";
    private static final String ENFERMEIRO_UBS = "94701867706";
    private static final List<String> CPFS = List.of(TARM, TARM_OUTRA, REGULADOR, GESTOR, RECEPCAO_HOSPITAL, RECEPCAO_UBS, ENFERMEIRO_UBS);

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private JwtService jwtService;
    @Autowired private UnidadeDeSaudeRepository unidadeRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private AtribuicaoAcessoRepository atribuicaoRepository;
    @Autowired private PapelRepository papelRepository;
    @Autowired private PacienteRepository pacienteRepository;
    @Autowired private ProfissionalRepository profissionalRepository;

    private UnidadeDeSaude central;
    private UnidadeDeSaude outraCentral;
    private UnidadeDeSaude hospital;
    private UnidadeDeSaude ubs;
    private Paciente paciente;

    @BeforeEach
    void seed() {
        limpar();
        UnidadeDeSaude municipio = unidade("Município", TipoUnidadeDeSaude.MUNICIPAL, null);
        central = unidade("Central", TipoUnidadeDeSaude.UPA, municipio);
        outraCentral = unidade("Outra central", TipoUnidadeDeSaude.UPA, null);
        hospital = unidade("Hospital", TipoUnidadeDeSaude.HOSPITAL, municipio);
        ubs = unidade("UBS", TipoUnidadeDeSaude.UBS, municipio);
        for (UnidadeDeSaude c : List.of(central, outraCentral)) {
            jdbc.update("insert into tb_unidade_capacidade_assistencial (uuid, unidade_id, capacidade_assistencial_id, inicio) select gen_random_uuid(), ?, uuid, ? "
                    + "from tb_capacidade_assistencial where codigo = 'REGULACAO_MEDICA_URGENCIA'", c.getUuid(), LocalDate.now().minusDays(1));
        }
        jdbc.update("insert into tb_unidade_perfil_operacional (uuid, unidade_id, perfil_operacional_id, inicio) select gen_random_uuid(), ?, uuid, ? "
                + "from tb_perfil_operacional where codigo = 'URGENCIA'", hospital.getUuid(), LocalDate.now().minusDays(1));
        Paciente p = new Paciente();
        p.setNome(PREFIXO + "Paciente");
        p.setCpf("90076200001");
        p.setAtivo(true);
        paciente = pacienteRepository.save(p);
        Profissional r = new Profissional();
        r.setMatricula("URA-REGULADOR");
        r.setCpf(REGULADOR);
        r.setNome(PREFIXO + "Regulador");
        r.setAtivo(true);
        profissionalRepository.save(r);
        atribuir(TARM, "ATENDENTE_REGULACAO_URGENCIA", central);
        atribuir(TARM_OUTRA, "ATENDENTE_REGULACAO_URGENCIA", outraCentral);
        atribuir(REGULADOR, "MEDICO_REGULADOR_URGENCIA", central);
        atribuir(GESTOR, "GESTOR", municipio);
        atribuir(RECEPCAO_HOSPITAL, "RECEPCAO", hospital);
        atribuir(RECEPCAO_UBS, "RECEPCAO", ubs);
        atribuir(ENFERMEIRO_UBS, "ENFERMEIRO", ubs);
    }

    @AfterEach
    void limpar() {
        String unidades = "(select uuid from tb_unidade_de_saude where nome like '" + PREFIXO + "%')";
        jdbc.update("delete from tb_evento_urgencia where ocorrencia_id in (select uuid from tb_ocorrencia_urgencia where central_id in " + unidades + ")");
        jdbc.update("delete from tb_ocorrencia_urgencia where central_id in " + unidades);
        jdbc.update("delete from tb_etapa_crise where crise_id in (select uuid from tb_evento_crise where unidade_id in " + unidades + ")");
        jdbc.update("delete from tb_evento_crise where unidade_id in " + unidades);
        for (String cpf : CPFS) {
            atribuicaoRepository.deleteAll(atribuicaoRepository.findByUsuarioCpf(cpf));
            usuarioRepository.findByCpf(cpf).ifPresent(usuarioRepository::delete);
            jdbc.update("delete from tb_evento_auditoria where usuario_cpf = ?", cpf);
        }
        jdbc.update("delete from tb_unidade_capacidade_assistencial where unidade_id in " + unidades);
        jdbc.update("delete from tb_unidade_perfil_operacional where unidade_id in " + unidades);
        jdbc.update("delete from tb_paciente where nome like ?", PREFIXO + "%");
        jdbc.update("delete from tb_profissional where matricula like 'URA-%'");
        jdbc.update("update tb_unidade_de_saude set unidade_superior_id = null where nome like ?", PREFIXO + "%");
        jdbc.update("delete from tb_unidade_de_saude where nome like ?", PREFIXO + "%");
    }

    private UnidadeDeSaude unidade(String nome, TipoUnidadeDeSaude tipo, UnidadeDeSaude superior) {
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + nome);
        u.setTipo(tipo);
        u.setAtivo(true);
        u.setUnidadeSuperior(superior);
        return unidadeRepository.save(u);
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

    private String chamado(UnidadeDeSaude c) {
        return ("{\"centralId\": \"%s\", \"origem\": \"CHAMADO_192\", \"solicitanteTelefone\": \"11988887777\", "
                + "\"queixa\": \"Dor torácica há 30 minutos.\"}").formatted(c.getUuid());
    }

    @Test
    void cadaPapelNoSeuLugar() throws Exception {
        mockMvc.perform(get(URL + "painel")).andExpect(status().isUnauthorized());
        mockMvc.perform(como(RECEPCAO_UBS, get(URL + "painel"))).andExpect(status().isForbidden());
        mockMvc.perform(como(TARM_OUTRA, json(post(URL + "ocorrencia"), chamado(central)))).andExpect(status().isForbidden());
        String id = JsonPath.read(mockMvc.perform(como(TARM, json(post(URL + "ocorrencia"), chamado(central)))).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(), "$.uuid");

        String regulacao = "{\"profissionalMatricula\": \"URA-REGULADOR\", \"prioridade\": \"VERMELHO\", \"decisao\": \"ENCAMINHAMENTO\", "
                + "\"conduta\": \"Ir à emergência do hospital.\", \"destinoId\": \"" + hospital.getUuid() + "\"}";
        mockMvc.perform(como(TARM, json(post(URL + "ocorrencia/" + id + "/regulacao"), regulacao))).andExpect(status().isForbidden());
        mockMvc.perform(como(REGULADOR, json(post(URL + "ocorrencia/" + id + "/regulacao"), regulacao))).andExpect(status().isOk());

        String gestor = mockMvc.perform(como(GESTOR, get(URL + "painel?centralId=" + central.getUuid()))).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat((List<String>) JsonPath.read(gestor, "$.abertas[*].uuid")).containsExactly(id);
        assertThat((List<Object>) JsonPath.read(gestor, "$.abertas[*].queixa")).containsOnlyNulls();
        assertThat((List<Boolean>) JsonPath.read(gestor, "$.abertas[*].clinico")).containsExactly(false);
        mockMvc.perform(como(TARM_OUTRA, get(URL + "ocorrencia/" + id))).andExpect(status().isForbidden());

        String noHospital = mockMvc.perform(como(RECEPCAO_HOSPITAL, get(URL + "ocorrencia?destinoId=" + hospital.getUuid()))).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat((List<String>) JsonPath.read(noHospital, "$[*].queixa")).containsExactly("Dor torácica há 30 minutos.");
        mockMvc.perform(como(RECEPCAO_UBS, json(post(URL + "ocorrencia/" + id + "/recebimento"), "{}"))).andExpect(status().isForbidden());
        mockMvc.perform(como(RECEPCAO_HOSPITAL, json(post(URL + "ocorrencia/" + id + "/paciente"), "{\"pacienteId\": \"" + paciente.getUuid() + "\"}")))
                .andExpect(status().isOk());
        mockMvc.perform(como(RECEPCAO_HOSPITAL, json(post(URL + "ocorrencia/" + id + "/recebimento"), "{}"))).andExpect(status().isOk());
    }

    @Test
    void enfermagemAcionaOSamuPelaCrise() throws Exception {
        String crise = JsonPath.read(mockMvc.perform(como(ENFERMEIRO_UBS, json(post("/api/v1/saude-mental/crise"),
                ("{\"pacienteId\": \"%s\", \"unidadeId\": \"%s\", \"tipo\": \"TENTATIVA_DE_SUICIDIO\", \"descricao\": \"Ingestão de medicamentos.\"}")
                        .formatted(paciente.getUuid(), ubs.getUuid())))).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString(), "$.uuid");
        mockMvc.perform(como(ENFERMEIRO_UBS, get(URL + "centrais"))).andExpect(status().isOk());
        String acionar = "{\"centralId\": \"" + central.getUuid() + "\", \"motivo\": \"Ingestão de medicamentos há 1 hora; sonolenta.\"}";
        mockMvc.perform(como(RECEPCAO_UBS, json(post("/api/v1/saude-mental/crise/" + crise + "/acionamento-samu"), acionar))).andExpect(status().isForbidden());
        mockMvc.perform(como(ENFERMEIRO_UBS, json(post("/api/v1/saude-mental/crise/" + crise + "/acionamento-samu"), acionar))).andExpect(status().isCreated());
        String painel = mockMvc.perform(como(TARM, get(URL + "painel"))).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat((List<String>) JsonPath.read(painel, "$.abertas[*].origem")).containsExactly("CRISE_SAUDE_MENTAL");
    }
}
