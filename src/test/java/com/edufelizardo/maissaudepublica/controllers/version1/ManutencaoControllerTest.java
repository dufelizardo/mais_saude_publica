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
 * Manutenção, fatia PM2 (ADR-0216): o chamado com o bem parado, a triagem, a execução interna, a conclusão com as peças do Estoque e o
 * custo; o plano de calibração vencido que gera a ordem uma vez, o certificado e a próxima data; e a execução por contrato de serviço com
 * o veículo da frota em manutenção.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ManutencaoControllerTest {

    private static final String PREFIXO = "Manutencao Teste ";
    private static final String API = "/api/v1/patrimonio/";

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
        for (String[] p : new String[][]{{"MNT-RESP", "90066710001"}, {"MNT-GESTOR", "90066710002"}, {"MNT-FISCAL", "90066710003"}}) {
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
        PatrimonioControllerTest.limparDados(jdbc, PREFIXO);
        jdbc.update("delete from tb_profissional where matricula like 'MNT-%'");
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

    private String bem(String classe) throws Exception {
        return JsonPath.read(enviar(post(API + "bem"), ("{\"descricao\": \"%sAutoclave\", \"classe\": \"%s\", \"unidadeId\": \"%s\", \"origem\": \"DOACAO\", "
                + "\"dataAquisicao\": \"%s\", \"responsavelMatricula\": \"MNT-RESP\"}").formatted(PREFIXO, classe, ubs.getUuid(), hoje), 201), "$.bem.uuid");
    }

    private String situacaoDoBem(String id) throws Exception {
        return JsonPath.read(ler(API + "bem/" + id), "$.bem.situacao");
    }

    @Test
    void chamadoExecucaoInternaEPecas() throws Exception {
        String b = bem("EQUIPAMENTO_MEDICO_HOSPITALAR");
        String material = JsonPath.read(enviar(post("/api/v1/estoque/material"), "{\"codigo\": \"MNT-RES\", \"nome\": \"" + PREFIXO + "Resistência\", "
                + "\"categoria\": \"OUTRO\", \"unidadeMedida\": \"UNIDADE\", \"controlaLote\": false}", 201), "$.uuid");
        enviar(post("/api/v1/estoque/entrada"), "{\"unidadeId\": \"" + ubs.getUuid() + "\", \"materialId\": \"" + material + "\", \"quantidade\": 10, "
                + "\"origem\": \"SALDO_INICIAL\"}", 201);

        String o = enviar(post(API + "bem/" + b + "/chamado"), "{\"defeito\": \"Não esquenta.\", \"bemParado\": true}", 201);
        String os = JsonPath.read(o, "$.uuid");
        assertThat((String) JsonPath.read(o, "$.situacao")).isEqualTo("ABERTA");
        assertThat((String) JsonPath.read(o, "$.tipo")).isEqualTo("CORRETIVA");
        assertThat((String) JsonPath.read(o, "$.paradoDesde")).isEqualTo(hoje.toString());
        enviar(post(API + "ordem-servico/" + os + "/triagem"), "{\"prioridade\": \"ALTA\"}", 200);

        // Execução interna: o técnico é obrigatório; o bem fica em manutenção e não muda de uso à mão.
        enviar(post(API + "ordem-servico/" + os + "/inicio"), "{\"execucao\": \"INTERNA\"}", 400);
        String e = enviar(post(API + "ordem-servico/" + os + "/inicio"), "{\"execucao\": \"INTERNA\", \"tecnico\": \"João da manutenção\"}", 200);
        assertThat((String) JsonPath.read(e, "$.situacao")).isEqualTo("EM_EXECUCAO");
        assertThat((String) JsonPath.read(e, "$.prioridade")).isEqualTo("ALTA");
        assertThat(situacaoDoBem(b)).isEqualTo("EM_MANUTENCAO");
        enviar(post(API + "bem/" + b + "/situacao"), "{\"situacao\": \"OCIOSO\"}", 422);
        enviar(post(API + "ordem-servico/" + os + "/triagem"), "{\"prioridade\": \"BAIXA\"}", 422);

        // Conclusão: a peça sem material nem descrição (400), acima do saldo (422); depois, o custo e a saída do Estoque.
        String conclusao = "{\"servicoRealizado\": \"Troca da resistência.\", \"custoMaoDeObra\": 100, \"pecas\": [%s]}";
        enviar(post(API + "ordem-servico/" + os + "/conclusao"), conclusao.formatted("{\"quantidade\": 1}"), 400);
        enviar(post(API + "ordem-servico/" + os + "/conclusao"), conclusao.formatted("{\"materialId\": \"" + material + "\", \"quantidade\": 20}"), 422);
        String c = enviar(post(API + "ordem-servico/" + os + "/conclusao"), conclusao.formatted("{\"materialId\": \"" + material + "\", \"quantidade\": 3, "
                + "\"custoUnitario\": 5}, {\"descricao\": \"Fusível\", \"quantidade\": 1}"), 200);
        assertThat((String) JsonPath.read(c, "$.situacao")).isEqualTo("CONCLUIDA");
        assertThat(((Number) JsonPath.read(c, "$.custoTotal")).doubleValue()).isEqualTo(115.0);
        assertThat((Integer) JsonPath.read(c, "$.diasParado")).isEqualTo(0);
        assertThat((List<?>) JsonPath.read(c, "$.pecas")).hasSize(2);
        assertThat(situacaoDoBem(b)).isEqualTo("EM_USO");
        assertThat(jdbc.queryForObject("select sum(quantidade) from tb_lote_material where material_id = cast(? as uuid)", Integer.class, material)).isEqualTo(7);
        enviar(post(API + "ordem-servico/" + os + "/cancelamento"), "{\"motivo\": \"x\"}", 422);

        String doBem = ler(API + "bem/" + b + "/manutencao");
        assertThat(((Number) JsonPath.read(doBem, "$.custoTotal")).doubleValue()).isEqualTo(115.0);
        assertThat((List<?>) JsonPath.read(doBem, "$.ordens")).hasSize(1);
    }

    @Test
    void planoDeCalibracaoVencido() throws Exception {
        String b = bem("EQUIPAMENTO_MEDICO_HOSPITALAR");
        enviar(post(API + "bem/" + b + "/plano"), "{\"tipo\": \"CORRETIVA\", \"periodicidadeDias\": 30}", 400);
        enviar(post(API + "bem/" + b + "/plano"), "{\"tipo\": \"CALIBRACAO\", \"periodicidadeDias\": 30, \"ultimaExecucao\": \"" + hoje.plusDays(1) + "\"}", 400);
        String p = enviar(post(API + "bem/" + b + "/plano"), "{\"tipo\": \"CALIBRACAO\", \"periodicidadeDias\": 365, \"ultimaExecucao\": \""
                + hoje.minusDays(400) + "\"}", 201);
        String plano = JsonPath.read(p, "$.planos[0].uuid");
        assertThat((Boolean) JsonPath.read(p, "$.planos[0].vencido")).isTrue();
        enviar(post(API + "bem/" + b + "/plano"), "{\"tipo\": \"CALIBRACAO\", \"periodicidadeDias\": 180}", 409);

        // A ordem do plano vencido nasce na leitura, uma só.
        String filtro = API + "ordens-servico?unidadeId=" + ubs.getUuid();
        assertThat((List<?>) JsonPath.read(ler(filtro), "$")).hasSize(1);
        String ordens = ler(filtro);
        assertThat((List<?>) JsonPath.read(ordens, "$")).hasSize(1);
        assertThat((String) JsonPath.read(ordens, "$[0].tipo")).isEqualTo("CALIBRACAO");
        assertThat((String) JsonPath.read(ordens, "$[0].planoId")).isEqualTo(plano);
        String os = JsonPath.read(ordens, "$[0].uuid");
        assertThat((List<?>) JsonPath.read(ler(API + "manutencao/resumo"), "$.calibracoesVencidas[?(@.uuid == '" + plano + "')]")).hasSize(1);

        // A calibração pede o certificado e a validade futura; a conclusão conta a próxima a partir de hoje.
        enviar(post(API + "ordem-servico/" + os + "/inicio"), "{\"execucao\": \"INTERNA\", \"tecnico\": \"Laboratório de metrologia\"}", 200);
        enviar(post(API + "ordem-servico/" + os + "/conclusao"), "{\"servicoRealizado\": \"Calibrado.\"}", 400);
        enviar(post(API + "ordem-servico/" + os + "/conclusao"), "{\"servicoRealizado\": \"Calibrado.\", \"certificado\": \"CAL-1\", \"calibracaoValidaAte\": \""
                + hoje + "\"}", 400);
        enviar(post(API + "ordem-servico/" + os + "/conclusao"), "{\"servicoRealizado\": \"Calibrado.\", \"certificado\": \"CAL-1\", \"calibracaoValidaAte\": \""
                + hoje.plusYears(1) + "\"}", 200);
        String doBem = ler(API + "bem/" + b + "/manutencao");
        assertThat((String) JsonPath.read(doBem, "$.planos[0].proximaEm")).isEqualTo(hoje.plusDays(365).toString());
        assertThat((String) JsonPath.read(doBem, "$.calibracaoValidaAte")).isEqualTo(hoje.plusYears(1).toString());
        assertThat((Boolean) JsonPath.read(doBem, "$.calibracaoVencida")).isFalse();
        assertThat((List<?>) JsonPath.read(ler(filtro), "$")).hasSize(1);

        enviar(post(API + "plano/" + plano + "/desativacao"), "", 200);
        enviar(post(API + "plano/" + plano + "/desativacao"), "", 422);
    }

    @Test
    void execucaoPorContratoComVeiculo() throws Exception {
        String b = bem("VEICULO");
        String veiculo = JsonPath.read(enviar(post("/api/v1/transporte/veiculo"), "{\"placa\": \"MNT1A23\", \"prefixo\": \"MNT-01\", "
                + "\"tipo\": \"CARRO_ADMINISTRATIVO\", \"unidadeId\": \"" + ubs.getUuid() + "\"}", 201), "$.uuid");
        enviar(post(API + "bem/" + b + "/veiculo"), "{\"veiculoId\": \"" + veiculo + "\"}", 200);
        String[] k = EntregasControllerTest.contrato(mockMvc, jdbc, PREFIXO, ubs, hoje, "908100000001", "MNT-GESTOR", "MNT-FISCAL", r -> r);

        assertThat((List<?>) JsonPath.read(ler(API + "manutencao/contratos-servico"), "$[?(@.uuid == '" + k[0] + "')]")).hasSize(1);
        String os = JsonPath.read(enviar(post(API + "bem/" + b + "/chamado"), "{\"defeito\": \"Barulho no motor.\"}", 201), "$.uuid");
        enviar(post(API + "ordem-servico/" + os + "/inicio"), "{\"execucao\": \"CONTRATO\"}", 400);
        String e = enviar(post(API + "ordem-servico/" + os + "/inicio"), "{\"execucao\": \"CONTRATO\", \"instrumentoId\": \"" + k[0] + "\"}", 200);
        assertThat((String) JsonPath.read(e, "$.instrumentoNumero")).isEqualTo(PREFIXO + "CT 908100000001");
        assertThat((String) JsonPath.read(e, "$.paradoDesde")).isEqualTo(hoje.toString());
        assertThat((String) JsonPath.read(ler("/api/v1/transporte/veiculo/" + veiculo), "$.situacao")).isEqualTo("MANUTENCAO");

        // O cancelamento da execução devolve o bem e o veículo.
        enviar(post(API + "ordem-servico/" + os + "/cancelamento"), "{\"motivo\": \"Oficina sem a peça.\"}", 200);
        assertThat(situacaoDoBem(b)).isEqualTo("EM_USO");
        assertThat((String) JsonPath.read(ler("/api/v1/transporte/veiculo/" + veiculo), "$.situacao")).isEqualTo("DISPONIVEL");
    }
}
