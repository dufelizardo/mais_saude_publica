package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.ExameLaboratorial;
import com.edufelizardo.maissaudepublica.models.ItemPedidoExame;
import com.edufelizardo.maissaudepublica.models.MaterialEstoque;
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
import com.edufelizardo.maissaudepublica.repositories.MaterialEstoqueRepository;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
import com.edufelizardo.maissaudepublica.repositories.PedidoExameRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Laudo em PDF com assinatura eletrônica interna, código de verificação e verificação pública, e os insumos de coleta e de
 * realização baixados do Estoque (ADR-0183), com login e autorização desligados. O escopo fica em
 * {@link LaboratorioAutorizacaoControllerTest}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LaudoInsumosControllerTest {

    private static final String PEDIDO = "/api/v1/pedido-exame/";
    private static final String CATALOGO = "/api/v1/exame-laboratorial/";
    private static final String ESTOQUE = "/api/v1/estoque/";
    private static final String PREFIXO = "Laudo Teste ";

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
    private PacienteRepository pacienteRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @Autowired
    private MaterialEstoqueRepository materialRepository;

    private UnidadeDeSaude ubs;
    private UnidadeDeSaude servicoImagem;
    private Paciente paciente;
    private ExameLaboratorial hemoglobina;
    private ExameLaboratorial plaquetas;
    private ExameLaboratorial tomografia;

    @BeforeEach
    void seed() {
        limpar();
        ubs = unidade("UBS", TipoUnidadeDeSaude.UBS);
        servicoImagem = unidade("Policlínica", TipoUnidadeDeSaude.POLICLINICA);
        CapacidadesDeTeste.habilitar(jdbc, servicoImagem.getUuid(), "DIAGNOSTICO_POR_IMAGEM");
        Profissional p = new Profissional();
        p.setMatricula("LD-001");
        p.setCpf("79135791373");
        p.setNome(PREFIXO + "Responsável");
        p.setAtivo(true);
        profissionalRepository.save(p);
        paciente = new Paciente();
        paciente.setNome(PREFIXO + "Maria da Silva Souza");
        paciente.setAtivo(true);
        paciente = pacienteRepository.save(paciente);
        hemoglobina = exameLab("Hemoglobina");
        plaquetas = exameLab("Plaquetas");
        tomografia = new ExameLaboratorial();
        tomografia.setNome(PREFIXO + "Tomografia de crânio");
        tomografia.setCategoria(CategoriaExame.IMAGEM);
        tomografia.setModalidade(ModalidadeImagem.TOMOGRAFIA);
        tomografia.setTipoResultado(TipoResultadoExame.TEXTO);
        tomografia.setAtivo(true);
        tomografia = exameRepository.save(tomografia);
    }

    @AfterEach
    void limpar() {
        List<UUID> pacientes = pacienteRepository.findAll().stream()
                .filter(p -> p.getNome() != null && p.getNome().startsWith(PREFIXO)).map(Paciente::getUuid).toList();
        for (UUID p : pacientes) {
            for (PedidoExame pedido : pedidoRepository.findByPaciente_UuidOrderBySolicitadoEmDesc(p)) {
                UUID id = pedido.getUuid();
                jdbc.update("delete from tb_emissao_laudo_exame_resultado where emissao_id in (select uuid from tb_emissao_laudo_exame where pedido_id = ?)", id);
                jdbc.update("delete from tb_emissao_laudo_exame where pedido_id = ?", id);
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
        jdbc.update("delete from tb_insumo_exame where exame_id in (select uuid from tb_exame_laboratorial where nome like ?)", PREFIXO + "%");
        exameRepository.deleteAll(exameRepository.findAll().stream().filter(e -> e.getNome().startsWith(PREFIXO)).toList());
        jdbc.update("delete from tb_consumo_sem_saldo where material_id in (select uuid from tb_material_estoque where codigo like 'LDT-%')");
        jdbc.update("delete from tb_movimentacao_estoque where lote_id in (select l.uuid from tb_lote_material l join tb_material_estoque m on m.uuid = l.material_id where m.codigo like 'LDT-%')");
        jdbc.update("delete from tb_lote_material where material_id in (select uuid from tb_material_estoque where codigo like 'LDT-%')");
        jdbc.update("delete from tb_material_estoque where codigo like 'LDT-%'");
        profissionalRepository.deleteAll(profissionalRepository.findAll().stream()
                .filter(p -> p.getMatricula() != null && p.getMatricula().startsWith("LD-")).toList());
        CapacidadesDeTeste.limpar(jdbc, PREFIXO);
        unidadeDeSaudeRepository.deleteAll(unidadeDeSaudeRepository.findAll().stream()
                .filter(u -> u.getNome() != null && u.getNome().startsWith(PREFIXO)).toList());
    }

    // ── Laudo em PDF e verificação ─────────────────────────────────────────────────────────────

    @Test
    void laudoPdfAssinadoComCodigoEVersaoSubstituidaNaRetificacao() throws Exception {
        UUID pedido = pedido(hemoglobina);
        UUID item = item(pedido, hemoglobina);
        coletar(pedido);
        liberado(item, "13.2");
        mockMvc.perform(get(PEDIDO + pedido))
                .andExpect(jsonPath("$.itens[0].resultado.hashConteudo").value(matchesPattern("[0-9a-f]{64}")));

        MvcResult primeiro = mockMvc.perform(get(PEDIDO + pedido + "/laudo.pdf"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("X-Codigo-Verificacao", matchesPattern("[A-Z2-9]{4}-[A-Z2-9]{4}-[A-Z2-9]{4}")))
                .andReturn();
        byte[] pdf = primeiro.getResponse().getContentAsByteArray();
        assertThat(new String(pdf, 0, 5, StandardCharsets.ISO_8859_1)).isEqualTo("%PDF-");
        String codigo = primeiro.getResponse().getHeader("X-Codigo-Verificacao");
        // Sem resultado novo, a mesma emissão e o mesmo arquivo.
        MvcResult denovo = mockMvc.perform(get(PEDIDO + pedido + "/laudo.pdf")).andExpect(status().isOk()).andReturn();
        assertThat(denovo.getResponse().getHeader("X-Codigo-Verificacao")).isEqualTo(codigo);
        assertThat(denovo.getResponse().getContentAsByteArray()).isEqualTo(pdf);

        mockMvc.perform(get("/api/v1/publico/laudo/" + codigo.toLowerCase()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valido").value(true))
                .andExpect(jsonPath("$.versao").value(1))
                .andExpect(jsonPath("$.hashPdf").value(sha256(pdf)))
                .andExpect(jsonPath("$.pacienteIniciais").value("L. T. M. S. S."))
                .andExpect(jsonPath("$.exames[0]").value(PREFIXO + "Hemoglobina"))
                .andExpect(content().string(not(containsString("Maria"))));

        acao(item, "retificacao", "{\"profissionalMatricula\": \"LD-001\", \"valorNumerico\": 12.8, \"motivo\": \"Diluição corrigida.\"}")
                .andExpect(status().isOk());
        MvcResult segundo = mockMvc.perform(get(PEDIDO + pedido + "/laudo.pdf")).andExpect(status().isOk()).andReturn();
        String codigoNovo = segundo.getResponse().getHeader("X-Codigo-Verificacao");
        assertThat(codigoNovo).isNotEqualTo(codigo);
        mockMvc.perform(get(PEDIDO + pedido + "/emissoes"))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].versao").value(2))
                .andExpect(jsonPath("$[0].vigente").value(true))
                .andExpect(jsonPath("$[1].vigente").value(false));
        mockMvc.perform(get("/api/v1/publico/laudo/" + codigo))
                .andExpect(jsonPath("$.valido").value(false))
                .andExpect(jsonPath("$.substituido").value(true))
                .andExpect(jsonPath("$.versaoVigente").value(2));
        mockMvc.perform(get("/api/v1/publico/laudo/AAAA-BBBB-CCCC")).andExpect(status().isNotFound());
    }

    @Test
    void semResultadoLiberadoNaoHaLaudo() throws Exception {
        UUID pedido = pedido(hemoglobina);
        mockMvc.perform(get(PEDIDO + pedido + "/laudo.pdf")).andExpect(status().isUnprocessableEntity());
    }

    // ── Insumos ────────────────────────────────────────────────────────────────────────────────

    @Test
    void coletaBaixaOsInsumosUmaVezPorAmostraESemSaldoViraAlerta() throws Exception {
        UUID tubo = material("LDT-TUBO", "Tubo EDTA");
        UUID agulha = material("LDT-AGULHA", "Agulha 25x7");
        entrada(tubo, 10);
        insumos(hemoglobina, "[{\"materialId\": \"%s\", \"quantidade\": 1}, {\"materialId\": \"%s\", \"quantidade\": 1}]".formatted(tubo, agulha))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.insumos", hasSize(2)));
        insumos(plaquetas, "[{\"materialId\": \"%s\", \"quantidade\": 2}]".formatted(tubo)).andExpect(status().isOk());
        insumos(plaquetas, "[{\"materialId\": \"%s\", \"quantidade\": 1}, {\"materialId\": \"%s\", \"quantidade\": 2}]".formatted(tubo, tubo))
                .andExpect(status().isBadRequest());
        insumos(plaquetas, "[{\"materialId\": \"%s\", \"quantidade\": 1, \"soComContraste\": true}]".formatted(tubo))
                .andExpect(status().isUnprocessableEntity());

        UUID pedido = pedido(hemoglobina, plaquetas);
        coletar(pedido);
        // Mesma amostra de sangue: o tubo conta uma vez, pela maior quantidade (2); a agulha não tem saldo.
        mockMvc.perform(get(ESTOQUE + "saldo?unidadeId=" + ubs.getUuid()))
                .andExpect(jsonPath("$.itens[?(@.codigo == 'LDT-TUBO')].total").value(org.hamcrest.Matchers.contains(8)))
                .andExpect(jsonPath("$.consumosSemSaldo", hasSize(1)))
                .andExpect(jsonPath("$.consumosSemSaldo[0].materialCodigo").value("LDT-AGULHA"))
                .andExpect(jsonPath("$.consumosSemSaldo[0].quantidadeFaltante").value(1));
        mockMvc.perform(get(PEDIDO + pedido)).andExpect(jsonPath("$.eventos[-1].texto").value(containsString("sem saldo no estoque: " + "Agulha 25x7 (1)")));
        String consumo = JsonPath.read(mockMvc.perform(get(ESTOQUE + "saldo?unidadeId=" + ubs.getUuid())).andReturn().getResponse().getContentAsString(),
                "$.consumosSemSaldo[0].uuid");
        mockMvc.perform(post(ESTOQUE + "consumo-sem-saldo/" + consumo + "/conferencia")).andExpect(status().isNoContent());
        mockMvc.perform(post(ESTOQUE + "consumo-sem-saldo/" + consumo + "/conferencia")).andExpect(status().isUnprocessableEntity());
        mockMvc.perform(get(ESTOQUE + "saldo?unidadeId=" + ubs.getUuid())).andExpect(jsonPath("$.consumosSemSaldo", hasSize(0)));
    }

    @Test
    void contrasteSoBaixaQuandoARealizacaoUsaContraste() throws Exception {
        UUID contraste = material("LDT-CONTRASTE", "Contraste iodado 50 mL");
        UUID seringa = material("LDT-SERINGA", "Seringa 20 mL");
        entradaNa(servicoImagem, contraste, 5);
        entradaNa(servicoImagem, seringa, 5);
        insumos(tomografia, "[{\"materialId\": \"%s\", \"quantidade\": 1, \"soComContraste\": true}, {\"materialId\": \"%s\", \"quantidade\": 1}]"
                .formatted(contraste, seringa)).andExpect(status().isOk());
        UUID semContraste = item(pedido(tomografia), tomografia);
        acao(semContraste, "realizacao", "{\"profissionalMatricula\": \"LD-001\"}").andExpect(status().isOk());
        UUID comContraste = item(pedido(tomografia), tomografia);
        acao(comContraste, "realizacao", "{\"profissionalMatricula\": \"LD-001\", \"contraste\": true, \"contrasteDescricao\": \"Iodado\"}")
                .andExpect(status().isOk());
        mockMvc.perform(get(ESTOQUE + "saldo?unidadeId=" + servicoImagem.getUuid()))
                .andExpect(jsonPath("$.itens[?(@.codigo == 'LDT-CONTRASTE')].total").value(org.hamcrest.Matchers.contains(4)))
                .andExpect(jsonPath("$.itens[?(@.codigo == 'LDT-SERINGA')].total").value(org.hamcrest.Matchers.contains(3)));
    }

    // ── Apoio ──────────────────────────────────────────────────────────────────────────────────

    private UnidadeDeSaude unidade(String nome, TipoUnidadeDeSaude tipo) {
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + nome);
        u.setTipo(tipo);
        u.setAtivo(true);
        return unidadeDeSaudeRepository.save(u);
    }

    private ExameLaboratorial exameLab(String nome) {
        ExameLaboratorial e = new ExameLaboratorial();
        e.setNome(PREFIXO + nome);
        e.setMaterial(MaterialExame.SANGUE);
        e.setTipoResultado(TipoResultadoExame.NUMERICO);
        e.setUnidadeMedida("g/dL");
        e.setReferenciaMinima(new BigDecimal("12"));
        e.setReferenciaMaxima(new BigDecimal("16"));
        e.setAtivo(true);
        return exameRepository.save(e);
    }

    private UUID material(String codigo, String nome) throws Exception {
        mockMvc.perform(post(ESTOQUE + "material").contentType(MediaType.APPLICATION_JSON).content("""
                {"codigo": "%s", "nome": "%s", "categoria": "LABORATORIO", "unidadeMedida": "UNIDADE", "controlaLote": false}
                """.formatted(codigo, nome))).andExpect(status().isCreated());
        return materialRepository.findAll().stream().filter(m -> m.getCodigo().equals(codigo)).map(MaterialEstoque::getUuid).findFirst().orElseThrow();
    }

    private void entrada(UUID material, int quantidade) throws Exception {
        entradaNa(ubs, material, quantidade);
    }

    private void entradaNa(UnidadeDeSaude unidade, UUID material, int quantidade) throws Exception {
        mockMvc.perform(post(ESTOQUE + "entrada").contentType(MediaType.APPLICATION_JSON).content("""
                {"unidadeId": "%s", "materialId": "%s", "quantidade": %d, "origem": "SALDO_INICIAL", "profissionalMatricula": "LD-001"}
                """.formatted(unidade.getUuid(), material, quantidade))).andExpect(status().isCreated());
    }

    private ResultActions insumos(ExameLaboratorial exame, String corpo) throws Exception {
        return mockMvc.perform(put(CATALOGO + exame.getUuid() + "/insumos").contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    private UUID pedido(ExameLaboratorial... exames) throws Exception {
        String ids = String.join("\", \"", Stream.of(exames).map(e -> e.getUuid().toString()).toList());
        boolean imagem = Stream.of(exames).anyMatch(e -> e.getCategoria() == CategoriaExame.IMAGEM);
        mockMvc.perform(post(PEDIDO).contentType(MediaType.APPLICATION_JSON).content("""
                {"pacienteId": "%s", "unidadeSolicitanteId": "%s", "profissionalMatricula": "LD-001", "exameIds": ["%s"],
                 "indicacaoClinica": "Controle.", "prioridade": "ROTINA"%s}
                """.formatted(paciente.getUuid(), ubs.getUuid(), ids,
                imagem ? ", \"unidadeExecutanteImagemId\": \"" + servicoImagem.getUuid() + "\"" : ""))).andExpect(status().isCreated());
        return pedidoRepository.findByPaciente_UuidOrderBySolicitadoEmDesc(paciente.getUuid()).stream()
                .filter(p -> itemRepository.findByPedido_Uuid(p.getUuid()).stream().allMatch(i -> i.getStatus().name().equals("SOLICITADO")))
                .findFirst().orElseThrow().getUuid();
    }

    private void coletar(UUID pedido) throws Exception {
        mockMvc.perform(post(PEDIDO + pedido + "/coleta").contentType(MediaType.APPLICATION_JSON).content("""
                {"profissionalMatricula": "LD-001", "unidadeColetaId": "%s"}
                """.formatted(ubs.getUuid()))).andExpect(status().isOk());
    }

    private UUID item(UUID pedido, ExameLaboratorial exame) {
        return itemRepository.findByPedido_Uuid(pedido).stream().filter(i -> i.getExame().getUuid().equals(exame.getUuid()))
                .map(ItemPedidoExame::getUuid).findFirst().orElseThrow();
    }

    private void liberado(UUID item, String valor) throws Exception {
        acao(item, "resultado", "{\"profissionalMatricula\": \"LD-001\", \"valorNumerico\": " + valor + "}").andExpect(status().isOk());
        acao(item, "liberacao", "{\"profissionalMatricula\": \"LD-001\"}").andExpect(status().isOk());
    }

    private ResultActions acao(UUID item, String rota, String corpo) throws Exception {
        return mockMvc.perform(post(PEDIDO + "item/" + item + "/" + rota).contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    private static String sha256(byte[] dados) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(dados));
    }
}
