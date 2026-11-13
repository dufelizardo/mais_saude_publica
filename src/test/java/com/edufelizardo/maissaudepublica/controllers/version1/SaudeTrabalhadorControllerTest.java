package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.AcidenteTrabalho;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.Sexo;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.AcidenteTrabalhoRepository;
import com.edufelizardo.maissaudepublica.repositories.AgravoRepository;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
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
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

/**
 * Saúde do Trabalhador, fatia V5a (ADR-0207): o bloco do trabalho obrigatório no agravo relacionado ao trabalho e só do acidente para
 * os dados do acidente; a investigação que completa o bloco; o acompanhamento sorológico do material biológico; o acidente do
 * servidor no RH ligado à notificação, com a marca no RH e a lista dos sem notificação.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SaudeTrabalhadorControllerTest {

    static final String PREFIXO = "Trabalhador Teste ";
    private static final String API = "/api/v1/vigilancia/";
    private static final String CNPJ = "11222333000181";

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private UnidadeDeSaudeRepository unidadeDeSaudeRepository;
    @Autowired private PacienteRepository pacienteRepository;
    @Autowired private ProfissionalRepository profissionalRepository;
    @Autowired private AcidenteTrabalhoRepository acidenteRepository;
    @Autowired private AgravoRepository agravoRepository;

    private final LocalDate hoje = LocalDate.now();
    private UnidadeDeSaude ubs;
    private Paciente pedreiro;

    @BeforeEach
    void seed() {
        limpar();
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + "UBS");
        u.setTipo(TipoUnidadeDeSaude.UBS);
        u.setAtivo(true);
        ubs = unidadeDeSaudeRepository.save(u);
        pedreiro = paciente("Pedreiro", "90066600001");
    }

    @AfterEach
    void limpar() {
        VigilanciaControllerTest.limparDados(jdbc, PREFIXO, "TSTV5");
    }

    private Paciente paciente(String nome, String cpf) {
        Paciente p = new Paciente();
        p.setNome(PREFIXO + nome);
        p.setCpf(cpf);
        p.setDataNascimento(hoje.minusYears(40));
        p.setSexo(Sexo.MASCULINO);
        p.setAtivo(true);
        return pacienteRepository.save(p);
    }

    private String agravo(String codigo) {
        return agravoRepository.findByCodigo(codigo).orElseThrow().getUuid().toString();
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

    private String notificacao(Paciente p, String codigo, LocalDate data, String respostas, String trabalho) {
        return "{\"pacienteId\": \"" + p.getUuid() + "\", \"agravoId\": \"" + agravo(codigo) + "\", \"unidadeId\": \"" + ubs.getUuid()
                + "\", \"origem\": \"ATENDIMENTO\"" + (data != null ? ", \"dataPrimeirosSintomas\": \"" + data + "\"" : "")
                + (respostas != null ? ", \"respostas\": " + respostas : "") + (trabalho != null ? ", \"trabalho\": " + trabalho : "") + "}";
    }

    private static final String BIOLOGICO = "{\"tipoExposicao\": [\"Percutânea\"], \"materialOrganico\": \"Sangue\", \"vacinadoHepatiteB\": true, "
            + "\"fonteConhecida\": false}";

    @Test
    void acidenteGraveComOBlocoDoTrabalho() throws Exception {
        // O catálogo marca os agravos do trabalho.
        String agravos = ler(API + "agravos");
        assertThat((List<Boolean>) JsonPath.read(agravos, "$[?(@.codigo == 'LER_DORT')].relacionadoAoTrabalho")).containsExactly(true);
        assertThat((List<Boolean>) JsonPath.read(agravos, "$[?(@.codigo == 'DENGUE')].relacionadoAoTrabalho")).containsExactly(false);

        LocalDate data = hoje.minusDays(2);
        String base = "\"ocupacaoDescricao\": \"Pedreiro\", \"situacaoTrabalho\": \"EMPREGADO_REGISTRADO\"";
        // Sem o bloco, CBO errado, CNPJ inválido, sem a data ou sem o tipo do acidente: 400.
        enviar(post(API + "notificacao"), notificacao(pedreiro, "ACIDENTE_TRABALHO_GRAVE", data, null, null), 400);
        enviar(post(API + "notificacao"), notificacao(pedreiro, "ACIDENTE_TRABALHO_GRAVE", data, null,
                "{\"ocupacaoCbo\": \"7152\", " + base + ", \"tipoAcidente\": \"TIPICO\"}"), 400);
        enviar(post(API + "notificacao"), notificacao(pedreiro, "ACIDENTE_TRABALHO_GRAVE", data, null,
                "{\"ocupacaoCbo\": \"7152-10\", " + base + ", \"tipoAcidente\": \"TIPICO\", \"empregadorDocumento\": \"11222333000180\", "
                        + "\"empregadorNome\": \"Construtora\"}"), 400);
        enviar(post(API + "notificacao"), notificacao(pedreiro, "ACIDENTE_TRABALHO_GRAVE", null, null,
                "{\"ocupacaoCbo\": \"7152-10\", " + base + ", \"tipoAcidente\": \"TIPICO\"}"), 400);
        enviar(post(API + "notificacao"), notificacao(pedreiro, "ACIDENTE_TRABALHO_GRAVE", data, null, "{\"ocupacaoCbo\": \"7152-10\", " + base + "}"), 400);
        // Dado do acidente fora do acidente: 400.
        enviar(post(API + "notificacao"), notificacao(pedreiro, "LER_DORT", null, null,
                "{\"ocupacaoCbo\": \"7152-10\", " + base + ", \"obito\": true}"), 400);

        String r = enviar(post(API + "notificacao"), notificacao(pedreiro, "ACIDENTE_TRABALHO_GRAVE", data, null,
                "{\"ocupacaoCbo\": \"7152-10\", " + base + ", \"tipoAcidente\": \"TIPICO\", \"empregadorDocumento\": \"11.222.333/0001-81\", "
                        + "\"empregadorNome\": \"Construtora Teste\", \"empregadorCnae\": \"4120-4/00\", \"horasAposInicioJornada\": 3, \"cat\": \"NAO\", "
                        + "\"localEvento\": \"Obra da Rua A\", \"obito\": true}"), 201);
        String n = JsonPath.read(r, "$.uuid");
        assertThat((String) JsonPath.read(r, "$.trabalho.ocupacaoCbo")).isEqualTo("715210");
        assertThat((String) JsonPath.read(r, "$.trabalho.empregadorDocumento")).isEqualTo(CNPJ);
        assertThat((String) JsonPath.read(r, "$.trabalho.empregadorCnae")).isEqualTo("4120400");
        assertThat((Boolean) JsonPath.read(r, "$.trabalho.acidente")).isTrue();
        assertThat((Boolean) JsonPath.read(r, "$.trabalho.obito")).isTrue();

        // A dengue sem bloco continua sem bloco; com bloco, só os dados comuns.
        r = enviar(post(API + "notificacao"), notificacao(pedreiro, "DENGUE", null, "{\"febre\": true}", null), 201);
        assertThat((Object) JsonPath.read(r, "$.trabalho")).isNull();

        // O painel: o acidente com óbito.
        String painel = ler(API + "saude-trabalhador/painel?unidadeId=" + ubs.getUuid());
        assertThat((Integer) JsonPath.read(painel, "$.total")).isEqualTo(1);
        assertThat((Integer) JsonPath.read(painel, "$.obitos")).isEqualTo(1);
        assertThat((List<String>) JsonPath.read(painel, "$.notificacoes[*].empregadorNome")).containsExactly("Construtora Teste");

        // A investigação corrige o bloco: a CAT foi emitida.
        r = enviar(put(API + "notificacao/" + n + "/trabalho"), "{\"ocupacaoCbo\": \"715210\", " + base + ", \"tipoAcidente\": \"TIPICO\", "
                + "\"empregadorDocumento\": \"" + CNPJ + "\", \"empregadorNome\": \"Construtora Teste\", \"cat\": \"SIM\", \"obito\": true}", 200);
        assertThat((String) JsonPath.read(r, "$.trabalho.cat")).isEqualTo("SIM");
        assertThat((String) JsonPath.read(r, "$.trabalho.atualizadoEm")).isNotNull();
        // Acompanhamento sorológico só no material biológico: 422.
        enviar(post(API + "notificacao/" + n + "/trabalho/acompanhamento"), "{\"etapa\": \"SEIS_SEMANAS\", \"realizadoEm\": \"" + hoje + "\"}", 422);
    }

    @Test
    void materialBiologicoComOAcompanhamentoSorologico() throws Exception {
        Paciente tecnica = paciente("Técnica", "90066600002");
        LocalDate acidente = hoje.minusDays(50);
        String r = enviar(post(API + "notificacao"), notificacao(tecnica, "ACIDENTE_TRABALHO_MATERIAL_BIOLOGICO", acidente, BIOLOGICO,
                "{\"ocupacaoCbo\": \"322205\", \"ocupacaoDescricao\": \"Técnica de enfermagem\", \"situacaoTrabalho\": \"SERVIDOR_ESTATUTARIO\"}"), 201);
        String n = JsonPath.read(r, "$.uuid");
        assertThat((Boolean) JsonPath.read(r, "$.trabalho.materialBiologico")).isTrue();
        assertThat((String) JsonPath.read(r, "$.prazoEncerramento")).isEqualTo(hoje.plusDays(180).toString());
        assertThat((String) JsonPath.read(r, "$.trabalho.acompanhamentos[0].prevista")).isEqualTo(acidente.plusWeeks(6).toString());
        assertThat((Boolean) JsonPath.read(r, "$.trabalho.acompanhamentos[0].vencida")).isTrue();
        assertThat((Boolean) JsonPath.read(r, "$.trabalho.acompanhamentos[1].vencida")).isFalse();
        assertThat((String) JsonPath.read(r, "$.trabalho.acompanhamentos[2].prevista")).isEqualTo(acidente.plusMonths(6).toString());

        String painel = ler(API + "saude-trabalhador/painel?unidadeId=" + ubs.getUuid());
        assertThat((Integer) JsonPath.read(painel, "$.materialBiologico")).isEqualTo(1);
        assertThat((List<String>) JsonPath.read(painel, "$.acompanhamentos[*].etapa")).containsExactly("SEIS_SEMANAS");

        // No futuro ou antes do acidente: 400; feito hoje, sai das pendências.
        String url = API + "notificacao/" + n + "/trabalho/acompanhamento";
        enviar(post(url), "{\"etapa\": \"SEIS_SEMANAS\", \"realizadoEm\": \"" + hoje.plusDays(1) + "\"}", 400);
        enviar(post(url), "{\"etapa\": \"SEIS_SEMANAS\", \"realizadoEm\": \"" + acidente.minusDays(1) + "\"}", 400);
        r = enviar(post(url), "{\"etapa\": \"SEIS_SEMANAS\", \"realizadoEm\": \"" + hoje + "\"}", 200);
        assertThat((String) JsonPath.read(r, "$.trabalho.acompanhamentos[0].realizadoEm")).isEqualTo(hoje.toString());
        assertThat((Boolean) JsonPath.read(r, "$.trabalho.acompanhamentos[0].vencida")).isFalse();
        painel = ler(API + "saude-trabalhador/painel?unidadeId=" + ubs.getUuid());
        assertThat((List<String>) JsonPath.read(painel, "$.acompanhamentos")).isEmpty();
    }

    @Test
    void servidorLigadoAoAcidenteDoRh() throws Exception {
        Profissional p = new Profissional();
        p.setMatricula("TSTV5-01");
        p.setCpf("90066600003");
        p.setNome(PREFIXO + "Servidora");
        p.setAtivo(true);
        p = profissionalRepository.save(p);
        AcidenteTrabalho a = acidenteRepository.save(new AcidenteTrabalho(p, LocalDateTime.now().minusDays(3), "Queda da escada do almoxarifado.",
                true, null, 5));
        Paciente servidora = paciente("Servidora", "900.666.000-03");

        String lista = ler(API + "saude-trabalhador/acidentes-sem-notificacao");
        assertThat((List<String>) JsonPath.read(lista, "$[?(@.acidenteId == '" + a.getUuid() + "')].pacienteId")).containsExactly(servidora.getUuid().toString());

        LocalDate data = hoje.minusDays(3);
        String bloco = "{\"ocupacaoCbo\": \"411005\", \"ocupacaoDescricao\": \"Auxiliar administrativo\", \"situacaoTrabalho\": \"SERVIDOR_ESTATUTARIO\", "
                + "\"tipoAcidente\": \"TIPICO\", \"cat\": \"SIM\", \"acidenteTrabalhoId\": \"%s\"}";
        // De outro trabalhador, 422; inexistente, 404.
        enviar(post(API + "notificacao"), notificacao(pedreiro, "ACIDENTE_TRABALHO_GRAVE", data, null, bloco.formatted(a.getUuid())), 422);
        enviar(post(API + "notificacao"), notificacao(servidora, "ACIDENTE_TRABALHO_GRAVE", data, null,
                bloco.formatted("00000000-0000-0000-0000-000000000001")), 404);
        String r = enviar(post(API + "notificacao"), notificacao(servidora, "ACIDENTE_TRABALHO_GRAVE", data, null, bloco.formatted(a.getUuid())), 201);
        assertThat((String) JsonPath.read(r, "$.trabalho.acidenteTrabalhoId")).isEqualTo(a.getUuid().toString());
        // O mesmo acidente em outra notificação: 409.
        enviar(post(API + "notificacao"), notificacao(servidora, "ACIDENTE_TRABALHO_GRAVE", data, null, bloco.formatted(a.getUuid())), 409);

        lista = ler(API + "saude-trabalhador/acidentes-sem-notificacao");
        assertThat((List<String>) JsonPath.read(lista, "$[*].acidenteId")).doesNotContain(a.getUuid().toString());
        String rh = ler("/api/v1/acidente-trabalho/profissional/TSTV5-01");
        assertThat((List<Boolean>) JsonPath.read(rh, "$[*].notificadoVigilancia")).containsExactly(true);
        String painel = ler(API + "saude-trabalhador/painel?unidadeId=" + ubs.getUuid());
        assertThat((List<Boolean>) JsonPath.read(painel, "$.notificacoes[*].servidor")).containsExactly(true);
    }
}
