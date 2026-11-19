package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Endereco;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.SolicitacaoTransporte;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.MotivoTransporte;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoSolicitacaoTransporte;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.TrechoTransporte;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.SolicitacaoTransporteRepository;
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
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Financeiro, fatia F2 (ADR-0219): o recebimento definitivo na fila a liquidar; a liquidação com o empenho da ordem, o teto do atesto, o
 * saldo do empenho e as retenções; o pagamento e a ordem cronológica; a liquidação avulsa; a diária do TFD com o credor pessoa física; a
 * execução do exercício.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LiquidacaoControllerTest {

    private static final String PREFIXO = "Liquidacao Teste ";
    private static final String API = "/api/v1/financeiro/";
    private static final String COMPRAS = "/api/v1/compras/";

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private UnidadeDeSaudeRepository unidadeRepository;
    @Autowired private ProfissionalRepository profissionalRepository;
    @Autowired private PacienteRepository pacienteRepository;
    @Autowired private SolicitacaoTransporteRepository solicitacaoRepository;

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
        for (String[] p : new String[][]{{"LIQ-GESTOR", "90033410001"}, {"LIQ-FISCAL", "90033410002"}}) {
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
        String diarias = "(select uuid from tb_diaria_tfd where solicitacao_id in (select uuid from tb_solicitacao_transporte where paciente_id in "
                + "(select uuid from tb_paciente where nome like '" + PREFIXO + "%')))";
        jdbc.update("delete from tb_liquidacao where diaria_id in " + diarias);
        jdbc.update("delete from tb_diaria_tfd where uuid in " + diarias);
        jdbc.update("delete from tb_solicitacao_transporte where paciente_id in (select uuid from tb_paciente where nome like ?)", PREFIXO + "%");
        jdbc.update("delete from tb_paciente where nome like ?", PREFIXO + "%");
        ComprasControllerTest.limparDados(jdbc, PREFIXO);
        jdbc.update("delete from tb_profissional where matricula like 'LIQ-%'");
        jdbc.update("delete from tb_fornecedor where razao_social like ? and uuid not in (select credor_id from tb_diaria_tfd)", PREFIXO + "%");
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

    private static double numero(String json, String caminho) {
        Object v = JsonPath.read(json, caminho);
        return v instanceof List<?> l ? ((Number) l.get(0)).doubleValue() : ((Number) v).doubleValue();
    }

    private String empenhar(String dotacao, String fornecedor, String instrumento, String numero, String valor) throws Exception {
        return JsonPath.read(enviar(post(API + "empenho"), ("{\"numero\": \"%s\", \"tipo\": \"GLOBAL\", \"dotacaoId\": \"%s\", \"fornecedorId\": \"%s\", "
                + "\"instrumentoId\": %s, \"valor\": %s, \"data\": \"%s\"}").formatted(numero, dotacao, fornecedor, instrumento == null ? "null" : "\"" + instrumento + "\"",
                valor, hoje), 201), "$.uuid");
    }

    /**
     * O contrato, a dotação, o empenho de 500 ligado à ordem de 60 luvas (120) e o recebimento definitivo de 50 (100). A fonte é própria do
     * teste, para a ordem cronológica não esbarrar em liquidações de outras execuções no mesmo banco.
     */
    private String[] recebimentoDefinitivo() throws Exception {
        String[] k = EntregasControllerTest.contrato(mockMvc, jdbc, PREFIXO, ubs, hoje, "910100000001", "LIQ-GESTOR", "LIQ-FISCAL", r -> r);
        String dot = JsonPath.read(enviar(post(API + "dotacao"), FinanceiroControllerTest.dotacao(hoje.getYear(), "10.301.0010.2047", "3.3.90.30", "10000", PREFIXO, "TRANSFERENCIA_ESTADUAL"), 201), "$.uuid");
        String emp = empenhar(dot, k[1], k[0], "LQ0001", "500");
        String o = enviar(post(COMPRAS + "instrumento/" + k[0] + "/ordem"), "{\"unidadeId\": \"" + ubs.getUuid() + "\", \"empenhoId\": \"" + emp
                + "\", \"itens\": [{\"itemInstrumentoId\": \"" + k[2] + "\", \"quantidade\": 60}]}", 201);
        String rec = enviar(post(COMPRAS + "ordem/" + JsonPath.read(o, "$.uuid") + "/recebimento"), "{\"notaFiscal\": \"NF 321\", \"notaEmitidaEm\": \"" + hoje
                + "\", \"itens\": [{\"itemOrdemId\": \"" + JsonPath.read(o, "$.itens[0].uuid") + "\", \"quantidade\": 50, \"numeroLote\": \"L1\", \"validade\": \""
                + hoje.plusYears(1) + "\"}]}", 201);
        String recId = JsonPath.read(rec, "$.recebimentos[0].uuid");
        enviar(post(COMPRAS + "recebimento/" + recId + "/definitivo"), "{\"itens\": [{\"itemRecebimentoId\": \"" + JsonPath.read(rec, "$.recebimentos[0].itens[0].uuid")
                + "\", \"quantidadeAceita\": 50}]}", 200);
        return new String[]{recId, emp, dot, k[1], k[0]};
    }

    @Test
    void liquidacaoEPagamentoDoRecebimento() throws Exception {
        String[] r = recebimentoDefinitivo();
        String rec = r[0], emp = r[1], dot = r[2], forn = r[3];
        String fila = ler(API + "a-liquidar");
        String daFila = "$[?(@.origemId == '" + rec + "')]";
        assertThat((List<?>) JsonPath.read(fila, daFila)).hasSize(1);
        assertThat(numero(fila, daFila + ".valor")).isEqualTo(100.0);
        assertThat(JsonPath.read(fila, daFila + ".empenhoId").toString()).contains(emp);

        // Outro empenho do mesmo fornecedor não serve: a ordem é do LQ0001.
        String outro = empenhar(dot, forn, null, "LQ0002", "300");
        String liq = "{\"origem\": \"RECEBIMENTO\", \"origemId\": \"" + rec + "\", \"empenhoId\": \"%s\", \"valorBruto\": %s, \"iss\": %s, \"irrf\": 0}";
        enviar(post(API + "liquidacao"), liq.formatted(outro, "100", "0"), 422);
        enviar(post(API + "liquidacao"), liq.formatted(emp, "101", "0"), 422);
        enviar(post(API + "liquidacao"), liq.formatted(emp, "100", "150"), 400);
        String l = enviar(post(API + "liquidacao"), liq.formatted(emp, "100", "5"), 201);
        String id = JsonPath.read(l, "$.uuid");
        assertThat(numero(l, "$.valorLiquido")).isEqualTo(95.0);
        assertThat((String) JsonPath.read(l, "$.notaFiscal")).isEqualTo("NF 321");
        assertThat((String) JsonPath.read(l, "$.vencimento")).isEqualTo(hoje.plusDays(30).toString());
        enviar(post(API + "liquidacao"), liq.formatted(emp, "100", "5"), 409);
        assertThat((List<?>) JsonPath.read(ler(API + "a-liquidar"), daFila)).isEmpty();

        // Avulsa: pede a justificativa e quem atestou; vence antes da outra e trava o pagamento fora da ordem cronológica.
        String avulsa = "{\"origem\": \"AVULSA\", \"empenhoId\": \"" + outro + "\", \"valorBruto\": 50, \"vencimento\": \"%s\"%s}";
        enviar(post(API + "liquidacao"), avulsa.formatted(hoje.plusDays(10), ""), 400);
        String a = enviar(post(API + "liquidacao"), avulsa.formatted(hoje, ", \"justificativa\": \"Conta de energia.\", \"atestadoPorMatricula\": \"LIQ-FISCAL\""), 201);
        String pagamento = "{\"data\": \"%s\", \"ordemBancaria\": \"2026OB0001\"%s}";
        enviar(post(API + "liquidacao/" + id + "/pagamento"), pagamento.formatted(hoje.plusDays(1), ""), 400);
        enviar(post(API + "liquidacao/" + id + "/pagamento"), pagamento.formatted(hoje, ""), 422);
        assertThat((Boolean) JsonPath.read(enviar(post(API + "liquidacao/" + JsonPath.read(a, "$.uuid") + "/pagamento"), pagamento.formatted(hoje, ""), 200), "$.paga")).isTrue();
        enviar(post(API + "liquidacao/" + id + "/pagamento"), pagamento.formatted(hoje, ""), 200);
        enviar(post(API + "liquidacao/" + id + "/pagamento"), pagamento.formatted(hoje, ""), 422);

        String exec = ler(API + "execucao?exercicio=" + hoje.getYear());
        String linha = "$.dotacoes[?(@.dotacaoId == '" + dot + "')]";
        assertThat(numero(exec, linha + ".liquidado")).isEqualTo(150.0);
        assertThat(numero(exec, linha + ".pago")).isEqualTo(150.0);
        assertThat(numero(exec, linha + ".empenhado")).isEqualTo(800.0);
    }

    @Test
    void ordemCronologica() throws Exception {
        String[] r = recebimentoDefinitivo();
        String emp = r[1], dot = r[2], forn = r[3];
        String outro = empenhar(dot, forn, null, "LQ0003", "300");
        String avulsa = "{\"origem\": \"AVULSA\", \"empenhoId\": \"" + outro + "\", \"valorBruto\": %s, \"vencimento\": \"%s\", \"justificativa\": \"Serviço.\", "
                + "\"atestadoPorMatricula\": \"LIQ-FISCAL\"}";
        enviar(post(API + "liquidacao"), avulsa.formatted("40", hoje.minusDays(5)), 201);
        String nova = JsonPath.read(enviar(post(API + "liquidacao"), avulsa.formatted("60", hoje), 201), "$.uuid");
        enviar(post(API + "liquidacao/" + nova + "/pagamento"), "{\"data\": \"" + hoje + "\", \"ordemBancaria\": \"OB2\"}", 422);
        String p = enviar(post(API + "liquidacao/" + nova + "/pagamento"), "{\"data\": \"" + hoje + "\", \"ordemBancaria\": \"OB2\", "
                + "\"justificativaForaDeOrdem\": \"Serviço essencial com risco de corte.\"}", 200);
        assertThat((String) JsonPath.read(p, "$.justificativaForaDeOrdem")).isEqualTo("Serviço essencial com risco de corte.");
        assertThat(numero(ler(API + "execucao"), "$.vencidasQuantidade")).isGreaterThanOrEqualTo(1.0);
        // O saldo a liquidar do empenho: 300 - 100.
        enviar(post(API + "liquidacao"), avulsa.formatted("201", hoje), 422);
        assertThat(emp).isNotBlank();
    }

    @Test
    void diariaDoTfd() throws Exception {
        Paciente p = new Paciente();
        p.setNome(PREFIXO + "Paciente");
        p.setCpf("90033410011");
        p.setDataNascimento(hoje.minusYears(50));
        p.setAtivo(true);
        Endereco e = new Endereco();
        e.setLogradouro("Rua A");
        e.setNumeroLogradouro("1");
        e.setCidade("Cidade");
        e.setEstado("SP");
        p.setEndereco(e);
        p = pacienteRepository.save(p);
        SolicitacaoTransporte s = new SolicitacaoTransporte();
        s.setPaciente(p);
        s.setUnidade(ubs);
        s.setMotivo(MotivoTransporte.TFD);
        s.setDataHora(hoje.plusDays(5).atTime(7, 0));
        s.setEnderecoEmbarque("Rua A, 1");
        s.setDestino("Hospital de referência");
        s.setDestinoMunicipio("Capital");
        s.setTrecho(TrechoTransporte.IDA_E_VOLTA);
        s.setSituacao(SituacaoSolicitacaoTransporte.AGUARDANDO_AUTORIZACAO);
        s.setSolicitadoEm(Instant.now());
        s = solicitacaoRepository.save(s);

        String pf = JsonPath.read(enviar(post(COMPRAS + "fornecedor"), "{\"documento\": \"90033410011\", \"razaoSocial\": \"" + PREFIXO + "Paciente\"}", 201), "$.uuid");
        String pj = JsonPath.read(enviar(post(COMPRAS + "fornecedor"), "{\"documento\": \"" + ComprasControllerTest.cnpj("910100000009") + "\", \"razaoSocial\": \""
                + PREFIXO + "Empresa\"}", 201), "$.uuid");
        String diaria = "{\"solicitacaoId\": \"" + s.getUuid() + "\", \"beneficiario\": \"%s\", \"credorId\": \"%s\", \"dias\": 3, \"valorDiaria\": 24.75}";
        enviar(post(API + "diaria-tfd"), diaria.formatted("PACIENTE", pf), 422);
        s.setSituacao(SituacaoSolicitacaoTransporte.AUTORIZADA);
        solicitacaoRepository.save(s);
        enviar(post(API + "diaria-tfd"), diaria.formatted("ACOMPANHANTE", pf), 422);
        enviar(post(API + "diaria-tfd"), diaria.formatted("PACIENTE", pj), 422);
        String d = enviar(post(API + "diaria-tfd"), diaria.formatted("PACIENTE", pf), 201);
        assertThat(numero(d, "$.valorTotal")).isEqualTo(74.25);
        enviar(post(API + "diaria-tfd"), diaria.formatted("PACIENTE", pf), 409);

        // A diária entra na fila e é liquidada contra um empenho do próprio credor.
        String id = JsonPath.read(d, "$.uuid");
        assertThat((List<?>) JsonPath.read(ler(API + "a-liquidar"), "$[?(@.origemId == '" + id + "')]")).hasSize(1);
        String dot = JsonPath.read(enviar(post(API + "dotacao"), FinanceiroControllerTest.dotacao(hoje.getYear(), "10.302.0011.2048", "3.3.90.48", "1000", PREFIXO), 201), "$.uuid");
        String outroCredor = empenhar(dot, pj, null, "LQ0010", "100");
        String doCredor = empenhar(dot, pf, null, "LQ0011", "100");
        String liq = "{\"origem\": \"DIARIA_TFD\", \"origemId\": \"" + id + "\", \"empenhoId\": \"%s\", \"valorBruto\": 74.25}";
        enviar(post(API + "liquidacao"), liq.formatted(outroCredor), 422);
        enviar(post(API + "liquidacao"), liq.formatted(doCredor), 201);
        assertThat(JsonPath.read(ler(API + "diarias-tfd?solicitacaoId=" + s.getUuid()), "$[?(@.beneficiario == 'PACIENTE')].liquidada").toString()).contains("true");

        // Pelo CPF: o acompanhante vira credor com o próprio nome.
        s.setAcompanhanteNome(PREFIXO + "Acompanhante");
        solicitacaoRepository.save(s);
        String porCpf = "{\"solicitacaoId\": \"" + s.getUuid() + "\", \"beneficiario\": \"ACOMPANHANTE\", \"credorCpf\": \"%s\", \"dias\": 2, \"valorDiaria\": 24.75}";
        enviar(post(API + "diaria-tfd"), porCpf.formatted("11111111111"), 400);
        String c = enviar(post(API + "diaria-tfd"), porCpf.formatted("529.982.247-25"), 201);
        assertThat((String) JsonPath.read(c, "$.credorNome")).isEqualTo(PREFIXO + "Acompanhante");
    }
}
