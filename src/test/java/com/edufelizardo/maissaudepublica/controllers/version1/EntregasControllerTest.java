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

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Compras, fatia CP3 (ADR-0214): a ordem que reserva o saldo; o recebimento provisório com a nota, o lote e a validade; o definitivo com o
 * aceite, a recusa que vira ocorrência e a entrada no Estoque (com o fornecedor ligado) e na Farmácia; o cancelamento que devolve o saldo;
 * o atraso que nasce uma vez; as ocorrências encerradas e sancionadas; e o painel.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EntregasControllerTest {

    static final String PREFIXO = "Entregas Teste ";
    private static final String API = "/api/v1/compras/";

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private UnidadeDeSaudeRepository unidadeRepository;
    @Autowired private ProfissionalRepository profissionalRepository;

    private final LocalDate hoje = LocalDate.now(ZoneId.of("America/Sao_Paulo"));
    private UnidadeDeSaude ubs;

    @BeforeEach
    void seed() {
        limpar();
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + "UBS");
        u.setTipo(TipoUnidadeDeSaude.UBS);
        u.setAtivo(true);
        ubs = unidadeRepository.save(u);
        for (String[] p : new String[][]{{"ENT-GESTOR", "90088910001"}, {"ENT-FISCAL", "90088910002"}}) {
            Profissional x = new Profissional();
            x.setMatricula(p[0]);
            x.setCpf(p[1]);
            x.setNome(PREFIXO + p[0]);
            x.setAtivo(true);
            profissionalRepository.save(x);
        }
    }

    @AfterEach
    void limpar() {
        ComprasControllerTest.limparDados(jdbc, PREFIXO);
        jdbc.update("delete from tb_profissional where matricula like 'ENT-%'");
    }

    private String enviar(MockHttpServletRequestBuilder r, String corpo, int esperado) throws Exception {
        var resposta = mockMvc.perform(r.contentType(MediaType.APPLICATION_JSON).content(corpo)).andReturn().getResponse();
        String texto = resposta.getContentAsString(StandardCharsets.UTF_8);
        assertThat(resposta.getStatus()).as(texto).isEqualTo(esperado);
        return texto;
    }

    private String ler(String url) throws Exception {
        var resposta = mockMvc.perform(get(url)).andReturn().getResponse();
        assertThat(resposta.getStatus()).as(resposta.getContentAsString()).isEqualTo(200);
        return resposta.getContentAsString(StandardCharsets.UTF_8);
    }

    /**
     * Um contrato de um fornecedor regular com três itens: luva (com lote) 100 a 2,00, dipirona 50 a 1,00 e manutenção 1 a 500,00. Devolve
     * [contrato, fornecedor, item luva, item dipirona, item manutenção, medicamento].
     */
    static String[] contrato(MockMvc mvc, JdbcTemplate jdbc, String prefixo, UnidadeDeSaude ubs, LocalDate hoje, String base, String gestor, String fiscal,
                             java.util.function.UnaryOperator<MockHttpServletRequestBuilder> decorar) throws Exception {
        EntregasControllerTest t = new EntregasControllerTest();
        t.decorar = decorar;
        return t.montar(mvc, jdbc, prefixo, ubs, hoje, base, gestor, fiscal);
    }

    private MockMvc mvc;
    /** Com a segurança ligada, põe o token de quem monta o contrato. */
    private java.util.function.UnaryOperator<MockHttpServletRequestBuilder> decorar = x -> x;

    private String enviarCom(MockHttpServletRequestBuilder r, String corpo, int esperado) throws Exception {
        var resposta = mvc.perform(decorar.apply(r).contentType(MediaType.APPLICATION_JSON).content(corpo)).andReturn().getResponse();
        String texto = resposta.getContentAsString(StandardCharsets.UTF_8);
        assertThat(resposta.getStatus()).as(texto).isEqualTo(esperado);
        return texto;
    }

    private String[] montar(MockMvc mockMvc, JdbcTemplate jdbc, String prefixo, UnidadeDeSaude ubs, LocalDate hoje, String base, String gestor, String fiscal)
            throws Exception {
        this.mvc = mockMvc;
        String luva = JsonPath.read(enviarCom(post("/api/v1/estoque/material"), ("{\"codigo\": \"%s\", \"nome\": \"%sLuva %s\", \"categoria\": \"EPI\", "
                + "\"unidadeMedida\": \"CAIXA\", \"controlaLote\": true}").formatted("ENT-" + base, prefixo, base), 201), "$.uuid");
        String dipirona = jdbc.queryForObject("insert into tb_medicamento (uuid, nome, ativo) values (gen_random_uuid(), ?, true) returning cast(uuid as varchar)",
                String.class, prefixo + "Dipirona " + base);
        String itens = "[{\"tipo\": \"MATERIAL\", \"materialId\": \"" + luva + "\", \"quantidade\": 100}, {\"tipo\": \"MEDICAMENTO\", \"medicamentoId\": \"" + dipirona
                + "\", \"quantidade\": 50}, {\"tipo\": \"SERVICO\", \"descricao\": \"Manutenção do autoclave\", \"unidadeMedida\": \"servico\", \"quantidade\": 1}]";
        String s = JsonPath.read(enviarCom(post(API + "solicitacao"), "{\"unidadeId\": \"" + ubs.getUuid() + "\", \"justificativa\": \"Reposição.\", \"itens\": "
                + itens + "}", 201), "$.uuid");
        enviarCom(post(API + "solicitacao/" + s + "/envio"), "", 200);
        String sol = enviarCom(post(API + "solicitacao/" + s + "/analise"), "{\"decisao\": \"APROVAR\"}", 200);
        String[] precos = {"2", "1", "500"};
        for (int i = 0; i < 3; i++) {
            enviarCom(post(API + "item/" + JsonPath.read(sol, "$.itens[" + i + "].uuid") + "/cotacao"), "{\"fonte\": \"SITE\", \"referencia\": \"x\", "
                    + "\"precoUnitario\": " + precos[i] + ", \"cotadoEm\": \"" + hoje + "\"}", 201);
        }
        String justificativas = "{\"justificativas\": [" + String.join(", ", List.of(0, 1, 2).stream().map(i -> "{\"itemId\": \"" + JsonPath.read(sol, "$.itens[" + i
                + "].uuid") + "\", \"justificativa\": \"Única fonte.\"}").toList()) + "]}";
        enviarCom(post(API + "solicitacao/" + s + "/pesquisa"), justificativas, 200);
        String forn = JsonPath.read(enviarCom(post(API + "fornecedor"), "{\"documento\": \"" + ComprasControllerTest.cnpj(base) + "\", \"razaoSocial\": \""
                + prefixo + "Distribuidora " + base + "\"}", 201), "$.uuid");
        for (String tipo : List.of("FEDERAL", "FGTS", "TRABALHISTA", "ESTADUAL", "MUNICIPAL")) {
            enviarCom(post(API + "fornecedor/" + forn + "/certidao"), "{\"tipo\": \"" + tipo + "\", \"emitidaEm\": \"" + hoje + "\", \"validaAte\": \""
                    + hoje.plusDays(120) + "\"}", 201);
        }
        String c = enviarCom(post(API + "contratacao"), "{\"numeroProcesso\": \"" + prefixo + base + "\", \"modalidade\": \"PREGAO_ELETRONICO\", \"objeto\": \"x\", "
                + "\"solicitacaoIds\": [\"" + s + "\"]}", 201);
        for (int i = 0; i < 3; i++) {
            enviarCom(post(API + "item-contratacao/" + JsonPath.read(c, "$.itens[" + i + "].uuid") + "/resultado"), "{\"resultado\": \"ADJUDICADO\", "
                    + "\"fornecedorId\": \"" + forn + "\", \"precoUnitario\": " + precos[i] + "}", 200);
        }
        c = enviarCom(post(API + "contratacao/" + JsonPath.read(c, "$.uuid") + "/homologacao"), "{\"instrumentos\": [{\"fornecedorId\": \"" + forn + "\", "
                + "\"tipo\": \"CONTRATO\", \"numero\": \"" + prefixo + "CT " + base + "\", \"inicio\": \"" + hoje + "\", \"fim\": \"" + hoje.plusMonths(11)
                + "\", \"gestorMatricula\": \"" + gestor + "\", \"fiscalMatricula\": \"" + fiscal + "\"}]}", 200);
        String ct = JsonPath.read(c, "$.instrumentos[0].uuid");
        var det = mvc.perform(decorar.apply(get(API + "instrumento/" + ct))).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return new String[]{ct, forn, JsonPath.read(det, "$.itens[0].uuid"), JsonPath.read(det, "$.itens[1].uuid"), JsonPath.read(det, "$.itens[2].uuid"), dipirona};
    }

    @Test
    void ordemRecebimentoEEntrada() throws Exception {
        String[] k = montar(mockMvc, jdbc, PREFIXO, ubs, hoje, "905100000001", "ENT-GESTOR", "ENT-FISCAL");
        String ct = k[0], forn = k[1], luva = k[2], dipirona = k[3], manutencao = k[4];
        String ordem = "{\"unidadeId\": \"" + ubs.getUuid() + "\", \"empenho\": \"2026NE000123\", \"itens\": [%s]}";
        enviar(post(API + "instrumento/" + ct + "/ordem"), ordem.formatted("{\"itemInstrumentoId\": \"" + luva + "\", \"quantidade\": 101}"), 422);
        enviar(post(API + "instrumento/" + ct + "/ordem"), ordem.formatted("{\"itemInstrumentoId\": \"" + ubs.getUuid() + "\", \"quantidade\": 1}"), 422);
        String o = enviar(post(API + "instrumento/" + ct + "/ordem"), ordem.formatted("{\"itemInstrumentoId\": \"" + luva + "\", \"quantidade\": 60}, "
                + "{\"itemInstrumentoId\": \"" + dipirona + "\", \"quantidade\": 50}, {\"itemInstrumentoId\": \"" + manutencao + "\", \"quantidade\": 1}"), 201);
        String ordemId = JsonPath.read(o, "$.uuid");
        assertThat((String) JsonPath.read(o, "$.prazoEm")).isEqualTo(hoje.plusDays(30).toString());
        assertThat(JsonPath.read(o, "$.valor").toString()).isEqualTo("670.0");
        assertThat((Integer) JsonPath.read(ler(API + "instrumento/" + ct), "$.itens[0].saldo")).isEqualTo(40);
        assertThat((Integer) JsonPath.read(ler(API + "instrumento/" + ct), "$.ordensEmAberto")).isEqualTo(1);
        String iLuva = JsonPath.read(o, "$.itens[0].uuid"), iDipirona = JsonPath.read(o, "$.itens[1].uuid"), iManut = JsonPath.read(o, "$.itens[2].uuid");
        assertThat((Boolean) JsonPath.read(o, "$.itens[0].pedeLote")).isTrue();
        assertThat((Boolean) JsonPath.read(o, "$.itens[2].pedeLote")).isFalse();

        // Recebimento provisório.
        String rec = "{\"notaFiscal\": \"NF 9988\", \"notaEmitidaEm\": \"%s\", \"itens\": [%s]}";
        String validade = hoje.plusYears(1).toString();
        String luvaOk = "{\"itemOrdemId\": \"" + iLuva + "\", \"quantidade\": 30, \"numeroLote\": \"L1\", \"validade\": \"" + validade + "\"}";
        enviar(post(API + "ordem/" + ordemId + "/recebimento"), rec.formatted(hoje.plusDays(1), luvaOk), 400);
        enviar(post(API + "ordem/" + ordemId + "/recebimento"), rec.formatted(hoje, "{\"itemOrdemId\": \"" + iLuva + "\", \"quantidade\": 30}"), 400);
        enviar(post(API + "ordem/" + ordemId + "/recebimento"), rec.formatted(hoje, "{\"itemOrdemId\": \"" + iManut + "\", \"quantidade\": 1, \"numeroLote\": \"X\", "
                + "\"validade\": \"" + validade + "\"}"), 400);
        enviar(post(API + "ordem/" + ordemId + "/recebimento"), rec.formatted(hoje, "{\"itemOrdemId\": \"" + iLuva + "\", \"quantidade\": 61, \"numeroLote\": \"L1\", "
                + "\"validade\": \"" + validade + "\"}"), 422);
        o = enviar(post(API + "ordem/" + ordemId + "/recebimento"), rec.formatted(hoje, luvaOk + ", {\"itemOrdemId\": \"" + iDipirona + "\", \"quantidade\": 50, "
                + "\"numeroLote\": \"M1\", \"validade\": \"" + validade + "\"}, {\"itemOrdemId\": \"" + iManut + "\", \"quantidade\": 1}"), 201);
        assertThat((Integer) JsonPath.read(o, "$.itens[0].emRecebimento")).isEqualTo(30);
        assertThat((Integer) JsonPath.read(o, "$.itens[0].pendente")).isEqualTo(30);
        enviar(post(API + "ordem/" + ordemId + "/recebimento"), rec.formatted(hoje, "{\"itemOrdemId\": \"" + iLuva + "\", \"quantidade\": 31, \"numeroLote\": \"L2\", "
                + "\"validade\": \"" + validade + "\"}"), 422);
        enviar(post(API + "ordem/" + ordemId + "/cancelamento"), "{\"motivo\": \"x\"}", 422);
        String recebimento = JsonPath.read(o, "$.recebimentos[0].uuid");
        String rLuva = ((List<?>) JsonPath.read(o, "$.recebimentos[0].itens[?(@.itemOrdemId == '" + iLuva + "')].uuid")).get(0).toString();
        String rDip = ((List<?>) JsonPath.read(o, "$.recebimentos[0].itens[?(@.itemOrdemId == '" + iDipirona + "')].uuid")).get(0).toString();
        String rMan = ((List<?>) JsonPath.read(o, "$.recebimentos[0].itens[?(@.itemOrdemId == '" + iManut + "')].uuid")).get(0).toString();

        // Definitivo: todos os itens; aceito até o entregue; recusa com motivo.
        String def = "{\"itens\": [%s]}";
        String outros = ", {\"itemRecebimentoId\": \"" + rDip + "\", \"quantidadeAceita\": 50}, {\"itemRecebimentoId\": \"" + rMan + "\", \"quantidadeAceita\": 1}";
        enviar(post(API + "recebimento/" + recebimento + "/definitivo"), def.formatted("{\"itemRecebimentoId\": \"" + rLuva + "\", \"quantidadeAceita\": 30}"), 400);
        enviar(post(API + "recebimento/" + recebimento + "/definitivo"), def.formatted("{\"itemRecebimentoId\": \"" + rLuva + "\", \"quantidadeAceita\": 31}" + outros), 400);
        enviar(post(API + "recebimento/" + recebimento + "/definitivo"), def.formatted("{\"itemRecebimentoId\": \"" + rLuva + "\", \"quantidadeAceita\": 25}" + outros), 400);
        o = enviar(post(API + "recebimento/" + recebimento + "/definitivo"), def.formatted("{\"itemRecebimentoId\": \"" + rLuva + "\", \"quantidadeAceita\": 25, "
                + "\"motivoRecusa\": \"Caixas avariadas\"}" + outros), 200);
        enviar(post(API + "recebimento/" + recebimento + "/definitivo"), def.formatted("{\"itemRecebimentoId\": \"" + rLuva + "\", \"quantidadeAceita\": 25}" + outros), 422);
        assertThat((String) JsonPath.read(o, "$.situacao")).isEqualTo("PARCIAL");
        assertThat((Integer) JsonPath.read(o, "$.itens[0].aceita")).isEqualTo(25);
        assertThat((Integer) JsonPath.read(o, "$.itens[0].pendente")).isEqualTo(35);
        assertThat((Integer) ((List<?>) JsonPath.read(o, "$.recebimentos[0].itens[?(@.uuid == '" + rLuva + "')].recusada")).get(0)).isEqualTo(5);
        assertThat((List<String>) JsonPath.read(o, "$.ocorrencias[*].tipo")).containsExactly("FORA_ESPECIFICACAO");

        // A entrada: o Estoque com o fornecedor e o custo; a Farmácia com o lote.
        Map<String, Object> mov = jdbc.queryForMap("select m.quantidade, m.documento, m.origem, l.custo_unitario, l.numero_lote from tb_movimentacao_estoque m "
                + "join tb_lote_material l on l.uuid = m.lote_id where m.fornecedor_id = cast(? as uuid)", forn);
        assertThat(((Number) mov.get("quantidade")).intValue()).isEqualTo(25);
        assertThat(mov.get("documento")).isEqualTo("NF 9988");
        assertThat(mov.get("origem")).isEqualTo("NOTA_FISCAL");
        assertThat(mov.get("numero_lote")).isEqualTo("L1");
        assertThat(((Number) mov.get("custo_unitario")).doubleValue()).isEqualTo(2.0);
        assertThat(jdbc.queryForObject("select quantidade from tb_lote where medicamento_id = cast(? as uuid) and unidade_id = ?", Integer.class, k[5], ubs.getUuid()))
                .isEqualTo(50);

        // O cancelamento devolve ao saldo o que não foi aceito.
        o = enviar(post(API + "ordem/" + ordemId + "/cancelamento"), "{\"motivo\": \"Fornecedor sem estoque.\"}", 200);
        assertThat((String) JsonPath.read(o, "$.situacao")).isEqualTo("CANCELADA");
        assertThat((Integer) JsonPath.read(ler(API + "instrumento/" + ct), "$.itens[0].saldo")).isEqualTo(75);
        enviar(post(API + "ordem/" + ordemId + "/recebimento"), rec.formatted(hoje, luvaOk), 422);
    }

    @Test
    void atrasoOcorrenciasEPainel() throws Exception {
        String[] k = montar(mockMvc, jdbc, PREFIXO, ubs, hoje, "905100000002", "ENT-GESTOR", "ENT-FISCAL");
        String o = enviar(post(API + "instrumento/" + k[0] + "/ordem"), "{\"unidadeId\": \"" + ubs.getUuid() + "\", \"prazoDias\": 10, \"itens\": [{\"itemInstrumentoId\": \""
                + k[4] + "\", \"quantidade\": 1}]}", 201);
        String ordemId = JsonPath.read(o, "$.uuid");
        jdbc.update("update tb_ordem_fornecimento set prazo_em = ? where uuid = cast(? as uuid)", hoje.minusDays(15), ordemId);

        // O atraso nasce uma vez.
        ler(API + "ocorrencias");
        String oc = ler(API + "ocorrencias?situacao=ABERTA");
        List<String> atrasos = JsonPath.read(oc, "$[?(@.ordemId == '" + ordemId + "' && @.tipo == 'ATRASO')].uuid");
        assertThat(atrasos).hasSize(1);
        o = ler(API + "ordem/" + ordemId);
        assertThat((Boolean) JsonPath.read(o, "$.atrasada")).isTrue();
        assertThat((Integer) JsonPath.read(o, "$.diasDeAtraso")).isEqualTo(15);

        // Ocorrências à mão; encerrar e sancionar.
        enviar(post(API + "ordem/" + ordemId + "/ocorrencia"), "{\"tipo\": \"ATRASO\", \"descricao\": \"x\"}", 400);
        o = enviar(post(API + "ordem/" + ordemId + "/ocorrencia"), "{\"tipo\": \"OUTRA\", \"descricao\": \"Técnico sem identificação.\"}", 201);
        String outra = ((List<?>) JsonPath.read(o, "$.ocorrencias[?(@.tipo == 'OUTRA')].uuid")).get(0).toString();
        String r = enviar(post(API + "ocorrencia/" + outra + "/encerramento"), "{\"motivo\": \"Fornecedor enviou a identificação.\"}", 200);
        assertThat((String) JsonPath.read(r, "$.situacao")).isEqualTo("ENCERRADA");
        enviar(post(API + "ocorrencia/" + outra + "/encerramento"), "{\"motivo\": \"x\"}", 422);
        r = enviar(post(API + "ocorrencia/" + atrasos.get(0) + "/sancao"), "{\"tipo\": \"ADVERTENCIA\", \"inicio\": \"" + hoje + "\", \"motivo\": \"Atraso na entrega.\"}", 200);
        assertThat((String) JsonPath.read(r, "$.situacao")).isEqualTo("SANCIONADA");
        assertThat((String) JsonPath.read(r, "$.sancaoId")).isNotNull();
        assertThat((List<String>) JsonPath.read(ler(API + "fornecedor/" + k[1]), "$.sancoes[*].tipo")).contains("ADVERTENCIA");

        // Painel.
        String p = ler(API + "painel");
        assertThat((List<String>) JsonPath.read(p, "$.atrasadas[*].ordemId")).contains(ordemId);
        assertThat((Integer) JsonPath.read(p, "$.solicitacoesPorSituacao.COTADA")).isNotNull();
        List<Map<String, Object>> f = JsonPath.read(p, "$.fornecedores[?(@.fornecedorId == '" + k[1] + "')]");
        assertThat(f).hasSize(1);
        assertThat(((Number) f.get(0).get("ordens")).intValue()).isEqualTo(1);
        assertThat(((Number) f.get(0).get("ocorrencias")).intValue()).isEqualTo(2);
        enviar(get(API + "painel?inicio=" + hoje + "&fim=" + hoje.minusDays(1)), "", 400);
    }
}
