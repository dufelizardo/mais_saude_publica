package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Equipe;
import com.edufelizardo.maissaudepublica.models.MembroEquipe;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.ParticipacaoPrograma;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.Programa;
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
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Saúde Mental, fatia SM1 (ADR-0120): acolhimento que abre o caso ou entra no vigente, reaproveitamento do acompanhamento
 * do programa Saúde Mental, regras de equipe e técnico de referência, referência como histórico, avaliação e
 * encerramento que fecha o programa junto.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SaudeMentalControllerTest {

    static final String PREFIXO = "SaudeMental Teste ";
    private static final String MATRICULA = "SMT-";

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

    private Programa saudeMental;
    private UnidadeDeSaude caps;
    private UnidadeDeSaude ubs;
    private Equipe equipeCaps;
    private Equipe equipeUbs;
    private Paciente maria;
    private Paciente jose;

    @BeforeEach
    void seed() {
        limpar();
        saudeMental = programaRepository.findByCodigo("SAUDE_MENTAL").orElseThrow();
        caps = unidade("CAPS Centro");
        ubs = unidade("UBS Norte");
        participacaoRepository.save(new ParticipacaoPrograma(null, saudeMental, caps, null, LocalDate.now().minusYears(1), null, null, null));
        equipeCaps = equipe(caps, "Equipe Multi");
        equipeUbs = equipe(ubs, "Equipe Norte");
        membro(equipeCaps, profissional("PSI1", "90044455501"), FuncaoEquipe.PSICOLOGO);
        membro(equipeCaps, profissional("AS1", "90044455502"), FuncaoEquipe.ASSISTENTE_SOCIAL);
        membro(equipeUbs, profissional("ENF1", "90044455503"), FuncaoEquipe.ENFERMEIRO);
        profissional("FORA", "90044455504");
        maria = paciente("Maria", "90044455511");
        jose = paciente("José", "90044455512");
    }

    @AfterEach
    void limpar() {
        limparDados(jdbc, PREFIXO, MATRICULA);
    }

    /** Apaga o que os testes de saúde mental criaram, pelas unidades e matrículas do prefixo. */
    static void limparDados(JdbcTemplate jdbc, String prefixo, String matricula) {
        String unidades = "(select uuid from tb_unidade_de_saude where nome like '" + prefixo + "%')";
        String casos = "(select uuid from tb_acompanhamento_saude_mental where unidade_id in " + unidades + ")";
        String pts = "(select uuid from tb_projeto_terapeutico_singular where acompanhamento_id in " + casos + ")";
        String solicitacoes = "(select uuid from tb_solicitacao_regulacao where unidade_solicitante_id in " + unidades + ")";
        String crises = "(select uuid from tb_evento_crise where unidade_id in " + unidades + " or acompanhamento_id in " + casos + ")";
        jdbc.update("delete from tb_etapa_crise where crise_id in " + crises);
        jdbc.update("delete from tb_evento_crise where uuid in " + crises);
        jdbc.update("delete from tb_avaliacao_risco where acompanhamento_id in " + casos);
        jdbc.update("delete from tb_transferencia_cuidado where acompanhamento_id in " + casos + " or unidade_destino_id in " + unidades);
        jdbc.update("delete from tb_encaminhamento_saude_mental where acompanhamento_id in " + casos);
        jdbc.update("delete from tb_compartilhamento_cuidado where acompanhamento_id in " + casos);
        jdbc.update("delete from tb_evento_regulacao where solicitacao_id in " + solicitacoes);
        jdbc.update("delete from tb_solicitacao_regulacao where uuid in " + solicitacoes);
        jdbc.update("delete from tb_procedimento_regulado where nome like '" + prefixo + "%'");
        jdbc.update("delete from tb_intervencao_saude_mental where acompanhamento_id in " + casos);
        jdbc.update("delete from tb_intervencao_prevista where pts_id in " + pts);
        jdbc.update("delete from tb_objetivo_terapeutico where pts_id in " + pts);
        jdbc.update("delete from tb_projeto_terapeutico_singular where acompanhamento_id in " + casos);
        jdbc.update("delete from tb_pessoa_rede_apoio where acompanhamento_id in " + casos);
        jdbc.update("delete from tb_avaliacao_saude_mental where acompanhamento_id in " + casos);
        jdbc.update("delete from tb_acolhimento_saude_mental where unidade_id in " + unidades + " or acompanhamento_id in " + casos);
        jdbc.update("delete from tb_referencia_saude_mental where acompanhamento_id in " + casos);
        jdbc.update("delete from tb_acompanhamento_saude_mental where unidade_id in " + unidades);
        jdbc.update("delete from tb_evento_acompanhamento where acompanhamento_id in (select uuid from tb_acompanhamento_programatico "
                + "where unidade_id in " + unidades + ")");
        jdbc.update("delete from tb_acompanhamento_programatico where unidade_id in " + unidades);
        jdbc.update("delete from tb_participacao_programa where unidade_id in " + unidades);
        jdbc.update("delete from tb_atendimento where unidade_id in " + unidades);
        jdbc.update("delete from tb_equipe_apoio where equipe_id in (select uuid from tb_equipe where unidade_id in " + unidades + ") "
                + "or apoiada_id in (select uuid from tb_equipe where unidade_id in " + unidades + ")");
        jdbc.update("delete from tb_membro_equipe where equipe_id in (select uuid from tb_equipe where unidade_id in " + unidades + ")");
        jdbc.update("delete from tb_equipe where unidade_id in " + unidades);
        jdbc.update("delete from tb_profissional where matricula like '" + matricula + "%'");
        jdbc.update("delete from tb_acesso_justificado where paciente_uuid in (select uuid from tb_paciente where nome like '" + prefixo + "%')");
        jdbc.update("delete from tb_paciente where nome like '" + prefixo + "%'");
        jdbc.update("delete from tb_unidade_de_saude where nome like '" + prefixo + "%'");
    }

    private UnidadeDeSaude unidade(String nome) {
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + nome);
        u.setTipo(TipoUnidadeDeSaude.UBS);
        u.setAtivo(true);
        return unidadeDeSaudeRepository.save(u);
    }

    private Equipe equipe(UnidadeDeSaude u, String nome) {
        Equipe e = new Equipe();
        e.setUnidade(u);
        e.setTipo(TipoEquipe.EMULTI);
        e.setNome(nome);
        e.setAtiva(true);
        return equipeRepository.save(e);
    }

    private Profissional profissional(String sufixo, String cpf) {
        Profissional p = new Profissional();
        p.setMatricula(MATRICULA + sufixo);
        p.setCpf(cpf);
        p.setNome(PREFIXO + sufixo);
        p.setAtivo(true);
        return profissionalRepository.save(p);
    }

    private void membro(Equipe e, Profissional p, FuncaoEquipe funcao) {
        membroRepository.save(new MembroEquipe(null, e, p, funcao, null, LocalDate.now().minusMonths(6), null, null, null));
    }

    private Paciente paciente(String nome, String cpf) {
        Paciente p = new Paciente();
        p.setNome(PREFIXO + nome);
        p.setCpf(cpf);
        p.setDataNascimento(LocalDate.now().minusYears(34));
        p.setAtivo(true);
        return pacienteRepository.save(p);
    }

    private ResultActions acolher(Paciente p, UnidadeDeSaude u, String desfecho, String extra) throws Exception {
        return mockMvc.perform(post("/api/v1/saude-mental/acolhimento").contentType(MediaType.APPLICATION_JSON).content("""
                {"pacienteId": "%s", "unidadeId": "%s", "motivoProcura": "Tristeza persistente e insônia há três meses.",
                 "riscoInicial": "MODERADO", "desfecho": "%s"%s}""".formatted(p.getUuid(), u.getUuid(), desfecho, extra)));
    }

    private ResultActions abrir(Paciente p, UnidadeDeSaude u, Equipe e, String tecnico) throws Exception {
        return mockMvc.perform(post("/api/v1/saude-mental/acompanhamento").contentType(MediaType.APPLICATION_JSON).content("""
                {"pacienteId": "%s", "unidadeId": "%s", "equipeId": "%s", "tecnicoMatricula": "%s"}"""
                .formatted(p.getUuid(), u.getUuid(), e.getUuid(), MATRICULA + tecnico)));
    }

    private String referencia(Equipe e, String tecnico) {
        return ", \"equipeId\": \"%s\", \"tecnicoMatricula\": \"%s\"".formatted(e.getUuid(), MATRICULA + tecnico);
    }

    @Test
    void acolhimentoAbreOCasoNoProgramaEOSeguinteEntraNele() throws Exception {
        String r = acolher(maria, caps, "ACOMPANHAMENTO_NA_UNIDADE", referencia(equipeCaps, "PSI1")).andExpect(status().isCreated())
                .andExpect(jsonPath("$.conteudoLiberado").value(true))
                .andExpect(jsonPath("$.acompanhamentoId").exists())
                .andExpect(jsonPath("$.aviso").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        String caso = JsonPath.read(r, "$.acompanhamentoId");
        mockMvc.perform(get("/api/v1/programa/" + saudeMental.getUuid() + "/acompanhamentos").param("unidadeId", caps.getUuid().toString()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.inscritos").value(1));

        acolher(maria, caps, "CUIDADO_COMPARTILHADO", ", \"riscoInicial\": \"ALTO\"").andExpect(status().isCreated())
                .andExpect(jsonPath("$.acompanhamentoId").value(caso))
                .andExpect(jsonPath("$.aviso", containsString("já é acompanhado")))
                .andExpect(jsonPath("$.aviso", containsString("Risco alto")));
        abrir(maria, caps, equipeCaps, "PSI1").andExpect(status().isConflict());

        mockMvc.perform(get("/api/v1/saude-mental/acompanhamento/" + caso)).andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("ATIVO"))
                .andExpect(jsonPath("$.equipeNome").value("Equipe Multi"))
                .andExpect(jsonPath("$.tecnicoMatricula").value(MATRICULA + "PSI1"))
                .andExpect(jsonPath("$.acolhimentos.length()").value(2))
                .andExpect(jsonPath("$.referencias.length()").value(1));
        mockMvc.perform(get("/api/v1/saude-mental/paciente/" + maria.getUuid() + "/acolhimentos")).andExpect(status().isOk())
                .andExpect(jsonPath("$.lista.length()").value(2))
                .andExpect(jsonPath("$.lista[0].motivoProcura").exists());
    }

    @Test
    void abrirReaproveitaOAcompanhamentoDoProgramaJaVigente() throws Exception {
        String inscricao = mockMvc.perform(post("/api/v1/programa/" + saudeMental.getUuid() + "/acompanhamento").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pacienteId\": \"%s\", \"unidadeId\": \"%s\"}".formatted(jose.getUuid(), caps.getUuid())))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String programatico = JsonPath.read(inscricao, "$.uuid");
        abrir(jose, caps, equipeCaps, "AS1").andExpect(status().isCreated())
                .andExpect(jsonPath("$.acompanhamentoProgramaticoId").value(programatico));
    }

    @Test
    void regrasDoAcolhimentoEDaReferencia() throws Exception {
        acolher(maria, caps, "ACOMPANHAMENTO_NA_UNIDADE", "").andExpect(status().isBadRequest());
        acolher(maria, caps, "ENCAMINHAMENTO", "").andExpect(status().isBadRequest());
        acolher(maria, caps, "ENCAMINHAMENTO", ", \"encaminhamentoDestino\": \"CAPS AD Leste\"").andExpect(status().isCreated())
                .andExpect(jsonPath("$.acompanhamentoId").doesNotExist())
                .andExpect(jsonPath("$.encaminhamentoDestino").value("CAPS AD Leste"));
        acolher(maria, caps, "ORIENTACAO_SEM_SEGUIMENTO", ", \"riscoInicial\": \"QUALQUER\"").andExpect(status().isBadRequest());
        abrir(maria, caps, equipeCaps, "FORA").andExpect(status().isUnprocessableEntity());
        abrir(maria, caps, equipeUbs, "ENF1").andExpect(status().isUnprocessableEntity());
        // A UBS não oferece o programa Saúde Mental: não há como inscrever.
        abrir(maria, ubs, equipeUbs, "ENF1").andExpect(status().isUnprocessableEntity());
        mockMvc.perform(get("/api/v1/saude-mental/acompanhamento/" + UUID.randomUUID())).andExpect(status().isNotFound());
    }

    @Test
    void referenciaComoHistoricoAvaliacaoEEncerramentoQueFechaOPrograma() throws Exception {
        String r = abrir(maria, caps, equipeCaps, "PSI1").andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String caso = JsonPath.read(r, "$.uuid");
        String programatico = JsonPath.read(r, "$.acompanhamentoProgramaticoId");
        String base = "/api/v1/saude-mental/acompanhamento/" + caso;

        mockMvc.perform(get("/api/v1/saude-mental/acompanhamentos").param("unidadeId", caps.getUuid().toString()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.emAcompanhamento").value(1)).andExpect(jsonPath("$.semAvaliacao").value(1));

        String troca = "{\"equipeId\": \"%s\", \"tecnicoMatricula\": \"%s\", \"motivo\": \"Vínculo maior com a assistente social.\"}"
                .formatted(equipeCaps.getUuid(), MATRICULA + "AS1");
        mockMvc.perform(post(base + "/referencia").contentType(MediaType.APPLICATION_JSON).content(troca)).andExpect(status().isOk())
                .andExpect(jsonPath("$.tecnicoMatricula").value(MATRICULA + "AS1"))
                .andExpect(jsonPath("$.referencias.length()").value(2))
                .andExpect(jsonPath("$.referencias[?(@.fim == null)].tecnicoMatricula").value(MATRICULA + "AS1"));
        mockMvc.perform(post(base + "/referencia").contentType(MediaType.APPLICATION_JSON).content(troca)).andExpect(status().isUnprocessableEntity());

        String avaliacao = """
                {"contextoHistoria": "Luto recente, afastada do trabalho.", "necessidades": "Acompanhamento semanal e rede de apoio.",
                 "fatoresProtecao": "Irmã próxima.", "cid": "%s"}""";
        mockMvc.perform(post(base + "/avaliacao").contentType(MediaType.APPLICATION_JSON).content(avaliacao.formatted("X1")))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post(base + "/avaliacao").contentType(MediaType.APPLICATION_JSON).content(avaliacao.formatted("f32.1")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.avaliacoes.length()").value(1))
                .andExpect(jsonPath("$.avaliacoes[0].cid").value("F32.1"))
                .andExpect(jsonPath("$.ultimaAvaliacaoEm").exists());

        // O acompanhamento do programa só sai pela Saúde Mental enquanto o caso está aberto.
        String saida = "{\"motivoSaida\": \"ALTA\", \"observacao\": \"Objetivos do cuidado alcançados.\"}";
        mockMvc.perform(post("/api/v1/acompanhamento/" + programatico + "/encerramento").contentType(MediaType.APPLICATION_JSON).content(saida))
                .andExpect(status().isUnprocessableEntity());
        mockMvc.perform(post(base + "/encerramento").contentType(MediaType.APPLICATION_JSON).content(saida)).andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("CONCLUIDO"))
                .andExpect(jsonPath("$.fim").value(LocalDate.now().toString()));
        mockMvc.perform(get("/api/v1/paciente/" + maria.getUuid() + "/programas")).andExpect(status().isOk())
                .andExpect(jsonPath("$.vigentes.length()").value(0))
                .andExpect(jsonPath("$.encerrados[0].situacao").value("CONCLUIDO"));
        mockMvc.perform(post(base + "/avaliacao").contentType(MediaType.APPLICATION_JSON).content(avaliacao.formatted("F32.1")))
                .andExpect(status().isUnprocessableEntity());
        mockMvc.perform(get("/api/v1/saude-mental/acompanhamentos").param("unidadeId", caps.getUuid().toString()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.emAcompanhamento").value(0));
    }
}
