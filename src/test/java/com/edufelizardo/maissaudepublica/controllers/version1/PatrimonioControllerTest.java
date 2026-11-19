package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.Setor;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.TipoSetor;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

/**
 * Patrimônio, fatia PM1 (ADR-0215): o cadastro já tombado com o termo de responsabilidade; a correção, a troca do responsável e o uso; o
 * vínculo com o veículo da frota; a transferência em duas etapas (recusa, cancelamento e aceite); e o bem permanente comprado, que nasce a
 * tombar no recebimento definitivo de Compras.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PatrimonioControllerTest {

    static final String PREFIXO = "Patrimonio Teste ";
    private static final String API = "/api/v1/patrimonio/";
    private static final String COMPRAS = "/api/v1/compras/";

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private UnidadeDeSaudeRepository unidadeRepository;
    @Autowired private ProfissionalRepository profissionalRepository;
    @Autowired private SetorRepository setorRepository;

    private final LocalDate hoje = LocalDate.now(ZoneId.of("America/Sao_Paulo"));
    private UnidadeDeSaude ubs;
    private UnidadeDeSaude ubs2;
    private Setor setor;
    private Setor setor2;

    @BeforeEach
    void seed() {
        limpar();
        ubs = unidade("UBS Centro");
        ubs2 = unidade("UBS Norte");
        setor = setor(ubs, "PAT-S1");
        setor2 = setor(ubs2, "PAT-S2");
        for (String[] p : new String[][]{{"PAT-A", "90077810001", "t"}, {"PAT-B", "90077810002", "t"}, {"PAT-INATIVO", "90077810003", "f"},
                {"PAT-GESTOR", "90077810004", "t"}, {"PAT-FISCAL", "90077810005", "t"}}) {
            Profissional x = new Profissional();
            x.setMatricula(p[0]);
            x.setCpf(p[1]);
            x.setNome(PREFIXO + p[0]);
            x.setAtivo(p[2].equals("t"));
            profissionalRepository.save(x);
        }
    }

    @AfterEach
    void limpar() {
        limparDados(jdbc, PREFIXO);
        jdbc.update("delete from tb_profissional where matricula like 'PAT-%'");
    }

    /** Os bens, os veículos e as unidades do prefixo, com o que Compras deixou. */
    static void limparDados(JdbcTemplate jdbc, String prefixo) {
        String unidades = "(select uuid from tb_unidade_de_saude where nome like '" + prefixo + "%')";
        String bens = "(select uuid from tb_bem_patrimonial where unidade_id in " + unidades + ")";
        jdbc.update("delete from tb_responsavel_bem where bem_id in " + bens);
        jdbc.update("delete from tb_transferencia_bem where bem_id in " + bens + " or origem_id in " + unidades + " or destino_id in " + unidades);
        jdbc.update("delete from tb_bem_patrimonial where uuid in " + bens);
        jdbc.update("delete from tb_situacao_veiculo where veiculo_id in (select uuid from tb_veiculo where unidade_id in " + unidades + ")");
        jdbc.update("delete from tb_veiculo where unidade_id in " + unidades);
        ComprasControllerTest.limparDados(jdbc, prefixo);
    }

    private UnidadeDeSaude unidade(String nome) {
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + nome);
        u.setTipo(TipoUnidadeDeSaude.UBS);
        u.setAtivo(true);
        return unidadeRepository.save(u);
    }

    private Setor setor(UnidadeDeSaude u, String codigo) {
        Setor s = new Setor();
        s.setUnidade(u);
        s.setNome(PREFIXO + codigo);
        s.setCodigo(codigo);
        s.setTipo(TipoSetor.APOIO);
        s.setAtivo(true);
        return setorRepository.save(s);
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

    private String bem(String classe, UnidadeDeSaude u, Object setorId, String matricula, LocalDate aquisicao) {
        return ("{\"descricao\": \"Monitor multiparamétrico\", \"classe\": \"%s\", \"marca\": \"Acme\", \"numeroSerie\": \"SN-%s\", \"unidadeId\": \"%s\", "
                + "\"setorId\": %s, \"origem\": \"DOACAO\", \"valorAquisicao\": 8500.00, \"dataAquisicao\": \"%s\", \"documento\": \"Termo 12/2026\", "
                + "\"responsavelMatricula\": \"%s\"}").formatted(classe, System.nanoTime(), u.getUuid(), setorId == null ? "null" : "\"" + setorId + "\"",
                aquisicao, matricula);
    }

    @Test
    void cadastroResponsavelEUso() throws Exception {
        enviar(post(API + "bem"), bem("EQUIPAMENTO_MEDICO_HOSPITALAR", ubs, setor.getUuid(), "PAT-A", hoje.plusDays(1)), 400);
        enviar(post(API + "bem"), bem("EQUIPAMENTO_MEDICO_HOSPITALAR", ubs, setor2.getUuid(), "PAT-A", hoje), 422);
        enviar(post(API + "bem"), bem("EQUIPAMENTO_MEDICO_HOSPITALAR", ubs, setor.getUuid(), "PAT-INATIVO", hoje), 422);
        enviar(post(API + "bem"), bem("EQUIPAMENTO_MEDICO_HOSPITALAR", ubs, setor.getUuid(), "PAT-NINGUEM", hoje), 404);
        String b = enviar(post(API + "bem"), bem("EQUIPAMENTO_MEDICO_HOSPITALAR", ubs, setor.getUuid(), "PAT-A", hoje.minusYears(2)), 201);
        String id = JsonPath.read(b, "$.bem.uuid");
        String numero = JsonPath.read(b, "$.bem.numeroTombamento");
        assertThat(numero).matches(hoje.getYear() + "/\\d{6}");
        assertThat((String) JsonPath.read(b, "$.bem.situacao")).isEqualTo("EM_USO");
        assertThat((String) JsonPath.read(b, "$.bem.responsavelMatricula")).isEqualTo("PAT-A");
        assertThat((String) JsonPath.read(b, "$.bem.setorNome")).isEqualTo(PREFIXO + "PAT-S1");

        // Correção e busca.
        enviar(put(API + "bem/" + id), "{\"descricao\": \"Monitor multiparamétrico 12 derivações\", \"modelo\": \"MX-12\", \"setorId\": null}", 200);
        String lista = ler(API + "bens?unidadeId=" + ubs.getUuid() + "&busca=" + numero.substring(5));
        assertThat((List<?>) JsonPath.read(lista, "$")).hasSize(1);
        assertThat((String) JsonPath.read(lista, "$[0].modelo")).isEqualTo("MX-12");
        assertThat((Object) JsonPath.read(lista, "$[0].setorId")).isNull();
        assertThat((List<?>) JsonPath.read(ler(API + "bens?unidadeId=" + ubs.getUuid() + "&classe=MOBILIARIO"), "$")).isEmpty();

        // Responsável: a mesma pessoa não troca; a troca encerra o termo anterior.
        enviar(post(API + "bem/" + id + "/responsavel"), "{\"responsavelMatricula\": \"PAT-A\"}", 422);
        String d = enviar(post(API + "bem/" + id + "/responsavel"), "{\"responsavelMatricula\": \"PAT-B\"}", 200);
        assertThat((String) JsonPath.read(d, "$.bem.responsavelMatricula")).isEqualTo("PAT-B");
        assertThat((List<?>) JsonPath.read(d, "$.responsaveis")).hasSize(2);
        assertThat((String) JsonPath.read(d, "$.responsaveis[1].ate")).isEqualTo(hoje.toString());

        // Uso: só em uso ou ocioso por aqui.
        enviar(post(API + "bem/" + id + "/situacao"), "{\"situacao\": \"EM_MANUTENCAO\"}", 400);
        assertThat((String) JsonPath.read(enviar(post(API + "bem/" + id + "/situacao"), "{\"situacao\": \"OCIOSO\"}", 200), "$.bem.situacao")).isEqualTo("OCIOSO");

        // Veículo: só o bem da classe veículo.
        String veiculo = JsonPath.read(enviar(post("/api/v1/transporte/veiculo"), "{\"placa\": \"PAT1A23\", \"prefixo\": \"PAT-01\", "
                + "\"tipo\": \"CARRO_ADMINISTRATIVO\", \"unidadeId\": \"" + ubs.getUuid() + "\"}", 201), "$.uuid");
        enviar(post(API + "bem/" + id + "/veiculo"), "{\"veiculoId\": \"" + veiculo + "\"}", 422);
        String carro = JsonPath.read(enviar(post(API + "bem"), bem("VEICULO", ubs, null, "PAT-A", hoje), 201), "$.bem.uuid");
        String outro = JsonPath.read(enviar(post(API + "bem"), bem("VEICULO", ubs, null, "PAT-A", hoje), 201), "$.bem.uuid");
        String v = enviar(post(API + "bem/" + carro + "/veiculo"), "{\"veiculoId\": \"" + veiculo + "\"}", 200);
        assertThat((String) JsonPath.read(v, "$.bem.veiculoPlaca")).isEqualTo("PAT1A23");
        enviar(post(API + "bem/" + outro + "/veiculo"), "{\"veiculoId\": \"" + veiculo + "\"}", 409);
    }

    @Test
    void transferenciaEmDuasEtapas() throws Exception {
        String id = JsonPath.read(enviar(post(API + "bem"), bem("MOBILIARIO", ubs, setor.getUuid(), "PAT-A", hoje), 201), "$.bem.uuid");
        String envio = "{\"destinoId\": \"%s\", \"motivo\": \"Remanejamento.\"}";
        enviar(post(API + "bem/" + id + "/transferencia"), envio.formatted(ubs.getUuid()), 400);
        String t = enviar(post(API + "bem/" + id + "/transferencia"), envio.formatted(ubs2.getUuid()), 201);
        String tid = JsonPath.read(t, "$.bem.transferenciaPendente.uuid");
        enviar(post(API + "bem/" + id + "/transferencia"), envio.formatted(ubs2.getUuid()), 409);
        assertThat((List<?>) JsonPath.read(ler(API + "transferencias?pendentes=true"), "$[?(@.uuid == '" + tid + "')]")).hasSize(1);

        // Recusa: o bem fica na origem.
        String r = enviar(post(API + "transferencia/" + tid + "/recusa"), "{\"motivo\": \"Sem espaço.\"}", 200);
        assertThat((String) JsonPath.read(r, "$.bem.unidadeId")).isEqualTo(ubs.getUuid().toString());
        assertThat((String) JsonPath.read(r, "$.transferencias[0].situacao")).isEqualTo("RECUSADA");
        enviar(post(API + "transferencia/" + tid + "/aceite"), "{\"responsavelMatricula\": \"PAT-B\"}", 422);

        // Cancelamento pela origem.
        tid = JsonPath.read(enviar(post(API + "bem/" + id + "/transferencia"), envio.formatted(ubs2.getUuid()), 201), "$.bem.transferenciaPendente.uuid");
        enviar(post(API + "transferencia/" + tid + "/cancelamento"), "", 200);

        // Aceite: setor do destino, novo responsável.
        tid = JsonPath.read(enviar(post(API + "bem/" + id + "/transferencia"), envio.formatted(ubs2.getUuid()), 201), "$.bem.transferenciaPendente.uuid");
        enviar(post(API + "transferencia/" + tid + "/aceite"), "{\"setorId\": \"" + setor.getUuid() + "\", \"responsavelMatricula\": \"PAT-B\"}", 422);
        String a = enviar(post(API + "transferencia/" + tid + "/aceite"), "{\"setorId\": \"" + setor2.getUuid() + "\", \"responsavelMatricula\": \"PAT-B\"}", 200);
        assertThat((String) JsonPath.read(a, "$.bem.unidadeId")).isEqualTo(ubs2.getUuid().toString());
        assertThat((String) JsonPath.read(a, "$.bem.setorId")).isEqualTo(setor2.getUuid().toString());
        assertThat((String) JsonPath.read(a, "$.bem.responsavelMatricula")).isEqualTo("PAT-B");
        assertThat((Object) JsonPath.read(a, "$.bem.transferenciaPendente")).isNull();
        assertThat((List<?>) JsonPath.read(a, "$.transferencias")).hasSize(3);
        enviar(post(API + "transferencia/" + tid + "/cancelamento"), "", 422);
    }

    @Test
    void bemCompradoNasceATombar() throws Exception {
        String sem = "{\"unidadeId\": \"" + ubs.getUuid() + "\", \"justificativa\": \"x\", \"itens\": [{\"tipo\": \"BEM_PERMANENTE\", \"quantidade\": 2}]}";
        enviar(post(COMPRAS + "solicitacao"), sem, 400);
        String s = JsonPath.read(enviar(post(COMPRAS + "solicitacao"), "{\"unidadeId\": \"" + ubs.getUuid() + "\", \"justificativa\": \"Equipar o consultório.\", "
                + "\"itens\": [{\"tipo\": \"BEM_PERMANENTE\", \"descricao\": \"Cadeira odontológica completa\", \"unidadeMedida\": \"unidade\", \"quantidade\": 2}]}", 201), "$.uuid");
        enviar(post(COMPRAS + "solicitacao/" + s + "/envio"), "", 200);
        String sol = enviar(post(COMPRAS + "solicitacao/" + s + "/analise"), "{\"decisao\": \"APROVAR\"}", 200);
        String item = JsonPath.read(sol, "$.itens[0].uuid");
        enviar(post(COMPRAS + "item/" + item + "/cotacao"), "{\"fonte\": \"SITE\", \"referencia\": \"x\", \"precoUnitario\": 15000, \"cotadoEm\": \"" + hoje + "\"}", 201);
        enviar(post(COMPRAS + "solicitacao/" + s + "/pesquisa"), "{\"justificativas\": [{\"itemId\": \"" + item + "\", \"justificativa\": \"Única fonte.\"}]}", 200);
        String forn = JsonPath.read(enviar(post(COMPRAS + "fornecedor"), "{\"documento\": \"" + ComprasControllerTest.cnpj("907100000001") + "\", \"razaoSocial\": \""
                + PREFIXO + "Odonto Equipamentos\"}", 201), "$.uuid");
        for (String tipo : List.of("FEDERAL", "FGTS", "TRABALHISTA", "ESTADUAL", "MUNICIPAL")) {
            enviar(post(COMPRAS + "fornecedor/" + forn + "/certidao"), "{\"tipo\": \"" + tipo + "\", \"emitidaEm\": \"" + hoje + "\", \"validaAte\": \""
                    + hoje.plusDays(120) + "\"}", 201);
        }
        String c = enviar(post(COMPRAS + "contratacao"), "{\"numeroProcesso\": \"" + PREFIXO + "907\", \"modalidade\": \"PREGAO_ELETRONICO\", \"objeto\": \"x\", "
                + "\"solicitacaoIds\": [\"" + s + "\"]}", 201);
        enviar(post(COMPRAS + "item-contratacao/" + JsonPath.read(c, "$.itens[0].uuid") + "/resultado"), "{\"resultado\": \"ADJUDICADO\", \"fornecedorId\": \""
                + forn + "\", \"precoUnitario\": 14800}", 200);
        c = enviar(post(COMPRAS + "contratacao/" + JsonPath.read(c, "$.uuid") + "/homologacao"), "{\"instrumentos\": [{\"fornecedorId\": \"" + forn + "\", "
                + "\"tipo\": \"CONTRATO\", \"numero\": \"" + PREFIXO + "CT 907\", \"inicio\": \"" + hoje + "\", \"fim\": \"" + hoje.plusMonths(11)
                + "\", \"gestorMatricula\": \"PAT-GESTOR\", \"fiscalMatricula\": \"PAT-FISCAL\"}]}", 200);
        String ct = JsonPath.read(c, "$.instrumentos[0].uuid");
        String itemCt = JsonPath.read(ler(COMPRAS + "instrumento/" + ct), "$.itens[0].uuid");
        String o = enviar(post(COMPRAS + "instrumento/" + ct + "/ordem"), "{\"unidadeId\": \"" + ubs.getUuid() + "\", \"itens\": [{\"itemInstrumentoId\": \""
                + itemCt + "\", \"quantidade\": 2}]}", 201);
        assertThat((Boolean) JsonPath.read(o, "$.itens[0].pedeLote")).isFalse();
        String rec = enviar(post(COMPRAS + "ordem/" + JsonPath.read(o, "$.uuid") + "/recebimento"), "{\"notaFiscal\": \"NF 4455\", \"notaEmitidaEm\": \"" + hoje
                + "\", \"itens\": [{\"itemOrdemId\": \"" + JsonPath.read(o, "$.itens[0].uuid") + "\", \"quantidade\": 2}]}", 201);
        enviar(post(COMPRAS + "recebimento/" + JsonPath.read(rec, "$.recebimentos[0].uuid") + "/definitivo"), "{\"itens\": [{\"itemRecebimentoId\": \""
                + JsonPath.read(rec, "$.recebimentos[0].itens[0].uuid") + "\", \"quantidadeAceita\": 2}]}", 200);

        String aTombar = ler(API + "bens?unidadeId=" + ubs.getUuid() + "&situacao=A_TOMBAR");
        assertThat((List<?>) JsonPath.read(aTombar, "$")).hasSize(2);
        assertThat((String) JsonPath.read(aTombar, "$[0].fornecedorNome")).isEqualTo(PREFIXO + "Odonto Equipamentos");
        assertThat((String) JsonPath.read(aTombar, "$[0].documento")).isEqualTo("NF 4455");
        assertThat(JsonPath.read(aTombar, "$[0].valorAquisicao").toString()).isEqualTo("14800.0");
        assertThat((String) JsonPath.read(aTombar, "$[0].origem")).isEqualTo("COMPRA");
        assertThat((Object) JsonPath.read(aTombar, "$[0].numeroTombamento")).isNull();
        String b1 = JsonPath.read(aTombar, "$[0].uuid"), b2 = JsonPath.read(aTombar, "$[1].uuid");

        enviar(post(API + "bem/" + b2 + "/responsavel"), "{\"responsavelMatricula\": \"PAT-A\"}", 422);
        enviar(post(API + "bem/" + b2 + "/transferencia"), "{\"destinoId\": \"" + ubs2.getUuid() + "\", \"motivo\": \"x\"}", 422);
        String tomb = "{\"classe\": \"EQUIPAMENTO_MEDICO_HOSPITALAR\", \"numeroSerie\": \"CO-1\", \"setorId\": \"" + setor.getUuid() + "\", \"responsavelMatricula\": \"PAT-A\"}";
        String t = enviar(post(API + "bem/" + b1 + "/tombamento"), tomb, 200);
        assertThat((String) JsonPath.read(t, "$.bem.numeroTombamento")).startsWith(hoje.getYear() + "/");
        assertThat((String) JsonPath.read(t, "$.bem.situacao")).isEqualTo("EM_USO");
        assertThat((String) JsonPath.read(t, "$.bem.tombadoEm")).isEqualTo(hoje.toString());
        enviar(post(API + "bem/" + b1 + "/tombamento"), tomb, 422);
    }
}
