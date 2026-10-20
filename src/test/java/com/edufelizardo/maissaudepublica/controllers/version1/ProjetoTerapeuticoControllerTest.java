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

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Saúde Mental, fatia SM2 (ADR-0121): o PTS versionado sem sobrescrever (e a revisão concorrente recusada), as regras
 * de responsável e de revisão, as intervenções com as faltas seguidas, a rede de apoio como histórico e os indicadores
 * "Sem PTS" e "PTS a revisar".
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProjetoTerapeuticoControllerTest {

    private static final String PREFIXO = "PTS Teste ";
    private static final String MATRICULA = "PTST-";

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
    private String individual;
    private String visita;

    @BeforeEach
    void seed() {
        limpar();
        caps = new UnidadeDeSaude();
        caps.setNome(PREFIXO + "CAPS");
        caps.setTipo(TipoUnidadeDeSaude.UBS);
        caps.setAtivo(true);
        caps = unidadeDeSaudeRepository.save(caps);
        participacaoRepository.save(new ParticipacaoPrograma(null, programaRepository.findByCodigo("SAUDE_MENTAL").orElseThrow(), caps,
                null, LocalDate.now().minusYears(1), null, null, null));
        equipe = new Equipe();
        equipe.setUnidade(caps);
        equipe.setTipo(TipoEquipe.EMULTI);
        equipe.setNome("Equipe PTS");
        equipe.setAtiva(true);
        equipe = equipeRepository.save(equipe);
        membro(profissional("PSI", "90066677701"), FuncaoEquipe.PSICOLOGO);
        membro(profissional("AS", "90066677702"), FuncaoEquipe.ASSISTENTE_SOCIAL);
        profissional("FORA", "90066677703");
        maria = paciente("Maria", "90066677711");
        jose = paciente("José", "90066677712");
        individual = jdbc.queryForObject("select uuid::text from tb_tipo_intervencao_saude_mental where codigo = 'ATENDIMENTO_INDIVIDUAL'", String.class);
        visita = jdbc.queryForObject("select uuid::text from tb_tipo_intervencao_saude_mental where codigo = 'VISITA_DOMICILIAR'", String.class);
    }

    @AfterEach
    void limpar() {
        SaudeMentalControllerTest.limparDados(jdbc, PREFIXO, MATRICULA);
    }

    private Profissional profissional(String sufixo, String cpf) {
        Profissional p = new Profissional();
        p.setMatricula(MATRICULA + sufixo);
        p.setCpf(cpf);
        p.setNome(PREFIXO + sufixo);
        p.setAtivo(true);
        return profissionalRepository.save(p);
    }

    private void membro(Profissional p, FuncaoEquipe funcao) {
        membroRepository.save(new MembroEquipe(null, equipe, p, funcao, null, LocalDate.now().minusMonths(6), null, null, null));
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
                        {"pacienteId": "%s", "unidadeId": "%s", "equipeId": "%s", "tecnicoMatricula": "%s"}"""
                        .formatted(p.getUuid(), caps.getUuid(), equipe.getUuid(), MATRICULA + "PSI")))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return JsonPath.read(r, "$.uuid");
    }

    private ResultActions pts(String caso, String corpo) throws Exception {
        return mockMvc.perform(post("/api/v1/saude-mental/acompanhamento/" + caso + "/pts").contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    private String primeiraVersao() {
        return """
                {"necessidades": "Vínculo com o serviço e adesão ao cuidado.", "pactuadoComPessoa": true,
                 "objetivos": [
                   {"descricao": "Vínculo com o CAPS", "meta": "Comparecer a 4 encontros no mês", "responsavelMatricula": "%sPSI"},
                   {"descricao": "Retomar a rotina de sono"}],
                 "intervencoesPrevistas": [{"tipoId": "%s", "frequencia": "semanal", "responsavelMatricula": "%sAS"}]}"""
                .formatted(MATRICULA, individual, MATRICULA);
    }

    private ResultActions intervencao(String caso, String extra) throws Exception {
        return mockMvc.perform(post("/api/v1/saude-mental/acompanhamento/" + caso + "/intervencao").contentType(MediaType.APPLICATION_JSON)
                .content("{\"tipoId\": \"%s\", \"resumo\": \"Escuta e combinados da semana.\"%s}".formatted(individual, extra)));
    }

    @Test
    void ptsVersionadoSemSobrescreverERevisaoConcorrenteRecusada() throws Exception {
        String caso = abrir(maria);
        pts(caso, primeiraVersao()).andExpect(status().isCreated())
                .andExpect(jsonPath("$.ptsVersao").value(1))
                .andExpect(jsonPath("$.ptsRevisarEm").value(LocalDate.now().plusDays(90).toString()))
                .andExpect(jsonPath("$.pts.length()").value(1))
                .andExpect(jsonPath("$.pts[0].objetivos.length()").value(2))
                .andExpect(jsonPath("$.pts[0].objetivos[0].situacao").value("EM_ANDAMENTO"))
                .andExpect(jsonPath("$.pts[0].intervencoesPrevistas[0].tipoNome").value("Atendimento individual"));
        // Outra primeira versão, com o PTS já elaborado: 409.
        pts(caso, primeiraVersao()).andExpect(status().isConflict());

        String revisao = """
                {"versaoBase": %d, "necessidades": "Retorno ao trabalho.", "periodicidadeRevisao": "SEMESTRAL", %s
                 "objetivos": [{"descricao": "Vínculo com o CAPS", "situacao": "ALCANCADO"}, {"descricao": "Retorno ao trabalho"}]}""";
        pts(caso, revisao.formatted(1, "")).andExpect(status().isBadRequest());
        pts(caso, revisao.formatted(1, "\"sinteseRevisao\": \"Vínculo alcançado; novo objetivo de trabalho.\",")).andExpect(status().isCreated())
                .andExpect(jsonPath("$.ptsVersao").value(2))
                .andExpect(jsonPath("$.ptsRevisarEm").value(LocalDate.now().plusDays(180).toString()))
                .andExpect(jsonPath("$.pts.length()").value(2))
                .andExpect(jsonPath("$.pts[0].vigente").value(true))
                .andExpect(jsonPath("$.pts[0].objetivos[0].situacao").value("ALCANCADO"))
                .andExpect(jsonPath("$.pts[1].vigente").value(false))
                .andExpect(jsonPath("$.pts[1].versao").value(1))
                .andExpect(jsonPath("$.pts[1].objetivos[0].situacao").value("EM_ANDAMENTO"))
                .andExpect(jsonPath("$.pts[1].intervencoesPrevistas.length()").value(1));
        // A revisão que parte da versão 1, depois da 2: 409.
        pts(caso, revisao.formatted(1, "\"sinteseRevisao\": \"Atrasada.\",")).andExpect(status().isConflict());
        mockMvc.perform(get("/api/v1/saude-mental/acompanhamento/" + caso + "/pts")).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void regrasDoPts() throws Exception {
        String caso = abrir(maria);
        pts(caso, "{\"necessidades\": \"x\", \"objetivos\": []}").andExpect(status().isBadRequest());
        pts(caso, "{\"necessidades\": \"x\", \"objetivos\": [{\"descricao\": \"y\", \"responsavelMatricula\": \"%sFORA\"}]}".formatted(MATRICULA))
                .andExpect(status().isUnprocessableEntity());
        pts(caso, "{\"necessidades\": \"x\", \"revisarEm\": \"%s\", \"objetivos\": [{\"descricao\": \"y\"}]}".formatted(LocalDate.now().minusDays(1)))
                .andExpect(status().isBadRequest());
        pts(caso, "{\"necessidades\": \"x\", \"objetivos\": [{\"descricao\": \"y\"}], \"intervencoesPrevistas\": [{\"tipoId\": \"%s\"}]}"
                .formatted(UUID.randomUUID())).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/saude-mental/acompanhamento/" + caso)).andExpect(status().isOk())
                .andExpect(jsonPath("$.ptsVersao").doesNotExist());
    }

    @Test
    void intervencoesComFaltasSeguidas() throws Exception {
        String caso = abrir(maria);
        String outro = abrir(jose);
        String r = pts(outro, primeiraVersao()).andReturn().getResponse().getContentAsString();
        String previstaDeOutro = JsonPath.read(r, "$.pts[0].intervencoesPrevistas[0].uuid");

        intervencao(caso, ", \"compartilhadoComMatricula\": \"%sFORA\"".formatted(MATRICULA)).andExpect(status().isCreated())
                .andExpect(jsonPath("$.intervencoes.length()").value(1))
                .andExpect(jsonPath("$.intervencoes[0].situacao").value("REALIZADA"))
                .andExpect(jsonPath("$.intervencoes[0].compartilhadoComNome").value(PREFIXO + "FORA"))
                .andExpect(jsonPath("$.faltasSeguidas").value(0));
        intervencao(caso, ", \"realizadaEm\": \"%s\"".formatted(Instant.now().plus(2, ChronoUnit.DAYS))).andExpect(status().isBadRequest());
        intervencao(caso, ", \"previstaId\": \"%s\"".formatted(previstaDeOutro)).andExpect(status().isUnprocessableEntity());
        mockMvc.perform(post("/api/v1/saude-mental/acompanhamento/" + caso + "/intervencao").contentType(MediaType.APPLICATION_JSON)
                .content("{\"tipoId\": \"%s\"}".formatted(visita))).andExpect(status().isBadRequest());
        intervencao(caso, ", \"situacao\": \"NAO_COMPARECEU\"").andExpect(status().isCreated());
        intervencao(caso, ", \"situacao\": \"NAO_COMPARECEU\"").andExpect(status().isCreated())
                .andExpect(jsonPath("$.intervencoes.length()").value(3))
                .andExpect(jsonPath("$.faltasSeguidas").value(2));
        mockMvc.perform(get("/api/v1/saude-mental/tipos-intervencao")).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(9))
                .andExpect(jsonPath("$[0].codigo").value("ATENDIMENTO_INDIVIDUAL"));
    }

    @Test
    void redeDeApoioComoHistorico() throws Exception {
        String caso = abrir(maria);
        String r = mockMvc.perform(post("/api/v1/saude-mental/acompanhamento/" + caso + "/rede-apoio").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\": \"Ana\", \"papel\": \"FAMILIAR\", \"vinculo\": \"irmã\", \"telefone\": \"11 99999-0000\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.redeApoio.length()").value(1))
                .andExpect(jsonPath("$.redeApoio[0].inicio").value(LocalDate.now().toString()))
                .andReturn().getResponse().getContentAsString();
        String pessoa = JsonPath.read(r, "$.redeApoio[0].uuid");
        String saida = "/api/v1/saude-mental/acompanhamento/" + caso + "/rede-apoio/" + pessoa + "/encerramento";
        mockMvc.perform(post(saida).contentType(MediaType.APPLICATION_JSON).content("{\"motivo\": \"Mudou de cidade.\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.redeApoio[0].fim").value(LocalDate.now().toString()))
                .andExpect(jsonPath("$.redeApoio[0].motivoFim").value("Mudou de cidade."));
        mockMvc.perform(post(saida).contentType(MediaType.APPLICATION_JSON).content("{\"motivo\": \"De novo.\"}"))
                .andExpect(status().isUnprocessableEntity());
        mockMvc.perform(post("/api/v1/saude-mental/acompanhamento/" + caso + "/rede-apoio").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\": \"Ana\"}")).andExpect(status().isBadRequest());
    }

    @Test
    void indicadoresSemPtsEPtsARevisar() throws Exception {
        String caso = abrir(maria);
        abrir(jose);
        String filtro = "/api/v1/saude-mental/acompanhamentos";
        mockMvc.perform(get(filtro).param("unidadeId", caps.getUuid().toString())).andExpect(status().isOk())
                .andExpect(jsonPath("$.semPts").value(2)).andExpect(jsonPath("$.ptsARevisar").value(0));
        pts(caso, primeiraVersao()).andExpect(status().isCreated());
        // A revisão vencida: a data vem do passado direto no banco, porque a API não aceita revisão no passado.
        jdbc.update("update tb_projeto_terapeutico_singular set revisar_em = ? where acompanhamento_id = ?::uuid", LocalDate.now().minusDays(3), caso);
        mockMvc.perform(get(filtro).param("unidadeId", caps.getUuid().toString())).andExpect(status().isOk())
                .andExpect(jsonPath("$.semPts").value(1)).andExpect(jsonPath("$.ptsARevisar").value(1));
        mockMvc.perform(get(filtro).param("unidadeId", caps.getUuid().toString()).param("pendencia", "PTS_A_REVISAR")).andExpect(status().isOk())
                .andExpect(jsonPath("$.lista.length()").value(1))
                .andExpect(jsonPath("$.lista[0].uuid").value(caso))
                .andExpect(jsonPath("$.lista[0].ptsRevisaoVencida").value(true));
        mockMvc.perform(get(filtro).param("unidadeId", caps.getUuid().toString()).param("pendencia", "SEM_PTS")).andExpect(status().isOk())
                .andExpect(jsonPath("$.lista.length()").value(1))
                .andExpect(jsonPath("$.lista[0].pacienteNome").value(PREFIXO + "José"));
    }
}
