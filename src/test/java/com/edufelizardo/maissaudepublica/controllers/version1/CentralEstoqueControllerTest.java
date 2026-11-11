package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

/**
 * Estoque, ES2 (ADR-0189), com login e autorização desligados: a central de abastecimento, a requisição até o recebimento, a
 * transferência avulsa com divergência e o inventário com fechamento. O escopo fica em {@link CentralEstoqueAutorizacaoControllerTest}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CentralEstoqueControllerTest {

    private static final String PREFIXO = "EstCentral Teste ";
    private static final String URL = "/api/v1/estoque/";
    private static final String MAT = "\"profissionalMatricula\": \"ECT-ALM\"";

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private UnidadeDeSaudeRepository unidadeRepository;
    @Autowired private ProfissionalRepository profissionalRepository;

    private UnidadeDeSaude central;
    private UnidadeDeSaude ubs;
    private UnidadeDeSaude upa;

    @BeforeEach
    void seed() {
        limpar();
        central = unidade("Central");
        ubs = unidade("UBS");
        upa = unidade("UPA");
        jdbc.update("insert into tb_unidade_capacidade_assistencial (uuid, unidade_id, capacidade_assistencial_id, inicio) select gen_random_uuid(), ?, uuid, ? "
                + "from tb_capacidade_assistencial where codigo = 'CENTRAL_ABASTECIMENTO'", central.getUuid(), LocalDate.now().minusDays(1));
        Profissional p = new Profissional();
        p.setMatricula("ECT-ALM");
        p.setCpf("91122233344");
        p.setNome(PREFIXO + "Almoxarife");
        p.setAtivo(true);
        profissionalRepository.save(p);
    }

    @AfterEach
    void limpar() {
        String unidades = "(select uuid from tb_unidade_de_saude where nome like '" + PREFIXO + "%')";
        String materiais = "(select uuid from tb_material_estoque where nome like '" + PREFIXO + "%')";
        jdbc.update("delete from tb_item_inventario_estoque where inventario_id in (select uuid from tb_inventario_estoque where unidade_id in " + unidades + ")");
        jdbc.update("delete from tb_inventario_estoque where unidade_id in " + unidades);
        jdbc.update("delete from tb_item_transferencia_estoque where transferencia_id in (select uuid from tb_transferencia_estoque where origem_id in " + unidades + ")");
        jdbc.update("delete from tb_transferencia_estoque where origem_id in " + unidades);
        jdbc.update("delete from tb_item_requisicao_estoque where requisicao_id in (select uuid from tb_requisicao_estoque where unidade_id in " + unidades + ")");
        jdbc.update("delete from tb_requisicao_estoque where unidade_id in " + unidades);
        jdbc.update("delete from tb_movimentacao_estoque where lote_id in (select uuid from tb_lote_material where material_id in " + materiais + ")");
        jdbc.update("delete from tb_lote_material where material_id in " + materiais);
        jdbc.update("delete from tb_material_estoque where nome like '" + PREFIXO + "%'");
        jdbc.update("delete from tb_unidade_capacidade_assistencial where unidade_id in " + unidades);
        jdbc.update("delete from tb_profissional where matricula = 'ECT-ALM'");
        jdbc.update("delete from tb_unidade_de_saude where nome like '" + PREFIXO + "%'");
    }

    @Test
    void requisicaoAtendidaEmParteRecebidaComDivergencia() throws Exception {
        String luva = material("ECT-LUVA", "Luva M");
        String gaze = material("ECT-GAZE", "Gaze");
        entrada(central, luva, 30, "L-CEDO", LocalDate.now().plusDays(30));
        entrada(central, luva, 50, "L-TARDE", LocalDate.now().plusDays(300));
        entrada(central, gaze, 5, "G1", LocalDate.now().plusDays(100));
        enviar(get(URL + "centrais"), null, 200);
        assertThat((List<String>) JsonPath.read(enviar(get(URL + "centrais"), null, 200), "$[*].unidadeId")).contains(central.getUuid().toString());

        enviar(post(URL + "requisicao"), "{%s, \"unidadeId\": \"%s\", \"centralId\": \"%s\", \"itens\": []}".formatted(MAT, ubs.getUuid(), central.getUuid()), 400);
        enviar(post(URL + "requisicao"), "{%s, \"unidadeId\": \"%s\", \"centralId\": \"%s\", \"itens\": [{\"materialId\": \"%s\", \"quantidade\": 1}]}"
                .formatted(MAT, ubs.getUuid(), upa.getUuid(), luva), 422);
        String req = JsonPath.read(enviar(post(URL + "requisicao"), ("{%s, \"unidadeId\": \"%s\", \"centralId\": \"%s\", \"prioridade\": \"URGENTE\", \"itens\": ["
                + "{\"materialId\": \"%s\", \"quantidade\": 40}, {\"materialId\": \"%s\", \"quantidade\": 10}]}")
                .formatted(MAT, ubs.getUuid(), central.getUuid(), luva, gaze), 201), "$.uuid");
        // A lista pela central (sem autorização, a unidade também vê).
        assertThat((List<String>) JsonPath.read(enviar(get(URL + "requisicao?centralId=" + central.getUuid()), null, 200), "$[*].uuid")).contains(req);
        enviar(post(URL + "requisicao/" + req + "/atendimento"), "{%s, \"itens\": []}".formatted(MAT), 422);
        enviar(put(URL + "requisicao/" + req), ("{%s, \"centralId\": \"%s\", \"itens\": [{\"materialId\": \"%s\", \"quantidade\": 40}, {\"materialId\": \"%s\", \"quantidade\": 8}]}")
                .formatted(MAT, central.getUuid(), luva, gaze), 200);
        enviar(post(URL + "requisicao/" + req + "/envio"), "{%s}".formatted(MAT), 200);
        enviar(put(URL + "requisicao/" + req), "{%s, \"centralId\": \"%s\", \"itens\": [{\"materialId\": \"%s\", \"quantidade\": 1}]}".formatted(MAT, central.getUuid(), luva), 422);

        String detalhe = enviar(get(URL + "requisicao/" + req), null, 200);
        assertThat((Integer) JsonPath.parse(detalhe).read("$.itens[?(@.materialId == '%s')].saldoNaCentral".formatted(luva), List.class).get(0)).isEqualTo(80);
        enviar(post(URL + "requisicao/" + req + "/atendimento"), "{%s, \"itens\": [{\"materialId\": \"%s\", \"quantidade\": 41}]}".formatted(MAT, luva), 400);
        enviar(post(URL + "requisicao/" + req + "/atendimento"), "{%s, \"itens\": [{\"materialId\": \"%s\", \"quantidade\": 40}, {\"materialId\": \"%s\", \"quantidade\": 8}]}"
                .formatted(MAT, luva, gaze), 422);
        String atendida = enviar(post(URL + "requisicao/" + req + "/atendimento"), "{%s, \"itens\": [{\"materialId\": \"%s\", \"quantidade\": 40}, {\"materialId\": \"%s\", \"quantidade\": 5}]}"
                .formatted(MAT, luva, gaze), 200);
        assertThat((String) JsonPath.read(atendida, "$.situacao")).isEqualTo("ATENDIDA");
        assertThat((Boolean) JsonPath.read(atendida, "$.parcial")).isTrue();
        String transferencia = JsonPath.read(atendida, "$.transferenciaId");
        // FEFO: 30 do lote que vence antes e 10 do outro.
        assertThat(saldoDoLote(central, "L-CEDO")).isZero();
        assertThat(saldoDoLote(central, "L-TARDE")).isEqualTo(40);

        String t = enviar(get(URL + "transferencia/" + transferencia), null, 200);
        String itemCedo = JsonPath.parse(t).read("$.itens[?(@.numeroLote == 'L-CEDO')].uuid", List.class).get(0).toString();
        String itemTarde = JsonPath.parse(t).read("$.itens[?(@.numeroLote == 'L-TARDE')].uuid", List.class).get(0).toString();
        String itemGaze = JsonPath.parse(t).read("$.itens[?(@.numeroLote == 'G1')].uuid", List.class).get(0).toString();
        enviar(post(URL + "transferencia/" + transferencia + "/recebimento"), "{%s, \"itens\": [{\"itemId\": \"%s\", \"quantidade\": 30}]}".formatted(MAT, itemCedo), 400);
        enviar(post(URL + "transferencia/" + transferencia + "/recebimento"), ("{%s, \"itens\": [{\"itemId\": \"%s\", \"quantidade\": 28}, {\"itemId\": \"%s\", \"quantidade\": 10}, "
                + "{\"itemId\": \"%s\", \"quantidade\": 5}]}").formatted(MAT, itemCedo, itemTarde, itemGaze), 400);
        String recebida = enviar(post(URL + "transferencia/" + transferencia + "/recebimento"), ("{%s, \"itens\": ["
                + "{\"itemId\": \"%s\", \"quantidade\": 28, \"motivoDivergencia\": \"FALTA\"}, {\"itemId\": \"%s\", \"quantidade\": 10}, "
                + "{\"itemId\": \"%s\", \"quantidade\": 4, \"motivoDivergencia\": \"AVARIA\", \"observacao\": \"Pacote rasgado.\"}]}")
                .formatted(MAT, itemCedo, itemTarde, itemGaze), 200);
        assertThat((Boolean) JsonPath.read(recebida, "$.comDivergencia")).isTrue();
        assertThat(saldoDoLote(ubs, "L-CEDO")).isEqualTo(28);
        assertThat(saldoDoLote(ubs, "L-TARDE")).isEqualTo(10);
        assertThat(saldoDoLote(ubs, "G1")).isEqualTo(4);
        assertThat(saldoDoLote(central, "L-CEDO")).isEqualTo(2);
        assertThat(saldoDoLote(central, "G1")).isZero();
        assertThat(jdbc.queryForObject("select count(*) from tb_movimentacao_estoque where tipo = 'PERDA' and motivo_perda = 'AVARIA' and transferencia_id = cast(? as uuid)",
                Integer.class, transferencia)).isEqualTo(1);
        assertThat((String) JsonPath.read(enviar(get(URL + "requisicao/" + req), null, 200), "$.situacao")).isEqualTo("RECEBIDA");
        enviar(post(URL + "transferencia/" + transferencia + "/recebimento"), "{%s, \"itens\": []}".formatted(MAT), 422);
    }

    @Test
    void negarCancelarETransferenciaAvulsa() throws Exception {
        String luva = material("ECT-LUVA2", "Luva G");
        entrada(ubs, luva, 10, "A1", LocalDate.now().plusDays(60));
        String r1 = JsonPath.read(enviar(post(URL + "requisicao"), "{%s, \"unidadeId\": \"%s\", \"centralId\": \"%s\", \"itens\": [{\"materialId\": \"%s\", \"quantidade\": 5}]}"
                .formatted(MAT, upa.getUuid(), central.getUuid(), luva), 201), "$.uuid");
        enviar(post(URL + "requisicao/" + r1 + "/negacao"), "{%s, \"motivo\": \"Sem estoque.\"}".formatted(MAT), 422);
        enviar(post(URL + "requisicao/" + r1 + "/envio"), "{%s}".formatted(MAT), 200);
        enviar(post(URL + "requisicao/" + r1 + "/negacao"), "{%s}".formatted(MAT), 400);
        assertThat((String) JsonPath.read(enviar(post(URL + "requisicao/" + r1 + "/negacao"), "{%s, \"motivo\": \"Sem estoque.\"}".formatted(MAT), 200), "$.situacao"))
                .isEqualTo("NEGADA");
        String r2 = JsonPath.read(enviar(post(URL + "requisicao"), "{%s, \"unidadeId\": \"%s\", \"centralId\": \"%s\", \"itens\": [{\"materialId\": \"%s\", \"quantidade\": 5}]}"
                .formatted(MAT, upa.getUuid(), central.getUuid(), luva), 201), "$.uuid");
        assertThat((String) JsonPath.read(enviar(post(URL + "requisicao/" + r2 + "/cancelamento"), "{%s, \"motivo\": \"Pedido em duplicidade.\"}".formatted(MAT), 200), "$.situacao"))
                .isEqualTo("CANCELADA");

        String lote = jdbc.queryForObject("select cast(uuid as varchar) from tb_lote_material where numero_lote = 'A1' and unidade_id = ?", String.class, ubs.getUuid());
        enviar(post(URL + "transferencia"), "{%s, \"unidadeId\": \"%s\", \"destinoId\": \"%s\", \"itens\": [{\"loteId\": \"%s\", \"quantidade\": 11}]}"
                .formatted(MAT, ubs.getUuid(), upa.getUuid(), lote), 422);
        enviar(post(URL + "transferencia"), "{%s, \"unidadeId\": \"%s\", \"destinoId\": \"%s\", \"itens\": [{\"loteId\": \"%s\", \"quantidade\": 4}]}"
                .formatted(MAT, ubs.getUuid(), ubs.getUuid(), lote), 422);
        String t = JsonPath.read(enviar(post(URL + "transferencia"), "{%s, \"unidadeId\": \"%s\", \"destinoId\": \"%s\", \"itens\": [{\"loteId\": \"%s\", \"quantidade\": 4}]}"
                .formatted(MAT, ubs.getUuid(), upa.getUuid(), lote), 201), "$.uuid");
        assertThat(saldoDoLote(ubs, "A1")).isEqualTo(6);
        String item = JsonPath.read(enviar(get(URL + "transferencia/" + t), null, 200), "$.itens[0].uuid");
        enviar(post(URL + "transferencia/" + t + "/recebimento"), "{%s, \"itens\": [{\"itemId\": \"%s\", \"quantidade\": 4}]}".formatted(MAT, item), 200);
        assertThat(saldoDoLote(upa, "A1")).isEqualTo(4);
        assertThat((List<String>) JsonPath.read(enviar(get(URL + "transferencia?unidadeId=" + upa.getUuid()), null, 200), "$[*].uuid")).contains(t);
    }

    @Test
    void inventarioBloqueiaAMovimentacaoEFechaComAjustes() throws Exception {
        String luva = material("ECT-LUVA3", "Luva P");
        entrada(ubs, luva, 10, "I1", LocalDate.now().plusDays(60));
        entrada(ubs, luva, 5, "I2", LocalDate.now().plusDays(90));
        String inv = JsonPath.read(enviar(post(URL + "inventario"), "{%s, \"unidadeId\": \"%s\"}".formatted(MAT, ubs.getUuid()), 201), "$.uuid");
        enviar(post(URL + "inventario"), "{%s, \"unidadeId\": \"%s\"}".formatted(MAT, ubs.getUuid()), 409);
        enviar(post(URL + "inventario"), "{%s, \"unidadeId\": \"%s\"}".formatted(MAT, upa.getUuid()), 422);
        enviar(post(URL + "saida"), "{\"unidadeId\": \"%s\", \"materialId\": \"%s\", \"quantidade\": 1, %s}".formatted(ubs.getUuid(), luva, MAT), 422);

        String detalhe = enviar(get(URL + "inventario/" + inv), null, 200);
        String i1 = JsonPath.parse(detalhe).read("$.itens[?(@.numeroLote == 'I1')].uuid", List.class).get(0).toString();
        String i2 = JsonPath.parse(detalhe).read("$.itens[?(@.numeroLote == 'I2')].uuid", List.class).get(0).toString();
        enviar(post(URL + "inventario/" + inv + "/contagem"), "{%s, \"itens\": [{\"itemId\": \"%s\", \"quantidade\": 8}]}".formatted(MAT, i1), 200);
        enviar(post(URL + "inventario/" + inv + "/fechamento"), "{%s}".formatted(MAT), 422);
        String contado = enviar(post(URL + "inventario/" + inv + "/contagem"), "{%s, \"itens\": [{\"itemId\": \"%s\", \"quantidade\": 5}]}".formatted(MAT, i2), 200);
        assertThat((Integer) JsonPath.read(contado, "$.comDiferenca")).isEqualTo(1);
        String fechado = enviar(post(URL + "inventario/" + inv + "/fechamento"), "{%s}".formatted(MAT), 200);
        assertThat((String) JsonPath.read(fechado, "$.situacao")).isEqualTo("FECHADO");
        assertThat(saldoDoLote(ubs, "I1")).isEqualTo(8);
        assertThat(saldoDoLote(ubs, "I2")).isEqualTo(5);
        enviar(post(URL + "saida"), "{\"unidadeId\": \"%s\", \"materialId\": \"%s\", \"quantidade\": 1, %s}".formatted(ubs.getUuid(), luva, MAT), 201);

        String outro = JsonPath.read(enviar(post(URL + "inventario"), "{%s, \"unidadeId\": \"%s\"}".formatted(MAT, ubs.getUuid()), 201), "$.uuid");
        enviar(post(URL + "inventario/" + outro + "/cancelamento"), "{%s}".formatted(MAT), 400);
        assertThat((String) JsonPath.read(enviar(post(URL + "inventario/" + outro + "/cancelamento"), "{%s, \"motivo\": \"Aberto por engano.\"}".formatted(MAT), 200), "$.situacao"))
                .isEqualTo("CANCELADO");
        assertThat((List<String>) JsonPath.read(enviar(get(URL + "inventario?unidadeId=" + ubs.getUuid()), null, 200), "$[*].situacao")).containsExactly("CANCELADO", "FECHADO");
    }

    // ── Apoio ──────────────────────────────────────────────────────────────────────────────────

    private UnidadeDeSaude unidade(String nome) {
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + nome);
        u.setTipo(TipoUnidadeDeSaude.UBS);
        u.setAtivo(true);
        return unidadeRepository.save(u);
    }

    private String material(String codigo, String nome) throws Exception {
        return JsonPath.read(enviar(post(URL + "material"), ("{\"codigo\": \"%s\", \"nome\": \"%s%s\", \"categoria\": \"MEDICO_HOSPITALAR\", "
                + "\"unidadeMedida\": \"UNIDADE\", \"controlaLote\": true}").formatted(codigo, PREFIXO, nome), 201), "$.uuid");
    }

    private void entrada(UnidadeDeSaude u, String material, int qtd, String lote, LocalDate validade) throws Exception {
        enviar(post(URL + "entrada"), ("{\"unidadeId\": \"%s\", \"materialId\": \"%s\", \"quantidade\": %d, \"origem\": \"DOACAO\", \"numeroLote\": \"%s\", \"validade\": \"%s\"}")
                .formatted(u.getUuid(), material, qtd, lote, validade), 201);
    }

    private int saldoDoLote(UnidadeDeSaude u, String lote) {
        return jdbc.queryForObject("select quantidade from tb_lote_material where numero_lote = ? and unidade_id = ?", Integer.class, lote, u.getUuid());
    }

    private String enviar(MockHttpServletRequestBuilder r, String corpo, int esperado) throws Exception {
        if (corpo != null) {
            r = r.contentType(MediaType.APPLICATION_JSON).content(corpo);
        }
        var resposta = mockMvc.perform(r).andReturn().getResponse();
        assertThat(resposta.getStatus()).as(resposta.getContentAsString()).isEqualTo(esperado);
        return resposta.getContentAsString();
    }
}
