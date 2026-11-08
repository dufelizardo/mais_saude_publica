package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

/**
 * Estoque e Almoxarifado, ES1 (ADR-0177): o catálogo, a entrada com e sem lote, a saída pelos lotes que vencem primeiro, a perda,
 * o ajuste, o mínimo e os alertas.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EstoqueControllerTest {

    private static final String PREFIXO = "Estoque Teste ";
    private static final String URL = "/api/v1/estoque/";

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private UnidadeDeSaudeRepository unidadeRepository;

    private UnidadeDeSaude ubs;
    private UnidadeDeSaude outra;

    @BeforeEach
    void seed() {
        limpar();
        ubs = unidade("UBS");
        outra = unidade("Outra");
    }

    @AfterEach
    void limpar() {
        String materiais = "(select uuid from tb_material_estoque where nome like '" + PREFIXO + "%')";
        jdbc.update("delete from tb_movimentacao_estoque where lote_id in (select uuid from tb_lote_material where material_id in " + materiais + ")");
        jdbc.update("delete from tb_lote_material where material_id in " + materiais);
        jdbc.update("delete from tb_estoque_minimo where material_id in " + materiais);
        jdbc.update("delete from tb_material_estoque where nome like '" + PREFIXO + "%'");
        jdbc.update("delete from tb_setor where nome like '" + PREFIXO + "%'");
        jdbc.update("delete from tb_unidade_de_saude where nome like '" + PREFIXO + "%'");
    }

    private UnidadeDeSaude unidade(String nome) {
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + nome);
        u.setTipo(TipoUnidadeDeSaude.UBS);
        u.setAtivo(true);
        return unidadeRepository.save(u);
    }

    private String enviar(MockHttpServletRequestBuilder r, String corpo, int esperado) throws Exception {
        var resposta = mockMvc.perform(r.contentType(MediaType.APPLICATION_JSON).content(corpo)).andReturn().getResponse();
        assertThat(resposta.getStatus()).as(resposta.getContentAsString()).isEqualTo(esperado);
        return resposta.getContentAsString();
    }

    private String ler(String url) throws Exception {
        var resposta = mockMvc.perform(get(url)).andReturn().getResponse();
        assertThat(resposta.getStatus()).as(resposta.getContentAsString()).isEqualTo(200);
        return resposta.getContentAsString();
    }

    private String material(String codigo, String nome, boolean lote) throws Exception {
        return JsonPath.read(enviar(post(URL + "material"), ("{\"codigo\": \"%s\", \"nome\": \"%s%s\", \"categoria\": \"MEDICO_HOSPITALAR\", "
                + "\"unidadeMedida\": \"UNIDADE\", \"controlaLote\": %s}").formatted(codigo, PREFIXO, nome, lote), 201), "$.uuid");
    }

    private String entrada(String material, int qtd, String lote, LocalDate validade, String extra, int esperado) throws Exception {
        String l = lote == null ? "" : ", \"numeroLote\": \"" + lote + "\"";
        String v = validade == null ? "" : ", \"validade\": \"" + validade + "\"";
        return enviar(post(URL + "entrada"), ("{\"unidadeId\": \"%s\", \"materialId\": \"%s\", \"quantidade\": %d, \"origem\": \"DOACAO\"%s%s%s}")
                .formatted(ubs.getUuid(), material, qtd, l, v, extra), esperado);
    }

    @Test
    void catalogoEEntrada() throws Exception {
        String luva = material("ES-LUVA", "Luva de procedimento M", true);
        enviar(post(URL + "material"), "{\"codigo\": \"es-luva\", \"nome\": \"%sOutra\", \"categoria\": \"EPI\", \"unidadeMedida\": \"CAIXA\"}".formatted(PREFIXO), 409);
        LocalDate validade = LocalDate.now().plusMonths(6);
        entrada(luva, 10, null, null, "", 400);
        enviar(post(URL + "entrada"), "{\"unidadeId\": \"%s\", \"materialId\": \"%s\", \"quantidade\": 5, \"origem\": \"NOTA_FISCAL\", \"numeroLote\": \"L1\", \"validade\": \"%s\"}"
                .formatted(ubs.getUuid(), luva, validade), 400);
        entrada(luva, 10, "L0", LocalDate.now().minusDays(1), "", 422);
        String primeira = entrada(luva, 10, "L1", validade, ", \"custoUnitario\": 0.5", 201);
        assertThat((String) JsonPath.read(primeira, "$.tipo")).isEqualTo("ENTRADA");
        entrada(luva, 5, "l1", validade.plusDays(1), "", 422);
        String segunda = entrada(luva, 5, "l1", validade, "", 201);
        assertThat((Integer) JsonPath.read(segunda, "$.saldoApos")).isEqualTo(15);
        enviar(put(URL + "material/" + luva), "{\"codigo\": \"ES-LUVA\", \"nome\": \"%sLuva de procedimento M\", \"categoria\": \"EPI\", \"unidadeMedida\": \"CAIXA\", \"controlaLote\": false}"
                .formatted(PREFIXO), 422);

        String papel = material("ES-PAPEL", "Papel A4", false);
        entrada(papel, 3, "X", null, "", 400);
        String carga = entrada(papel, 20, null, null, "", 201);
        assertThat((Integer) JsonPath.read(carga, "$.saldoApos")).isEqualTo(20);
    }

    @Test
    void saidaPelosLotesQueVencemPrimeiroPerdaEAjuste() throws Exception {
        String gaze = material("ES-GAZE", "Gaze estéril", true);
        entrada(gaze, 5, "TARDE", LocalDate.now().plusDays(200), "", 201);
        entrada(gaze, 4, "CEDO", LocalDate.now().plusDays(10), "", 201);
        // Um lote vencido, como fica quando a validade passa com saldo.
        jdbc.update("insert into tb_lote_material (uuid, material_id, unidade_id, numero_lote, validade, quantidade, criado_em) values (?, cast(? as uuid), ?, 'VENCIDO', ?, 3, now())",
                UUID.randomUUID(), gaze, ubs.getUuid(), LocalDate.now().minusDays(2));
        String vencido = jdbc.queryForObject("select cast(uuid as varchar) from tb_lote_material where numero_lote = 'VENCIDO' and material_id = cast(? as uuid)", String.class, gaze);
        jdbc.update("insert into tb_setor (uuid, unidade_id, nome, codigo, tipo, ativo) values (?, ?, ?, 'EST-CUR', 'ASSISTENCIAL', true)", UUID.randomUUID(), ubs.getUuid(), PREFIXO + "Curativo");
        jdbc.update("insert into tb_setor (uuid, unidade_id, nome, codigo, tipo, ativo) values (?, ?, ?, 'EST-ALH', 'ASSISTENCIAL', true)", UUID.randomUUID(), outra.getUuid(), PREFIXO + "Alheio");
        String setor = jdbc.queryForObject("select cast(uuid as varchar) from tb_setor where nome = ?", String.class, PREFIXO + "Curativo");
        String alheio = jdbc.queryForObject("select cast(uuid as varchar) from tb_setor where nome = ?", String.class, PREFIXO + "Alheio");

        String saida = "{\"unidadeId\": \"%s\", \"materialId\": \"%s\", \"quantidade\": %d%s}";
        enviar(post(URL + "saida"), saida.formatted(ubs.getUuid(), gaze, 10, ""), 422);
        enviar(post(URL + "saida"), saida.formatted(ubs.getUuid(), gaze, 1, ", \"setorId\": \"" + alheio + "\""), 422);
        enviar(post(URL + "saida"), saida.formatted(ubs.getUuid(), gaze, 1, ", \"loteId\": \"" + vencido + "\""), 422);
        String feitas = enviar(post(URL + "saida"), saida.formatted(ubs.getUuid(), gaze, 6, ", \"setorId\": \"" + setor + "\""), 201);
        assertThat((List<String>) JsonPath.read(feitas, "$[*].numeroLote")).containsExactly("CEDO", "TARDE");
        assertThat((List<Integer>) JsonPath.read(feitas, "$[*].quantidade")).containsExactly(-4, -2);
        assertThat((List<String>) JsonPath.read(feitas, "$[*].setorNome")).containsOnly(PREFIXO + "Curativo");

        String perda = "{\"loteId\": \"%s\", \"quantidade\": %d, \"motivo\": \"VENCIMENTO\"}";
        enviar(post(URL + "perda"), perda.formatted(vencido, 4), 422);
        enviar(post(URL + "perda"), perda.formatted(vencido, 3), 201);
        String tarde = jdbc.queryForObject("select cast(uuid as varchar) from tb_lote_material where numero_lote = 'TARDE' and material_id = cast(? as uuid)", String.class, gaze);
        enviar(post(URL + "ajuste"), "{\"loteId\": \"%s\", \"quantidadeContada\": 3, \"justificativa\": \"Contagem.\"}".formatted(tarde), 422);
        String ajuste = enviar(post(URL + "ajuste"), "{\"loteId\": \"%s\", \"quantidadeContada\": 2, \"justificativa\": \"Pacote rasgado achado na contagem.\"}".formatted(tarde), 201);
        assertThat((Integer) JsonPath.read(ajuste, "$.quantidade")).isEqualTo(-1);

        String livro = ler(URL + "movimentacao?unidadeId=" + ubs.getUuid() + "&materialId=" + gaze);
        assertThat((List<String>) JsonPath.read(livro, "$[*].tipo")).containsExactlyInAnyOrder("ENTRADA", "ENTRADA", "SAIDA_CONSUMO", "SAIDA_CONSUMO",
                "PERDA", "AJUSTE_INVENTARIO");
    }

    @Test
    void saldoComMinimoEAlertas() throws Exception {
        String seringa = material("ES-SERINGA", "Seringa 5 ml", true);
        String mascara = material("ES-MASCARA", "Máscara cirúrgica", true);
        entrada(seringa, 8, "S1", LocalDate.now().plusDays(20), ", \"custoUnitario\": 0.25", 201);
        entrada(mascara, 50, "M1", LocalDate.now().plusDays(400), ", \"custoUnitario\": 0.1", 201);
        String minimo = enviar(put(URL + "minimo"), "{\"unidadeId\": \"%s\", \"materialId\": \"%s\", \"quantidadeMinima\": 10}".formatted(ubs.getUuid(), seringa), 200);
        assertThat((Boolean) JsonPath.read(minimo, "$.abaixoDoMinimo")).isTrue();
        enviar(put(URL + "minimo"), "{\"unidadeId\": \"%s\", \"materialId\": \"%s\", \"quantidadeMinima\": 20}".formatted(ubs.getUuid(), mascara), 200);

        String saldo = ler(URL + "saldo?unidadeId=" + ubs.getUuid());
        assertThat((List<String>) JsonPath.read(saldo, "$.itens[*].codigo")).containsExactly("ES-MASCARA", "ES-SERINGA");
        assertThat((Integer) JsonPath.read(saldo, "$.abaixoDoMinimo")).isEqualTo(1);
        assertThat((Integer) JsonPath.read(saldo, "$.vencendo")).isEqualTo(1);
        assertThat((List<String>) JsonPath.read(saldo, "$.vencimentos[*].numeroLote")).containsExactly("S1");
        assertThat(((Number) JsonPath.read(saldo, "$.valorTotal")).doubleValue()).isEqualTo(7.0);
        assertThat((List<Object>) JsonPath.read(ler(URL + "saldo?unidadeId=" + outra.getUuid()), "$.itens")).isEmpty();
        assertThat((List<String>) JsonPath.read(ler(URL + "material?ativos=true"), "$[*].codigo")).contains("ES-MASCARA", "ES-SERINGA");
    }
}
