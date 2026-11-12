package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Processo administrativo sanitário e medidas, fatia V3b (ADR-0200): o auto de infração, a ciência, a defesa (no prazo, intempestiva e a
 * revelia), a decisão com as penalidades, o recurso (intempestivo, parcial), o encerramento com os efeitos na licença e a interdição, o
 * arquivamento, as medidas cautelares, os termos com a verificação pública e o relatório no #19.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProcessoSanitarioControllerTest {

    private static final String PREFIXO = VigilanciaSanitariaControllerTest.PREFIXO;
    private static final String API = "/api/v1/vigilancia-sanitaria/";

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private ProfissionalRepository profissionalRepository;

    private final LocalDate hoje = LocalDate.now();

    @BeforeEach
    void seed() {
        limpar();
        Profissional p = new Profissional();
        p.setMatricula("VISA-PROC");
        p.setCpf("90177700011");
        p.setNome(PREFIXO + "Fiscal do processo");
        p.setAtivo(true);
        profissionalRepository.save(p);
    }

    @AfterEach
    void limpar() {
        VigilanciaSanitariaControllerTest.limparDados(jdbc, PREFIXO, "VISA-%");
    }

    private String enviar(MockHttpServletRequestBuilder r, String corpo, int esperado) throws Exception {
        var resposta = mockMvc.perform(r.contentType(MediaType.APPLICATION_JSON).content(corpo)).andReturn().getResponse();
        String texto = resposta.getContentAsString(StandardCharsets.UTF_8);
        assertThat(resposta.getStatus()).as(texto).isEqualTo(esperado);
        return texto;
    }

    private String ler(String url) throws Exception {
        return mockMvc.perform(get(url)).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    /** Estabelecimento de risco II com licença vigente. */
    private String licenciado(String nome) throws Exception {
        String id = JsonPath.read(enviar(post(API + "estabelecimento"), VigilanciaSanitariaControllerTest.corpoEstabelecimento(
                VigilanciaSanitariaControllerTest.cnpj(), nome, "5611201:MEDIO"), 201), "$.uuid");
        enviar(post(API + "estabelecimento/" + id + "/licenca"), "{\"emitidaEm\": \"" + hoje.minusDays(100) + "\"}", 201);
        return id;
    }

    /** Inspeção com duas irregularidades (grave e leve), na data informada; devolve os ids das irregularidades. */
    private List<String> irregularidades(String estab, LocalDate data) throws Exception {
        String r = enviar(post(API + "inspecao"), "{\"estabelecimentoId\": \"" + estab + "\", \"motivo\": \"PROGRAMADA\", \"dataInspecao\": \"" + data
                + "\", \"resultado\": \"INSATISFATORIA\", \"relatorio\": \"Cozinha suja.\", \"fiscais\": [\"VISA-PROC\"], \"irregularidades\": ["
                + "{\"descricao\": \"Alimento vencido\", \"gravidade\": \"GRAVE\"}, {\"descricao\": \"Lixeira sem tampa\", \"gravidade\": \"LEVE\"}]}", 201);
        return JsonPath.read(r, "$.irregularidades[*].uuid");
    }

    private String auto(String estab, List<String> irregs, LocalDate data) throws Exception {
        String r = enviar(post(API + "processo"), "{\"estabelecimentoId\": \"" + estab + "\", \"irregularidades\": [\"" + String.join("\", \"", irregs)
                + "\"], \"dispositivoLegal\": \"Lei 6.437/1977, art. 10, IV\", \"descricao\": \"" + PREFIXO + "Expor à venda alimento vencido.\", "
                + "\"dataLavratura\": \"" + data + "\", \"fiscais\": [\"VISA-PROC\"]}", 201);
        return JsonPath.read(r, "$.uuid");
    }

    private String acao(String processo, String acao, String corpo, int esperado) throws Exception {
        return enviar(post(API + "processo/" + processo + "/" + acao), corpo, esperado);
    }

    @Test
    void ritoComDefesaIntempestivaEEncerramento() throws Exception {
        String loja = licenciado("Mercearia");
        List<String> irregs = irregularidades(loja, hoje.minusDays(80));
        String outra = licenciado("Outra");

        // As regras do auto.
        String base = "\"dispositivoLegal\": \"art. 10\", \"descricao\": \"x\", \"fiscais\": [\"VISA-PROC\"]";
        enviar(post(API + "processo"), "{\"estabelecimentoId\": \"" + outra + "\", \"irregularidades\": [\"" + irregs.get(0) + "\"], " + base + "}", 422);
        enviar(post(API + "processo"), "{\"estabelecimentoId\": \"" + loja + "\", \"irregularidades\": [\"" + irregs.get(0) + "\"], \"gravidade\": \"LEVE\", "
                + base + "}", 400);
        String processo = auto(loja, irregs, hoje.minusDays(80));
        String r = ler(API + "processo/" + processo);
        assertThat((String) JsonPath.read(r, "$.fase")).isEqualTo("AGUARDANDO_CIENCIA");
        assertThat((String) JsonPath.read(r, "$.gravidade")).isEqualTo("GRAVE");
        assertThat((List<String>) JsonPath.read(r, "$.irregularidades[*].situacao")).containsOnly("AUTUADA");
        assertThat((List<String>) JsonPath.read(r, "$.termos[*].tipo")).containsExactly("AUTO_INFRACAO");
        String codigo = JsonPath.read(r, "$.termos[0].codigo");
        enviar(post(API + "processo"), "{\"estabelecimentoId\": \"" + loja + "\", \"irregularidades\": [\"" + irregs.get(0) + "\"], " + base + "}", 422);

        // A verificação pública do termo, sem pessoa e com o arquivo íntegro.
        String v = ler("/api/v1/publico/visa/verificacao/" + codigo.toLowerCase());
        assertThat((Boolean) JsonPath.read(v, "$.integro")).isTrue();
        assertThat((String) JsonPath.read(v, "$.tipo")).isEqualTo("AUTO_INFRACAO");
        assertThat((String) JsonPath.read(v, "$.estabelecimento")).isEqualTo(PREFIXO + "Mercearia");
        mockMvc.perform(get("/api/v1/publico/visa/verificacao/XXXX-XXXX-XXXX")).andExpect(
                org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isNotFound());

        // Nada antes da ciência; a defesa depois do prazo é intempestiva.
        acao(processo, "defesa", "{\"texto\": \"x\"}", 422);
        acao(processo, "decisao", "{\"resultado\": \"ARQUIVAMENTO\", \"fundamentacao\": \"x\"}", 422);
        acao(processo, "ciencia", "{\"forma\": \"PESSOAL\", \"data\": \"" + hoje.minusDays(81) + "\"}", 400);
        r = acao(processo, "ciencia", "{\"forma\": \"PESSOAL\", \"data\": \"" + hoje.minusDays(79) + "\"}", 200);
        assertThat((String) JsonPath.read(r, "$.prazoDefesa")).isEqualTo(hoje.minusDays(64).toString());
        r = acao(processo, "defesa", "{\"texto\": \"O alimento era para descarte.\", \"data\": \"" + hoje.minusDays(60) + "\"}", 200);
        assertThat((Boolean) JsonPath.read(r, "$.defesaIntempestiva")).isTrue();
        assertThat((String) JsonPath.read(r, "$.fase")).isEqualTo("AGUARDANDO_DECISAO");

        // As regras das penalidades e a decisão.
        String data = "\"data\": \"" + hoje.minusDays(50) + "\", \"fundamentacao\": \"Infração comprovada.\"";
        acao(processo, "decisao", "{\"resultado\": \"PENALIDADE\", " + data + "}", 400);
        acao(processo, "decisao", "{\"resultado\": \"PENALIDADE\", " + data + ", \"penalidades\": [{\"tipo\": \"MULTA\"}]}", 400);
        acao(processo, "decisao", "{\"resultado\": \"PENALIDADE\", " + data + ", \"penalidades\": [{\"tipo\": \"SUSPENSAO_LICENCA\", \"suspensaoAte\": \""
                + hoje.plusDays(30) + "\"}, {\"tipo\": \"CANCELAMENTO_LICENCA\"}]}", 400);
        acao(processo, "decisao", "{\"resultado\": \"ARQUIVAMENTO\", " + data + ", \"penalidades\": [{\"tipo\": \"ADVERTENCIA\"}]}", 400);
        r = acao(processo, "decisao", "{\"resultado\": \"PENALIDADE\", " + data + ", \"atenuantes\": [\"PRIMARIO\"], \"agravantes\": [\"REINCIDENCIA\"], "
                + "\"penalidades\": [{\"tipo\": \"MULTA\", \"valorMulta\": 5000}, {\"tipo\": \"SUSPENSAO_LICENCA\", \"suspensaoAte\": \"" + hoje.plusDays(30)
                + "\"}, {\"tipo\": \"INTERDICAO_PARCIAL\", \"descricao\": \"Setor de frios\"}]}", 200);
        assertThat((String) JsonPath.read(r, "$.fase")).isEqualTo("DECIDIDO");
        assertThat((List<String>) JsonPath.read(r, "$.atenuantes")).containsExactly("PRIMARIO");
        assertThat((List<String>) JsonPath.read(r, "$.termos[*].tipo")).containsExactly("AUTO_INFRACAO", "DECISAO");
        // Antes de valer: a licença segue vigente.
        assertThat((String) JsonPath.read(ler(API + "estabelecimento/" + loja), "$.estabelecimento.licencaAtual.situacao")).isEqualTo("VIGENTE");

        // O recurso pede a ciência da decisão e o prazo; o encerramento espera o prazo.
        acao(processo, "recurso", "{\"texto\": \"x\"}", 422);
        acao(processo, "encerramento", "", 422);
        acao(processo, "ciencia", "{\"forma\": \"POSTAL\", \"data\": \"" + hoje.minusDays(40) + "\"}", 200);
        acao(processo, "recurso", "{\"texto\": \"Tarde demais.\", \"data\": \"" + hoje.minusDays(20) + "\"}", 422);
        r = acao(processo, "encerramento", "", 200);
        assertThat((String) JsonPath.read(r, "$.fase")).isEqualTo("ENCERRADO");
        assertThat((List<String>) JsonPath.read(r, "$.medidas[*].tipo")).containsExactly("INTERDICAO");
        assertThat((Boolean) JsonPath.read(r, "$.medidas[0].total")).isFalse();

        // Os efeitos: licença suspensa, interdição vigente, licença nova recusada.
        String detalhe = ler(API + "estabelecimento/" + loja);
        assertThat((String) JsonPath.read(detalhe, "$.estabelecimento.licencaAtual.situacao")).isEqualTo("SUSPENSA");
        enviar(post(API + "estabelecimento/" + loja + "/licenca"), "{}", 422);
        assertThat((List<String>) JsonPath.read(ler(API + "interdicoes"), "$[*].estabelecimentoNome")).contains(PREFIXO + "Mercearia");

        // O relatório no #19.
        String rel = ler("/api/v1/relatorios/VIGILANCIA_SANITARIA?de=" + hoje.minusDays(90) + "&ate=" + hoje);
        assertThat((String) JsonPath.read(rel, "$.categoria")).isEqualTo("Vigilância");
        assertThat((List<String>) JsonPath.read(rel, "$.resumo[?(@.rotulo == 'Multas aplicadas')].valor")).allMatch(x -> x.contains("5.000,00"));
        assertThat((List<String>) JsonPath.read(rel, "$.secoes[*].titulo")).contains("Interdições vigentes", "Autos de infração por gravidade");
    }

    @Test
    void reveliaERecursoParcial() throws Exception {
        String loja = licenciado("Padaria");
        String processo = auto(loja, irregularidades(loja, hoje.minusDays(60)), hoje.minusDays(60));
        acao(processo, "ciencia", "{\"forma\": \"EDITAL\", \"data\": \"" + hoje.minusDays(59) + "\"}", 200);
        // O prazo de defesa corre até hoje-44: decidir antes é 422; depois, à revelia.
        String fund = "\"fundamentacao\": \"Sem defesa.\"";
        String pen = ", \"penalidades\": [{\"tipo\": \"ADVERTENCIA\"}, {\"tipo\": \"MULTA\", \"valorMulta\": 1000}, {\"tipo\": \"CANCELAMENTO_LICENCA\"}]";
        acao(processo, "decisao", "{\"resultado\": \"PENALIDADE\", \"data\": \"" + hoje.minusDays(50) + "\", " + fund + pen + "}", 422);
        String r = acao(processo, "decisao", "{\"resultado\": \"PENALIDADE\", \"data\": \"" + hoje.minusDays(40) + "\", " + fund + pen + "}", 200);
        assertThat((Boolean) JsonPath.read(r, "$.revelia")).isTrue();
        List<String> penalidades = JsonPath.read(r, "$.penalidades[*].uuid");
        List<String> tipos = JsonPath.read(r, "$.penalidades[*].tipo");
        acao(processo, "ciencia", "{\"forma\": \"PESSOAL\", \"data\": \"" + hoje.minusDays(30) + "\"}", 200);
        r = acao(processo, "recurso", "{\"texto\": \"Pedimos a revisão.\", \"data\": \"" + hoje.minusDays(20) + "\"}", 200);
        assertThat((String) JsonPath.read(r, "$.fase")).isEqualTo("EM_RECURSO");
        acao(processo, "encerramento", "", 422);

        // Parcial: ao menos uma e não todas.
        String julg = "\"resultado\": \"PARCIALMENTE_PROVIDO\", \"fundamentacao\": \"Cancelamento desproporcional.\"";
        acao(processo, "julgamento-recurso", "{" + julg + ", \"penalidadesMantidas\": [\"" + String.join("\", \"", penalidades) + "\"]}", 400);
        String advertencia = penalidades.get(tipos.indexOf("ADVERTENCIA"));
        String multa = penalidades.get(tipos.indexOf("MULTA"));
        r = acao(processo, "julgamento-recurso", "{" + julg + ", \"penalidadesMantidas\": [\"" + advertencia + "\", \"" + multa + "\"]}", 200);
        assertThat((String) JsonPath.read(r, "$.fase")).isEqualTo("ENCERRADO");
        assertThat((List<Boolean>) JsonPath.read(r, "$.penalidades[?(@.tipo == 'CANCELAMENTO_LICENCA')].revogada")).containsExactly(true);
        assertThat((List<String>) JsonPath.read(r, "$.termos[*].tipo")).containsExactly("AUTO_INFRACAO", "DECISAO", "DECISAO_RECURSO");
        // O cancelamento revogado não cassa a licença.
        assertThat((String) JsonPath.read(ler(API + "estabelecimento/" + loja), "$.estabelecimento.licencaAtual.situacao")).isEqualTo("VIGENTE");
        acao(processo, "julgamento-recurso", "{" + julg + "}", 422);
    }

    @Test
    void arquivamentoReabreAsIrregularidades() throws Exception {
        String loja = licenciado("Açougue");
        List<String> irregs = irregularidades(loja, hoje.minusDays(30));
        String processo = auto(loja, irregs, hoje.minusDays(30));
        acao(processo, "ciencia", "{\"forma\": \"PESSOAL\", \"data\": \"" + hoje.minusDays(29) + "\"}", 200);
        acao(processo, "defesa", "{\"texto\": \"Laudo mostra conformidade.\", \"data\": \"" + hoje.minusDays(20) + "\"}", 200);
        String r = acao(processo, "decisao", "{\"resultado\": \"ARQUIVAMENTO\", \"fundamentacao\": \"Defesa acolhida.\"}", 200);
        assertThat((String) JsonPath.read(r, "$.fase")).isEqualTo("ENCERRADO");
        assertThat((List<String>) JsonPath.read(r, "$.irregularidades[*].situacao")).containsOnly("ABERTA");
        assertThat((Boolean) JsonPath.read(r, "$.revelia")).isFalse();
        acao(processo, "ciencia", "{\"forma\": \"PESSOAL\"}", 422);
        assertThat((List<String>) JsonPath.read(ler(API + "processos?fase=ENCERRADO"), "$[*].uuid")).contains(processo);
    }

    @Test
    void medidasCautelares() throws Exception {
        String loja = licenciado("Distribuidora");
        String url = API + "medida";
        enviar(post(url), "{\"estabelecimentoId\": \"" + loja + "\", \"tipo\": \"INTERDICAO\", \"descricao\": \"x\"}", 400);
        enviar(post(url), "{\"estabelecimentoId\": \"" + loja + "\", \"tipo\": \"INTERDICAO_CAUTELAR\", \"descricao\": \"x\", \"fimPrevisto\": \""
                + hoje.plusDays(91) + "\"}", 400);
        enviar(post(url), "{\"estabelecimentoId\": \"" + loja + "\", \"tipo\": \"APREENSAO\", \"descricao\": \"x\"}", 400);
        String r = enviar(post(url), "{\"estabelecimentoId\": \"" + loja + "\", \"tipo\": \"INTERDICAO_CAUTELAR\", \"descricao\": \"Câmara fria sem "
                + "refrigeração.\", \"inicio\": \"" + hoje.minusDays(85) + "\"}", 201);
        String cautelar = JsonPath.read(r, "$.uuid");
        assertThat((String) JsonPath.read(r, "$.fimPrevisto")).isEqualTo(hoje.plusDays(5).toString());
        assertThat((Boolean) JsonPath.read(r, "$.total")).isTrue();
        assertThat((List<String>) JsonPath.read(r, "$.termos[*].tipo")).containsExactly("INTERDICAO");
        r = enviar(post(url), "{\"estabelecimentoId\": \"" + loja + "\", \"tipo\": \"APREENSAO\", \"descricao\": \"Produto sem registro.\", \"produto\": "
                + "\"Suplemento X\", \"lote\": \"L12\", \"quantidade\": \"40 frascos\", \"localGuarda\": \"Depósito da VISA\"}", 201);
        String apreensao = JsonPath.read(r, "$.uuid");
        assertThat((List<String>) JsonPath.read(r, "$.termos[*].tipo")).containsExactly("APREENSAO");

        // Interditado não renova; a cautelar perto do fim nas pendências.
        enviar(post(API + "estabelecimento/" + loja + "/licenca"), "{}", 422);
        assertThat((List<String>) JsonPath.read(ler(API + "pendencias"), "$.medidas[?(@.medidaId == '" + cautelar + "')].motivo"))
                .containsExactly("CAUTELAR_VENCENDO");

        // A destinação do apreendido e a desinterdição com o termo.
        r = enviar(post(url + "/" + apreensao + "/encerramento"), "{\"motivo\": \"Inutilizado.\"}", 200);
        assertThat((List<String>) JsonPath.read(r, "$.termos[*].tipo")).containsExactly("APREENSAO");
        enviar(post(url + "/" + cautelar + "/encerramento"), "{\"motivo\": \"x\", \"data\": \"" + hoje.minusDays(86) + "\"}", 400);
        r = enviar(post(url + "/" + cautelar + "/encerramento"), "{\"motivo\": \"Refrigeração consertada.\"}", 200);
        assertThat((String) JsonPath.read(r, "$.situacao")).isEqualTo("ENCERRADA");
        assertThat((List<String>) JsonPath.read(r, "$.termos[*].tipo")).containsExactly("INTERDICAO", "DESINTERDICAO");
        enviar(post(url + "/" + cautelar + "/encerramento"), "{\"motivo\": \"x\"}", 422);
        assertThat((List<String>) JsonPath.read(ler(API + "medidas?situacao=VIGENTE&estabelecimentoId=" + loja), "$[*].uuid")).isEmpty();

        // A licença pelo número, sem login.
        String numero = JsonPath.read(ler(API + "estabelecimento/" + loja), "$.estabelecimento.licencaAtual.numero");
        String v = ler("/api/v1/publico/visa/licenca?numero=" + numero);
        assertThat((String) JsonPath.read(v, "$.situacao")).isEqualTo("VIGENTE");
        assertThat((String) JsonPath.read(v, "$.estabelecimento")).isEqualTo(PREFIXO + "Distribuidora");
    }
}
