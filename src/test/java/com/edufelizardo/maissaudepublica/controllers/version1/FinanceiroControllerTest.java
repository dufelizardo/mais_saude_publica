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
 * Financeiro, fatia F1 (ADR-0218): a dotação com as validações, a suplementação e a anulação; o empenho dentro do disponível, do fornecedor
 * e do contrato; a ordem de fornecimento que consome o saldo do empenho; o reforço, a anulação e o cancelamento da ordem que devolve o saldo.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FinanceiroControllerTest {

    static final String PREFIXO = "Financeiro Teste ";
    private static final String API = "/api/v1/financeiro/";
    private static final String COMPRAS = "/api/v1/compras/";

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
        for (String[] p : new String[][]{{"FIN-GESTOR", "90044510001"}, {"FIN-FISCAL", "90044510002"}}) {
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
        jdbc.update("delete from tb_profissional where matricula like 'FIN-%'");
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

    static String dotacao(int exercicio, String programa, String natureza, String valor, String prefixo) {
        return dotacao(exercicio, programa, natureza, valor, prefixo, "BLOCO_MANUTENCAO");
    }

    static String dotacao(int exercicio, String programa, String natureza, String valor, String prefixo, String fonte) {
        return ("{\"exercicio\": %d, \"programaTrabalho\": \"%s\", \"descricao\": \"%sAtenção básica\", \"naturezaDespesa\": \"%s\", "
                + "\"fonte\": \"%s\", \"fonteCodigo\": \"1.600\", \"valorInicial\": %s}").formatted(exercicio, programa, prefixo, natureza, fonte, valor);
    }

    private static double numero(String json, String caminho) {
        return ((Number) JsonPath.read(json, caminho)).doubleValue();
    }

    @Test
    void dotacaoComAlteracoes() throws Exception {
        int ano = hoje.getYear();
        enviar(post(API + "dotacao"), dotacao(ano + 2, "10.301.0010.2045", "3.3.90.30", "100000", PREFIXO), 400);
        enviar(post(API + "dotacao"), dotacao(ano, "10.301", "3.3.90.30", "100000", PREFIXO), 400);
        enviar(post(API + "dotacao"), dotacao(ano, "10.301.0010.2045", "33.90.30", "100000", PREFIXO), 400);
        String d = enviar(post(API + "dotacao"), dotacao(ano, "10.301.0010.2045", "3.3.90.30", "100000", PREFIXO), 201);
        String id = JsonPath.read(d, "$.uuid");
        enviar(post(API + "dotacao"), dotacao(ano, "10.301.0010.2045", "3.3.90.30", "5", PREFIXO), 409);

        String alt = "{\"tipo\": \"%s\", \"valor\": %s, \"ato\": \"Decreto 12/%d\", \"data\": \"%s\"}";
        assertThat(numero(enviar(post(API + "dotacao/" + id + "/alteracao"), alt.formatted("SUPLEMENTACAO", "20000", ano, hoje), 201), "$.autorizado")).isEqualTo(120000.0);
        enviar(post(API + "dotacao/" + id + "/alteracao"), alt.formatted("ANULACAO", "200000", ano, hoje), 422);
        enviar(post(API + "dotacao/" + id + "/alteracao"), alt.formatted("ANULACAO", "5000", ano, hoje.plusDays(1)), 400);
        String a = enviar(post(API + "dotacao/" + id + "/alteracao"), alt.formatted("ANULACAO", "5000", ano, hoje), 201);
        assertThat(numero(a, "$.autorizado")).isEqualTo(115000.0);
        assertThat(numero(a, "$.disponivel")).isEqualTo(115000.0);
        assertThat((List<?>) JsonPath.read(a, "$.alteracoes")).hasSize(2);
        assertThat((List<?>) JsonPath.read(ler(API + "dotacoes?exercicio=" + ano), "$[?(@.uuid == '" + id + "')]")).hasSize(1);
    }

    @Test
    void empenhoEOrdemDeFornecimento() throws Exception {
        int ano = hoje.getYear();
        String[] k = EntregasControllerTest.contrato(mockMvc, jdbc, PREFIXO, ubs, hoje, "909100000001", "FIN-GESTOR", "FIN-FISCAL", r -> r);
        String ct = k[0], forn = k[1], luva = k[2];
        String dot = JsonPath.read(enviar(post(API + "dotacao"), dotacao(ano, "10.301.0010.2046", "3.3.90.30", "1000", PREFIXO), 201), "$.uuid");
        String empenho = "{\"numero\": \"%s\", \"tipo\": \"GLOBAL\", \"dotacaoId\": \"%s\", \"fornecedorId\": \"%s\", \"instrumentoId\": %s, \"valor\": %s, \"data\": \"%s\"}";
        enviar(post(API + "empenho"), empenho.formatted("2026NE0001", dot, forn, "\"" + ct + "\"", "2000", hoje), 422);
        enviar(post(API + "empenho"), empenho.formatted("2026NE0001", dot, forn, "\"" + ct + "\"", "500", hoje.minusYears(1)), 400);
        String e = enviar(post(API + "empenho"), empenho.formatted("2026NE0001", dot, forn, "\"" + ct + "\"", "500", hoje), 201);
        String id = JsonPath.read(e, "$.uuid");
        enviar(post(API + "empenho"), empenho.formatted("2026NE0001", dot, forn, "null", "10", hoje), 409);
        String outro = JsonPath.read(enviar(post(COMPRAS + "fornecedor"), "{\"documento\": \"" + ComprasControllerTest.cnpj("909100000002") + "\", \"razaoSocial\": \""
                + PREFIXO + "Outra Distribuidora\"}", 201), "$.uuid");
        enviar(post(API + "empenho"), empenho.formatted("2026NE0002", dot, outro, "\"" + ct + "\"", "10", hoje), 422);
        assertThat(((Number) ((List<?>) JsonPath.read(ler(API + "dotacoes?exercicio=" + ano), "$[?(@.uuid == '" + dot + "')].disponivel")).get(0)).doubleValue()).isEqualTo(500.0);
        assertThat((List<?>) JsonPath.read(ler(API + "empenhos-disponiveis?instrumentoId=" + ct), "$[?(@.uuid == '" + id + "')]")).hasSize(1);

        // A ordem consome o saldo do empenho (luva a 2,00): 60 = 120.
        String ordem = "{\"unidadeId\": \"" + ubs.getUuid() + "\", \"empenhoId\": \"" + id + "\", \"itens\": [{\"itemInstrumentoId\": \"" + luva + "\", \"quantidade\": %d}]}";
        String o = enviar(post(COMPRAS + "instrumento/" + ct + "/ordem"), ordem.formatted(60), 201);
        assertThat((String) JsonPath.read(o, "$.empenho")).isEqualTo("2026NE0001");
        assertThat((String) JsonPath.read(o, "$.empenhoId")).isEqualTo(id);
        String ver = ler(API + "empenho/" + id);
        assertThat(numero(ver, "$.comprometido")).isEqualTo(120.0);
        assertThat(numero(ver, "$.saldo")).isEqualTo(380.0);

        // Anulação até o saldo; depois, a ordem acima do saldo dá 422.
        String mov = "{\"tipo\": \"%s\", \"valor\": %s, \"data\": \"%s\", \"motivo\": \"Ajuste.\"}";
        enviar(post(API + "empenho/" + id + "/movimento"), mov.formatted("ANULACAO", "381", hoje), 422);
        assertThat(numero(enviar(post(API + "empenho/" + id + "/movimento"), mov.formatted("ANULACAO", "350", hoje), 201), "$.saldo")).isEqualTo(30.0);
        enviar(post(COMPRAS + "instrumento/" + ct + "/ordem"), ordem.formatted(20), 422);
        enviar(post(API + "empenho/" + id + "/movimento"), mov.formatted("REFORCO", "900", hoje), 422);
        assertThat(numero(enviar(post(API + "empenho/" + id + "/movimento"), mov.formatted("REFORCO", "100", hoje), 201), "$.valorAtual")).isEqualTo(250.0);

        // O cancelamento da ordem devolve o saldo do empenho.
        enviar(post(COMPRAS + "ordem/" + JsonPath.read(o, "$.uuid") + "/cancelamento"), "{\"motivo\": \"Desistência.\"}", 200);
        assertThat(numero(ler(API + "empenho/" + id), "$.saldo")).isEqualTo(250.0);
    }
}
