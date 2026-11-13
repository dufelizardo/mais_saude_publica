package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.AtribuicaoAcesso;
import com.edufelizardo.maissaudepublica.models.Internacao;
import com.edufelizardo.maissaudepublica.models.Leito;
import com.edufelizardo.maissaudepublica.models.Medicamento;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.Setor;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.Usuario;
import com.edufelizardo.maissaudepublica.models.enuns.CaraterInternacao;
import com.edufelizardo.maissaudepublica.models.enuns.FaixaEtariaLeito;
import com.edufelizardo.maissaudepublica.models.enuns.Sexo;
import com.edufelizardo.maissaudepublica.models.enuns.SexoLeito;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoLeito;
import com.edufelizardo.maissaudepublica.models.enuns.StatusInternacao;
import com.edufelizardo.maissaudepublica.models.enuns.TipoLeito;
import com.edufelizardo.maissaudepublica.models.enuns.TipoSetor;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.AtribuicaoAcessoRepository;
import com.edufelizardo.maissaudepublica.repositories.InternacaoRepository;
import com.edufelizardo.maissaudepublica.repositories.LeitoRepository;
import com.edufelizardo.maissaudepublica.repositories.MedicamentoRepository;
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
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Escopo da prescrição hospitalar (ADR-0187), com a segurança ligada: o médico do hospital prescreve; o técnico de enfermagem checa o
 * medicamento mas não prescreve; a nutricionista checa só a dieta e só vê a dieta no posto; a enfermeira de outro hospital recebe 403.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"app.security.enabled=true", "app.security.authorization.enabled=true"})
class PrescricaoHospitalarAutorizacaoControllerTest {

    private static final String PREFIXO = "PrescHospAut Teste ";
    private static final String MEDICO = "69708192035";
    private static final String TECNICO = "70819203146";
    private static final String NUTRICIONISTA = "81920314257";
    private static final String ENFERMEIRA_OUTRA = "92031425368";
    private static final List<String> CPFS = List.of(MEDICO, TECNICO, NUTRICIONISTA, ENFERMEIRA_OUTRA);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PapelRepository papelRepository;

    @Autowired
    private AtribuicaoAcessoRepository atribuicaoRepository;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @Autowired
    private SetorRepository setorRepository;

    @Autowired
    private LeitoRepository leitoRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private InternacaoRepository internacaoRepository;

    @Autowired
    private MedicamentoRepository medicamentoRepository;

    private Internacao internacao;
    private UUID medicamento;

    @BeforeEach
    void seed() {
        limpar();
        UnidadeDeSaude hospital = unidade("Hospital");
        UnidadeDeSaude outro = unidade("Outro hospital");
        usuario(MEDICO, "MEDICO", hospital);
        usuario(TECNICO, "TECNICO_DE_ENFERMAGEM", hospital);
        usuario(NUTRICIONISTA, "NUTRICIONISTA", hospital);
        usuario(ENFERMEIRA_OUTRA, "ENFERMEIRO", outro);
        Profissional medico = profissional("PHA-MED", MEDICO);
        profissional("PHA-TEC", TECNICO);
        profissional("PHA-NUT", NUTRICIONISTA);
        Paciente paciente = new Paciente();
        paciente.setNome(PREFIXO + "Paciente");
        paciente.setSexo(Sexo.MASCULINO);
        paciente.setDataNascimento(LocalDate.of(1960, 2, 2));
        paciente.setAtivo(true);
        paciente = pacienteRepository.save(paciente);
        Setor setor = setorRepository.save(new Setor(hospital, PREFIXO + "Clínica", "PHA-CM", TipoSetor.ASSISTENCIAL, true, null));
        Leito l = new Leito();
        l.setUnidade(hospital);
        l.setSetor(setor);
        l.setIdentificacao("PHA-01");
        l.setTipo(TipoLeito.CLINICO);
        l.setSexo(SexoLeito.MISTO);
        l.setFaixaEtaria(FaixaEtariaLeito.ADULTO);
        l.setSituacao(SituacaoLeito.OCUPADO);
        l.setAtivo(true);
        l = leitoRepository.save(l);
        Internacao i = new Internacao();
        i.setPaciente(paciente);
        i.setUnidade(hospital);
        i.setLeito(l);
        i.setMedicoResponsavel(medico);
        i.setCid("J189");
        i.setMotivo("Pneumonia.");
        i.setCarater(CaraterInternacao.URGENCIA);
        i.setAdmitidaEm(Instant.now());
        i.setStatus(StatusInternacao.INTERNADO);
        internacao = internacaoRepository.save(i);
        Medicamento m = new Medicamento();
        m.setNome(PREFIXO + "Dipirona");
        m.setAtivo(true);
        medicamento = medicamentoRepository.save(m).getUuid();
    }

    @AfterEach
    void limpar() {
        List<UUID> unidades = unidadeDeSaudeRepository.findAll().stream()
                .filter(u -> u.getNome() != null && u.getNome().startsWith(PREFIXO)).map(UnidadeDeSaude::getUuid).toList();
        for (UUID u : unidades) {
            String internacoes = "select uuid from tb_internacao where unidade_id = ?";
            String prescricoes = "select uuid from tb_prescricao_hospitalar where internacao_id in (" + internacoes + ")";
            String itens = "select uuid from tb_item_prescricao where prescricao_id in (" + prescricoes + ")";
            jdbc.update("delete from tb_checagem_prescricao where item_id in (" + itens + ")", u);
            jdbc.update("delete from tb_horario_prescricao where item_id in (" + itens + ")", u);
            jdbc.update("delete from tb_item_prescricao where prescricao_id in (" + prescricoes + ")", u);
            jdbc.update("delete from tb_prescricao_hospitalar where internacao_id in (" + internacoes + ")", u);
            jdbc.update("delete from tb_internacao where unidade_id = ?", u);
            jdbc.update("delete from tb_leito where unidade_id = ?", u);
            jdbc.update("delete from tb_setor where unidade_id = ?", u);
        }
        for (String cpf : CPFS) {
            atribuicaoRepository.deleteAll(atribuicaoRepository.findByUsuarioCpf(cpf));
            usuarioRepository.findByCpf(cpf).ifPresent(usuarioRepository::delete);
        }
        for (String m : List.of("PHA-MED", "PHA-TEC", "PHA-NUT")) {
            profissionalRepository.findByMatricula(m).ifPresent(profissionalRepository::delete);
        }
        medicamentoRepository.deleteAll(medicamentoRepository.findAll().stream()
                .filter(m -> m.getNome() != null && m.getNome().startsWith(PREFIXO)).toList());
        pacienteRepository.deleteAll(pacienteRepository.findAll().stream()
                .filter(p -> p.getNome() != null && p.getNome().startsWith(PREFIXO)).toList());
        unidadeDeSaudeRepository.deleteAllById(unidades);
    }

    @Test
    void medicoPrescreveTecnicoChecaENutricionistaSoADieta() throws Exception {
        Instant inicio = Instant.now().truncatedTo(ChronoUnit.HOURS);
        String corpo = """
                {"internacaoId": "%s", "medicoMatricula": "PHA-MED", "inicio": "%s", "itens": [
                  {"tipo": "MEDICAMENTO", "medicamentoId": "%s", "dose": "1 g", "via": "ORAL", "intervaloHoras": 6},
                  {"tipo": "DIETA", "tipoDieta": "ENTERAL", "intervaloHoras": 3}]}
                """.formatted(internacao.getUuid(), inicio, medicamento);
        mockMvc.perform(json(post("/api/v1/prescricao-hospitalar/"), corpo)).andExpect(status().isUnauthorized());
        mockMvc.perform(como(TECNICO, json(post("/api/v1/prescricao-hospitalar/"), corpo))).andExpect(status().isForbidden());
        String r = mockMvc.perform(como(MEDICO, json(post("/api/v1/prescricao-hospitalar/"), corpo)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String remedio = JsonPath.read(r, "$.itens[0].horarios[0].uuid");
        String dieta = JsonPath.read(r, "$.itens[1].horarios[0].uuid");

        String feitoDieta = "{\"profissionalMatricula\": \"PHA-NUT\", \"resultado\": \"FEITO\", \"aceitacao\": \"TOTAL\"}";
        String feitoRemedio = "{\"profissionalMatricula\": \"PHA-NUT\", \"resultado\": \"FEITO\"}";
        mockMvc.perform(como(NUTRICIONISTA, json(post("/api/v1/prescricao-hospitalar/horarios/" + remedio + "/checagem"), feitoRemedio)))
                .andExpect(status().isForbidden());
        mockMvc.perform(como(NUTRICIONISTA, json(post("/api/v1/prescricao-hospitalar/horarios/" + dieta + "/checagem"), feitoDieta)))
                .andExpect(status().isOk());
        mockMvc.perform(como(NUTRICIONISTA, get("/api/v1/prescricao-hospitalar/posto")))
                .andExpect(status().isOk()).andExpect(jsonPath("$[*].tipo").value(everyItem(is("DIETA"))));
        mockMvc.perform(como(ENFERMEIRA_OUTRA, json(post("/api/v1/prescricao-hospitalar/horarios/" + remedio + "/checagem"),
                "{\"profissionalMatricula\": \"PHA-TEC\", \"resultado\": \"FEITO\"}"))).andExpect(status().isForbidden());
        mockMvc.perform(como(TECNICO, json(post("/api/v1/prescricao-hospitalar/horarios/" + remedio + "/checagem"),
                "{\"profissionalMatricula\": \"PHA-TEC\", \"resultado\": \"FEITO\"}"))).andExpect(status().isOk());
        mockMvc.perform(como(ENFERMEIRA_OUTRA, get("/api/v1/prescricao-hospitalar/?internacaoId=" + internacao.getUuid()))).andExpect(status().isForbidden());
    }

    private UnidadeDeSaude unidade(String nome) {
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + nome);
        u.setTipo(TipoUnidadeDeSaude.HOSPITAL);
        u.setAtivo(true);
        return unidadeDeSaudeRepository.save(u);
    }

    private Profissional profissional(String matricula, String cpf) {
        Profissional p = new Profissional();
        p.setMatricula(matricula);
        p.setCpf(cpf);
        p.setNome(PREFIXO + matricula);
        p.setAtivo(true);
        return profissionalRepository.save(p);
    }

    private void usuario(String cpf, String papel, UnidadeDeSaude unidade) {
        Usuario usuario = usuarioRepository.findByCpf(cpf).orElseGet(() -> usuarioRepository.save(new Usuario(cpf, PREFIXO + cpf, "hash-irrelevante")));
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
}
