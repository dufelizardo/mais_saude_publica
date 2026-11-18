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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Compras, fatia CP2 (ADR-0213): a contratação que soma os itens iguais das solicitações cotadas, o resultado com o fornecedor regular e a
 * justificativa acima da referência, a homologação com os limites de vigência e a segregação gestor/fiscal, o cancelamento que libera as
 * solicitações e os aditivos (ata prorroga uma vez; contrato com 25% de acréscimo e de supressão; reequilíbrio).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ContratosControllerTest {

    static final String PREFIXO = "Contratos Teste ";
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
        profissional("CTT-GESTOR", "90088900001");
        profissional("CTT-FISCAL", "90088900002");
    }

    @AfterEach
    void limpar() {
        ComprasControllerTest.limparDados(jdbc, PREFIXO);
        jdbc.update("delete from tb_profissional where matricula like 'CTT-%'");
    }

    private void profissional(String matricula, String cpf) {
        Profissional p = new Profissional();
        p.setMatricula(matricula);
        p.setCpf(cpf);
        p.setNome(PREFIXO + matricula);
        p.setAtivo(true);
        profissionalRepository.save(p);
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

    private String material(String codigo) throws Exception {
        return JsonPath.read(enviar(post("/api/v1/estoque/material"), ("{\"codigo\": \"%s\", \"nome\": \"%s%s\", \"categoria\": \"EPI\", "
                + "\"unidadeMedida\": \"CAIXA\", \"controlaLote\": false}").formatted(codigo, PREFIXO, codigo), 201), "$.uuid");
    }

    /** Fornecedor com as cinco certidões válidas. */
    private String fornecedorRegular(String base, String nome) throws Exception {
        String id = fornecedorSemCertidao(base, nome);
        for (String tipo : List.of("FEDERAL", "FGTS", "TRABALHISTA", "ESTADUAL", "MUNICIPAL")) {
            enviar(post(API + "fornecedor/" + id + "/certidao"), "{\"tipo\": \"" + tipo + "\", \"emitidaEm\": \"" + hoje + "\", \"validaAte\": \""
                    + hoje.plusDays(180) + "\"}", 201);
        }
        return id;
    }

    private String fornecedorSemCertidao(String base, String nome) throws Exception {
        return JsonPath.read(enviar(post(API + "fornecedor"), "{\"documento\": \"" + ComprasControllerTest.cnpj(base) + "\", \"razaoSocial\": \"" + PREFIXO + nome
                + "\"}", 201), "$.uuid");
    }

    /** Solicitação com um item, aprovada e cotada com três preços. */
    private String cotada(String item, String... precos) throws Exception {
        String id = JsonPath.read(enviar(post(API + "solicitacao"), "{\"unidadeId\": \"" + ubs.getUuid() + "\", \"justificativa\": \"Reposição.\", \"itens\": ["
                + item + "]}", 201), "$.uuid");
        enviar(post(API + "solicitacao/" + id + "/envio"), "", 200);
        String s = enviar(post(API + "solicitacao/" + id + "/analise"), "{\"decisao\": \"APROVAR\"}", 200);
        String itemId = JsonPath.read(s, "$.itens[0].uuid");
        for (String p : precos) {
            enviar(post(API + "item/" + itemId + "/cotacao"), "{\"fonte\": \"SITE\", \"referencia\": \"loja.exemplo\", \"precoUnitario\": " + p
                    + ", \"cotadoEm\": \"" + hoje + "\"}", 201);
        }
        enviar(post(API + "solicitacao/" + id + "/pesquisa"), "{}", 200);
        return id;
    }

    private String contratacao(String processo, String modalidade, String extra, String... solicitacoes) {
        return "{\"numeroProcesso\": \"" + PREFIXO + processo + "\", \"modalidade\": \"" + modalidade + "\", \"objeto\": \"Aquisição de insumos.\"" + extra
                + ", \"solicitacaoIds\": [\"" + String.join("\", \"", solicitacoes) + "\"]}";
    }

    private String instrumento(String fornecedor, String tipo, String numero, LocalDate fim, boolean continuo, String fiscal) {
        return "{\"fornecedorId\": \"" + fornecedor + "\", \"tipo\": \"" + tipo + "\", \"numero\": \"" + PREFIXO + numero + "\", \"inicio\": \"" + hoje
                + "\", \"fim\": \"" + fim + "\", \"servicoContinuo\": " + continuo + ", \"gestorMatricula\": \"CTT-GESTOR\", \"fiscalMatricula\": \"" + fiscal + "\"}";
    }

    @Test
    void contratacaoHomologacaoEAta() throws Exception {
        String luva = material("CTT-LUVA");
        String item = "{\"tipo\": \"MATERIAL\", \"materialId\": \"" + luva + "\", \"quantidade\": %d}";
        String s1 = cotada(item.formatted(100), "10", "12", "14");
        String s2 = cotada(item.formatted(50), "11", "13", "15");
        String rascunho = JsonPath.read(enviar(post(API + "solicitacao"), "{\"unidadeId\": \"" + ubs.getUuid() + "\", \"justificativa\": \"x\", \"itens\": ["
                + item.formatted(1) + "]}", 201), "$.uuid");

        enviar(post(API + "contratacao"), contratacao("D-1", "DISPENSA", "", s1), 400);
        enviar(post(API + "contratacao"), contratacao("P-0", "PREGAO_ELETRONICO", "", s1, rascunho), 422);
        String c = enviar(post(API + "contratacao"), contratacao("P-1", "PREGAO_ELETRONICO", "", s1, s2), 201);
        String id = JsonPath.read(c, "$.uuid");
        enviar(post(API + "contratacao"), contratacao("P-1", "PREGAO_ELETRONICO", "", s1), 409);
        enviar(post(API + "contratacao"), contratacao("P-2", "PREGAO_ELETRONICO", "", s1), 409);
        assertThat((List<?>) JsonPath.read(c, "$.itens")).hasSize(1);
        assertThat((Integer) JsonPath.read(c, "$.itens[0].quantidade")).isEqualTo(150);
        assertThat(JsonPath.read(c, "$.itens[0].precoReferencia").toString()).isEqualTo("12.3333");
        String itemId = JsonPath.read(c, "$.itens[0].uuid");

        // Resultado: fornecedor irregular, preço acima da referência sem justificativa.
        String irregular = fornecedorSemCertidao("903100000001", "Irregular");
        String regular = fornecedorRegular("903100000002", "Regular");
        enviar(post(API + "item-contratacao/" + itemId + "/resultado"), "{\"resultado\": \"ADJUDICADO\", \"fornecedorId\": \"" + irregular + "\", \"precoUnitario\": 12}", 422);
        enviar(post(API + "item-contratacao/" + itemId + "/resultado"), "{\"resultado\": \"ADJUDICADO\", \"fornecedorId\": \"" + regular + "\", \"precoUnitario\": 13}", 422);
        enviar(post(API + "item-contratacao/" + itemId + "/resultado"), "{\"resultado\": \"ADJUDICADO\"}", 400);
        c = enviar(post(API + "item-contratacao/" + itemId + "/resultado"), "{\"resultado\": \"ADJUDICADO\", \"fornecedorId\": \"" + regular
                + "\", \"precoUnitario\": 12}", 200);
        assertThat(JsonPath.read(c, "$.valorAdjudicado").toString()).isEqualTo("1800.0");

        // Homologação: um instrumento por vencedor; limites de vigência; gestor e fiscal diferentes.
        enviar(post(API + "contratacao/" + id + "/homologacao"), "{\"instrumentos\": []}", 400);
        enviar(post(API + "contratacao/" + id + "/homologacao"), "{\"instrumentos\": [" + instrumento(regular, "ATA_REGISTRO_PRECOS", "ARP-1",
                hoje.plusYears(1).plusDays(1), false, "CTT-FISCAL") + "]}", 400);
        enviar(post(API + "contratacao/" + id + "/homologacao"), "{\"instrumentos\": [" + instrumento(regular, "ATA_REGISTRO_PRECOS", "ARP-1",
                hoje.plusYears(1), false, "CTT-GESTOR") + "]}", 422);
        c = enviar(post(API + "contratacao/" + id + "/homologacao"), "{\"instrumentos\": [" + instrumento(regular, "ATA_REGISTRO_PRECOS", "ARP-1",
                hoje.plusMonths(2), false, "CTT-FISCAL") + "]}", 200);
        assertThat((String) JsonPath.read(c, "$.situacao")).isEqualTo("HOMOLOGADA");
        assertThat((String) JsonPath.read(ler(API + "solicitacao/" + s1), "$.situacao")).isEqualTo("CONTRATADA");
        enviar(post(API + "item-contratacao/" + itemId + "/resultado"), "{\"resultado\": \"DESERTO\"}", 422);
        String ata = JsonPath.read(c, "$.instrumentos[0].uuid");

        String a = ler(API + "instrumento/" + ata);
        assertThat(JsonPath.read(a, "$.valorInicial").toString()).isEqualTo("1800.0");
        assertThat((Integer) JsonPath.read(a, "$.itens[0].saldo")).isEqualTo(150);
        assertThat((List<String>) JsonPath.read(a, "$.alertas")).anyMatch(x -> x.startsWith("Vigência termina em"));

        // Ata: sem acréscimo; prorroga uma vez, até 2 anos do início.
        String itemAta = JsonPath.read(a, "$.itens[0].uuid");
        enviar(post(API + "instrumento/" + ata + "/aditivo"), "{\"tipo\": \"ACRESCIMO\", \"itemId\": \"" + itemAta + "\", \"quantidade\": 10, "
                + "\"justificativa\": \"Mais demanda.\", \"assinadoEm\": \"" + hoje + "\"}", 422);
        enviar(post(API + "instrumento/" + ata + "/aditivo"), "{\"tipo\": \"PRAZO\", \"novoFim\": \"" + hoje.plusYears(2).plusDays(1)
                + "\", \"justificativa\": \"Vantajosa.\", \"assinadoEm\": \"" + hoje + "\"}", 422);
        a = enviar(post(API + "instrumento/" + ata + "/aditivo"), "{\"tipo\": \"PRAZO\", \"novoFim\": \"" + hoje.plusYears(1)
                + "\", \"justificativa\": \"Preços vantajosos (art. 84).\", \"assinadoEm\": \"" + hoje + "\"}", 201);
        assertThat((String) JsonPath.read(a, "$.fim")).isEqualTo(hoje.plusYears(1).toString());
        assertThat((List<String>) JsonPath.read(a, "$.alertas")).isEmpty();
        enviar(post(API + "instrumento/" + ata + "/aditivo"), "{\"tipo\": \"PRAZO\", \"novoFim\": \"" + hoje.plusYears(1).plusMonths(6)
                + "\", \"justificativa\": \"De novo.\", \"assinadoEm\": \"" + hoje + "\"}", 422);
        assertThat((List<String>) JsonPath.read(ler(API + "instrumentos?tipo=ATA_REGISTRO_PRECOS"), "$[*].uuid")).contains(ata);
    }

    @Test
    void contratoContinuoEAditivos() throws Exception {
        String s = cotada("{\"tipo\": \"SERVICO\", \"descricao\": \"Limpeza hospitalar\", \"unidadeMedida\": \"mes\", \"quantidade\": 10}", "95", "100", "105");
        String c = enviar(post(API + "contratacao"), contratacao("PE-9", "PREGAO_ELETRONICO", "", s), 201);
        String id = JsonPath.read(c, "$.uuid");
        String forn = fornecedorRegular("903100000003", "Limpeza");
        enviar(post(API + "item-contratacao/" + JsonPath.read(c, "$.itens[0].uuid") + "/resultado"), "{\"resultado\": \"ADJUDICADO\", \"fornecedorId\": \""
                + forn + "\", \"precoUnitario\": 100}", 200);
        enviar(post(API + "contratacao/" + id + "/homologacao"), "{\"instrumentos\": [" + instrumento(forn, "CONTRATO", "CT-1", hoje.plusYears(5).plusDays(1),
                true, "CTT-FISCAL") + "]}", 400);
        enviar(post(API + "contratacao/" + id + "/homologacao"), "{\"instrumentos\": [" + instrumento(forn, "CONTRATO", "CT-1", hoje.plusYears(2),
                false, "CTT-FISCAL") + "]}", 400);
        c = enviar(post(API + "contratacao/" + id + "/homologacao"), "{\"instrumentos\": [" + instrumento(forn, "CONTRATO", "CT-1", hoje.plusYears(2),
                true, "CTT-FISCAL") + "]}", 200);
        String ct = JsonPath.read(c, "$.instrumentos[0].uuid");
        String det = ler(API + "instrumento/" + ct);
        String item = JsonPath.read(det, "$.itens[0].uuid");
        assertThat(JsonPath.read(det, "$.valorInicial").toString()).isEqualTo("1000.0");

        // Acréscimos até 25% do valor inicial; supressão até o saldo.
        String ad = "{\"tipo\": \"%s\", \"itemId\": \"" + item + "\", \"quantidade\": %d, \"justificativa\": \"Ajuste.\", \"assinadoEm\": \"" + hoje + "\"}";
        String r = enviar(post(API + "instrumento/" + ct + "/aditivo"), ad.formatted("ACRESCIMO", 2), 201);
        assertThat((Integer) JsonPath.read(r, "$.itens[0].quantidade")).isEqualTo(12);
        assertThat(JsonPath.read(r, "$.percentualAcrescido").toString()).isEqualTo("20.0");
        enviar(post(API + "instrumento/" + ct + "/aditivo"), ad.formatted("ACRESCIMO", 1), 422);
        enviar(post(API + "instrumento/" + ct + "/aditivo"), ad.formatted("SUPRESSAO", 13), 422);
        r = enviar(post(API + "instrumento/" + ct + "/aditivo"), ad.formatted("SUPRESSAO", 2), 201);
        assertThat((Integer) JsonPath.read(r, "$.itens[0].saldo")).isEqualTo(10);
        r = enviar(post(API + "instrumento/" + ct + "/aditivo"), "{\"tipo\": \"REEQUILIBRIO\", \"itemId\": \"" + item + "\", \"novoPreco\": 110, "
                + "\"justificativa\": \"Convenção coletiva.\", \"assinadoEm\": \"" + hoje + "\"}", 201);
        assertThat(JsonPath.read(r, "$.valorAtual").toString()).isEqualTo("1100.0");
        assertThat((List<?>) JsonPath.read(r, "$.aditivos")).hasSize(3);
        enviar(post(API + "instrumento/" + ct + "/aditivo"), "{\"tipo\": \"PRAZO\", \"novoFim\": \"" + hoje.plusYears(11)
                + "\", \"justificativa\": \"x\", \"assinadoEm\": \"" + hoje + "\"}", 422);
        enviar(post(API + "instrumento/" + ct + "/aditivo"), "{\"tipo\": \"PRAZO\", \"novoFim\": \"" + hoje.plusYears(3)
                + "\", \"justificativa\": \"x\", \"assinadoEm\": \"" + hoje.plusDays(1) + "\"}", 400);
    }

    @Test
    void cancelamentoLiberaAsSolicitacoes() throws Exception {
        String luva = material("CTT-AVENTAL");
        String s = cotada("{\"tipo\": \"MATERIAL\", \"materialId\": \"" + luva + "\", \"quantidade\": 5}", "1", "2", "3");
        String c = enviar(post(API + "contratacao"), contratacao("C-1", "DISPENSA", ", \"fundamentoLegal\": \"Art. 75, II\"", s), 201);
        String id = JsonPath.read(c, "$.uuid");
        enviar(post(API + "contratacao/" + id + "/homologacao"), "{\"instrumentos\": []}", 422);
        c = enviar(post(API + "contratacao/" + id + "/cancelamento"), "{\"motivo\": \"Recurso remanejado.\"}", 200);
        assertThat((String) JsonPath.read(c, "$.situacao")).isEqualTo("CANCELADA");
        enviar(post(API + "contratacao/" + id + "/cancelamento"), "{\"motivo\": \"x\"}", 422);
        c = enviar(post(API + "contratacao"), contratacao("C-2", "ADESAO_ATA", ", \"ataOrigem\": \"ARP 4/2026 do Consórcio\"", s), 201);
        assertThat((String) JsonPath.read(c, "$.ataOrigem")).isEqualTo("ARP 4/2026 do Consórcio");
        String itemId = JsonPath.read(c, "$.itens[0].uuid");
        c = enviar(post(API + "item-contratacao/" + itemId + "/resultado"), "{\"resultado\": \"FRACASSADO\"}", 200);
        c = enviar(post(API + "contratacao/" + JsonPath.read(c, "$.uuid") + "/homologacao"), "{\"instrumentos\": []}", 200);
        assertThat((List<?>) JsonPath.read(c, "$.instrumentos")).isEmpty();
        assertThat((List<String>) JsonPath.read(ler(API + "contratacoes?situacao=HOMOLOGADA"), "$[*].numeroProcesso")).contains(PREFIXO + "C-2");
    }
}
