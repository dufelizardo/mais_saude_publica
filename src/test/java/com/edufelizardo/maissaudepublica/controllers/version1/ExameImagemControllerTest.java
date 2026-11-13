package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.ExameLaboratorial;
import com.edufelizardo.maissaudepublica.models.ItemPedidoExame;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.PedidoExame;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.CategoriaExame;
import com.edufelizardo.maissaudepublica.models.enuns.MaterialExame;
import com.edufelizardo.maissaudepublica.models.enuns.ModalidadeImagem;
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

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Exames de imagem (ADR-0182): catálogo com categoria e modalidade, serviço de imagem com a capacidade, realização, laudo,
 * adendo e o achado crítico entrando na comunicação da L2, com login e autorização desligados. O escopo fica em
 * {@link LaboratorioAutorizacaoControllerTest}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ExameImagemControllerTest {

    private static final String PEDIDO = "/api/v1/pedido-exame/";
    private static final String CATALOGO = "/api/v1/exame-laboratorial/";
    private static final String PREFIXO = "Imagem Teste ";
    private static final String EMAIL_MEDICO = "medico.imagem@prefeitura.teste";

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
    private UnidadeDeSaude servicoImagem;
    private Paciente paciente;
    private ExameLaboratorial hemograma;
    private ExameLaboratorial raioX;

    @BeforeEach
    void seed() {
        limpar();
        ubs = unidade("UBS", TipoUnidadeDeSaude.UBS);
        servicoImagem = unidade("Policlínica", TipoUnidadeDeSaude.POLICLINICA);
        CapacidadesDeTeste.habilitar(jdbc, servicoImagem.getUuid(), "DIAGNOSTICO_POR_IMAGEM");
        profissional("IM-MED", "46802468042", EMAIL_MEDICO);
        profissional("IM-TEC", "57913579153", null);
        profissional("IM-RAD", "68024680262", null);
        paciente = new Paciente();
        paciente.setNome(PREFIXO + "Paciente");
        paciente.setAtivo(true);
        paciente = pacienteRepository.save(paciente);
        hemograma = new ExameLaboratorial();
        hemograma.setNome(PREFIXO + "Hemoglobina");
        hemograma.setMaterial(MaterialExame.SANGUE);
        hemograma.setTipoResultado(TipoResultadoExame.NUMERICO);
        hemograma.setReferenciaMinima(new BigDecimal("12"));
        hemograma.setReferenciaMaxima(new BigDecimal("16"));
        hemograma.setAtivo(true);
        hemograma = exameRepository.save(hemograma);
        raioX = new ExameLaboratorial();
        raioX.setNome(PREFIXO + "Raio-X de tórax PA e perfil");
        raioX.setCategoria(CategoriaExame.IMAGEM);
        raioX.setModalidade(ModalidadeImagem.RAIO_X);
        raioX.setTipoResultado(TipoResultadoExame.TEXTO);
        raioX.setAtivo(true);
        raioX = exameRepository.save(raioX);
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
                jdbc.update("delete from tb_realizacao_exame_imagem where item_id in (select uuid from tb_item_pedido_exame where pedido_id = ?)", id);
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
                .filter(p -> p.getMatricula() != null && p.getMatricula().startsWith("IM-")).toList());
        CapacidadesDeTeste.limpar(jdbc, PREFIXO);
        unidadeDeSaudeRepository.deleteAll(unidadeDeSaudeRepository.findAll().stream()
                .filter(u -> u.getNome() != null && u.getNome().startsWith(PREFIXO)).toList());
    }

    // ── Catálogo ───────────────────────────────────────────────────────────────────────────────

    @Test
    void catalogoDeImagemTemModalidadeSemMaterialECategoriaFixa() throws Exception {
        catalogo("{\"nome\": \"" + PREFIXO + "USG abdome\", \"categoria\": \"IMAGEM\"}").andExpect(status().isBadRequest());
        catalogo("{\"nome\": \"" + PREFIXO + "Glicose\", \"tipoResultado\": \"NUMERICO\"}").andExpect(status().isBadRequest());
        catalogo("""
                {"nome": "%sUSG abdome total", "categoria": "IMAGEM", "modalidade": "ULTRASSONOGRAFIA", "material": "SANGUE",
                 "tipoResultado": "NUMERICO", "referenciaMinima": 1, "preparo": "Jejum de 8 horas."}
                """.formatted(PREFIXO)).andExpect(status().isCreated());
        ExameLaboratorial usg = exameRepository.findAll().stream().filter(e -> e.getNome().equals(PREFIXO + "USG abdome total")).findFirst().orElseThrow();
        mockMvc.perform(get(CATALOGO + usg.getUuid()))
                .andExpect(jsonPath("$.categoria").value("IMAGEM"))
                .andExpect(jsonPath("$.modalidade").value("ULTRASSONOGRAFIA"))
                .andExpect(jsonPath("$.material", nullValue()))
                .andExpect(jsonPath("$.tipoResultado").value("TEXTO"))
                .andExpect(jsonPath("$.referenciaMinima", nullValue()))
                .andExpect(jsonPath("$.preparo").value("Jejum de 8 horas."));
        mockMvc.perform(patch(CATALOGO + usg.getUuid()).contentType(MediaType.APPLICATION_JSON).content("""
                {"nome": "%sUSG abdome total", "categoria": "LABORATORIAL", "material": "SANGUE", "tipoResultado": "TEXTO"}
                """.formatted(PREFIXO))).andExpect(status().isUnprocessableEntity());
        // Sem categoria na edição, continua a do exame.
        mockMvc.perform(patch(CATALOGO + usg.getUuid()).contentType(MediaType.APPLICATION_JSON).content("""
                {"nome": "%sUSG abdome total", "modalidade": "ULTRASSONOGRAFIA", "prazoDias": 5}
                """.formatted(PREFIXO))).andExpect(status().isOk());
        mockMvc.perform(get(CATALOGO + hemograma.getUuid())).andExpect(jsonPath("$.categoria").value("LABORATORIAL"));
    }

    // ── Pedido ─────────────────────────────────────────────────────────────────────────────────

    @Test
    void pedidoComImagemExigeServicoDeImagemComCapacidade() throws Exception {
        String sem = corpoPedido(null, raioX);
        mockMvc.perform(post(PEDIDO).contentType(MediaType.APPLICATION_JSON).content(sem)).andExpect(status().isBadRequest());
        mockMvc.perform(post(PEDIDO).contentType(MediaType.APPLICATION_JSON).content(corpoPedido(ubs.getUuid(), raioX)))
                .andExpect(status().isUnprocessableEntity());
        // Só laboratorial: o serviço de imagem não é pedido nem guardado.
        mockMvc.perform(post(PEDIDO).contentType(MediaType.APPLICATION_JSON).content(corpoPedido(null, hemograma))).andExpect(status().isCreated());
        UUID pedido = pedido(hemograma, raioX);
        mockMvc.perform(get(PEDIDO + pedido))
                .andExpect(jsonPath("$.unidadeExecutanteImagemNome").value(PREFIXO + "Policlínica"))
                .andExpect(jsonPath("$.itens[?(@.exameId == '%s')].categoria".formatted(raioX.getUuid())).value(org.hamcrest.Matchers.contains("IMAGEM")));
    }

    // ── Realização, laudo e adendo ─────────────────────────────────────────────────────────────

    @Test
    void realizaLaudaEAdendaSemPassarPelaColeta() throws Exception {
        UUID pedido = pedido(hemograma, raioX);
        UUID rx = item(pedido, raioX);
        UUID hb = item(pedido, hemograma);

        // A coleta leva só o laboratorial.
        mockMvc.perform(post(PEDIDO + pedido + "/coleta").contentType(MediaType.APPLICATION_JSON).content("""
                {"profissionalMatricula": "IM-TEC", "unidadeColetaId": "%s"}
                """.formatted(ubs.getUuid()))).andExpect(status().isOk());
        mockMvc.perform(get(PEDIDO + pedido))
                .andExpect(jsonPath("$.itens[?(@.uuid == '%s')].status".formatted(hb)).value(org.hamcrest.Matchers.contains("COLETADO")))
                .andExpect(jsonPath("$.itens[?(@.uuid == '%s')].status".formatted(rx)).value(org.hamcrest.Matchers.contains("SOLICITADO")));
        mockMvc.perform(get(PEDIDO + "trabalho?etapa=PARA_COLETAR")).andExpect(jsonPath("$[?(@.itemId == '%s')]".formatted(rx), hasSize(0)));
        mockMvc.perform(get(PEDIDO + "trabalho?etapa=PARA_REALIZAR"))
                .andExpect(jsonPath("$[?(@.itemId == '%s')].modalidade".formatted(rx)).value(org.hamcrest.Matchers.contains("RAIO_X")))
                .andExpect(jsonPath("$[?(@.itemId == '%s')].laboratorioNome".formatted(rx)).value(org.hamcrest.Matchers.contains(PREFIXO + "Policlínica")));

        String laudo = """
                {"profissionalMatricula": "IM-RAD", "tecnica": "Incidências PA e perfil.", "achados": "Opacidade em base direita.",
                 "conclusao": "Pneumonia em base direita.", "classificacao": "%s"%s}
                """;
        acao(rx, "laudo", laudo.formatted("ALTERADO", "")).andExpect(status().isUnprocessableEntity());
        acao(hb, "realizacao", "{\"profissionalMatricula\": \"IM-TEC\"}").andExpect(status().isUnprocessableEntity());
        acao(rx, "realizacao", "{\"profissionalMatricula\": \"IM-TEC\", \"contraste\": true}").andExpect(status().isBadRequest());
        acao(rx, "realizacao", "{\"profissionalMatricula\": \"IM-TEC\", \"realizadaEm\": \"" + LocalDateTime.now().plusHours(3).withNano(0) + "\"}")
                .andExpect(status().isUnprocessableEntity());
        acao(rx, "realizacao", "{\"profissionalMatricula\": \"IM-TEC\", \"equipamento\": \"Aparelho da sala 1\"}").andExpect(status().isOk());
        acao(rx, "realizacao", "{\"profissionalMatricula\": \"IM-TEC\"}").andExpect(status().isUnprocessableEntity());
        mockMvc.perform(get(PEDIDO + "trabalho?etapa=PARA_LAUDAR")).andExpect(jsonPath("$[?(@.itemId == '%s')]".formatted(rx), hasSize(1)));

        acao(rx, "laudo", laudo.formatted("ALTERADO", ", \"imagemUrl\": \"http://pacs.local/estudo/1\"")).andExpect(status().isBadRequest());
        acao(rx, "laudo", laudo.formatted("ACIMA", "")).andExpect(status().isBadRequest());
        acao(rx, "laudo", laudo.formatted("ALTERADO", ", \"imagemUrl\": \"https://pacs.local/estudo/1\"")).andExpect(status().isOk());
        mockMvc.perform(get(PEDIDO + pedido))
                .andExpect(jsonPath("$.itens[?(@.uuid == '%s')].status".formatted(rx)).value(org.hamcrest.Matchers.contains("LIBERADO")))
                .andExpect(jsonPath("$.itens[?(@.uuid == '%s')].resultado.laudoConclusao".formatted(rx)).value(org.hamcrest.Matchers.contains("Pneumonia em base direita.")))
                .andExpect(jsonPath("$.itens[?(@.uuid == '%s')].resultado.valorTexto".formatted(rx)).value(org.hamcrest.Matchers.contains("Pneumonia em base direita.")))
                .andExpect(jsonPath("$.itens[?(@.uuid == '%s')].resultado.liberadoPorMatricula".formatted(rx)).value(org.hamcrest.Matchers.contains("IM-RAD")))
                .andExpect(jsonPath("$.itens[?(@.uuid == '%s')].resultado.interpretacao".formatted(rx)).value(org.hamcrest.Matchers.contains("ALTERADO")))
                .andExpect(jsonPath("$.realizacoes", hasSize(1)))
                .andExpect(jsonPath("$.realizacoes[0].equipamento").value("Aparelho da sala 1"))
                .andExpect(jsonPath("$.realizacoes[0].unidadeNome").value(PREFIXO + "Policlínica"));

        acao(rx, "retificacao", "{\"profissionalMatricula\": \"IM-RAD\", \"valorTexto\": \"x\", \"motivo\": \"y\"}").andExpect(status().isUnprocessableEntity());
        acao(rx, "adendo", laudo.formatted("NORMAL", "")).andExpect(status().isBadRequest());
        acao(rx, "adendo", laudo.formatted("NORMAL", ", \"motivo\": \"Revisão com a incidência em perfil.\"")).andExpect(status().isOk());
        mockMvc.perform(get(PEDIDO + pedido))
                .andExpect(jsonPath("$.itens[?(@.uuid == '%s')].resultado.retificacaoDeId".formatted(rx)).value(org.hamcrest.Matchers.contains(notNullValue())))
                .andExpect(jsonPath("$.eventos[*].tipo").value(org.hamcrest.Matchers.hasItems("REALIZACAO", "LAUDO", "ADENDO")))
                .andExpect(jsonPath("$.eventos[-1].texto").value("Revisão com a incidência em perfil."));
        // A ciência da L2 vale para o laudo de imagem.
        mockMvc.perform(get(PEDIDO + "para-ciencia?matricula=IM-MED")).andExpect(jsonPath("$[?(@.itemId == '%s')]".formatted(rx), hasSize(1)));
    }

    @Test
    void achadoCriticoAvisaEEntraNosCriticosAComunicar() throws Exception {
        when(envioDeEmail.disponivel()).thenReturn(true);
        when(envioDeEmail.enviar(anyString(), anyString(), anyString())).thenReturn(true);
        UUID pedido = pedido(raioX);
        UUID rx = item(pedido, raioX);
        acao(rx, "realizacao", "{\"profissionalMatricula\": \"IM-TEC\"}").andExpect(status().isOk());
        acao(rx, "laudo", """
                {"profissionalMatricula": "IM-RAD", "achados": "Pneumotórax hipertensivo à esquerda.", "conclusao": "Pneumotórax.",
                 "classificacao": "ACHADO_CRITICO"}
                """).andExpect(status().isOk());
        verify(envioDeEmail, times(1)).enviar(eq(EMAIL_MEDICO), anyString(), anyString());
        mockMvc.perform(get(PEDIDO + "criticos-a-comunicar"))
                .andExpect(jsonPath("$[?(@.itemId == '%s')].interpretacao".formatted(rx)).value(org.hamcrest.Matchers.contains("ACHADO_CRITICO")));
        mockMvc.perform(get(PEDIDO + "para-ciencia?matricula=IM-MED")).andExpect(jsonPath("$[0].critico").value(true));
        acao(rx, "comunicacao-critico", "{\"profissionalMatricula\": \"IM-RAD\", \"receptorNome\": \"Dr. Plantonista\", \"meio\": \"TELEFONE\"}")
                .andExpect(status().isOk());
        mockMvc.perform(get(PEDIDO + "criticos-a-comunicar")).andExpect(jsonPath("$[?(@.itemId == '%s')]".formatted(rx), hasSize(0)));
    }

    @Test
    void realizadoSemLaudoSeguraAEntregaEPodeSerCancelado() throws Exception {
        UUID pedido = pedido(raioX);
        UUID rx = item(pedido, raioX);
        acao(rx, "realizacao", "{\"profissionalMatricula\": \"IM-TEC\"}").andExpect(status().isOk());
        mockMvc.perform(post(PEDIDO + pedido + "/entrega").contentType(MediaType.APPLICATION_JSON).content("""
                {"profissionalMatricula": "IM-TEC", "unidadeId": "%s", "retiradoPeloPaciente": true, "forma": "IMPRESSO_NA_UNIDADE"}
                """.formatted(servicoImagem.getUuid()))).andExpect(status().isUnprocessableEntity());
        acao(rx, "cancelamento", "{\"profissionalMatricula\": \"IM-MED\", \"motivo\": \"Paciente transferido.\"}").andExpect(status().isOk());
        mockMvc.perform(get(PEDIDO + pedido)).andExpect(jsonPath("$.situacao").value("CANCELADO"))
                .andExpect(jsonPath("$.eventos[-1].texto").value(containsString("transferido")));
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

    private ResultActions catalogo(String corpo) throws Exception {
        return mockMvc.perform(post(CATALOGO).contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    private String corpoPedido(UUID servico, ExameLaboratorial... exames) {
        String ids = String.join("\", \"", Stream.of(exames).map(e -> e.getUuid().toString()).toList());
        return """
                {"pacienteId": "%s", "unidadeSolicitanteId": "%s", "profissionalMatricula": "IM-MED", "exameIds": ["%s"],
                 "indicacaoClinica": "Tosse e febre há 5 dias.", "prioridade": "ROTINA"%s}
                """.formatted(paciente.getUuid(), ubs.getUuid(), ids, servico == null ? "" : ", \"unidadeExecutanteImagemId\": \"" + servico + "\"");
    }

    private UUID pedido(ExameLaboratorial... exames) throws Exception {
        mockMvc.perform(post(PEDIDO).contentType(MediaType.APPLICATION_JSON).content(corpoPedido(servicoImagem.getUuid(), exames)))
                .andExpect(status().isCreated());
        return pedidoRepository.findByPaciente_UuidOrderBySolicitadoEmDesc(paciente.getUuid()).stream()
                .filter(p -> p.getUnidadeExecutanteImagem() != null)
                .filter(p -> itemRepository.findByPedido_Uuid(p.getUuid()).stream().allMatch(i -> i.getStatus().name().equals("SOLICITADO")))
                .findFirst().orElseThrow().getUuid();
    }

    private UUID item(UUID pedido, ExameLaboratorial exame) {
        return itemRepository.findByPedido_Uuid(pedido).stream().filter(i -> i.getExame().getUuid().equals(exame.getUuid()))
                .map(ItemPedidoExame::getUuid).findFirst().orElseThrow();
    }

    private ResultActions acao(UUID item, String rota, String corpo) throws Exception {
        return mockMvc.perform(post(PEDIDO + "item/" + item + "/" + rota).contentType(MediaType.APPLICATION_JSON).content(corpo));
    }
}
