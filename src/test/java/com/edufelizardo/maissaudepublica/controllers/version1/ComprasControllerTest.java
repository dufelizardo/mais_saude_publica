package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Setor;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.SetorRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

/**
 * Compras, fatia CP1 (ADR-0212): o fornecedor com o documento conferido, as certidões e as sanções do art. 156; a solicitação de compra do
 * rascunho à pesquisa concluída, com a devolução e a mediana; e a sugestão pelo estoque mínimo.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ComprasControllerTest {

    static final String PREFIXO = "Compras Teste ";
    private static final String API = "/api/v1/compras/";

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private UnidadeDeSaudeRepository unidadeRepository;
    @Autowired private SetorRepository setorRepository;

    private final LocalDate hoje = LocalDate.now(ZoneId.of("America/Sao_Paulo"));
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
        limparDados(jdbc, PREFIXO);
    }

    static void limparDados(JdbcTemplate jdbc, String prefixo) {
        String unidades = "(select uuid from tb_unidade_de_saude where nome like '" + prefixo + "%')";
        String solicitacoes = "(select uuid from tb_solicitacao_compra where unidade_id in " + unidades + ")";
        String itens = "(select uuid from tb_item_solicitacao_compra where solicitacao_id in " + solicitacoes + ")";
        String fornecedores = "(select uuid from tb_fornecedor where razao_social like '" + prefixo + "%')";
        String materiais = "(select uuid from tb_material_estoque where nome like '" + prefixo + "%')";
        // CP2 (ADR-0213): os instrumentos e as contratações dos fornecedores ou das solicitações do teste.
        String contratacoes = "(select uuid from tb_contratacao where numero_processo like '" + prefixo + "%' or uuid in (select contratacao_id from "
                + "tb_solicitacao_compra where unidade_id in " + unidades + "))";
        String instrumentos = "(select uuid from tb_instrumento_contratual where contratacao_id in " + contratacoes + " or fornecedor_id in " + fornecedores + ")";
        // CP3 (ADR-0214): as ordens dos instrumentos, os recebimentos e as ocorrências; a entrada na Farmácia das unidades do teste.
        String ordens = "(select uuid from tb_ordem_fornecimento where instrumento_id in " + instrumentos + ")";
        // PM1 (ADR-0215): os bens das unidades do teste ou dos fornecedores do teste, com os responsáveis e as transferências.
        String bens = "(select uuid from tb_bem_patrimonial where unidade_id in " + unidades + " or fornecedor_id in " + fornecedores + ")";
        jdbc.update("delete from tb_responsavel_bem where bem_id in " + bens);
        jdbc.update("delete from tb_transferencia_bem where bem_id in " + bens + " or origem_id in " + unidades + " or destino_id in " + unidades);
        jdbc.update("delete from tb_bem_patrimonial where uuid in " + bens);
        jdbc.update("delete from tb_ocorrencia_fornecedor where ordem_id in " + ordens + " or fornecedor_id in " + fornecedores);
        jdbc.update("delete from tb_item_recebimento_compra where recebimento_id in (select uuid from tb_recebimento_compra where ordem_id in " + ordens + ")");
        jdbc.update("delete from tb_recebimento_compra where ordem_id in " + ordens);
        jdbc.update("delete from tb_item_ordem_fornecimento where ordem_id in " + ordens);
        jdbc.update("delete from tb_ordem_fornecimento where uuid in " + ordens);
        jdbc.update("delete from tb_movimentacao_farmacia where lote_id in (select uuid from tb_lote where unidade_id in " + unidades + ")");
        jdbc.update("delete from tb_lote where unidade_id in " + unidades);
        jdbc.update("update tb_movimentacao_estoque set fornecedor_id = null where fornecedor_id in " + fornecedores);
        jdbc.update("delete from tb_aditivo_contratual where instrumento_id in " + instrumentos);
        jdbc.update("delete from tb_item_instrumento where instrumento_id in " + instrumentos);
        jdbc.update("delete from tb_instrumento_contratual where uuid in " + instrumentos);
        jdbc.update("update tb_item_solicitacao_compra set item_contratacao_id = null where item_contratacao_id in (select uuid from tb_item_contratacao "
                + "where contratacao_id in " + contratacoes + ")");
        jdbc.update("update tb_solicitacao_compra set contratacao_id = null where contratacao_id in " + contratacoes);
        jdbc.update("delete from tb_item_contratacao where contratacao_id in (select uuid from tb_contratacao where numero_processo like '" + prefixo + "%')");
        jdbc.update("delete from tb_contratacao where numero_processo like '" + prefixo + "%'");
        jdbc.update("delete from tb_cotacao_compra where item_id in " + itens + " or fornecedor_id in " + fornecedores);
        jdbc.update("delete from tb_item_solicitacao_compra where solicitacao_id in " + solicitacoes);
        jdbc.update("delete from tb_solicitacao_compra where unidade_id in " + unidades);
        jdbc.update("delete from tb_certidao_fornecedor where fornecedor_id in " + fornecedores);
        jdbc.update("delete from tb_sancao_fornecedor where fornecedor_id in " + fornecedores);
        jdbc.update("delete from tb_fornecedor where razao_social like '" + prefixo + "%'");
        jdbc.update("delete from tb_movimentacao_estoque where lote_id in (select uuid from tb_lote_material where material_id in " + materiais + ")");
        jdbc.update("delete from tb_lote_material where material_id in " + materiais);
        jdbc.update("delete from tb_estoque_minimo where material_id in " + materiais);
        jdbc.update("delete from tb_material_estoque where nome like '" + prefixo + "%'");
        jdbc.update("delete from tb_medicamento where nome like '" + prefixo + "%'");
        jdbc.update("delete from tb_setor where unidade_id in " + unidades);
        jdbc.update("delete from tb_unidade_de_saude where nome like '" + prefixo + "%'");
    }

    /** Um CNPJ válido a partir dos 12 primeiros dígitos. */
    static String cnpj(String base) {
        String d = base;
        int[] pesos = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        for (int n = 12; n <= 13; n++) {
            int soma = 0;
            for (int i = 0; i < n; i++) {
                soma += (d.charAt(i) - '0') * pesos[i + 13 - n];
            }
            int resto = soma % 11;
            d += resto < 2 ? 0 : 11 - resto;
        }
        return d;
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
        String texto = resposta.getContentAsString(StandardCharsets.UTF_8);
        assertThat(resposta.getStatus()).as(texto).isEqualTo(esperado);
        return texto;
    }

    private String ler(String url) throws Exception {
        var resposta = mockMvc.perform(get(url)).andReturn().getResponse();
        assertThat(resposta.getStatus()).isEqualTo(200);
        return resposta.getContentAsString(StandardCharsets.UTF_8);
    }

    private String fornecedor(String documento, String nome) throws Exception {
        return JsonPath.read(enviar(post(API + "fornecedor"), "{\"documento\": \"" + documento + "\", \"razaoSocial\": \"" + PREFIXO + nome + "\"}", 201), "$.uuid");
    }

    private String material(String codigo, String nome) throws Exception {
        return JsonPath.read(enviar(post("/api/v1/estoque/material"), ("{\"codigo\": \"%s\", \"nome\": \"%s%s\", \"categoria\": \"MEDICO_HOSPITALAR\", "
                + "\"unidadeMedida\": \"CAIXA\", \"controlaLote\": false}").formatted(codigo, PREFIXO, nome), 201), "$.uuid");
    }

    private String cotacao(String fonte, String fornecedor, String referencia, String preco, LocalDate data) {
        return "{\"fonte\": \"" + fonte + "\"" + (fornecedor != null ? ", \"fornecedorId\": \"" + fornecedor + "\"" : "")
                + (referencia != null ? ", \"referencia\": \"" + referencia + "\"" : "") + ", \"precoUnitario\": " + preco + ", \"cotadoEm\": \"" + data + "\"}";
    }

    @Test
    void fornecedorCertidoesESancoes() throws Exception {
        enviar(post(API + "fornecedor"), "{\"documento\": \"11.222.333/0001-00\", \"razaoSocial\": \"" + PREFIXO + "Inválido\"}", 400);
        String doc = cnpj("902100000001");
        String id = fornecedor(doc.substring(0, 2) + "." + doc.substring(2, 5) + "." + doc.substring(5, 8) + "/" + doc.substring(8, 12) + "-" + doc.substring(12),
                "Distribuidora");
        enviar(post(API + "fornecedor"), "{\"documento\": \"" + doc + "\", \"razaoSocial\": \"" + PREFIXO + "Outra\"}", 409);
        String f = ler(API + "fornecedor/" + id);
        assertThat((String) JsonPath.read(f, "$.documento")).isEqualTo(doc);
        assertThat((List<String>) JsonPath.read(f, "$.certidoesPendentes")).containsExactly("FEDERAL", "FGTS", "TRABALHISTA", "ESTADUAL", "MUNICIPAL");
        assertThat((Boolean) JsonPath.read(f, "$.regular")).isFalse();

        // Certidões: a validade antes da emissão é 400; com as cinco válidas, regular.
        enviar(post(API + "fornecedor/" + id + "/certidao"), "{\"tipo\": \"FGTS\", \"emitidaEm\": \"" + hoje + "\", \"validaAte\": \"" + hoje.minusDays(1) + "\"}", 400);
        enviar(post(API + "fornecedor/" + id + "/certidao"), "{\"tipo\": \"FGTS\", \"emitidaEm\": \"" + hoje.minusDays(60) + "\", \"validaAte\": \""
                + hoje.minusDays(1) + "\"}", 201);
        for (String tipo : List.of("FEDERAL", "FGTS", "TRABALHISTA", "ESTADUAL")) {
            enviar(post(API + "fornecedor/" + id + "/certidao"), "{\"tipo\": \"" + tipo + "\", \"emitidaEm\": \"" + hoje + "\", \"validaAte\": \"" + hoje.plusDays(90) + "\"}", 201);
        }
        f = ler(API + "fornecedor/" + id);
        assertThat((List<String>) JsonPath.read(f, "$.certidoesPendentes")).containsExactly("MUNICIPAL");
        f = enviar(post(API + "fornecedor/" + id + "/certidao"), "{\"tipo\": \"MUNICIPAL\", \"emitidaEm\": \"" + hoje + "\", \"validaAte\": \"" + hoje.plusDays(30) + "\"}", 201);
        assertThat((Boolean) JsonPath.read(f, "$.regular")).isTrue();
        assertThat((List<?>) JsonPath.read(f, "$.certidoes")).hasSize(6);

        // Sanções do art. 156.
        enviar(post(API + "fornecedor/" + id + "/sancao"), "{\"tipo\": \"MULTA\", \"inicio\": \"" + hoje + "\", \"motivo\": \"Atraso.\"}", 400);
        enviar(post(API + "fornecedor/" + id + "/sancao"), "{\"tipo\": \"IMPEDIMENTO\", \"inicio\": \"" + hoje + "\", \"fim\": \"" + hoje.plusYears(4)
                + "\", \"motivo\": \"Inexecução.\"}", 400);
        enviar(post(API + "fornecedor/" + id + "/sancao"), "{\"tipo\": \"INIDONEIDADE\", \"inicio\": \"" + hoje + "\", \"fim\": \"" + hoje.plusYears(2)
                + "\", \"motivo\": \"Fraude.\"}", 400);
        enviar(post(API + "fornecedor/" + id + "/sancao"), "{\"tipo\": \"MULTA\", \"inicio\": \"" + hoje + "\", \"valor\": 1500.00, \"motivo\": \"Atraso.\"}", 201);
        f = enviar(post(API + "fornecedor/" + id + "/sancao"), "{\"tipo\": \"IMPEDIMENTO\", \"inicio\": \"" + hoje + "\", \"fim\": \"" + hoje.plusYears(1)
                + "\", \"motivo\": \"Inexecução parcial.\", \"processo\": \"123/2026\"}", 201);
        assertThat((Boolean) JsonPath.read(f, "$.impedido")).isTrue();
        assertThat((Boolean) JsonPath.read(f, "$.regular")).isFalse();
        String sancao = ((List<String>) JsonPath.read(f, "$.sancoes[?(@.tipo == 'IMPEDIMENTO')].uuid")).get(0);
        f = enviar(post(API + "sancao/" + sancao + "/revogacao"), "{\"motivo\": \"Reabilitação (art. 163).\"}", 200);
        assertThat((Boolean) JsonPath.read(f, "$.impedido")).isFalse();
        enviar(post(API + "sancao/" + sancao + "/revogacao"), "{\"motivo\": \"De novo.\"}", 422);

        // O documento não muda; a pessoa física não tem FGTS.
        enviar(put(API + "fornecedor/" + id), "{\"documento\": \"" + cnpj("902100000002") + "\", \"razaoSocial\": \"" + PREFIXO + "X\"}", 422);
        f = enviar(put(API + "fornecedor/" + id), "{\"documento\": \"" + doc + "\", \"razaoSocial\": \"" + PREFIXO + "Distribuidora Ltda\", \"ativo\": false}", 200);
        assertThat((Boolean) JsonPath.read(f, "$.ativo")).isFalse();
        String pf = fornecedor("529.982.247-25", "Pessoa Física");
        enviar(post(API + "fornecedor/" + pf + "/certidao"), "{\"tipo\": \"FGTS\", \"emitidaEm\": \"" + hoje + "\", \"validaAte\": \"" + hoje.plusDays(30) + "\"}", 400);
        assertThat((List<String>) JsonPath.read(ler(API + "fornecedor/" + pf), "$.certidoesPendentes")).doesNotContain("FGTS");
        assertThat((List<String>) JsonPath.read(ler(API + "fornecedores?busca=pessoa"), "$[*].uuid")).containsExactly(pf);
    }

    @Test
    void solicitacaoDoRascunhoAPesquisa() throws Exception {
        String luva = material("CP-LUVA", "Luva nitrílica M");
        Setor setorOutra = new Setor();
        setorOutra.setNome(PREFIXO + "Setor");
        setorOutra.setUnidade(outra);
        setorOutra.setCodigo("CP-SETOR");
        setorOutra.setTipo(com.edufelizardo.maissaudepublica.models.enuns.TipoSetor.ASSISTENCIAL);
        setorOutra.setAtivo(true);
        setorOutra = setorRepository.save(setorOutra);

        String item = "{\"tipo\": \"MATERIAL\", \"materialId\": \"" + luva + "\", \"quantidade\": 100}";
        String servico = "{\"tipo\": \"SERVICO\", \"descricao\": \"Manutenção do autoclave\", \"unidadeMedida\": \"servico\", \"quantidade\": 1}";
        String base = "{\"unidadeId\": \"" + ubs.getUuid() + "\", \"justificativa\": \"Reposição do trimestre.\", \"itens\": [";
        enviar(post(API + "solicitacao"), base + item + ", " + item + "]}", 400);
        enviar(post(API + "solicitacao"), base + "{\"tipo\": \"SERVICO\", \"descricao\": \"Sem medida\", \"quantidade\": 1}]}", 400);
        enviar(post(API + "solicitacao"), "{\"unidadeId\": \"" + ubs.getUuid() + "\", \"setorId\": \"" + setorOutra.getUuid()
                + "\", \"justificativa\": \"x\", \"itens\": [" + item + "]}", 422);
        String s = enviar(post(API + "solicitacao"), base + item + "]}", 201);
        String id = JsonPath.read(s, "$.uuid");
        assertThat((String) JsonPath.read(s, "$.situacao")).isEqualTo("RASCUNHO");
        assertThat((String) JsonPath.read(s, "$.numero")).matches(hoje.getYear() + "/\\d{6}");
        assertThat((String) JsonPath.read(s, "$.itens[0].unidadeMedida")).isEqualTo("CAIXA");

        // Envia, não edita mais; a análise devolve com o motivo e a unidade corrige.
        enviar(post(API + "solicitacao/" + id + "/envio"), "", 200);
        enviar(put(API + "solicitacao/" + id), base + item + "]}", 422);
        enviar(post(API + "solicitacao/" + id + "/analise"), "{\"decisao\": \"DEVOLVER\"}", 400);
        s = enviar(post(API + "solicitacao/" + id + "/analise"), "{\"decisao\": \"DEVOLVER\", \"motivo\": \"Inclua a manutenção.\"}", 200);
        assertThat((String) JsonPath.read(s, "$.situacao")).isEqualTo("DEVOLVIDA");
        s = enviar(put(API + "solicitacao/" + id), base + item + ", " + servico + "]}", 200);
        assertThat((List<?>) JsonPath.read(s, "$.itens")).hasSize(2);
        assertThat((String) JsonPath.read(s, "$.itens[1].unidadeMedida")).isEqualTo("SERVICO");
        enviar(post(API + "solicitacao/" + id + "/analise"), "{\"decisao\": \"APROVAR\"}", 422);
        enviar(post(API + "solicitacao/" + id + "/envio"), "", 200);
        s = enviar(post(API + "solicitacao/" + id + "/analise"), "{\"decisao\": \"APROVAR\"}", 200);
        assertThat((String) JsonPath.read(s, "$.situacao")).isEqualTo("APROVADA");
        String itemLuva = JsonPath.read(s, "$.itens[0].uuid");
        String itemServico = JsonPath.read(s, "$.itens[1].uuid");

        // Cotações: as regras da fonte, da data e do fornecedor impedido.
        String forn = fornecedor(cnpj("902100000003"), "Fornecedor A");
        String impedido = fornecedor(cnpj("902100000004"), "Fornecedor Impedido");
        enviar(post(API + "fornecedor/" + impedido + "/sancao"), "{\"tipo\": \"IMPEDIMENTO\", \"inicio\": \"" + hoje + "\", \"fim\": \"" + hoje.plusYears(1)
                + "\", \"motivo\": \"Inexecução.\"}", 201);
        enviar(post(API + "item/" + itemLuva + "/cotacao"), cotacao("FORNECEDOR", null, null, "10", hoje), 400);
        enviar(post(API + "item/" + itemLuva + "/cotacao"), cotacao("SITE", null, null, "10", hoje), 400);
        enviar(post(API + "item/" + itemLuva + "/cotacao"), cotacao("SITE", null, "loja.exemplo", "10", hoje.minusYears(1).minusDays(1)), 400);
        enviar(post(API + "item/" + itemLuva + "/cotacao"), cotacao("FORNECEDOR", impedido, null, "10", hoje), 422);
        enviar(post(API + "item/" + itemLuva + "/cotacao"), cotacao("FORNECEDOR", forn, null, "20", hoje), 201);
        enviar(post(API + "item/" + itemLuva + "/cotacao"), cotacao("CONTRATACAO_PUBLICA", null, "Pregão 12/2026 de Município X", "10.5", hoje.minusMonths(3)), 201);
        enviar(post(API + "item/" + itemLuva + "/cotacao"), cotacao("SITE", null, "loja.exemplo", "12", hoje), 201);
        String errada = JsonPath.read(enviar(post(API + "item/" + itemLuva + "/cotacao"), cotacao("OUTRA", null, "Lançada errada", "999", hoje), 201),
                "$.itens[0].cotacoes[3].uuid");
        s = enviar(delete(API + "cotacao/" + errada), "", 200);
        assertThat((String) JsonPath.read(s, "$.situacao")).isEqualTo("EM_COTACAO");
        assertThat(JsonPath.read(s, "$.itens[0].mediana").toString()).isEqualTo("12.0");
        enviar(post(API + "item/" + itemServico + "/cotacao"), cotacao("FORNECEDOR", forn, null, "800", hoje), 201);
        enviar(post(API + "item/" + itemServico + "/cotacao"), cotacao("SITE", null, "assistencia.exemplo", "900", hoje), 201);

        // Conclui: o item com 2 cotações pede a justificativa; a mediana vira o preço de referência.
        enviar(post(API + "solicitacao/" + id + "/pesquisa"), "{}", 422);
        s = enviar(post(API + "solicitacao/" + id + "/pesquisa"), "{\"justificativas\": [{\"itemId\": \"" + itemServico
                + "\", \"justificativa\": \"Só duas assistências autorizadas na região.\"}]}", 200);
        assertThat((String) JsonPath.read(s, "$.situacao")).isEqualTo("COTADA");
        assertThat(JsonPath.read(s, "$.itens[0].precoReferencia").toString()).isEqualTo("12.0");
        assertThat(JsonPath.read(s, "$.itens[1].precoReferencia").toString()).isEqualTo("850.0");
        assertThat(JsonPath.read(s, "$.itens[0].justificativaCotacao") == null).isTrue();
        assertThat(JsonPath.read(s, "$.valorEstimado").toString()).isEqualTo("2050.0");
        enviar(post(API + "item/" + itemLuva + "/cotacao"), cotacao("SITE", null, "loja.exemplo", "11", hoje), 422);
        enviar(post(API + "solicitacao/" + id + "/cancelamento"), "{\"motivo\": \"x\"}", 422);

        // Listas.
        assertThat((List<String>) JsonPath.read(ler(API + "solicitacoes?unidadeId=" + ubs.getUuid() + "&situacao=COTADA"), "$[*].uuid")).containsExactly(id);
        assertThat((List<String>) JsonPath.read(ler(API + "solicitacoes?unidadeId=" + outra.getUuid()), "$[*].uuid")).isEmpty();
    }

    @Test
    void cancelamentoERejeicao() throws Exception {
        String luva = material("CP-AVENTAL", "Avental descartável");
        String corpo = "{\"unidadeId\": \"" + ubs.getUuid() + "\", \"justificativa\": \"x\", \"prioridade\": \"URGENTE\", \"itens\": [{\"tipo\": \"MATERIAL\", "
                + "\"materialId\": \"" + luva + "\", \"quantidade\": 5}]}";
        String a = JsonPath.read(enviar(post(API + "solicitacao"), corpo, 201), "$.uuid");
        String s = enviar(post(API + "solicitacao/" + a + "/cancelamento"), "{\"motivo\": \"Pedido em duplicidade.\"}", 200);
        assertThat((String) JsonPath.read(s, "$.situacao")).isEqualTo("CANCELADA");
        String b = JsonPath.read(enviar(post(API + "solicitacao"), corpo, 201), "$.uuid");
        enviar(post(API + "solicitacao/" + b + "/envio"), "", 200);
        s = enviar(post(API + "solicitacao/" + b + "/analise"), "{\"decisao\": \"REJEITAR\", \"motivo\": \"Há ata vigente para o item.\"}", 200);
        assertThat((String) JsonPath.read(s, "$.situacao")).isEqualTo("REJEITADA");
        assertThat((String) JsonPath.read(s, "$.prioridade")).isEqualTo("URGENTE");
        enviar(post(API + "solicitacao/" + b + "/envio"), "", 422);
        enviar(post(API + "solicitacao/" + b + "/pesquisa"), "{}", 422);
    }

    @Test
    void sugestaoPeloEstoqueMinimo() throws Exception {
        String gaze = material("CP-GAZE", "Compressa de gaze");
        String esparadrapo = material("CP-ESPARADRAPO", "Esparadrapo");
        enviar(put("/api/v1/estoque/minimo"), "{\"unidadeId\": \"" + ubs.getUuid() + "\", \"materialId\": \"" + gaze + "\", \"quantidadeMinima\": 10}", 200);
        enviar(put("/api/v1/estoque/minimo"), "{\"unidadeId\": \"" + ubs.getUuid() + "\", \"materialId\": \"" + esparadrapo + "\", \"quantidadeMinima\": 2}", 200);
        enviar(post("/api/v1/estoque/entrada"), "{\"unidadeId\": \"" + ubs.getUuid() + "\", \"materialId\": \"" + gaze + "\", \"quantidade\": 4, \"origem\": \"DOACAO\"}", 201);
        enviar(post("/api/v1/estoque/entrada"), "{\"unidadeId\": \"" + ubs.getUuid() + "\", \"materialId\": \"" + esparadrapo + "\", \"quantidade\": 5, \"origem\": \"DOACAO\"}", 201);
        String sug = ler(API + "sugestao?unidadeId=" + ubs.getUuid());
        assertThat((List<String>) JsonPath.read(sug, "$[*].materialId")).containsExactly(gaze);
        assertThat((Integer) JsonPath.read(sug, "$[0].saldo")).isEqualTo(4);
        assertThat((Integer) JsonPath.read(sug, "$[0].quantidadeSugerida")).isEqualTo(16);
        assertThat((List<?>) JsonPath.read(ler(API + "sugestao?unidadeId=" + outra.getUuid()), "$")).isEmpty();
        enviar(post(API + "solicitacao"), "{\"unidadeId\": \"" + ubs.getUuid() + "\", \"justificativa\": \"Abaixo do mínimo.\", \"itens\": [{\"tipo\": \"MATERIAL\", "
                + "\"materialId\": \"" + gaze + "\", \"quantidade\": 16}]}", 201);
        assertThat((List<?>) JsonPath.read(ler(API + "sugestao?unidadeId=" + ubs.getUuid()), "$")).isEmpty();
    }
}
