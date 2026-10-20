package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Equipe;
import com.edufelizardo.maissaudepublica.models.MembroEquipe;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.ParticipacaoPrograma;
import com.edufelizardo.maissaudepublica.models.ProcedimentoRegulado;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.Programa;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.FuncaoEquipe;
import com.edufelizardo.maissaudepublica.models.enuns.TipoEquipe;
import com.edufelizardo.maissaudepublica.models.enuns.TipoProcedimentoRegulado;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.EquipeRepository;
import com.edufelizardo.maissaudepublica.repositories.MembroEquipeRepository;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
import com.edufelizardo.maissaudepublica.repositories.ParticipacaoProgramaRepository;
import com.edufelizardo.maissaudepublica.repositories.ProcedimentoReguladoRepository;
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
 * Saúde Mental, fatia SM4 (ADR-0122): encaminhar e vincular pela Regulação, cuidado compartilhado como histórico,
 * matriciamento pedindo o apoio da ADR-0103 e a transferência do cuidado (proposta, aceite com o caso novo ligado ao
 * anterior, recusa, cancelamento).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RedeSaudeMentalControllerTest {

    private static final String PREFIXO = "RedeSM Teste ";
    private static final String MATRICULA = "RSMT-";

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

    @Autowired
    private ProcedimentoReguladoRepository procedimentoRepository;

    private UnidadeDeSaude caps;
    private UnidadeDeSaude ubs;
    private Equipe equipeCaps;
    private Equipe equipeUbs;
    private Paciente maria;
    private Paciente jose;
    private ProcedimentoRegulado psiquiatria;

    @BeforeEach
    void seed() {
        limpar();
        Programa saudeMental = programaRepository.findByCodigo("SAUDE_MENTAL").orElseThrow();
        caps = unidade("CAPS");
        ubs = unidade("UBS");
        participacaoRepository.save(new ParticipacaoPrograma(null, saudeMental, caps, null, LocalDate.now().minusYears(1), null, null, null));
        participacaoRepository.save(new ParticipacaoPrograma(null, saudeMental, ubs, null, LocalDate.now().minusYears(1), null, null, null));
        equipeCaps = equipe(caps, "Equipe CAPS", TipoEquipe.EMULTI);
        equipeUbs = equipe(ubs, "Equipe eSF", TipoEquipe.ESF);
        membro(equipeCaps, profissional("PSI", "90077788801"), FuncaoEquipe.PSICOLOGO);
        membro(equipeUbs, profissional("ENF", "90077788802"), FuncaoEquipe.ENFERMEIRO);
        maria = paciente("Maria", "90077788811");
        jose = paciente("José", "90077788812");
        psiquiatria = procedimentoRepository.save(new ProcedimentoRegulado(PREFIXO + "Consulta em psiquiatria", TipoProcedimentoRegulado.CONSULTA_ESPECIALIZADA, true));
    }

    @AfterEach
    void limpar() {
        SaudeMentalControllerTest.limparDados(jdbc, PREFIXO, MATRICULA);
    }

    private UnidadeDeSaude unidade(String nome) {
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + nome);
        u.setTipo(TipoUnidadeDeSaude.UBS);
        u.setAtivo(true);
        return unidadeDeSaudeRepository.save(u);
    }

    private Equipe equipe(UnidadeDeSaude u, String nome, TipoEquipe tipo) {
        Equipe e = new Equipe();
        e.setUnidade(u);
        e.setTipo(tipo);
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
        membroRepository.save(new MembroEquipe(null, e, p, funcao, LocalDate.now().minusMonths(6), null, null, null));
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
                        .formatted(p.getUuid(), caps.getUuid(), equipeCaps.getUuid(), MATRICULA)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return JsonPath.read(r, "$.uuid");
    }

    private ResultActions postar(String url, String corpo) throws Exception {
        return mockMvc.perform(post("/api/v1/saude-mental/" + url).contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    private String encaminhamento() {
        return """
                {"procedimentoId": "%s", "cid": "F32.1", "prioridade": "VERDE", "profissionalMatricula": "%sPSI",
                 "justificativa": "Episódio depressivo moderado sem resposta ao manejo na atenção psicossocial."}"""
                .formatted(psiquiatria.getUuid(), MATRICULA);
    }

    @Test
    void encaminharEVincularPelaRegulacao() throws Exception {
        String caso = abrir(maria);
        String outro = abrir(jose);
        postar("acompanhamento/" + caso + "/encaminhamento", "{\"procedimentoId\": \"%s\"}".formatted(psiquiatria.getUuid()))
                .andExpect(status().isBadRequest());
        postar("acompanhamento/" + caso + "/encaminhamento", encaminhamento()).andExpect(status().isCreated())
                .andExpect(jsonPath("$.encaminhamentos.length()").value(1))
                .andExpect(jsonPath("$.encaminhamentos[0].status").value("SOLICITADA"))
                .andExpect(jsonPath("$.encaminhamentos[0].procedimentoNome").value(PREFIXO + "Consulta em psiquiatria"));
        String r = postar("acompanhamento/" + outro + "/encaminhamento", encaminhamento()).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String solicitacaoDeJose = JsonPath.read(r, "$.encaminhamentos[0].solicitacaoId");
        postar("acompanhamento/" + caso + "/encaminhamento", "{\"solicitacaoId\": \"%s\"}".formatted(solicitacaoDeJose))
                .andExpect(status().isUnprocessableEntity());
        postar("acompanhamento/" + outro + "/encaminhamento", "{\"solicitacaoId\": \"%s\"}".formatted(solicitacaoDeJose))
                .andExpect(status().isConflict());
    }

    @Test
    void cuidadoCompartilhadoComoHistorico() throws Exception {
        String caso = abrir(maria);
        String corpo = "{\"equipeId\": \"%s\", \"objetivo\": \"Acompanhar a adesão à medicação na eSF.\"}";
        postar("acompanhamento/" + caso + "/compartilhamento", corpo.formatted(equipeCaps.getUuid())).andExpect(status().isUnprocessableEntity());
        String r = postar("acompanhamento/" + caso + "/compartilhamento", corpo.formatted(equipeUbs.getUuid())).andExpect(status().isCreated())
                .andExpect(jsonPath("$.compartilhamentos.length()").value(1))
                .andExpect(jsonPath("$.compartilhamentos[0].equipeNome").value("Equipe eSF"))
                .andReturn().getResponse().getContentAsString();
        postar("acompanhamento/" + caso + "/compartilhamento", corpo.formatted(equipeUbs.getUuid())).andExpect(status().isConflict());
        String id = JsonPath.read(r, "$.compartilhamentos[0].uuid");
        String fim = "acompanhamento/" + caso + "/compartilhamento/" + id + "/encerramento";
        postar(fim, "{\"motivo\": \"Estabilizada.\"}").andExpect(status().isOk())
                .andExpect(jsonPath("$.compartilhamentos[0].fim").value(LocalDate.now().toString()));
        postar(fim, "{\"motivo\": \"De novo.\"}").andExpect(status().isUnprocessableEntity());
    }

    @Test
    void matriciamentoPedeOApoioEntreAsEquipes() throws Exception {
        String caso = abrir(maria);
        String tipo = jdbc.queryForObject("select uuid::text from tb_tipo_intervencao_saude_mental where codigo = 'MATRICIAMENTO'", String.class);
        String sem = "{\"tipoId\": \"%s\", \"resumo\": \"Discussão do caso com a eSF.\"}".formatted(tipo);
        String com = "{\"tipoId\": \"%s\", \"resumo\": \"Discussão do caso com a eSF.\", \"equipeMatriciadaId\": \"%s\"}".formatted(tipo, equipeUbs.getUuid());
        postar("acompanhamento/" + caso + "/intervencao", sem).andExpect(status().isBadRequest());
        postar("acompanhamento/" + caso + "/intervencao", com).andExpect(status().isUnprocessableEntity());
        jdbc.update("insert into tb_equipe_apoio (equipe_id, apoiada_id) values (?, ?)", equipeCaps.getUuid(), equipeUbs.getUuid());
        postar("acompanhamento/" + caso + "/intervencao", com).andExpect(status().isCreated())
                .andExpect(jsonPath("$.intervencoes[0].equipeMatriciadaNome").value("Equipe eSF"));
        String visita = jdbc.queryForObject("select uuid::text from tb_tipo_intervencao_saude_mental where codigo = 'VISITA_DOMICILIAR'", String.class);
        postar("acompanhamento/" + caso + "/intervencao", "{\"tipoId\": \"%s\", \"resumo\": \"Visita.\", \"equipeMatriciadaId\": \"%s\"}"
                .formatted(visita, equipeUbs.getUuid())).andExpect(status().isBadRequest());
    }

    @Test
    void transferenciaDoCuidadoComAceite() throws Exception {
        String caso = abrir(maria);
        postar("acompanhamento/" + caso + "/rede-apoio", "{\"nome\": \"Ana\", \"papel\": \"FAMILIAR\"}").andExpect(status().isCreated());
        String proposta = "{\"unidadeDestinoId\": \"%s\", \"resumo\": \"Estável há 6 meses; segue na eSF com consulta mensal.\"}";
        postar("acompanhamento/" + caso + "/transferencia", proposta.formatted(caps.getUuid())).andExpect(status().isUnprocessableEntity());
        String r = postar("acompanhamento/" + caso + "/transferencia", proposta.formatted(ubs.getUuid())).andExpect(status().isCreated())
                .andExpect(jsonPath("$.transferencias[0].situacao").value("PROPOSTA"))
                .andReturn().getResponse().getContentAsString();
        postar("acompanhamento/" + caso + "/transferencia", proposta.formatted(ubs.getUuid())).andExpect(status().isConflict());
        postar("acompanhamento/" + caso + "/encerramento", "{\"motivoSaida\": \"ALTA\", \"observacao\": \"Alta.\"}")
                .andExpect(status().isUnprocessableEntity());
        String transferencia = JsonPath.read(r, "$.transferencias[0].uuid");
        mockMvc.perform(get("/api/v1/saude-mental/transferencias").param("unidadeId", ubs.getUuid().toString())).andExpect(status().isOk())
                .andExpect(jsonPath("$.recebidas.length()").value(1))
                .andExpect(jsonPath("$.recebidas[0].unidadeOrigemNome").value(PREFIXO + "CAPS"));

        String aceite = "{\"equipeId\": \"%s\", \"tecnicoMatricula\": \"%sENF\"}".formatted(equipeUbs.getUuid(), MATRICULA);
        String novo = postar("transferencia/" + transferencia + "/aceite", aceite).andExpect(status().isOk())
                .andExpect(jsonPath("$.unidadeNome").value(PREFIXO + "UBS"))
                .andExpect(jsonPath("$.casoAnteriorId").value(caso))
                .andExpect(jsonPath("$.equipeNome").value("Equipe eSF"))
                .andExpect(jsonPath("$.situacao").value("ATIVO"))
                .andExpect(jsonPath("$.redeApoio.length()").value(1))
                .andReturn().getResponse().getContentAsString();
        mockMvc.perform(get("/api/v1/saude-mental/acompanhamento/" + caso)).andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("ENCERRADO"))
                .andExpect(jsonPath("$.motivoSaida").value("TRANSFERENCIA"))
                .andExpect(jsonPath("$.transferencias[0].situacao").value("ACEITA"))
                .andExpect(jsonPath("$.transferencias[0].casoDestinoId").value((String) JsonPath.read(novo, "$.uuid")));
        postar("transferencia/" + transferencia + "/aceite", aceite).andExpect(status().isUnprocessableEntity());
        mockMvc.perform(get("/api/v1/paciente/" + maria.getUuid() + "/programas")).andExpect(status().isOk())
                .andExpect(jsonPath("$.vigentes.length()").value(1))
                .andExpect(jsonPath("$.vigentes[0].unidadeNome").value(PREFIXO + "UBS"));
    }

    @Test
    void transferenciaRecusadaECancelada() throws Exception {
        String caso = abrir(maria);
        String proposta = "{\"unidadeDestinoId\": \"%s\", \"resumo\": \"Mudou de bairro.\"}".formatted(ubs.getUuid());
        String r = postar("acompanhamento/" + caso + "/transferencia", proposta).andReturn().getResponse().getContentAsString();
        String t1 = JsonPath.read(r, "$.transferencias[0].uuid");
        postar("transferencia/" + t1 + "/recusa", "{\"motivo\": \"Fora da área da eSF.\"}").andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("RECUSADA"))
                .andExpect(jsonPath("$.motivoDecisao").value("Fora da área da eSF."));
        r = postar("acompanhamento/" + caso + "/transferencia", proposta).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String t2 = JsonPath.read(r, "$.transferencias[0].uuid");
        postar("transferencia/" + t2 + "/cancelamento", "{\"motivo\": \"Família decidiu ficar.\"}").andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("CANCELADA"));
        mockMvc.perform(get("/api/v1/saude-mental/acompanhamento/" + caso)).andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("ATIVO"))
                .andExpect(jsonPath("$.transferencias.length()").value(2));
    }
}
