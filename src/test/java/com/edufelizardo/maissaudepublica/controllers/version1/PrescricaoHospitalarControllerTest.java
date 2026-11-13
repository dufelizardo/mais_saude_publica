package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Leito;
import com.edufelizardo.maissaudepublica.models.Lote;
import com.edufelizardo.maissaudepublica.models.Medicamento;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.Setor;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.FaixaEtariaLeito;
import com.edufelizardo.maissaudepublica.models.enuns.Sexo;
import com.edufelizardo.maissaudepublica.models.enuns.SexoLeito;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoLeito;
import com.edufelizardo.maissaudepublica.models.enuns.TipoLeito;
import com.edufelizardo.maissaudepublica.models.enuns.TipoSetor;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.LeitoRepository;
import com.edufelizardo.maissaudepublica.repositories.LoteRepository;
import com.edufelizardo.maissaudepublica.repositories.MedicamentoRepository;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.SetorRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prescrição hospitalar, aprazamento e checagem (ADR-0187), com login e autorização desligados. O escopo e a nutricionista ficam em
 * {@link PrescricaoHospitalarAutorizacaoControllerTest}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PrescricaoHospitalarControllerTest {

    private static final String PRESCRICAO = "/api/v1/prescricao-hospitalar/";
    private static final String PREFIXO = "PrescHosp Teste ";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

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
    private MedicamentoRepository medicamentoRepository;

    @Autowired
    private LoteRepository loteRepository;

    private UnidadeDeSaude hospital;
    private Setor clinica;
    private UUID dipirona;
    private UUID lote;
    private int leitos;

    @BeforeEach
    void seed() {
        limpar();
        hospital = new UnidadeDeSaude();
        hospital.setNome(PREFIXO + "Hospital");
        hospital.setTipo(TipoUnidadeDeSaude.HOSPITAL);
        hospital.setAtivo(true);
        hospital = unidadeDeSaudeRepository.save(hospital);
        clinica = setorRepository.save(new Setor(hospital, PREFIXO + "Clínica médica", "PH-CM", TipoSetor.ASSISTENCIAL, true, null));
        for (String[] p : List.of(new String[]{"PH-MED", "47586970813", "Médico"}, new String[]{"PH-ENF", "58697081924", "Enfermeira"})) {
            Profissional prof = new Profissional();
            prof.setMatricula(p[0]);
            prof.setCpf(p[1]);
            prof.setNome(PREFIXO + p[2]);
            prof.setAtivo(true);
            profissionalRepository.save(prof);
        }
        Medicamento m = new Medicamento();
        m.setNome(PREFIXO + "Dipirona");
        m.setApresentacao("Ampola 500 mg/ml");
        m.setAtivo(true);
        m = medicamentoRepository.save(m);
        dipirona = m.getUuid();
        Lote l = new Lote();
        l.setMedicamento(m);
        l.setUnidade(hospital);
        l.setNumeroLote("PH-L1");
        l.setValidade(LocalDate.now().plusYears(1));
        l.setQuantidade(10);
        lote = loteRepository.save(l).getUuid();
        leitos = 0;
    }

    @AfterEach
    void limpar() {
        List<UUID> unidades = unidadeDeSaudeRepository.findAll().stream()
                .filter(u -> u.getNome() != null && u.getNome().startsWith(PREFIXO)).map(UnidadeDeSaude::getUuid).toList();
        for (UUID u : unidades) {
            String internacoes = "select uuid from tb_internacao where unidade_id = ?";
            String prescricoes = "select uuid from tb_prescricao_hospitalar where internacao_id in (" + internacoes + ")";
            String itens = "select uuid from tb_item_prescricao where prescricao_id in (" + prescricoes + ")";
            jdbc.update("update tb_checagem_prescricao set retificacao_de_id = null where item_id in (" + itens + ")", u);
            jdbc.update("delete from tb_checagem_prescricao where item_id in (" + itens + ")", u);
            jdbc.update("delete from tb_horario_prescricao where item_id in (" + itens + ")", u);
            jdbc.update("delete from tb_item_prescricao where prescricao_id in (" + prescricoes + ")", u);
            jdbc.update("delete from tb_prescricao_hospitalar where internacao_id in (" + internacoes + ")", u);
            jdbc.update("delete from tb_movimentacao_farmacia where lote_id in (select uuid from tb_lote where unidade_id = ?)", u);
            jdbc.update("delete from tb_lote where unidade_id = ?", u);
            jdbc.update("delete from tb_evento_leito where leito_id in (select uuid from tb_leito where unidade_id = ?)", u);
            jdbc.update("delete from tb_internacao where unidade_id = ?", u);
            jdbc.update("delete from tb_leito where unidade_id = ?", u);
            jdbc.update("delete from tb_setor where unidade_id = ?", u);
        }
        unidadeDeSaudeRepository.deleteAllById(unidades);
        medicamentoRepository.deleteAll(medicamentoRepository.findAll().stream()
                .filter(m -> m.getNome() != null && m.getNome().startsWith(PREFIXO)).toList());
        pacienteRepository.deleteAll(pacienteRepository.findAll().stream()
                .filter(p -> p.getNome() != null && p.getNome().startsWith(PREFIXO)).toList());
        for (String m : List.of("PH-MED", "PH-ENF")) {
            profissionalRepository.findByMatricula(m).ifPresent(profissionalRepository::delete);
        }
    }

    @Test
    void aprazaPorIntervaloRefeicoesEDoseUnica() throws Exception {
        UUID internacao = internar("Ana");
        Instant inicio = Instant.now().truncatedTo(ChronoUnit.HOURS).plus(Duration.ofHours(1));
        mockMvc.perform(post(PRESCRICAO).contentType(MediaType.APPLICATION_JSON).content(prescricao(internacao, inicio, "")))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post(PRESCRICAO).contentType(MediaType.APPLICATION_JSON).content(prescricao(internacao, inicio,
                "{\"tipo\": \"MEDICAMENTO\", \"medicamentoId\": \"%s\", \"via\": \"INTRAVENOSA\"}".formatted(dipirona)))).andExpect(status().isBadRequest());
        mockMvc.perform(post(PRESCRICAO).contentType(MediaType.APPLICATION_JSON).content(prescricao(internacao, inicio,
                "{\"tipo\": \"MEDICAMENTO\", \"medicamentoId\": \"%s\", \"dose\": \"1 g\", \"via\": \"INTRAVENOSA\", \"seNecessario\": true}".formatted(dipirona))))
                .andExpect(status().isBadRequest());

        String itens = String.join(", ",
                "{\"tipo\": \"MEDICAMENTO\", \"medicamentoId\": \"%s\", \"dose\": \"1 g\", \"via\": \"INTRAVENOSA\", \"intervaloHoras\": 6}".formatted(dipirona),
                "{\"tipo\": \"MEDICAMENTO\", \"medicamentoId\": \"%s\", \"dose\": \"1 g\", \"via\": \"INTRAVENOSA\", \"seNecessario\": true, \"condicao\": \"febre acima de 37,8\"}".formatted(dipirona),
                "{\"tipo\": \"MEDICAMENTO\", \"medicamentoId\": \"%s\", \"dose\": \"2 g\", \"via\": \"INTRAVENOSA\"}".formatted(dipirona),
                "{\"tipo\": \"DIETA\", \"tipoDieta\": \"BRANDA\"}",
                "{\"tipo\": \"DIETA\", \"tipoDieta\": \"ZERO\", \"intervaloHoras\": 2}",
                "{\"tipo\": \"CUIDADO\", \"descricao\": \"Sinais vitais\", \"intervaloHoras\": 4}",
                "{\"tipo\": \"CUIDADO\", \"descricao\": \"Decúbito elevado a 30 graus\"}");
        mockMvc.perform(post(PRESCRICAO).contentType(MediaType.APPLICATION_JSON).content(prescricao(internacao, inicio, itens)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.vigente").value(false))
                .andExpect(jsonPath("$.itens", hasSize(7)))
                .andExpect(jsonPath("$.itens[0].horarios", hasSize(4)))
                .andExpect(jsonPath("$.itens[0].resumo").value(PREFIXO + "Dipirona 1 g intravenosa de 6/6 h"))
                .andExpect(jsonPath("$.itens[1].horarios", hasSize(0)))
                .andExpect(jsonPath("$.itens[2].horarios", hasSize(1)))
                .andExpect(jsonPath("$.itens[3].horarios", hasSize(6)))
                .andExpect(jsonPath("$.itens[4].horarios", hasSize(0)))
                .andExpect(jsonPath("$.itens[5].horarios", hasSize(6)))
                .andExpect(jsonPath("$.itens[6].horarios", hasSize(0)));
        mockMvc.perform(post(PRESCRICAO).contentType(MediaType.APPLICATION_JSON).content(prescricao(internacao, inicio, itens)))
                .andExpect(status().isConflict());
    }

    @Test
    void novaEncerraAAnteriorERenovarCopiaOsItensAtivos() throws Exception {
        UUID internacao = internar("Bia");
        Instant agora = Instant.now().truncatedTo(ChronoUnit.HOURS);
        UUID primeira = prescrever(internacao, agora, itensBasicos());
        String r = mockMvc.perform(get(PRESCRICAO + primeira)).andReturn().getResponse().getContentAsString();
        UUID cuidado = UUID.fromString(JsonPath.read(r, "$.itens[1].uuid"));
        acao("itens/" + cuidado + "/suspensao", "{\"profissionalMatricula\": \"PH-MED\"}").andExpect(status().isBadRequest());
        acao("itens/" + cuidado + "/suspensao", "{\"profissionalMatricula\": \"PH-MED\", \"motivo\": \"Paciente estável.\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens[1].suspensoEm").isNotEmpty())
                .andExpect(jsonPath("$.itens[1].horarios[*].situacao").value(org.hamcrest.Matchers.everyItem(org.hamcrest.Matchers.is("CANCELADO"))));

        mockMvc.perform(post(PRESCRICAO + primeira + "/renovacao").contentType(MediaType.APPLICATION_JSON).content("{\"medicoMatricula\": \"PH-MED\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.renovadaDe").value(primeira.toString()))
                .andExpect(jsonPath("$.inicio").value(agora.plus(Duration.ofHours(24)).toString()))
                .andExpect(jsonPath("$.itens", hasSize(1)))
                .andExpect(jsonPath("$.itens[0].horarios", hasSize(4)));

        Instant meio = agora.plus(Duration.ofHours(2));
        mockMvc.perform(post(PRESCRICAO).contentType(MediaType.APPLICATION_JSON).content(prescricao(internacao, meio, itensBasicos())))
                .andExpect(status().isConflict());
    }

    @Test
    void novaPrescricaoEncerraAAnteriorNoSeuInicio() throws Exception {
        UUID internacao = internar("Caio");
        Instant agora = Instant.now().truncatedTo(ChronoUnit.HOURS);
        UUID primeira = prescrever(internacao, agora, itensBasicos());
        Instant depois = agora.plus(Duration.ofHours(3));
        prescrever(internacao, depois, itensBasicos());
        mockMvc.perform(get(PRESCRICAO + primeira))
                .andExpect(jsonPath("$.encerradaEm").value(depois.toString()))
                .andExpect(jsonPath("$.itens[0].horarios[0].situacao").value("PENDENTE"))
                .andExpect(jsonPath("$.itens[0].horarios[1].situacao").value("CANCELADO"));
        mockMvc.perform(get(PRESCRICAO + "?internacaoId=" + internacao)).andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void checaComBaixaDoLoteRetificaComEstornoEReapraza() throws Exception {
        UUID internacao = internar("Davi");
        Instant agora = Instant.now().truncatedTo(ChronoUnit.HOURS);
        UUID p = prescrever(internacao, agora, itensBasicos());
        String r = mockMvc.perform(get(PRESCRICAO + p)).andReturn().getResponse().getContentAsString();
        UUID primeiro = UUID.fromString(JsonPath.read(r, "$.itens[0].horarios[0].uuid"));
        UUID segundo = UUID.fromString(JsonPath.read(r, "$.itens[0].horarios[1].uuid"));

        acao("horarios/" + primeiro + "/checagem", "{\"profissionalMatricula\": \"PH-ENF\", \"resultado\": \"NAO_FEITO\"}").andExpect(status().isBadRequest());
        acao("horarios/" + primeiro + "/checagem", "{\"profissionalMatricula\": \"PH-ENF\", \"resultado\": \"FEITO\", \"loteId\": \"%s\", \"quantidade\": 2}".formatted(lote))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens[0].horarios[0].situacao").value("FEITO"))
                .andExpect(jsonPath("$.itens[0].horarios[0].checagem.loteNumero").value("PH-L1"));
        assertThat(loteRepository.findById(lote).orElseThrow().getQuantidade()).isEqualTo(8);
        acao("horarios/" + primeiro + "/checagem", "{\"profissionalMatricula\": \"PH-ENF\", \"resultado\": \"FEITO\"}").andExpect(status().isUnprocessableEntity());

        String depois = mockMvc.perform(get(PRESCRICAO + p)).andReturn().getResponse().getContentAsString();
        UUID checagem = UUID.fromString(JsonPath.read(depois, "$.itens[0].horarios[0].checagem.uuid"));
        acao("checagens/" + checagem + "/retificacao", "{\"profissionalMatricula\": \"PH-ENF\", \"resultado\": \"NAO_FEITO\", \"motivoNaoAdministracao\": \"RECUSA_DO_PACIENTE\"}")
                .andExpect(status().isBadRequest());
        acao("checagens/" + checagem + "/retificacao", """
                {"profissionalMatricula": "PH-ENF", "resultado": "NAO_FEITO", "motivoNaoAdministracao": "RECUSA_DO_PACIENTE", "motivoRetificacao": "Checado no paciente errado."}
                """).andExpect(status().isOk())
                .andExpect(jsonPath("$.itens[0].horarios[0].situacao").value("NAO_FEITO"))
                .andExpect(jsonPath("$.itens[0].checagens", hasSize(2)))
                .andExpect(jsonPath("$.itens[0].checagens[0].retificada").value(true));
        assertThat(loteRepository.findById(lote).orElseThrow().getQuantidade()).isEqualTo(10);

        acao("horarios/" + segundo + "/reaprazamento", "{\"profissionalMatricula\": \"PH-ENF\", \"previstoPara\": \"%s\"}".formatted(agora.plus(Duration.ofHours(7))))
                .andExpect(status().isBadRequest());
        acao("horarios/" + segundo + "/reaprazamento", "{\"profissionalMatricula\": \"PH-ENF\", \"previstoPara\": \"%s\", \"motivo\": \"Exame.\"}"
                .formatted(agora.plus(Duration.ofHours(30)))).andExpect(status().isUnprocessableEntity());
        acao("horarios/" + segundo + "/reaprazamento", "{\"profissionalMatricula\": \"PH-ENF\", \"previstoPara\": \"%s\", \"motivo\": \"Paciente em exame.\"}"
                .formatted(agora.plus(Duration.ofHours(7))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens[0].horarios[1].previstoPara").value(agora.plus(Duration.ofHours(7)).toString()))
                .andExpect(jsonPath("$.itens[0].horarios[1].aprazadoOriginalmente").value(agora.plus(Duration.ofHours(6)).toString()));
    }

    @Test
    void seNecessarioDietaAtrasoEAltaEncerra() throws Exception {
        UUID internacao = internar("Eva");
        Instant agora = Instant.now().truncatedTo(ChronoUnit.HOURS);
        UUID p = prescrever(internacao, agora, String.join(", ",
                "{\"tipo\": \"MEDICAMENTO\", \"medicamentoId\": \"%s\", \"dose\": \"1 g\", \"via\": \"ORAL\", \"seNecessario\": true, \"condicao\": \"dor\"}".formatted(dipirona),
                "{\"tipo\": \"DIETA\", \"tipoDieta\": \"ENTERAL\", \"intervaloHoras\": 3, \"descricao\": \"200 ml\"}",
                "{\"tipo\": \"CUIDADO\", \"descricao\": \"Glicemia capilar\", \"intervaloHoras\": 6}"));
        String r = mockMvc.perform(get(PRESCRICAO + p)).andReturn().getResponse().getContentAsString();
        UUID sn = UUID.fromString(JsonPath.read(r, "$.itens[0].uuid"));
        UUID dieta = UUID.fromString(JsonPath.read(r, "$.itens[1].horarios[0].uuid"));
        UUID cuidado = UUID.fromString(JsonPath.read(r, "$.itens[2].horarios[0].uuid"));

        acao("itens/" + sn + "/checagem", "{\"profissionalMatricula\": \"PH-ENF\", \"resultado\": \"FEITO\"}")
                .andExpect(status().isOk()).andExpect(jsonPath("$.itens[0].checagens", hasSize(1)));
        acao("horarios/" + dieta + "/checagem", "{\"profissionalMatricula\": \"PH-ENF\", \"resultado\": \"FEITO\"}").andExpect(status().isBadRequest());
        acao("horarios/" + dieta + "/checagem", "{\"profissionalMatricula\": \"PH-ENF\", \"resultado\": \"FEITO\", \"aceitacao\": \"PARCIAL\"}")
                .andExpect(status().isOk()).andExpect(jsonPath("$.itens[1].horarios[0].checagem.aceitacao").value("PARCIAL"));

        jdbc.update("update tb_horario_prescricao set previsto_para = ? where uuid = ?", Timestamp.from(Instant.now().minus(Duration.ofHours(2))), cuidado);
        mockMvc.perform(get(PRESCRICAO + "posto?unidadeId=" + hospital.getUuid()))
                .andExpect(jsonPath("$[?(@.horarioId == '%s')].atrasado".formatted(cuidado)).value(org.hamcrest.Matchers.contains(true)));

        mockMvc.perform(post("/api/v1/internacao/" + internacao + "/alta").contentType(MediaType.APPLICATION_JSON).content("""
                {"tipoAlta": "MELHORADO", "sumario": "Alta no teste da prescrição.", "medicoMatricula": "PH-MED"}
                """)).andExpect(status().isOk());
        mockMvc.perform(get(PRESCRICAO + p))
                .andExpect(jsonPath("$.encerradaEm").isNotEmpty())
                .andExpect(jsonPath("$.itens[2].horarios[0].situacao").value("CANCELADO"));
        acao("itens/" + sn + "/checagem", "{\"profissionalMatricula\": \"PH-ENF\", \"resultado\": \"FEITO\"}").andExpect(status().isUnprocessableEntity());
        mockMvc.perform(post(PRESCRICAO).contentType(MediaType.APPLICATION_JSON).content(prescricao(internacao, agora, itensBasicos())))
                .andExpect(status().isUnprocessableEntity());
    }

    // ── Apoio ──────────────────────────────────────────────────────────────────────────────────

    private String itensBasicos() {
        return String.join(", ",
                "{\"tipo\": \"MEDICAMENTO\", \"medicamentoId\": \"%s\", \"dose\": \"1 g\", \"via\": \"INTRAVENOSA\", \"intervaloHoras\": 6}".formatted(dipirona),
                "{\"tipo\": \"CUIDADO\", \"descricao\": \"Sinais vitais\", \"intervaloHoras\": 4}");
    }

    private String prescricao(UUID internacao, Instant inicio, String itens) {
        return """
                {"internacaoId": "%s", "medicoMatricula": "PH-MED", "inicio": "%s", "itens": [%s]}
                """.formatted(internacao, inicio, itens);
    }

    private UUID prescrever(UUID internacao, Instant inicio, String itens) throws Exception {
        String r = mockMvc.perform(post(PRESCRICAO).contentType(MediaType.APPLICATION_JSON).content(prescricao(internacao, inicio, itens)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return UUID.fromString(JsonPath.read(r, "$.uuid"));
    }

    private ResultActions acao(String rota, String corpo) throws Exception {
        return mockMvc.perform(post(PRESCRICAO + rota).contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    private UUID internar(String nome) throws Exception {
        Paciente p = new Paciente();
        p.setNome(PREFIXO + nome);
        p.setSexo(Sexo.FEMININO);
        p.setDataNascimento(LocalDate.of(1970, 1, 1));
        p.setAtivo(true);
        p = pacienteRepository.save(p);
        Leito l = new Leito();
        l.setUnidade(hospital);
        l.setSetor(clinica);
        l.setIdentificacao("PH-" + (++leitos));
        l.setTipo(TipoLeito.CLINICO);
        l.setSexo(SexoLeito.MISTO);
        l.setFaixaEtaria(FaixaEtariaLeito.ADULTO);
        l.setSituacao(SituacaoLeito.LIVRE);
        l.setAtivo(true);
        UUID leito = leitoRepository.save(l).getUuid();
        String corpo = mockMvc.perform(post("/api/v1/internacao/").contentType(MediaType.APPLICATION_JSON).content("""
                {"pacienteId": "%s", "leitoId": "%s", "medicoMatricula": "PH-MED", "cid": "J18.9", "motivo": "Pneumonia.", "carater": "URGENCIA"}
                """.formatted(p.getUuid(), leito))).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return UUID.fromString(JsonPath.read(corpo, "$.uuid"));
    }
}
