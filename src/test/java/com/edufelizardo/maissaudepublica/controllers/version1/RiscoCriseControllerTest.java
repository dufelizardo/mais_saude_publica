package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Equipe;
import com.edufelizardo.maissaudepublica.models.MembroEquipe;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.ParticipacaoPrograma;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.FuncaoEquipe;
import com.edufelizardo.maissaudepublica.models.enuns.TipoEquipe;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.EquipeRepository;
import com.edufelizardo.maissaudepublica.repositories.MembroEquipeRepository;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
import com.edufelizardo.maissaudepublica.repositories.ParticipacaoProgramaRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.ProgramaRepository;
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

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Saúde Mental, fatia SM3 (ADR-0123): risco com histórico e vigente por tipo, regras de medidas e de reavaliação,
 * indicadores; crise com e sem caso, etapas, encerramento com pós-crise e a notificação compulsória sugerida e registrada.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RiscoCriseControllerTest {

    private static final String PREFIXO = "RiscoCrise Teste ";
    private static final String MATRICULA = "RCT-";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private ProgramaRepository programaRepository;

    @Autowired
    private ParticipacaoProgramaRepository participacaoRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private EquipeRepository equipeRepository;

    @Autowired
    private MembroEquipeRepository membroRepository;

    private UnidadeDeSaude caps;
    private Equipe equipe;
    private Paciente maria;
    private Paciente jose;

    @BeforeEach
    void seed() {
        limpar();
        caps = new UnidadeDeSaude();
        caps.setNome(PREFIXO + "CAPS");
        caps.setTipo(TipoUnidadeDeSaude.UBS);
        caps.setAtivo(true);
        caps = unidadeDeSaudeRepository.save(caps);
        participacaoRepository.save(new ParticipacaoPrograma(null, programaRepository.findByCodigo("SAUDE_MENTAL").orElseThrow(), caps, null,
                LocalDate.now().minusYears(1), null, null, null));
        equipe = new Equipe();
        equipe.setUnidade(caps);
        equipe.setTipo(TipoEquipe.EMULTI);
        equipe.setNome("Equipe Crise");
        equipe.setAtiva(true);
        equipe = equipeRepository.save(equipe);
        Profissional psi = new Profissional();
        psi.setMatricula(MATRICULA + "PSI");
        psi.setCpf("90088899901");
        psi.setNome(PREFIXO + "PSI");
        psi.setAtivo(true);
        psi = profissionalRepository.save(psi);
        membroRepository.save(new MembroEquipe(null, equipe, psi, FuncaoEquipe.PSICOLOGO, null, LocalDate.now().minusMonths(6), null, null, null));
        maria = paciente("Maria", "90088899911");
        jose = paciente("José", "90088899912");
    }

    @AfterEach
    void limpar() {
        SaudeMentalControllerTest.limparDados(jdbc, PREFIXO, MATRICULA);
    }

    private Paciente paciente(String nome, String cpf) {
        Paciente p = new Paciente();
        p.setNome(PREFIXO + nome);
        p.setCpf(cpf);
        p.setAtivo(true);
        return pacienteRepository.save(p);
    }

    private String abrir(Paciente p) throws Exception {
        String r = mockMvc.perform(post("/api/v1/saude-mental/acompanhamento").contentType(MediaType.APPLICATION_JSON).content("""
                        {"pacienteId": "%s", "unidadeId": "%s", "equipeId": "%s", "tecnicoMatricula": "%sPSI"}"""
                        .formatted(p.getUuid(), caps.getUuid(), equipe.getUuid(), MATRICULA)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return JsonPath.read(r, "$.uuid");
    }

    private ResultActions postar(String url, String corpo) throws Exception {
        return mockMvc.perform(post("/api/v1/saude-mental/" + url).contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    @Test
    void riscoComHistoricoEVigentePorTipo() throws Exception {
        String caso = abrir(maria);
        String url = "acompanhamento/" + caso + "/risco";
        postar(url, "{\"tipo\": \"AUTOAGRESSAO\", \"nivel\": \"ALTO\", \"reavaliarEm\": \"%s\"}".formatted(LocalDate.now().plusDays(7)))
                .andExpect(status().isBadRequest());
        postar(url, "{\"tipo\": \"AUTOAGRESSAO\", \"nivel\": \"ALTO\", \"medidas\": \"Plano de segurança e contato diário.\"}")
                .andExpect(status().isBadRequest());
        postar(url, "{\"tipo\": \"AUTOAGRESSAO\", \"nivel\": \"BAIXO\", \"reavaliarEm\": \"%s\"}".formatted(LocalDate.now().minusDays(1)))
                .andExpect(status().isBadRequest());
        postar(url, """
                {"tipo": "AUTOAGRESSAO", "nivel": "ALTO", "medidas": "Plano de segurança e contato diário.", "planoSeguranca": true,
                 "frequenciaContato": "diário", "reavaliarEm": "%s"}""".formatted(LocalDate.now().plusDays(7)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.riscoAlto").value(true))
                .andExpect(jsonPath("$.riscos.length()").value(1))
                .andExpect(jsonPath("$.riscos[0].vigente").value(true))
                .andExpect(jsonPath("$.riscos[0].planoSeguranca").value(true));
        mockMvc.perform(get("/api/v1/saude-mental/acompanhamentos").param("unidadeId", caps.getUuid().toString()).param("pendencia", "RISCO_ALTO"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.riscoAlto").value(1)).andExpect(jsonPath("$.lista.length()").value(1));

        postar(url, "{\"tipo\": \"AUTOAGRESSAO\", \"nivel\": \"MODERADO\", \"medidas\": \"Contato semanal.\", \"frequenciaContato\": \"semanal\"}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.riscoAlto").value(false))
                .andExpect(jsonPath("$.riscos.length()").value(2))
                .andExpect(jsonPath("$.riscos[0].nivel").value("MODERADO"))
                .andExpect(jsonPath("$.riscos[0].vigente").value(true))
                .andExpect(jsonPath("$.riscos[1].vigente").value(false));
        postar(url, "{\"tipo\": \"USO_DE_SUBSTANCIAS\", \"nivel\": \"BAIXO\"}").andExpect(status().isCreated())
                .andExpect(jsonPath("$.riscos[?(@.vigente == true)].tipo").isArray())
                .andExpect(jsonPath("$.riscos.length()").value(3));

        // Reavaliação vencida: a data vem do passado direto no banco, porque a API não aceita reavaliar no passado.
        jdbc.update("update tb_avaliacao_risco set reavaliar_em = ? where acompanhamento_id = ?::uuid and nivel = 'MODERADO'",
                LocalDate.now().minusDays(2), caso);
        mockMvc.perform(get("/api/v1/saude-mental/acompanhamentos").param("unidadeId", caps.getUuid().toString()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.reavaliacaoRiscoVencida").value(1));
    }

    @Test
    void criseComCasoEtapasEncerramentoENotificacao() throws Exception {
        String caso = abrir(maria);
        String r = postar("crise", """
                {"pacienteId": "%s", "unidadeId": "%s", "tipo": "TENTATIVA_DE_SUICIDIO", "localOcorrencia": "Domicílio",
                 "descricao": "Ingestão de medicamentos; trazida pela irmã."}""".formatted(maria.getUuid(), caps.getUuid()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.acompanhamentoId").value(caso))
                .andExpect(jsonPath("$.situacao").value("EM_CURSO"))
                .andExpect(jsonPath("$.notificacaoSugerida").value(true))
                .andExpect(jsonPath("$.notificacaoPendente").value(true))
                .andReturn().getResponse().getContentAsString();
        String crise = "crise/" + JsonPath.read(r, "$.uuid");
        postar(crise + "/etapa", "{\"etapa\": \"INTERVENCAO\", \"descricao\": \"Manejo e observação.\"}").andExpect(status().isCreated())
                .andExpect(jsonPath("$.etapas.length()").value(1));
        postar(crise + "/encerramento", "{\"desfecho\": \"RETORNO_AO_DOMICILIO\"}").andExpect(status().isUnprocessableEntity());
        postar(crise + "/etapa", "{\"etapa\": \"ACIONAMENTO_DA_REDE\", \"descricao\": \"Família e SAMU acionados.\"}").andExpect(status().isCreated());
        postar(crise + "/etapa", "{\"etapa\": \"POS_CRISE\", \"descricao\": \"Retorno ao CAPS em 24 horas.\"}").andExpect(status().isCreated());
        postar(crise + "/encerramento", "{\"desfecho\": \"RETORNO_AO_DOMICILIO\"}").andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("ENCERRADA"))
                .andExpect(jsonPath("$.etapas.length()").value(3));
        postar(crise + "/etapa", "{\"etapa\": \"POS_CRISE\", \"descricao\": \"De novo.\"}").andExpect(status().isUnprocessableEntity());

        mockMvc.perform(get("/api/v1/saude-mental/crises").param("unidadeId", caps.getUuid().toString())).andExpect(status().isOk())
                .andExpect(jsonPath("$.notificacoesPendentes").value(1));
        postar(crise + "/notificacao", "{\"numero\": \"SINAN-123\"}").andExpect(status().isOk())
                .andExpect(jsonPath("$.notificacaoPendente").value(false))
                .andExpect(jsonPath("$.notificadaEm").value(LocalDate.now().toString()));
        postar(crise + "/notificacao", "{}").andExpect(status().isUnprocessableEntity());
        mockMvc.perform(get("/api/v1/saude-mental/acompanhamento/" + caso)).andExpect(status().isOk())
                .andExpect(jsonPath("$.crises.length()").value(1))
                .andExpect(jsonPath("$.crises[0].etapas.length()").value(3));
    }

    @Test
    void criseSemCasoESemNotificacao() throws Exception {
        String r = postar("crise", """
                {"pacienteId": "%s", "unidadeId": "%s", "tipo": "AGITACAO_PSICOMOTORA", "descricao": "Agitação na recepção."}"""
                .formatted(jose.getUuid(), caps.getUuid()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.acompanhamentoId").doesNotExist())
                .andExpect(jsonPath("$.notificacaoSugerida").value(false))
                .andReturn().getResponse().getContentAsString();
        postar("crise/" + JsonPath.read(r, "$.uuid") + "/notificacao", "{}").andExpect(status().isUnprocessableEntity());
        mockMvc.perform(get("/api/v1/saude-mental/crise/" + JsonPath.read(r, "$.uuid"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.descricao").value("Agitação na recepção."));
        postar("crise", "{\"pacienteId\": \"%s\", \"unidadeId\": \"%s\", \"tipo\": \"OUTRO\"}".formatted(jose.getUuid(), caps.getUuid()))
                .andExpect(status().isBadRequest());
    }
}
