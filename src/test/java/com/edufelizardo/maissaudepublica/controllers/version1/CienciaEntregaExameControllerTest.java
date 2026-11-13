package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.ExameLaboratorial;
import com.edufelizardo.maissaudepublica.models.ItemPedidoExame;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.PedidoExame;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.MaterialExame;
import com.edufelizardo.maissaudepublica.models.enuns.TipoResultadoExame;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.ExameLaboratorialRepository;
import com.edufelizardo.maissaudepublica.repositories.ItemPedidoExameRepository;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
import com.edufelizardo.maissaudepublica.repositories.PedidoExameRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import com.edufelizardo.maissaudepublica.services.version1.EnvioDeEmail;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Ciência do solicitante, comunicação do valor crítico e entrega do resultado ao paciente (ADR-0181), com login e
 * autorização desligados. O escopo fica em {@link LaboratorioAutorizacaoControllerTest}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CienciaEntregaExameControllerTest {

    private static final String PEDIDO = "/api/v1/pedido-exame/";
    private static final String PREFIXO = "Ciencia Teste ";
    private static final String EMAIL_MEDICO = "medico.ciencia@prefeitura.teste";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @MockitoBean
    private EnvioDeEmail envioDeEmail;

    @Autowired
    private ExameLaboratorialRepository exameRepository;

    @Autowired
    private PedidoExameRepository pedidoRepository;

    @Autowired
    private ItemPedidoExameRepository itemRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    private UnidadeDeSaude ubs;
    private UnidadeDeSaude laboratorio;
    private Paciente paciente;
    private ExameLaboratorial hemoglobina;
    private ExameLaboratorial potassio;

    @BeforeEach
    void seed() {
        limpar();
        ubs = unidade("UBS", TipoUnidadeDeSaude.UBS);
        laboratorio = unidade("Laboratório", TipoUnidadeDeSaude.LABORATORIO);
        profissional("CE-MED", "13579246828", EMAIL_MEDICO);
        profissional("CE-OUT", "24680135735", null);
        profissional("CE-TEC", "35791357915", null);
        paciente = new Paciente();
        paciente.setNome(PREFIXO + "Paciente");
        paciente.setAtivo(true);
        paciente = pacienteRepository.save(paciente);
        hemoglobina = exame("Hemoglobina", "g/dL", "12", "16", "7", "20");
        potassio = exame("Potássio", "mEq/L", "3.5", "5.1", "2.5", "6.5");
    }

    @AfterEach
    void limpar() {
        List<UUID> pacientes = pacienteRepository.findAll().stream()
                .filter(p -> p.getNome() != null && p.getNome().startsWith(PREFIXO)).map(Paciente::getUuid).toList();
        for (UUID p : pacientes) {
            for (PedidoExame pedido : pedidoRepository.findByPaciente_UuidOrderBySolicitadoEmDesc(p)) {
                UUID id = pedido.getUuid();
                jdbc.update("delete from tb_comunicacao_critico_exame where item_id in (select uuid from tb_item_pedido_exame where pedido_id = ?)", id);
                jdbc.update("delete from tb_entrega_resultado_exame where pedido_id = ?", id);
                jdbc.update("update tb_item_pedido_exame set resultado_atual_id = null, amostra_id = null where pedido_id = ?", id);
                jdbc.update("update tb_resultado_exame set retificacao_de_id = null where item_id in (select uuid from tb_item_pedido_exame where pedido_id = ?)", id);
                jdbc.update("delete from tb_resultado_exame where item_id in (select uuid from tb_item_pedido_exame where pedido_id = ?)", id);
                jdbc.update("delete from tb_evento_exame where pedido_id = ?", id);
                jdbc.update("delete from tb_item_pedido_exame where pedido_id = ?", id);
                jdbc.update("delete from tb_amostra_exame where pedido_id = ?", id);
                jdbc.update("delete from tb_pedido_exame where uuid = ?", id);
            }
        }
        pacienteRepository.deleteAllById(pacientes);
        exameRepository.deleteAll(exameRepository.findAll().stream().filter(e -> e.getNome().startsWith(PREFIXO)).toList());
        profissionalRepository.deleteAll(profissionalRepository.findAll().stream()
                .filter(p -> p.getMatricula() != null && p.getMatricula().startsWith("CE-")).toList());
        unidadeDeSaudeRepository.deleteAll(unidadeDeSaudeRepository.findAll().stream()
                .filter(u -> u.getNome() != null && u.getNome().startsWith(PREFIXO)).toList());
    }

    // ── Ciência ────────────────────────────────────────────────────────────────────────────────

    @Test
    void soQuemPediuDaCienciaUmaVezEARetificacaoReabre() throws Exception {
        UUID pedido = pedido(hemoglobina);
        UUID item = item(pedido, hemoglobina);
        liberado(item, "13");

        mockMvc.perform(get(PEDIDO + "para-ciencia")).andExpect(status().isBadRequest());
        mockMvc.perform(get(PEDIDO + "para-ciencia?matricula=CE-MED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].itemId").value(item.toString()))
                .andExpect(jsonPath("$[0].interpretacao").value("NORMAL"))
                .andExpect(jsonPath("$[0].critico").value(false))
                .andExpect(jsonPath("$[0].solicitanteMatricula").value("CE-MED"));
        mockMvc.perform(get(PEDIDO + "para-ciencia?matricula=CE-OUT")).andExpect(jsonPath("$", hasSize(0)));

        acao(item, "ciencia", "{\"profissionalMatricula\": \"CE-OUT\"}").andExpect(status().isUnprocessableEntity());
        acao(item, "ciencia", "{\"profissionalMatricula\": \"CE-MED\"}").andExpect(status().isOk());
        acao(item, "ciencia", "{\"profissionalMatricula\": \"CE-MED\"}").andExpect(status().isConflict());
        mockMvc.perform(post(PEDIDO + pedido + "/ciencia").contentType(MediaType.APPLICATION_JSON)
                .content("{\"profissionalMatricula\": \"CE-MED\"}")).andExpect(status().isConflict());
        mockMvc.perform(get(PEDIDO + pedido))
                .andExpect(jsonPath("$.itens[0].resultado.cienciaPorMatricula").value("CE-MED"))
                .andExpect(jsonPath("$.itens[0].resultado.cienciaEm", notNullValue()))
                .andExpect(jsonPath("$.eventos[-1].tipo").value("CIENCIA"));
        mockMvc.perform(get(PEDIDO + "para-ciencia?matricula=CE-MED")).andExpect(jsonPath("$", hasSize(0)));

        // A retificação grava outro resultado: a ciência volta a ficar pendente.
        acao(item, "retificacao", """
                {"profissionalMatricula": "CE-TEC", "valorNumerico": 11, "motivo": "Troca de amostra na digitação."}
                """).andExpect(status().isOk());
        mockMvc.perform(get(PEDIDO + "para-ciencia?matricula=CE-MED"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].retificado").value(true))
                .andExpect(jsonPath("$[0].interpretacao").value("ABAIXO"));
        mockMvc.perform(get(PEDIDO + pedido)).andExpect(jsonPath("$.itens[0].resultado.cienciaEm", nullValue()));
    }

    @Test
    void cienciaDoPedidoInteiroSoNosLiberados() throws Exception {
        UUID pedido = pedido(hemoglobina, potassio);
        UUID hb = item(pedido, hemoglobina);
        UUID k = item(pedido, potassio);
        liberado(hb, "13");
        acao(k, "resultado", "{\"profissionalMatricula\": \"CE-TEC\", \"valorNumerico\": 4.2}").andExpect(status().isOk());

        mockMvc.perform(post(PEDIDO + pedido + "/ciencia").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"profissionalMatricula\": \"CE-MED\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(get(PEDIDO + pedido))
                .andExpect(jsonPath("$.itens[?(@.uuid == '%s')].resultado.cienciaEm".formatted(hb), hasSize(1)))
                .andExpect(jsonPath("$.itens[?(@.uuid == '%s')].resultado.cienciaPorMatricula".formatted(k)).value(org.hamcrest.Matchers.contains(nullValue())));
        // O potássio, liberado depois, fica pendente sozinho.
        acao(k, "liberacao", "{\"profissionalMatricula\": \"CE-TEC\"}").andExpect(status().isOk());
        mockMvc.perform(get(PEDIDO + "para-ciencia?matricula=CE-MED"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].itemId").value(k.toString()));
    }

    // ── Crítico ────────────────────────────────────────────────────────────────────────────────

    @Test
    void criticoVemPrimeiroAvisaPorEmailEOLaboratorioRegistraAComunicacao() throws Exception {
        when(envioDeEmail.disponivel()).thenReturn(true);
        when(envioDeEmail.enviar(anyString(), anyString(), anyString())).thenReturn(true);
        UUID pedido = pedido(hemoglobina, potassio);
        UUID hb = item(pedido, hemoglobina);
        UUID k = item(pedido, potassio);
        liberado(hb, "13");
        verify(envioDeEmail, never()).enviar(anyString(), anyString(), anyString());
        liberado(k, "7.2");
        ArgumentCaptor<String> texto = ArgumentCaptor.forClass(String.class);
        verify(envioDeEmail, times(1)).enviar(eq(EMAIL_MEDICO), anyString(), texto.capture());
        assertThat(texto.getValue()).contains("valor crítico").doesNotContain(PREFIXO).doesNotContain("7.2").doesNotContain("Potássio");

        mockMvc.perform(get(PEDIDO + "para-ciencia?matricula=CE-MED"))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].itemId").value(k.toString()))
                .andExpect(jsonPath("$[0].interpretacao").value("CRITICO_ALTO"))
                .andExpect(jsonPath("$[0].critico").value(true))
                .andExpect(jsonPath("$[0].comunicadoEm", nullValue()));
        mockMvc.perform(get(PEDIDO + "criticos-a-comunicar"))
                .andExpect(jsonPath("$[?(@.itemId == '%s')]".formatted(k), hasSize(1)))
                .andExpect(jsonPath("$[?(@.itemId == '%s')]".formatted(hb), hasSize(0)));

        String comunicacao = """
                {"profissionalMatricula": "CE-TEC", "receptorNome": "Dra. Plantonista", "meio": "TELEFONE"%s}
                """;
        acao(hb, "comunicacao-critico", comunicacao.formatted("")).andExpect(status().isUnprocessableEntity());
        acao(k, "comunicacao-critico", comunicacao.formatted(", \"comunicadoEm\": \"" + LocalDateTime.now().plusHours(2).withNano(0) + "\""))
                .andExpect(status().isUnprocessableEntity());
        acao(k, "comunicacao-critico", "{\"profissionalMatricula\": \"CE-TEC\", \"meio\": \"TELEFONE\"}").andExpect(status().isBadRequest());
        acao(k, "comunicacao-critico", comunicacao.formatted("")).andExpect(status().isOk());
        acao(k, "comunicacao-critico", comunicacao.formatted("")).andExpect(status().isConflict());

        mockMvc.perform(get(PEDIDO + pedido))
                .andExpect(jsonPath("$.comunicacoesCritico", hasSize(1)))
                .andExpect(jsonPath("$.comunicacoesCritico[0].receptorNome").value("Dra. Plantonista"))
                .andExpect(jsonPath("$.comunicacoesCritico[0].meio").value("TELEFONE"))
                .andExpect(jsonPath("$.comunicacoesCritico[0].profissionalMatricula").value("CE-TEC"))
                .andExpect(jsonPath("$.eventos[-1].tipo").value("COMUNICACAO_CRITICO"))
                .andExpect(jsonPath("$.eventos[-1].texto").value(containsString("Dra. Plantonista (telefone)")));
        mockMvc.perform(get(PEDIDO + "criticos-a-comunicar")).andExpect(jsonPath("$[?(@.itemId == '%s')]".formatted(k), hasSize(0)));
        mockMvc.perform(get(PEDIDO + "para-ciencia?matricula=CE-MED")).andExpect(jsonPath("$[0].comunicadoEm", notNullValue()));
    }

    @Test
    void cienciaDoSolicitanteTiraOCriticoDaListaDoLaboratorio() throws Exception {
        UUID pedido = pedido(potassio);
        UUID k = item(pedido, potassio);
        liberado(k, "2.1");
        mockMvc.perform(get(PEDIDO + "criticos-a-comunicar"))
                .andExpect(jsonPath("$[?(@.itemId == '%s')].interpretacao".formatted(k)).value("CRITICO_BAIXO"));
        acao(k, "ciencia", "{\"profissionalMatricula\": \"CE-MED\"}").andExpect(status().isOk());
        mockMvc.perform(get(PEDIDO + "criticos-a-comunicar")).andExpect(jsonPath("$[?(@.itemId == '%s')]".formatted(k), hasSize(0)));
        // Sem SMTP, nada sai, e a liberação não falha.
        verify(envioDeEmail, never()).enviar(anyString(), anyString(), anyString());
    }

    // ── Entrega ────────────────────────────────────────────────────────────────────────────────

    @Test
    void entregaSoComTudoLiberadoTerceiroIdentificadoESegundaVia() throws Exception {
        UUID pedido = pedido(hemoglobina, potassio);
        UUID hb = item(pedido, hemoglobina);
        UUID k = item(pedido, potassio);
        liberado(hb, "13");
        String peloPaciente = """
                {"profissionalMatricula": "CE-TEC", "unidadeId": "%s", "retiradoPeloPaciente": true, "forma": "IMPRESSO_NA_UNIDADE"}
                """.formatted(ubs.getUuid());
        entrega(pedido, peloPaciente).andExpect(status().isUnprocessableEntity());
        acao(k, "cancelamento", "{\"profissionalMatricula\": \"CE-MED\", \"motivo\": \"Pedido em duplicidade.\"}").andExpect(status().isOk());

        entrega(pedido, """
                {"profissionalMatricula": "CE-TEC", "unidadeId": "%s", "retiradoPeloPaciente": false, "terceiroNome": "Maria",
                 "forma": "IMPRESSO_NA_UNIDADE"}
                """.formatted(ubs.getUuid())).andExpect(status().isBadRequest());
        entrega(pedido, "{\"profissionalMatricula\": \"CE-TEC\", \"unidadeId\": \"%s\", \"retiradoPeloPaciente\": true}".formatted(ubs.getUuid()))
                .andExpect(status().isBadRequest());
        entrega(pedido, peloPaciente).andExpect(status().isOk());
        entrega(pedido, """
                {"profissionalMatricula": "CE-TEC", "unidadeId": "%s", "retiradoPeloPaciente": false, "terceiroNome": "Maria da Silva",
                 "terceiroDocumento": "RG 12.345.678-9", "terceiroParentesco": "Mãe", "forma": "IMPRESSO_NA_UNIDADE"}
                """.formatted(ubs.getUuid())).andExpect(status().isOk());

        mockMvc.perform(get(PEDIDO + pedido))
                .andExpect(jsonPath("$.entregas", hasSize(2)))
                .andExpect(jsonPath("$.entregas[0].retiradoPeloPaciente").value(true))
                .andExpect(jsonPath("$.entregas[0].terceiroNome", nullValue()))
                .andExpect(jsonPath("$.entregas[0].unidadeNome").value(PREFIXO + "UBS"))
                .andExpect(jsonPath("$.entregas[1].terceiroNome").value("Maria da Silva"))
                .andExpect(jsonPath("$.entregas[1].terceiroDocumento").value("RG 12.345.678-9"))
                .andExpect(jsonPath("$.entregas[1].terceiroParentesco").value("Mãe"))
                .andExpect(jsonPath("$.eventos[-1].tipo").value("ENTREGA"))
                .andExpect(jsonPath("$.eventos[-1].texto").value("Segunda via entregue impresso a Maria da Silva (Mãe)"));
    }

    @Test
    void pedidoSemNadaLiberadoNaoTemEntrega() throws Exception {
        UUID pedido = pedido(hemoglobina);
        acao(item(pedido, hemoglobina), "cancelamento", "{\"profissionalMatricula\": \"CE-MED\", \"motivo\": \"Desistência.\"}")
                .andExpect(status().isOk());
        entrega(pedido, """
                {"profissionalMatricula": "CE-TEC", "unidadeId": "%s", "retiradoPeloPaciente": true, "forma": "NA_CONSULTA"}
                """.formatted(ubs.getUuid())).andExpect(status().isUnprocessableEntity());
    }

    // ── Apoio ──────────────────────────────────────────────────────────────────────────────────

    private UnidadeDeSaude unidade(String nome, TipoUnidadeDeSaude tipo) {
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + nome);
        u.setTipo(tipo);
        u.setAtivo(true);
        return unidadeDeSaudeRepository.save(u);
    }

    private void profissional(String matricula, String cpf, String email) {
        Profissional p = new Profissional();
        p.setMatricula(matricula);
        p.setCpf(cpf);
        p.setNome(PREFIXO + matricula);
        p.setEmail(email);
        p.setAtivo(true);
        profissionalRepository.save(p);
    }

    private ExameLaboratorial exame(String nome, String unidade, String min, String max, String critMin, String critMax) {
        ExameLaboratorial e = new ExameLaboratorial();
        e.setNome(PREFIXO + nome);
        e.setMaterial(MaterialExame.SANGUE);
        e.setTipoResultado(TipoResultadoExame.NUMERICO);
        e.setUnidadeMedida(unidade);
        e.setReferenciaMinima(new BigDecimal(min));
        e.setReferenciaMaxima(new BigDecimal(max));
        e.setCriticoMinimo(new BigDecimal(critMin));
        e.setCriticoMaximo(new BigDecimal(critMax));
        e.setAtivo(true);
        return exameRepository.save(e);
    }

    /** Pede os exames pelo CE-MED e coleta todos; devolve o pedido. */
    private UUID pedido(ExameLaboratorial... exames) throws Exception {
        String ids = String.join("\", \"", Stream.of(exames).map(e -> e.getUuid().toString()).toList());
        mockMvc.perform(post(PEDIDO).contentType(MediaType.APPLICATION_JSON).content("""
                {"pacienteId": "%s", "unidadeSolicitanteId": "%s", "profissionalMatricula": "CE-MED", "exameIds": ["%s"],
                 "indicacaoClinica": "Controle de anemia e de função renal.", "prioridade": "ROTINA"}
                """.formatted(paciente.getUuid(), ubs.getUuid(), ids))).andExpect(status().isCreated());
        PedidoExame pedido = pedidoRepository.findByPaciente_UuidOrderBySolicitadoEmDesc(paciente.getUuid()).stream()
                .filter(p -> itemRepository.findByPedido_Uuid(p.getUuid()).stream().allMatch(i -> i.getAmostra() == null))
                .findFirst().orElseThrow();
        mockMvc.perform(post(PEDIDO + pedido.getUuid() + "/coleta").contentType(MediaType.APPLICATION_JSON).content("""
                {"profissionalMatricula": "CE-TEC", "unidadeColetaId": "%s", "laboratorioId": "%s"}
                """.formatted(ubs.getUuid(), laboratorio.getUuid()))).andExpect(status().isOk());
        return pedido.getUuid();
    }

    private UUID item(UUID pedido, ExameLaboratorial exame) {
        return itemRepository.findByPedido_Uuid(pedido).stream().filter(i -> i.getExame().getUuid().equals(exame.getUuid()))
                .map(ItemPedidoExame::getUuid).findFirst().orElseThrow();
    }

    private void liberado(UUID item, String valor) throws Exception {
        acao(item, "resultado", "{\"profissionalMatricula\": \"CE-TEC\", \"valorNumerico\": " + valor + "}").andExpect(status().isOk());
        acao(item, "liberacao", "{\"profissionalMatricula\": \"CE-TEC\"}").andExpect(status().isOk());
    }

    private ResultActions acao(UUID item, String rota, String corpo) throws Exception {
        return mockMvc.perform(post(PEDIDO + "item/" + item + "/" + rota).contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    private ResultActions entrega(UUID pedido, String corpo) throws Exception {
        return mockMvc.perform(post(PEDIDO + pedido + "/entrega").contentType(MediaType.APPLICATION_JSON).content(corpo));
    }
}
