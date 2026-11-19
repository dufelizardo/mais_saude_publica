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

/**
 * Patrimônio, fatia PM3 (ADR-0217): o inventário com a comissão, a conferência (encontrado, outro setor, outra unidade, não encontrado),
 * o achado a mais e o fechamento que corrige o setor, abre a transferência e sugere a baixa; a baixa na hora, a que espera a confirmação
 * acima da alçada, a recusa e os bloqueios; e o painel com o valor contábil.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class InventarioPatrimonioControllerTest {

    private static final String PREFIXO = "Inventario Teste ";
    private static final String API = "/api/v1/patrimonio/";

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private UnidadeDeSaudeRepository unidadeRepository;
    @Autowired private ProfissionalRepository profissionalRepository;
    @Autowired private SetorRepository setorRepository;

    private final LocalDate hoje = LocalDate.now(ZoneId.of("America/Sao_Paulo"));
    private UnidadeDeSaude ubs;
    private UnidadeDeSaude ubs2;
    private Setor s1;
    private Setor s2;
    private Setor s3;

    @BeforeEach
    void seed() {
        limpar();
        ubs = unidade("UBS Centro");
        ubs2 = unidade("UBS Norte");
        s1 = setor(ubs, "INV-S1");
        s2 = setor(ubs, "INV-S2");
        s3 = setor(ubs2, "INV-S3");
        for (String[] p : new String[][]{{"INV-RESP", "90055610001"}, {"INV-M1", "90055610002"}, {"INV-M2", "90055610003"}, {"INV-M3", "90055610004"}}) {
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
        jdbc.update("delete from tb_profissional where matricula like 'INV-%'");
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

    private String bem(String descricao, String origem, String valor, LocalDate aquisicao) throws Exception {
        return JsonPath.read(enviar(post(API + "bem"), ("{\"descricao\": \"%s%s\", \"classe\": \"EQUIPAMENTO_MEDICO_HOSPITALAR\", \"unidadeId\": \"%s\", "
                + "\"setorId\": \"%s\", \"origem\": \"%s\", \"valorAquisicao\": %s, \"dataAquisicao\": \"%s\", \"responsavelMatricula\": \"INV-RESP\"}")
                .formatted(PREFIXO, descricao, ubs.getUuid(), s1.getUuid(), origem, valor, aquisicao), 201), "$.bem.uuid");
    }

    private String itemDo(String inventario, String bem) {
        return JsonPath.read(inventario, "$.itens[?(@.bemId == '" + bem + "')].uuid").toString().replaceAll("[\\[\\]\"]", "");
    }

    @Test
    void inventarioComDivergencias() throws Exception {
        String b1 = bem("Monitor", "DOACAO", "1000", hoje), b2 = bem("Maca", "DOACAO", "1000", hoje),
                b3 = bem("Cadeira", "DOACAO", "1000", hoje), b4 = bem("Balança", "DOACAO", "1000", hoje);
        String abertura = "{\"unidadeId\": \"" + ubs.getUuid() + "\", \"membrosMatriculas\": [%s]}";
        enviar(post(API + "inventario"), abertura.formatted("\"INV-M1\", \"INV-M2\", \"INV-M2\""), 400);
        String inv = enviar(post(API + "inventario"), abertura.formatted("\"INV-M1\", \"INV-M2\", \"INV-M3\""), 201);
        String id = JsonPath.read(inv, "$.uuid");
        assertThat((Integer) JsonPath.read(inv, "$.total")).isEqualTo(4);
        assertThat((List<?>) JsonPath.read(inv, "$.membros")).hasSize(3);
        enviar(post(API + "inventario"), abertura.formatted("\"INV-M1\", \"INV-M2\", \"INV-M3\""), 409);
        enviar(post(API + "bem/" + b1 + "/transferencia"), "{\"destinoId\": \"" + ubs2.getUuid() + "\", \"motivo\": \"x\"}", 422);

        String conf = API + "inventario-item/%s/conferencia";
        enviar(post(conf.formatted(itemDo(inv, b1))), "{\"resultado\": \"ENCONTRADO\"}", 400);
        enviar(post(conf.formatted(itemDo(inv, b1))), "{\"resultado\": \"ENCONTRADO\", \"conservacao\": \"BOM\"}", 200);
        enviar(post(conf.formatted(itemDo(inv, b2))), "{\"resultado\": \"OUTRO_LOCAL\", \"conservacao\": \"REGULAR\"}", 400);
        enviar(post(conf.formatted(itemDo(inv, b2))), "{\"resultado\": \"OUTRO_LOCAL\", \"conservacao\": \"REGULAR\", \"setorId\": \"" + s2.getUuid() + "\"}", 200);
        enviar(post(conf.formatted(itemDo(inv, b3))), "{\"resultado\": \"OUTRO_LOCAL\", \"conservacao\": \"BOM\", \"unidadeId\": \"" + ubs2.getUuid()
                + "\", \"setorId\": \"" + s1.getUuid() + "\"}", 422);
        enviar(post(conf.formatted(itemDo(inv, b3))), "{\"resultado\": \"OUTRO_LOCAL\", \"conservacao\": \"BOM\", \"unidadeId\": \"" + ubs2.getUuid()
                + "\", \"setorId\": \"" + s3.getUuid() + "\"}", 200);
        enviar(post(API + "inventario/" + id + "/fechamento"), "", 422);
        String c = enviar(post(conf.formatted(itemDo(inv, b4))), "{\"resultado\": \"NAO_ENCONTRADO\"}", 200);
        assertThat((Integer) JsonPath.read(c, "$.conferidos")).isEqualTo(4);
        assertThat((Integer) JsonPath.read(c, "$.naoEncontrados")).isEqualTo(1);
        enviar(post(API + "inventario/" + id + "/achado"), "{\"descricao\": \"Cadeira de rodas sem plaqueta\", \"local\": \"Recepção\"}", 201);

        String f = enviar(post(API + "inventario/" + id + "/fechamento"), "", 200);
        assertThat((String) JsonPath.read(f, "$.situacao")).isEqualTo("FECHADO");
        assertThat((List<?>) JsonPath.read(f, "$.achados")).hasSize(1);
        assertThat(JsonPath.read(f, "$.itens[?(@.bemId == '" + b2 + "')].acao").toString()).contains("SETOR_CORRIGIDO");
        assertThat(JsonPath.read(f, "$.itens[?(@.bemId == '" + b3 + "')].acao").toString()).contains("TRANSFERENCIA");
        assertThat(JsonPath.read(f, "$.itens[?(@.bemId == '" + b4 + "')].acao").toString()).contains("BAIXA_SUGERIDA");
        assertThat((String) JsonPath.read(ler(API + "bem/" + b2), "$.bem.setorId")).isEqualTo(s2.getUuid().toString());
        assertThat((String) JsonPath.read(ler(API + "bem/" + b3), "$.bem.transferenciaPendente.destinoId")).isEqualTo(ubs2.getUuid().toString());
        enviar(post(conf.formatted(itemDo(inv, b1))), "{\"resultado\": \"NAO_ENCONTRADO\"}", 422);
        assertThat((List<?>) JsonPath.read(ler(API + "inventarios?unidadeId=" + ubs.getUuid()), "$")).hasSize(1);
    }

    @Test
    void baixaNaHoraComConfirmacaoERecusa() throws Exception {
        String barato = bem("Biombo", "DOACAO", "900", hoje.minusYears(3));
        enviar(post(API + "bem/" + barato + "/plano"), "{\"tipo\": \"PREVENTIVA\", \"periodicidadeDias\": 365}", 201);
        String baixa = "{\"motivo\": \"%s\", \"processo\": \"PA 77/2026\", \"dataBaixa\": \"%s\"%s}";
        enviar(post(API + "bem/" + barato + "/baixa"), baixa.formatted("INSERVIVEL", hoje.plusDays(1), ""), 400);
        enviar(post(API + "bem/" + barato + "/baixa"), baixa.formatted("FURTO_ROUBO", hoje, ""), 400);
        enviar(post(API + "bem/" + barato + "/baixa"), baixa.formatted("DEVOLUCAO_CESSAO", hoje, ""), 422);
        String x = enviar(post(API + "bem/" + barato + "/baixa"), baixa.formatted("INSERVIVEL", hoje, ""), 201);
        assertThat((String) JsonPath.read(x, "$.situacao")).isEqualTo("CONFIRMADA");
        String d = ler(API + "bem/" + barato);
        assertThat((String) JsonPath.read(d, "$.bem.situacao")).isEqualTo("BAIXADO");
        assertThat((String) JsonPath.read(d, "$.responsaveis[0].ate")).isEqualTo(hoje.toString());
        assertThat((String) JsonPath.read(d, "$.baixa.processo")).isEqualTo("PA 77/2026");
        assertThat((Boolean) JsonPath.read(ler(API + "bem/" + barato + "/manutencao"), "$.planos[0].ativo")).isFalse();
        enviar(post(API + "bem/" + barato + "/baixa"), baixa.formatted("INSERVIVEL", hoje, ""), 422);

        // Acima da alçada: espera a confirmação; a recusa mantém o bem.
        String caro = bem("Ultrassom", "DOACAO", "20000", hoje);
        String p = enviar(post(API + "bem/" + caro + "/baixa"), baixa.formatted("FURTO_ROUBO", hoje, ", \"boletimOcorrencia\": \"BO 1234\""), 201);
        assertThat((String) JsonPath.read(p, "$.situacao")).isEqualTo("PENDENTE");
        assertThat((String) JsonPath.read(ler(API + "bem/" + caro), "$.bem.situacao")).isEqualTo("EM_USO");
        enviar(post(API + "bem/" + caro + "/baixa"), baixa.formatted("INSERVIVEL", hoje, ""), 409);
        assertThat((List<?>) JsonPath.read(ler(API + "baixas?pendentes=true"), "$[?(@.uuid == '" + JsonPath.read(p, "$.uuid") + "')]")).hasSize(1);
        enviar(post(API + "baixa/" + JsonPath.read(p, "$.uuid") + "/recusa"), "{\"motivo\": \"Bem recuperado.\"}", 200);
        assertThat((String) JsonPath.read(ler(API + "bem/" + caro), "$.bem.situacao")).isEqualTo("EM_USO");
        p = enviar(post(API + "bem/" + caro + "/baixa"), baixa.formatted("INSERVIVEL", hoje, ""), 201);
        String confirmada = enviar(post(API + "baixa/" + JsonPath.read(p, "$.uuid") + "/confirmacao"), "", 200);
        assertThat((String) JsonPath.read(confirmada, "$.situacao")).isEqualTo("CONFIRMADA");
        assertThat((String) JsonPath.read(ler(API + "bem/" + caro), "$.bem.situacao")).isEqualTo("BAIXADO");
        enviar(post(API + "baixa/" + JsonPath.read(p, "$.uuid") + "/confirmacao"), "", 422);

        // Com ordem de serviço aberta, a baixa espera.
        String comOrdem = bem("Autoclave", "DOACAO", "500", hoje);
        enviar(post(API + "bem/" + comOrdem + "/chamado"), "{\"defeito\": \"Vazamento.\"}", 201);
        enviar(post(API + "bem/" + comOrdem + "/baixa"), baixa.formatted("INSERVIVEL", hoje, ""), 422);
    }

    @Test
    void painelComValorContabil() throws Exception {
        String b = bem("Desfibrilador", "DOACAO", "8500", hoje.minusMonths(24));
        // Equipamento médico: 10 anos, residual de 10%. Em 24 meses, 8500 - 7650 × 24/120 = 6970.
        assertThat(((Number) JsonPath.read(ler(API + "bem/" + b), "$.bem.valorContabil")).doubleValue()).isEqualTo(6970.0);
        bem("Oxímetro", "DOACAO", "1500", hoje);
        enviar(post(API + "bem/" + b + "/situacao"), "{\"situacao\": \"OCIOSO\"}", 200);
        String p = ler(API + "painel");
        String unidade = "$.porUnidade[?(@.unidadeId == '" + ubs.getUuid() + "')]";
        assertThat(JsonPath.read(p, unidade + ".bens").toString()).isEqualTo("[2]");
        assertThat(JsonPath.read(p, unidade + ".semInventarioHa12Meses").toString()).isEqualTo("[true]");
        assertThat(((Number) JsonPath.read(p, "$.ociosos")).longValue()).isGreaterThanOrEqualTo(1);
        assertThat((List<?>) JsonPath.read(p, "$.porClasse")).isNotEmpty();
    }
}
