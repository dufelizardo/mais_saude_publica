package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Leito;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.Setor;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.Sexo;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoLeito;
import com.edufelizardo.maissaudepublica.models.enuns.TipoSetor;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.EventoAuditoriaRepository;
import com.edufelizardo.maissaudepublica.repositories.LeitoRepository;
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

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Leitos e internação (ADR-0098): cadastro de leito, admissão com as travas (leito livre, sexo, uma internação por
 * paciente), troca de leito, alta, higienização, bloqueio, mapa, indicadores e leitura auditada.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LeitoInternacaoControllerTest {

    private static final String LEITO = "/api/v1/leito/";
    private static final String INTERNACAO = "/api/v1/internacao/";
    private static final String PREFIXO = "Leito Teste ";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @Autowired
    private SetorRepository setorRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private LeitoRepository leitoRepository;

    @Autowired
    private EventoAuditoriaRepository auditoriaRepository;

    private UnidadeDeSaude hospital;
    private UnidadeDeSaude outra;
    private Setor enfermaria;
    private Paciente joao;
    private Paciente maria;

    @BeforeEach
    void seed() {
        limpar();
        hospital = unidade("Hospital");
        outra = unidade("Outro hospital");
        enfermaria = setorRepository.save(new Setor(hospital, PREFIXO + "Clínica médica", "LT-CM", TipoSetor.ASSISTENCIAL, true, null));
        setorRepository.save(new Setor(hospital, PREFIXO + "Faturamento", "LT-FAT", TipoSetor.ADMINISTRATIVO, true, null));
        setorRepository.save(new Setor(outra, PREFIXO + "Clínica do outro", "LT-OUT", TipoSetor.ASSISTENCIAL, true, null));
        Profissional medico = new Profissional();
        medico.setMatricula("LT-MED");
        medico.setCpf("31847296055");
        medico.setNome(PREFIXO + "Médica");
        medico.setAtivo(true);
        profissionalRepository.save(medico);
        joao = paciente("João", Sexo.MASCULINO);
        maria = paciente("Maria", Sexo.FEMININO);
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
        unidadeDeSaudeRepository.deleteAllById(unidades);
        pacienteRepository.deleteAll(pacienteRepository.findAll().stream()
                .filter(p -> p.getNome() != null && p.getNome().startsWith(PREFIXO)).toList());
        profissionalRepository.findByMatricula("LT-MED").ifPresent(profissionalRepository::delete);
    }

    private UnidadeDeSaude unidade(String nome) {
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + nome);
        u.setTipo(TipoUnidadeDeSaude.HOSPITAL);
        u.setAtivo(true);
        return unidadeDeSaudeRepository.save(u);
    }

    private Paciente paciente(String nome, Sexo sexo) {
        Paciente p = new Paciente();
        p.setNome(PREFIXO + nome);
        p.setSexo(sexo);
        p.setAtivo(true);
        return pacienteRepository.save(p);
    }

    private ResultActions criarLeito(UUID unidade, UUID setor, String identificacao, String sexo) throws Exception {
        return mockMvc.perform(post(LEITO).contentType(MediaType.APPLICATION_JSON).content("""
                {"unidadeId": "%s", "setorId": "%s", "identificacao": "%s", "tipo": "CLINICO", "sexo": "%s"}
                """.formatted(unidade, setor, identificacao, sexo)));
    }

    private UUID leito(String identificacao, String sexo) throws Exception {
        String corpo = criarLeito(hospital.getUuid(), enfermaria.getUuid(), identificacao, sexo)
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return UUID.fromString(JsonPath.read(corpo, "$.uuid"));
    }

    private ResultActions internar(Paciente p, UUID leito) throws Exception {
        return mockMvc.perform(post(INTERNACAO).contentType(MediaType.APPLICATION_JSON).content("""
                {"pacienteId": "%s", "leitoId": "%s", "medicoMatricula": "LT-MED", "cid": "j18.9",
                 "motivo": "Pneumonia com hipoxemia, sem resposta ao tratamento oral.", "carater": "URGENCIA"}
                """.formatted(p.getUuid(), leito)));
    }

    private SituacaoLeito situacao(UUID leito) {
        return leitoRepository.findById(leito).orElseThrow().getSituacao();
    }

    @Test
    void cadastroDeLeitoExigeSetorAssistencialDaUnidadeEIdentificacaoUnica() throws Exception {
        UUID faturamento = setorRepository.findAll().stream().filter(s -> "LT-FAT".equals(s.getCodigo())).findFirst().orElseThrow().getUuid();
        UUID setorOutra = setorRepository.findAll().stream().filter(s -> "LT-OUT".equals(s.getCodigo())).findFirst().orElseThrow().getUuid();
        criarLeito(hospital.getUuid(), enfermaria.getUuid(), "Enf. 2 · Leito 01", "MASCULINO")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.situacao").value("LIVRE"))
                .andExpect(jsonPath("$.setorNome").value(PREFIXO + "Clínica médica"));
        criarLeito(hospital.getUuid(), enfermaria.getUuid(), "enf. 2 · leito 01", "MISTO").andExpect(status().isConflict());
        criarLeito(hospital.getUuid(), faturamento, "Adm 01", "MISTO").andExpect(status().isUnprocessableEntity());
        criarLeito(hospital.getUuid(), setorOutra, "Outro 01", "MISTO").andExpect(status().isBadRequest());
    }

    @Test
    void internacaoTrocaDeLeitoAltaEHigienizacao() throws Exception {
        UUID masculino1 = leito("Enf. 2 · Leito 01", "MASCULINO");
        UUID masculino2 = leito("Enf. 2 · Leito 02", "MASCULINO");
        UUID feminino = leito("Enf. 3 · Leito 01", "FEMININO");

        internar(maria, masculino1).andExpect(status().isUnprocessableEntity());
        String corpo = internar(joao, masculino1)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("INTERNADO"))
                .andExpect(jsonPath("$.cid").value("J189"))
                .andExpect(jsonPath("$.eventos[0].tipo").value("ADMISSAO"))
                .andReturn().getResponse().getContentAsString();
        UUID internacao = UUID.fromString(JsonPath.read(corpo, "$.uuid"));
        assertThat(situacao(masculino1)).isEqualTo(SituacaoLeito.OCUPADO);
        internar(joao, masculino2).andExpect(status().isConflict());
        internar(paciente("Pedro", Sexo.MASCULINO), masculino1).andExpect(status().isUnprocessableEntity());

        mockMvc.perform(post(INTERNACAO + internacao + "/troca-de-leito").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"leitoId\": \"%s\", \"profissionalMatricula\": \"LT-MED\", \"motivo\": \"Isolamento de contato\"}".formatted(feminino)))
                .andExpect(status().isUnprocessableEntity());
        mockMvc.perform(post(INTERNACAO + internacao + "/troca-de-leito").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"leitoId\": \"%s\", \"profissionalMatricula\": \"LT-MED\", \"motivo\": \"Perto do posto de enfermagem\"}".formatted(masculino2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.leitoIdentificacao").value("Enf. 2 · Leito 02"));
        assertThat(situacao(masculino1)).isEqualTo(SituacaoLeito.HIGIENIZACAO);
        assertThat(situacao(masculino2)).isEqualTo(SituacaoLeito.OCUPADO);

        mockMvc.perform(get(LEITO).param("unidadeId", hospital.getUuid().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.uuid == '%s')].pacienteNome".formatted(masculino2)).value(PREFIXO + "João"))
                .andExpect(jsonPath("$[?(@.uuid == '%s')].pacienteNome".formatted(feminino)).value(org.hamcrest.Matchers.contains(org.hamcrest.Matchers.nullValue())));
        mockMvc.perform(get(LEITO + "indicadores").param("unidadeId", hospital.getUuid().toString()))
                .andExpect(jsonPath("$.leitos").value(3))
                .andExpect(jsonPath("$.ocupados").value(1))
                .andExpect(jsonPath("$.higienizacao").value(1))
                .andExpect(jsonPath("$.taxaOcupacao").value(33.3));

        String alta = "{\"tipoAlta\": \"MELHORADO\", \"sumario\": \"%s\", \"medicoMatricula\": \"LT-MED\"%s}";
        mockMvc.perform(post(INTERNACAO + internacao + "/alta").contentType(MediaType.APPLICATION_JSON)
                        .content(alta.formatted("Pneumonia tratada com antibiótico venoso por 7 dias.", ", \"altaEm\": \"2020-01-01T10:00:00Z\"")))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post(INTERNACAO + internacao + "/alta").contentType(MediaType.APPLICATION_JSON)
                        .content(alta.formatted("curto", "")))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post(INTERNACAO + internacao + "/alta").contentType(MediaType.APPLICATION_JSON)
                        .content(alta.formatted("Pneumonia tratada com antibiótico venoso por 7 dias.", "")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ALTA"))
                .andExpect(jsonPath("$.tipoAlta").value("MELHORADO"))
                .andExpect(jsonPath("$.altaPorNome").value(PREFIXO + "Médica"));
        assertThat(situacao(masculino2)).isEqualTo(SituacaoLeito.HIGIENIZACAO);
        mockMvc.perform(post(INTERNACAO + internacao + "/troca-de-leito").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"leitoId\": \"%s\", \"profissionalMatricula\": \"LT-MED\", \"motivo\": \"Depois da alta\"}".formatted(masculino1)))
                .andExpect(status().isUnprocessableEntity());

        // Higienização: o leito só fica livre quando alguém registra a limpeza.
        internar(paciente("Paulo", Sexo.MASCULINO), masculino2).andExpect(status().isUnprocessableEntity());
        mockMvc.perform(post(LEITO + masculino2 + "/liberacao")).andExpect(status().isOk()).andExpect(jsonPath("$.situacao").value("LIVRE"));
        mockMvc.perform(post(LEITO + masculino2 + "/liberacao")).andExpect(status().isUnprocessableEntity());

        mockMvc.perform(get(INTERNACAO).param("pacienteId", joao.getUuid().toString()))
                .andExpect(jsonPath("$[0].status").value("ALTA"))
                .andExpect(jsonPath("$[0].sumarioAlta").doesNotExist());
        mockMvc.perform(get(INTERNACAO + internacao))
                .andExpect(jsonPath("$.sumarioAlta").value("Pneumonia tratada com antibiótico venoso por 7 dias."))
                .andExpect(jsonPath("$.eventos.length()").value(4));
        assertThat(auditoriaRepository.findByRegistroIdOrderByOcorridoEmDesc(internacao))
                .anyMatch(e -> "GET".equals(e.getMetodo()) && joao.getUuid().equals(e.getPacienteId()));
    }

    @Test
    void bloqueioComMotivoSoEmLeitoLivreEOcupadoNaoSaiDeUso() throws Exception {
        UUID leito = leito("Isolamento 01", "MISTO");
        mockMvc.perform(post(LEITO + leito + "/bloqueio").contentType(MediaType.APPLICATION_JSON).content("{\"motivo\": \"\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post(LEITO + leito + "/bloqueio").contentType(MediaType.APPLICATION_JSON).content("{\"motivo\": \"Troca do gás medicinal\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("BLOQUEADO"))
                .andExpect(jsonPath("$.motivoBloqueio").value("Troca do gás medicinal"));
        internar(maria, leito).andExpect(status().isUnprocessableEntity());
        mockMvc.perform(post(LEITO + leito + "/desbloqueio")).andExpect(status().isOk()).andExpect(jsonPath("$.situacao").value("LIVRE"));

        internar(maria, leito).andExpect(status().isCreated());
        mockMvc.perform(post(LEITO + leito + "/bloqueio").contentType(MediaType.APPLICATION_JSON).content("{\"motivo\": \"Manutenção\"}"))
                .andExpect(status().isUnprocessableEntity());
        mockMvc.perform(patch(LEITO + leito).contentType(MediaType.APPLICATION_JSON).content("""
                {"unidadeId": "%s", "setorId": "%s", "identificacao": "Isolamento 01", "tipo": "ISOLAMENTO", "sexo": "MISTO", "ativo": false}
                """.formatted(hospital.getUuid(), enfermaria.getUuid())))
                .andExpect(status().isUnprocessableEntity());
        Leito salvo = leitoRepository.findById(leito).orElseThrow();
        assertThat(salvo.isAtivo()).isTrue();
    }
}
