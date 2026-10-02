package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.ProcedimentoRegulado;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.SolicitacaoRegulacao;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.TipoProcedimentoRegulado;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.Agendamento;
import com.edufelizardo.maissaudepublica.models.enuns.StatusAgendamento;
import com.edufelizardo.maissaudepublica.models.enuns.TipoAgendamento;
import com.edufelizardo.maissaudepublica.repositories.AgendamentoRepository;
import com.edufelizardo.maissaudepublica.repositories.EventoAuditoriaRepository;
import com.edufelizardo.maissaudepublica.repositories.EventoRegulacaoRepository;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
import com.edufelizardo.maissaudepublica.repositories.ProcedimentoReguladoRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.SolicitacaoRegulacaoRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Regulação do acesso (ADR-0087), com login e autorização desligados: catálogo, ciclo da solicitação,
 * fila e eventos. O escopo por unidade fica em {@link RegulacaoAutorizacaoControllerTest}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SolicitacaoRegulacaoControllerTest {

    private static final String SOLICITACAO_URL = "/api/v1/solicitacao-regulacao/";
    private static final String PROCEDIMENTO_URL = "/api/v1/procedimento-regulado/";
    private static final String PREFIXO = "Regul Teste ";
    private static final DateTimeFormatter ISO = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SolicitacaoRegulacaoRepository solicitacaoRepository;

    @Autowired
    private EventoRegulacaoRepository eventoRepository;

    @Autowired
    private ProcedimentoReguladoRepository procedimentoRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @Autowired
    private EventoAuditoriaRepository auditoriaRepository;

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    private UnidadeDeSaude ubs;
    private UnidadeDeSaude policlinica;
    private Profissional medico;
    private Profissional regulador;
    private ProcedimentoRegulado cardiologia;

    @BeforeEach
    void seed() {
        limpar();
        ubs = unidade("UBS", TipoUnidadeDeSaude.UBS);
        policlinica = unidade("Policlínica", TipoUnidadeDeSaude.POLICLINICA);
        medico = profissional("REGUL-MED", "75830142500");
        regulador = profissional("REGUL-REG", "94215036812");
        profissional("REGUL-EXE", "36184520989");
        cardiologia = procedimentoRepository.save(new ProcedimentoRegulado(PREFIXO + "Cardiologia",
                TipoProcedimentoRegulado.CONSULTA_ESPECIALIZADA, true));
    }

    @AfterEach
    void limpar() {
        Set<UUID> procedimentosDoTeste = procedimentoRepository.findAll().stream()
                .filter(p -> p.getNome().startsWith(PREFIXO)).map(ProcedimentoRegulado::getUuid).collect(Collectors.toSet());
        List<SolicitacaoRegulacao> solicitacoes = solicitacaoRepository.findAll().stream()
                .filter(s -> procedimentosDoTeste.contains(s.getProcedimento().getUuid())).toList();
        for (SolicitacaoRegulacao s : solicitacoes) {
            eventoRepository.deleteAll(eventoRepository.findBySolicitacao_UuidOrderByOcorridoEmAsc(s.getUuid()));
        }
        solicitacaoRepository.deleteAll(solicitacoes);
        // O agendamento criado pela regulação (ADR-0089) sai depois da solicitação que aponta para ele.
        for (Paciente p : pacienteRepository.findAll().stream()
                .filter(p -> p.getNome() != null && p.getNome().startsWith(PREFIXO)).toList()) {
            agendamentoRepository.deleteAll(agendamentoRepository.findByPaciente_UuidOrderByDataHoraAsc(p.getUuid()));
        }
        procedimentoRepository.deleteAll(procedimentoRepository.findAll().stream()
                .filter(p -> p.getNome().startsWith(PREFIXO)).toList());
        pacienteRepository.deleteAll(pacienteRepository.findAll().stream()
                .filter(p -> p.getNome() != null && p.getNome().startsWith(PREFIXO)).toList());
        profissionalRepository.deleteAll(profissionalRepository.findAll().stream()
                .filter(p -> p.getMatricula() != null && p.getMatricula().startsWith("REGUL-")).toList());
        unidadeDeSaudeRepository.deleteAll(unidadeDeSaudeRepository.findAll().stream()
                .filter(u -> u.getNome() != null && u.getNome().startsWith(PREFIXO)).toList());
    }

    // ── Apoio ──────────────────────────────────────────────────────────────────────────────────

    private UnidadeDeSaude unidade(String nome, TipoUnidadeDeSaude tipo) {
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + nome);
        u.setTipo(tipo);
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

    private Paciente paciente(String nome) {
        Paciente p = new Paciente();
        p.setNome(PREFIXO + nome);
        p.setAtivo(true);
        return pacienteRepository.save(p);
    }

    private ResultActions solicitar(Paciente paciente, ProcedimentoRegulado procedimento, String prioridade, String cid) throws Exception {
        return mockMvc.perform(post(SOLICITACAO_URL).contentType(MediaType.APPLICATION_JSON).content("""
                {"pacienteId": "%s", "procedimentoId": "%s", "unidadeSolicitanteId": "%s", "profissionalMatricula": "REGUL-MED",
                 "cid": "%s", "justificativa": "Dor precordial aos esforços há três meses, ECG alterado.", "prioridade": "%s"}
                """.formatted(paciente.getUuid(), procedimento.getUuid(), ubs.getUuid(), cid, prioridade)));
    }

    private UUID solicitada(Paciente paciente, String prioridade) throws Exception {
        solicitar(paciente, cardiologia, prioridade, "I20.9").andExpect(status().isCreated());
        return solicitacaoRepository.findByPaciente_Uuid(paciente.getUuid()).get(0).getUuid();
    }

    private ResultActions acao(UUID uuid, String rota, String corpo) throws Exception {
        return mockMvc.perform(post(SOLICITACAO_URL + uuid + "/" + rota).contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    private String autorizacao(String matricula, LocalDateTime quando) {
        return """
                {"profissionalMatricula": "%s", "unidadeExecutanteId": "%s", "dataHoraPrevista": "%s", "observacao": "Trazer ECG"}
                """.formatted(matricula, policlinica.getUuid(), ISO.format(quando));
    }

    private static String motivo(String matricula, String texto) {
        return """
                {"profissionalMatricula": "%s", "motivo": "%s"}
                """.formatted(matricula, texto);
    }

    // ── Catálogo ───────────────────────────────────────────────────────────────────────────────

    @Test
    void catalogoCriaEditaRecusaNomeRepetidoEFiltraAtivos() throws Exception {
        mockMvc.perform(post(PROCEDIMENTO_URL).contentType(MediaType.APPLICATION_JSON).content("""
                        {"nome": "%s", "tipo": "EXAME"}
                        """.formatted(PREFIXO + "Ecocardiograma")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Procedimento regulado cadastrado com sucesso!"));
        mockMvc.perform(post(PROCEDIMENTO_URL).contentType(MediaType.APPLICATION_JSON).content("""
                        {"nome": "%s", "tipo": "EXAME"}
                        """.formatted((PREFIXO + "ecocardiograma").toUpperCase())))
                .andExpect(status().isConflict());

        UUID eco = procedimentoRepository.findByNomeIgnoreCase(PREFIXO + "Ecocardiograma").orElseThrow().getUuid();
        mockMvc.perform(patch(PROCEDIMENTO_URL + eco).contentType(MediaType.APPLICATION_JSON).content("""
                        {"nome": "%s", "tipo": "EXAME", "ativo": false}
                        """.formatted(PREFIXO + "Ecocardiograma transtorácico")))
                .andExpect(status().isOk());
        mockMvc.perform(get(PROCEDIMENTO_URL + eco))
                .andExpect(jsonPath("$.nome").value(PREFIXO + "Ecocardiograma transtorácico"))
                .andExpect(jsonPath("$.ativo").value(false));
        mockMvc.perform(get(PROCEDIMENTO_URL + "?ativos=true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.uuid == '%s')]".formatted(eco)).isEmpty());
        mockMvc.perform(get(PROCEDIMENTO_URL + UUID.randomUUID())).andExpect(status().isNotFound());
    }

    // ── Solicitação ────────────────────────────────────────────────────────────────────────────

    @Test
    void solicitaComCidNormalizadoPrimeiroEventoEPosicaoNaFila() throws Exception {
        Paciente ana = paciente("Ana");
        solicitar(ana, cardiologia, "AMARELO", " e11.9 ")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.details").value(org.hamcrest.Matchers.containsString("Posição na fila: 1")));
        UUID uuid = solicitacaoRepository.findByPaciente_Uuid(ana.getUuid()).get(0).getUuid();

        mockMvc.perform(get(SOLICITACAO_URL + uuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SOLICITADA"))
                .andExpect(jsonPath("$.cid").value("E119"))
                .andExpect(jsonPath("$.posicaoNaFila").value(1))
                .andExpect(jsonPath("$.eventos.length()").value(1))
                .andExpect(jsonPath("$.eventos[0].tipo").value("SOLICITACAO"))
                .andExpect(jsonPath("$.eventos[0].profissionalMatricula").value("REGUL-MED"));

        assertThat(auditoriaRepository.findByRegistroIdOrderByOcorridoEmDesc(uuid))
                .anyMatch(e -> "GET".equals(e.getMetodo()) && ana.getUuid().equals(e.getPacienteId()));
    }

    @Test
    void recusaCidInvalidoProcedimentoForaDeUsoESegundaSolicitacaoEmAberto() throws Exception {
        Paciente bia = paciente("Bia");
        solicitar(bia, cardiologia, "VERDE", "XYZ").andExpect(status().isBadRequest());

        ProcedimentoRegulado inativo = procedimentoRepository.save(new ProcedimentoRegulado(PREFIXO + "Fora de uso",
                TipoProcedimentoRegulado.EXAME, false));
        solicitar(bia, inativo, "VERDE", "I10").andExpect(status().isUnprocessableEntity());

        solicitar(bia, cardiologia, "VERDE", "I10").andExpect(status().isCreated());
        solicitar(bia, cardiologia, "AMARELO", "I10").andExpect(status().isConflict());
    }

    @Test
    void filaOrdenaPorPrioridadeEHoraDoPedidoEListagemNaoTrazDadoClinico() throws Exception {
        UUID azul = solicitada(paciente("Azul"), "AZUL");
        UUID vermelho = solicitada(paciente("Vermelho"), "VERMELHO");
        UUID verde = solicitada(paciente("Verde"), "VERDE");

        mockMvc.perform(get(SOLICITACAO_URL + "fila?procedimentoId=" + cardiologia.getUuid()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].uuid").value(vermelho.toString()))
                .andExpect(jsonPath("$[0].posicaoNaFila").value(1))
                .andExpect(jsonPath("$[1].uuid").value(verde.toString()))
                .andExpect(jsonPath("$[2].uuid").value(azul.toString()))
                .andExpect(jsonPath("$[2].posicaoNaFila").value(3));

        mockMvc.perform(get(SOLICITACAO_URL + "?procedimentoId=" + cardiologia.getUuid()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].cid").doesNotExist())
                .andExpect(jsonPath("$[0].justificativa").doesNotExist());

        // Reclassificar muda a ordem; a mesma prioridade é recusada.
        acao(azul, "reclassificacao", """
                {"profissionalMatricula": "REGUL-REG", "prioridade": "AZUL", "motivo": "Sem mudança"}
                """).andExpect(status().isUnprocessableEntity());
        acao(azul, "reclassificacao", """
                {"profissionalMatricula": "REGUL-REG", "prioridade": "VERMELHO", "motivo": "Troponina alterada"}
                """).andExpect(status().isOk());
        mockMvc.perform(get(SOLICITACAO_URL + "fila?procedimentoId=" + cardiologia.getUuid()))
                .andExpect(jsonPath("$[0].uuid").value(azul.toString()))
                .andExpect(jsonPath("$[1].uuid").value(vermelho.toString()));
    }

    @Test
    void devolvidaSaiDaFilaEComplementadaVoltaNaPosicaoOriginal() throws Exception {
        UUID primeira = solicitada(paciente("Primeira"), "VERDE");
        UUID segunda = solicitada(paciente("Segunda"), "VERDE");

        acao(primeira, "devolucao", motivo("REGUL-REG", "")).andExpect(status().isBadRequest());
        acao(primeira, "devolucao", motivo("REGUL-REG", "Anexar o ECG")).andExpect(status().isOk());
        mockMvc.perform(get(SOLICITACAO_URL + "fila?procedimentoId=" + cardiologia.getUuid()))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].uuid").value(segunda.toString()));
        acao(primeira, "autorizacao", autorizacao("REGUL-REG", LocalDateTime.now().plusDays(3)))
                .andExpect(status().isUnprocessableEntity());

        acao(primeira, "complemento", """
                {"profissionalMatricula": "REGUL-MED", "complemento": "ECG de 10/09 com infradesnivelamento de ST."}
                """).andExpect(status().isOk());
        mockMvc.perform(get(SOLICITACAO_URL + "fila?procedimentoId=" + cardiologia.getUuid()))
                .andExpect(jsonPath("$[0].uuid").value(primeira.toString()))
                .andExpect(jsonPath("$[0].posicaoNaFila").value(1));
        mockMvc.perform(get(SOLICITACAO_URL + primeira))
                .andExpect(jsonPath("$.status").value("SOLICITADA"))
                .andExpect(jsonPath("$.eventos.length()").value(3))
                .andExpect(jsonPath("$.eventos[1].tipo").value("DEVOLUCAO"))
                .andExpect(jsonPath("$.eventos[1].texto").value("Anexar o ECG"))
                .andExpect(jsonPath("$.eventos[2].tipo").value("COMPLEMENTO"));
    }

    @Test
    void autorizaComVagaPorOutroReguladorERecusaQuemSolicitouEDataPassada() throws Exception {
        UUID uuid = solicitada(paciente("Carla"), "AMARELO");
        LocalDateTime vaga = LocalDateTime.now().plusDays(7).withHour(9).withMinute(30).withSecond(0).withNano(0);

        acao(uuid, "autorizacao", autorizacao("REGUL-MED", vaga)).andExpect(status().isUnprocessableEntity());
        acao(uuid, "autorizacao", autorizacao("REGUL-REG", LocalDateTime.now().minusDays(1)))
                .andExpect(status().isUnprocessableEntity());
        acao(uuid, "autorizacao", autorizacao("REGUL-REG", vaga))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Solicitação autorizada com sucesso!"));

        mockMvc.perform(get(SOLICITACAO_URL + uuid))
                .andExpect(jsonPath("$.status").value("AUTORIZADA"))
                .andExpect(jsonPath("$.posicaoNaFila").doesNotExist())
                .andExpect(jsonPath("$.unidadeExecutanteId").value(policlinica.getUuid().toString()))
                .andExpect(jsonPath("$.dataHoraPrevista").value(org.hamcrest.Matchers.startsWith(vaga.toLocalDate().toString())))
                .andExpect(jsonPath("$.eventos[1].tipo").value("AUTORIZACAO"))
                .andExpect(jsonPath("$.eventos[1].texto").value(org.hamcrest.Matchers.containsString("Trazer ECG")));

        acao(uuid, "autorizacao", autorizacao("REGUL-REG", vaga)).andExpect(status().isUnprocessableEntity());
        acao(uuid, "cancelamento", motivo("REGUL-MED", "Paciente mudou de município")).andExpect(status().isOk());
        acao(uuid, "cancelamento", motivo("REGUL-MED", "De novo")).andExpect(status().isUnprocessableEntity());
        mockMvc.perform(get(SOLICITACAO_URL + "?status=CANCELADA&procedimentoId=" + cardiologia.getUuid()))
                .andExpect(jsonPath("$.length()").value(1));
    }

    // ── Fechamento do ciclo (ADR-0089) ─────────────────────────────────────────────────────────

    private Agendamento agendamentoDe(UUID solicitacao) {
        return agendamentoRepository.findById(solicitacaoRepository.findById(solicitacao).orElseThrow()
                .getAgendamento().getUuid()).orElseThrow();
    }

    @Test
    void autorizarComOExecutanteJaAgendaECancelarCancelaOAgendamento() throws Exception {
        UUID uuid = solicitada(paciente("Elisa"), "AMARELO");
        LocalDateTime vaga = LocalDateTime.now().plusDays(5).withHour(14).withMinute(0).withSecond(0).withNano(0);
        acao(uuid, "autorizacao", """
                {"profissionalMatricula": "REGUL-REG", "unidadeExecutanteId": "%s", "dataHoraPrevista": "%s",
                 "profissionalExecutanteMatricula": "REGUL-EXE"}
                """.formatted(policlinica.getUuid(), ISO.format(vaga))).andExpect(status().isOk());

        mockMvc.perform(get(SOLICITACAO_URL + uuid))
                .andExpect(jsonPath("$.status").value("AGENDADA"))
                .andExpect(jsonPath("$.profissionalExecutanteMatricula").value("REGUL-EXE"))
                .andExpect(jsonPath("$.agendamentoId").isNotEmpty())
                .andExpect(jsonPath("$.eventos[2].tipo").value("AGENDAMENTO"));
        Agendamento agendamento = agendamentoDe(uuid);
        assertThat(agendamento.getStatus()).isEqualTo(StatusAgendamento.AGENDADO);
        assertThat(agendamento.getTipo()).isEqualTo(TipoAgendamento.CONSULTA);
        assertThat(agendamento.getDataHora()).isEqualTo(vaga);

        acao(uuid, "cancelamento", motivo("REGUL-MED", "Paciente internado")).andExpect(status().isOk());
        assertThat(agendamentoDe(uuid).getStatus()).isEqualTo(StatusAgendamento.CANCELADO);
    }

    @Test
    void executanteAgendaDepoisERegistraARealizacaoComContrarreferencia() throws Exception {
        UUID uuid = solicitada(paciente("Fabio"), "VERDE");
        String agendar = """
                {"profissionalMatricula": "REGUL-REG", "profissionalExecutanteMatricula": "REGUL-EXE"}
                """;
        String realizar = """
                {"profissionalMatricula": "REGUL-EXE", "contrarreferencia": "Ecocardiograma normal. Manter seguimento na UBS."}
                """;
        acao(uuid, "agendamento", agendar).andExpect(status().isUnprocessableEntity());

        acao(uuid, "autorizacao", autorizacao("REGUL-REG", LocalDateTime.now().plusDays(3))).andExpect(status().isOk());
        acao(uuid, "realizacao", realizar).andExpect(status().isUnprocessableEntity());
        acao(uuid, "agendamento", agendar).andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Solicitação agendada na unidade executante!"));
        acao(uuid, "realizacao", """
                {"profissionalMatricula": "REGUL-EXE", "contrarreferencia": "curto"}
                """).andExpect(status().isBadRequest());
        acao(uuid, "realizacao", realizar).andExpect(status().isOk());

        mockMvc.perform(get(SOLICITACAO_URL + uuid))
                .andExpect(jsonPath("$.status").value("REALIZADA"))
                .andExpect(jsonPath("$.contrarreferencia").value("Ecocardiograma normal. Manter seguimento na UBS."))
                .andExpect(jsonPath("$.concluidoEm").isNotEmpty())
                .andExpect(jsonPath("$.eventos[3].tipo").value("REALIZACAO"));
        assertThat(agendamentoDe(uuid).getStatus()).isEqualTo(StatusAgendamento.REALIZADO);
        // Realizada não se cancela.
        acao(uuid, "cancelamento", motivo("REGUL-MED", "Tarde")).andExpect(status().isUnprocessableEntity());
    }

    @Test
    void faltaFechaOAgendamentoComAFaltaNaObservacao() throws Exception {
        UUID uuid = solicitada(paciente("Gina"), "AZUL");
        acao(uuid, "autorizacao", """
                {"profissionalMatricula": "REGUL-REG", "unidadeExecutanteId": "%s", "dataHoraPrevista": "%s",
                 "profissionalExecutanteMatricula": "REGUL-EXE"}
                """.formatted(policlinica.getUuid(), ISO.format(LocalDateTime.now().plusDays(2)))).andExpect(status().isOk());
        acao(uuid, "falta", motivo("REGUL-EXE", "Não compareceu e não avisou")).andExpect(status().isOk());

        mockMvc.perform(get(SOLICITACAO_URL + uuid))
                .andExpect(jsonPath("$.status").value("FALTOU"))
                .andExpect(jsonPath("$.eventos[3].tipo").value("FALTA"));
        Agendamento agendamento = agendamentoDe(uuid);
        assertThat(agendamento.getStatus()).isEqualTo(StatusAgendamento.CANCELADO);
        assertThat(agendamento.getObservacao()).contains("Paciente faltou");
        acao(uuid, "falta", motivo("REGUL-EXE", "De novo")).andExpect(status().isUnprocessableEntity());
    }

    @Test
    void negaComMotivoENaoPermiteQuemSolicitouNegar() throws Exception {
        UUID uuid = solicitada(paciente("Davi"), "AZUL");
        acao(uuid, "negativa", motivo("REGUL-MED", "Não indicado")).andExpect(status().isUnprocessableEntity());
        acao(uuid, "negativa", motivo("REGUL-REG", "Critério de encaminhamento não atendido"))
                .andExpect(status().isOk());
        mockMvc.perform(get(SOLICITACAO_URL + uuid))
                .andExpect(jsonPath("$.status").value("NEGADA"))
                .andExpect(jsonPath("$.eventos[1].tipo").value("NEGATIVA"));
        acao(uuid, "cancelamento", motivo("REGUL-MED", "Tarde demais")).andExpect(status().isUnprocessableEntity());
        mockMvc.perform(get(SOLICITACAO_URL + UUID.randomUUID())).andExpect(status().isNotFound());
    }
}
