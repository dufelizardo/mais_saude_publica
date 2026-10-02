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
import com.edufelizardo.maissaudepublica.repositories.AmostraExameRepository;
import com.edufelizardo.maissaudepublica.repositories.EventoAuditoriaRepository;
import com.edufelizardo.maissaudepublica.repositories.EventoExameRepository;
import com.edufelizardo.maissaudepublica.repositories.ExameLaboratorialRepository;
import com.edufelizardo.maissaudepublica.repositories.ItemPedidoExameRepository;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
import com.edufelizardo.maissaudepublica.repositories.PedidoExameRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.ResultadoExameRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Laboratório assistencial (ADR-0093), com login e autorização desligados: catálogo, pedido, coleta por material,
 * resultado com interpretação, liberação, retificação, rejeição de amostra e cancelamento. O escopo fica em
 * {@link LaboratorioAutorizacaoControllerTest}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PedidoExameControllerTest {

    private static final String PEDIDO = "/api/v1/pedido-exame/";
    private static final String CATALOGO = "/api/v1/exame-laboratorial/";
    private static final String PREFIXO = "Lab Teste ";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private ExameLaboratorialRepository exameRepository;

    @Autowired
    private PedidoExameRepository pedidoRepository;

    @Autowired
    private ItemPedidoExameRepository itemRepository;

    @Autowired
    private AmostraExameRepository amostraRepository;

    @Autowired
    private ResultadoExameRepository resultadoRepository;

    @Autowired
    private EventoExameRepository eventoRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @Autowired
    private EventoAuditoriaRepository auditoriaRepository;

    private UnidadeDeSaude ubs;
    private UnidadeDeSaude laboratorio;
    private Paciente paciente;
    private ExameLaboratorial glicemia;
    private ExameLaboratorial urina;

    @BeforeEach
    void seed() {
        limpar();
        ubs = unidade("UBS", TipoUnidadeDeSaude.UBS);
        laboratorio = unidade("Laboratório", TipoUnidadeDeSaude.LABORATORIO);
        profissional("LAB-MED", "61839204517");
        profissional("LAB-TEC", "72940315628");
        profissional("LAB-RT", "83051426739");
        paciente = new Paciente();
        paciente.setNome(PREFIXO + "Paciente");
        paciente.setAtivo(true);
        paciente = pacienteRepository.save(paciente);
        glicemia = exame("Glicemia de jejum", MaterialExame.SANGUE, TipoResultadoExame.NUMERICO, "mg/dL",
                new BigDecimal("70"), new BigDecimal("99"), null);
        urina = exame("Urina tipo 1", MaterialExame.URINA, TipoResultadoExame.TEXTO, null, null, null, "Sem alterações");
    }

    /** Limpeza por SQL: resultado e item apontam um para o outro. */
    @AfterEach
    void limpar() {
        List<UUID> pacientes = pacienteRepository.findAll().stream()
                .filter(p -> p.getNome() != null && p.getNome().startsWith(PREFIXO)).map(Paciente::getUuid).toList();
        for (UUID p : pacientes) {
            for (PedidoExame pedido : pedidoRepository.findByPaciente_UuidOrderBySolicitadoEmDesc(p)) {
                UUID id = pedido.getUuid();
                jdbc.update("update tb_item_pedido_exame set resultado_atual_id = null, amostra_id = null where pedido_id = ?", id);
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
                .filter(p -> p.getMatricula() != null && p.getMatricula().startsWith("LAB-")).toList());
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

    private void profissional(String matricula, String cpf) {
        Profissional p = new Profissional();
        p.setMatricula(matricula);
        p.setCpf(cpf);
        p.setNome(PREFIXO + matricula);
        p.setAtivo(true);
        profissionalRepository.save(p);
    }

    private ExameLaboratorial exame(String nome, MaterialExame material, TipoResultadoExame tipo, String unidadeMedida,
                                    BigDecimal minima, BigDecimal maxima, String referenciaTexto) {
        ExameLaboratorial e = new ExameLaboratorial();
        e.setNome(PREFIXO + nome);
        e.setMaterial(material);
        e.setTipoResultado(tipo);
        e.setUnidadeMedida(unidadeMedida);
        e.setReferenciaMinima(minima);
        e.setReferenciaMaxima(maxima);
        e.setReferenciaTexto(referenciaTexto);
        e.setPreparo("Jejum de 8 horas");
        e.setAtivo(true);
        return exameRepository.save(e);
    }

    private ResultActions pedir(String prioridade, UUID... exames) throws Exception {
        String lista = String.join(", ", java.util.Arrays.stream(exames).map(u -> "\"" + u + "\"").toList());
        return mockMvc.perform(post(PEDIDO).contentType(MediaType.APPLICATION_JSON).content("""
                {"pacienteId": "%s", "unidadeSolicitanteId": "%s", "profissionalMatricula": "LAB-MED", "exameIds": [%s],
                 "indicacaoClinica": "Rastreio de diabetes, polidipsia há 2 meses.", "cid": "r73.0", "prioridade": "%s"}
                """.formatted(paciente.getUuid(), ubs.getUuid(), lista, prioridade)));
    }

    private UUID pedido() throws Exception {
        pedir("ROTINA", glicemia.getUuid(), urina.getUuid()).andExpect(status().isCreated());
        return pedidoRepository.findByPaciente_UuidOrderBySolicitadoEmDesc(paciente.getUuid()).get(0).getUuid();
    }

    private ResultActions coletar(UUID pedido) throws Exception {
        return mockMvc.perform(post(PEDIDO + pedido + "/coleta").contentType(MediaType.APPLICATION_JSON).content("""
                {"profissionalMatricula": "LAB-TEC", "unidadeColetaId": "%s", "laboratorioId": "%s"}
                """.formatted(ubs.getUuid(), laboratorio.getUuid())));
    }

    private UUID item(UUID pedido, ExameLaboratorial exame) {
        return itemRepository.findByPedido_Uuid(pedido).stream().filter(i -> i.getExame().getUuid().equals(exame.getUuid()))
                .findFirst().map(ItemPedidoExame::getUuid).orElseThrow();
    }

    private ResultActions acao(UUID item, String rota, String corpo) throws Exception {
        return mockMvc.perform(post(PEDIDO + "item/" + item + "/" + rota).contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    // ── Catálogo ───────────────────────────────────────────────────────────────────────────────

    @Test
    void catalogoCriaEditaRecusaNomeRepetidoEFaixaInvertida() throws Exception {
        mockMvc.perform(post(CATALOGO).contentType(MediaType.APPLICATION_JSON).content("""
                        {"nome": "%s", "material": "SANGUE", "tipoResultado": "NUMERICO", "unidadeMedida": "mg/dL",
                         "referenciaMinima": 0, "referenciaMaxima": 200}
                        """.formatted(PREFIXO + "Colesterol total"))).andExpect(status().isCreated());
        mockMvc.perform(post(CATALOGO).contentType(MediaType.APPLICATION_JSON).content("""
                        {"nome": "%s", "material": "SANGUE", "tipoResultado": "NUMERICO"}
                        """.formatted((PREFIXO + "colesterol total").toUpperCase()))).andExpect(status().isConflict());
        mockMvc.perform(post(CATALOGO).contentType(MediaType.APPLICATION_JSON).content("""
                        {"nome": "%s", "material": "SANGUE", "tipoResultado": "NUMERICO", "referenciaMinima": 10, "referenciaMaxima": 5}
                        """.formatted(PREFIXO + "Invertido"))).andExpect(status().isBadRequest());
        UUID uuid = exameRepository.findByNomeIgnoreCase(PREFIXO + "Colesterol total").orElseThrow().getUuid();
        mockMvc.perform(patch(CATALOGO + uuid).contentType(MediaType.APPLICATION_JSON).content("""
                        {"nome": "%s", "material": "SANGUE", "tipoResultado": "NUMERICO", "ativo": false}
                        """.formatted(PREFIXO + "Colesterol total"))).andExpect(status().isOk());
        mockMvc.perform(get(CATALOGO + "?ativos=true"))
                .andExpect(jsonPath("$[?(@.uuid == '%s')]".formatted(uuid)).isEmpty());
    }

    // ── Pedido e coleta ────────────────────────────────────────────────────────────────────────

    @Test
    void pedidoRecusaExameRepetidoEForaDeUsoEColetaFazUmaAmostraPorMaterial() throws Exception {
        pedir("ROTINA", glicemia.getUuid(), glicemia.getUuid()).andExpect(status().isBadRequest());
        glicemia.setAtivo(false);
        exameRepository.save(glicemia);
        pedir("ROTINA", glicemia.getUuid()).andExpect(status().isUnprocessableEntity());
        glicemia.setAtivo(true);
        exameRepository.save(glicemia);

        UUID pedido = pedido();
        mockMvc.perform(get(PEDIDO + "?pacienteId=" + paciente.getUuid()))
                .andExpect(jsonPath("$[0].situacao").value("AGUARDANDO_COLETA"))
                .andExpect(jsonPath("$[0].totalExames").value(2));
        coletar(pedido).andExpect(status().isOk());
        coletar(pedido).andExpect(status().isUnprocessableEntity());

        mockMvc.perform(get(PEDIDO + pedido))
                .andExpect(jsonPath("$.situacao").value("EM_ANDAMENTO"))
                .andExpect(jsonPath("$.cid").value("R730"))
                .andExpect(jsonPath("$.amostras.length()").value(2))
                .andExpect(jsonPath("$.amostras[0].laboratorioId").value(laboratorio.getUuid().toString()))
                .andExpect(jsonPath("$.amostras[0].codigo").value(org.hamcrest.Matchers.startsWith("AM")))
                .andExpect(jsonPath("$.itens[0].status").value("COLETADO"))
                .andExpect(jsonPath("$.eventos[1].tipo").value("COLETA"));
        mockMvc.perform(get(PEDIDO + "trabalho?etapa=EM_ANALISE"))
                .andExpect(jsonPath("$[?(@.pedidoId == '%s')]".formatted(pedido)).isNotEmpty());
        mockMvc.perform(get(PEDIDO + "trabalho?etapa=QUALQUER")).andExpect(status().isBadRequest());
    }

    // ── Resultado, liberação, retificação ──────────────────────────────────────────────────────

    @Test
    void resultadoNumericoInterpretaFaixaLiberacaoTravaERetificacaoGuardaOAnterior() throws Exception {
        UUID pedido = pedido();
        UUID item = item(pedido, glicemia);
        acao(item, "resultado", "{\"profissionalMatricula\": \"LAB-TEC\", \"valorNumerico\": 130}").andExpect(status().isUnprocessableEntity());
        coletar(pedido).andExpect(status().isOk());

        acao(item, "resultado", "{\"profissionalMatricula\": \"LAB-TEC\", \"valorTexto\": \"alto\"}").andExpect(status().isBadRequest());
        acao(item, "resultado", "{\"profissionalMatricula\": \"LAB-TEC\", \"valorNumerico\": 130}").andExpect(status().isOk());
        mockMvc.perform(get(PEDIDO + "trabalho?etapa=PARA_LIBERAR"))
                .andExpect(jsonPath("$[?(@.itemId == '%s')]".formatted(item)).isNotEmpty());
        acao(item, "liberacao", "{\"profissionalMatricula\": \"LAB-RT\"}").andExpect(status().isOk());
        acao(item, "resultado", "{\"profissionalMatricula\": \"LAB-TEC\", \"valorNumerico\": 90}").andExpect(status().isUnprocessableEntity());

        mockMvc.perform(get(PEDIDO + pedido))
                .andExpect(jsonPath("$.itens[?(@.uuid == '%s')].resultado.interpretacao".formatted(item)).value("ACIMA"))
                .andExpect(jsonPath("$.itens[?(@.uuid == '%s')].resultado.unidadeMedida".formatted(item)).value("mg/dL"))
                .andExpect(jsonPath("$.itens[?(@.uuid == '%s')].resultado.liberadoPorMatricula".formatted(item)).value("LAB-RT"));

        acao(item, "retificacao", "{\"profissionalMatricula\": \"LAB-RT\", \"valorNumerico\": 93}").andExpect(status().isBadRequest());
        acao(item, "retificacao", "{\"profissionalMatricula\": \"LAB-RT\", \"valorNumerico\": 93, \"motivo\": \"Erro de digitação\"}")
                .andExpect(status().isOk());
        mockMvc.perform(get(PEDIDO + pedido))
                .andExpect(jsonPath("$.itens[?(@.uuid == '%s')].resultado.interpretacao".formatted(item)).value("NORMAL"))
                .andExpect(jsonPath("$.itens[?(@.uuid == '%s')].resultado.retificacaoDeId".formatted(item)).isNotEmpty())
                .andExpect(jsonPath("$.itens[?(@.uuid == '%s')].resultado.motivoRetificacao".formatted(item)).value("Erro de digitação"));
        assertThat(resultadoRepository.findByItem_UuidOrderByRegistradoEmAsc(item)).hasSize(2);
        assertThat(auditoriaRepository.findByRegistroIdOrderByOcorridoEmDesc(pedido))
                .anyMatch(e -> "GET".equals(e.getMetodo()) && paciente.getUuid().equals(e.getPacienteId()));
    }

    @Test
    void resultadoEmTextoCancelamentoEListagemSemValores() throws Exception {
        UUID pedido = pedido();
        coletar(pedido).andExpect(status().isOk());
        UUID itemUrina = item(pedido, urina);
        UUID itemGlicemia = item(pedido, glicemia);
        acao(itemUrina, "resultado", "{\"profissionalMatricula\": \"LAB-TEC\", \"valorTexto\": \"Leucócitos aumentados\"}").andExpect(status().isOk());
        acao(itemUrina, "liberacao", "{\"profissionalMatricula\": \"LAB-RT\"}").andExpect(status().isOk());
        acao(itemUrina, "cancelamento", "{\"profissionalMatricula\": \"LAB-MED\", \"motivo\": \"Tarde\"}").andExpect(status().isUnprocessableEntity());
        acao(itemGlicemia, "cancelamento", "{\"profissionalMatricula\": \"LAB-MED\", \"motivo\": \"Pedido em duplicidade\"}").andExpect(status().isOk());

        mockMvc.perform(get(PEDIDO + "?pacienteId=" + paciente.getUuid()))
                .andExpect(jsonPath("$[0].situacao").value("CONCLUIDO"))
                .andExpect(jsonPath("$[0].totalExames").value(1))
                .andExpect(jsonPath("$[0].liberados").value(1))
                .andExpect(jsonPath("$[0].itens[0].resultado").doesNotExist());
        mockMvc.perform(get(PEDIDO + pedido))
                .andExpect(jsonPath("$.itens[?(@.uuid == '%s')].resultado.valorTexto".formatted(itemUrina)).value("Leucócitos aumentados"))
                .andExpect(jsonPath("$.itens[?(@.uuid == '%s')].resultado.referenciaTexto".formatted(itemUrina)).value("Sem alterações"));
    }

    // ── Rejeição de amostra ────────────────────────────────────────────────────────────────────

    @Test
    void amostraRejeitadaDevolveOsExamesParaRecoleta() throws Exception {
        UUID pedido = pedido();
        coletar(pedido).andExpect(status().isOk());
        UUID itemGlicemia = item(pedido, glicemia);
        acao(itemGlicemia, "resultado", "{\"profissionalMatricula\": \"LAB-TEC\", \"valorNumerico\": 88}").andExpect(status().isOk());
        UUID amostra = itemRepository.findById(itemGlicemia).orElseThrow().getAmostra().getUuid();

        mockMvc.perform(post(PEDIDO + "amostra/" + amostra + "/rejeicao").contentType(MediaType.APPLICATION_JSON)
                .content("{\"profissionalMatricula\": \"LAB-TEC\", \"motivo\": \"HEMOLISADA\"}")).andExpect(status().isOk());
        mockMvc.perform(post(PEDIDO + "amostra/" + amostra + "/rejeicao").contentType(MediaType.APPLICATION_JSON)
                .content("{\"profissionalMatricula\": \"LAB-TEC\", \"motivo\": \"HEMOLISADA\"}")).andExpect(status().isUnprocessableEntity());

        mockMvc.perform(get(PEDIDO + pedido))
                .andExpect(jsonPath("$.itens[?(@.uuid == '%s')].status".formatted(itemGlicemia)).value("SOLICITADO"))
                .andExpect(jsonPath("$.itens[?(@.uuid == '%s')].amostraCodigo".formatted(itemGlicemia)).value(org.hamcrest.Matchers.contains(org.hamcrest.Matchers.nullValue())))
                .andExpect(jsonPath("$.amostras[?(@.uuid == '%s')].rejeitada".formatted(amostra)).value(true))
                .andExpect(jsonPath("$.eventos[-1].tipo").value("REJEICAO_AMOSTRA"));
        // A recoleta gera amostra nova só para o que voltou.
        coletar(pedido).andExpect(status().isOk());
        assertThat(amostraRepository.findByPedido_UuidOrderByColetadaEmAsc(pedido)).hasSize(3);
    }

    @Test
    void urgenteVemPrimeiroNaListaDeColeta() throws Exception {
        pedir("ROTINA", glicemia.getUuid()).andExpect(status().isCreated());
        pedir("URGENTE", urina.getUuid()).andExpect(status().isCreated());
        mockMvc.perform(get(PEDIDO + "trabalho?etapa=PARA_COLETAR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].prioridade").value("URGENTE"));
    }
}
