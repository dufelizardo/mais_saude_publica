package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.Sexo;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
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
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Imunização, fatia I4 (ADR-0150): o ESAVI ligado às doses, com a gravidade, as recusas e o encerramento pela causalidade
 * e pela conduta; a contraindicação na caderneta e no registro da dose; o erro de imunização sugerido pela dose fora do
 * calendário; e o ESAVI na Sala de Situação.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EsaviControllerTest {

    private static final String PREFIXO = "Esavi Teste ";
    private static final String MATRICULA = "P-IMU4-TEC";

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private UnidadeDeSaudeRepository unidadeDeSaudeRepository;
    @Autowired private PacienteRepository pacienteRepository;
    @Autowired private ProfissionalRepository profissionalRepository;

    private final LocalDate hoje = LocalDate.now();
    private UnidadeDeSaude ubs;
    private int pacientes;

    @BeforeEach
    void seed() {
        limpar();
        ubs = new UnidadeDeSaude();
        ubs.setNome(PREFIXO + "UBS");
        ubs.setTipo(TipoUnidadeDeSaude.UBS);
        ubs.setAtivo(true);
        ubs = unidadeDeSaudeRepository.save(ubs);
        Profissional tec = new Profissional();
        tec.setMatricula(MATRICULA);
        tec.setCpf("90066600001");
        tec.setNome(PREFIXO + "Técnica");
        tec.setConselhoClasse("COREN");
        tec.setAtivo(true);
        profissionalRepository.save(tec);
    }

    @AfterEach
    void limpar() {
        String pacientesSql = "(select uuid from tb_paciente where nome like '" + PREFIXO + "%')";
        String notificacoes = "(select uuid from tb_notificacao where paciente_id in " + pacientesSql + ")";
        jdbc.update("delete from tb_contraindicacao_vacinal where paciente_id in " + pacientesSql);
        jdbc.update("delete from tb_notificacao_dose where notificacao_id in " + notificacoes);
        jdbc.update("delete from tb_classificacao_caso where notificacao_id in " + notificacoes);
        jdbc.update("delete from tb_notificacao where paciente_id in " + pacientesSql);
        jdbc.update("update tb_registro_vacinacao set retificacao_de_id = null where paciente_id in " + pacientesSql);
        jdbc.update("delete from tb_registro_vacinacao where paciente_id in " + pacientesSql);
        jdbc.update("delete from tb_paciente where nome like '" + PREFIXO + "%'");
        jdbc.update("delete from tb_profissional where matricula = '" + MATRICULA + "'");
        jdbc.update("delete from tb_unidade_de_saude where nome like '" + PREFIXO + "%'");
    }

    private Paciente paciente(LocalDate nascimento) {
        pacientes++;
        Paciente p = new Paciente();
        p.setNome(PREFIXO + "Criança " + pacientes);
        p.setCpf(String.format("900666%05d", pacientes));
        p.setSexo(Sexo.FEMININO);
        p.setDataNascimento(nascimento);
        p.setAtivo(true);
        return pacienteRepository.save(p);
    }

    private String id(String sql, Object... args) {
        return jdbc.queryForObject(sql, String.class, args);
    }

    private String agravo(String codigo) {
        return id("select cast(uuid as varchar) from tb_agravo where codigo = ?", codigo);
    }

    private String imuno(String codigo) {
        return id("select cast(uuid as varchar) from tb_imunobiologico where codigo = ?", codigo);
    }

    private String enviar(MockHttpServletRequestBuilder r, String corpo, int esperado) throws Exception {
        return mockMvc.perform(r.contentType(MediaType.APPLICATION_JSON).content(corpo)).andExpect(status().is(esperado)).andReturn().getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
    }

    private String ler(String url) throws Exception {
        return mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    private String transcrita(Paciente p, String codigo, String dose, LocalDate data) throws Exception {
        String r = enviar(post("/api/v1/vacinacao"), ("{\"pacienteId\": \"%s\", \"imunobiologicoId\": \"%s\", \"dose\": \"%s\", \"unidadeId\": \"%s\", "
                + "\"origem\": \"TRANSCRITA\", \"aplicadaEm\": \"%s\"}").formatted(p.getUuid(), imuno(codigo), dose, ubs.getUuid(),
                data.atTime(LocalTime.of(9, 0))), 201);
        return JsonPath.read(r, "$.uuid");
    }

    private String aplicada(Paciente p, String codigo, String dose, String justificativa) {
        return ("{\"pacienteId\": \"%s\", \"imunobiologicoId\": \"%s\", \"dose\": \"%s\", \"unidadeId\": \"%s\", \"origem\": \"APLICADA\", "
                + "\"profissionalMatricula\": \"%s\", \"lote\": \"L123\"%s}").formatted(p.getUuid(), imuno(codigo), dose, ubs.getUuid(), MATRICULA,
                justificativa == null ? "" : ", \"justificativa\": \"" + justificativa + "\"");
    }

    private String notificacao(Paciente p, String agravo, String extra) {
        return "{\"pacienteId\": \"%s\", \"agravoId\": \"%s\", \"unidadeId\": \"%s\", \"origem\": \"VACINACAO\"%s}"
                .formatted(p.getUuid(), agravo, ubs.getUuid(), extra);
    }

    @Test
    void esaviLigadoAsDosesEncerradoComContraindicacao() throws Exception {
        Paciente crianca = paciente(hoje.minusMonths(4));
        Paciente outra = paciente(hoje.minusMonths(4));
        LocalDate dia = hoje.minusDays(60);
        String penta = transcrita(crianca, "PENTA", "PRIMEIRA", dia);
        String vip = transcrita(crianca, "VIP", "PRIMEIRA", dia);
        String daOutra = transcrita(outra, "PENTA", "PRIMEIRA", dia);
        String esavi = agravo("ESAVI");
        String ficha = ", \"respostas\": {\"manifestacoes\": [\"Febre\", \"Convulsão\"], \"hospitalizado\": true}";

        // As recusas: sem dose, sem gravidade, dose de outro paciente, sintomas antes da dose, dose em outro agravo.
        enviar(post("/api/v1/vigilancia/notificacao"), notificacao(crianca, esavi, ficha + ", \"gravidade\": \"GRAVE\""), 400);
        enviar(post("/api/v1/vigilancia/notificacao"), notificacao(crianca, esavi, ficha + ", \"doseIds\": [\"" + penta + "\"]"), 400);
        enviar(post("/api/v1/vigilancia/notificacao"), notificacao(crianca, esavi, ficha + ", \"gravidade\": \"GRAVE\", \"doseIds\": [\"" + daOutra + "\"]"), 422);
        enviar(post("/api/v1/vigilancia/notificacao"), notificacao(crianca, esavi, ficha + ", \"gravidade\": \"GRAVE\", \"doseIds\": [\"" + penta
                + "\"], \"dataPrimeirosSintomas\": \"" + dia.minusDays(1) + "\""), 400);
        enviar(post("/api/v1/vigilancia/notificacao"), notificacao(crianca, agravo("COQUELUCHE"), ", \"doseIds\": [\"" + penta + "\"]"), 400);

        String n = enviar(post("/api/v1/vigilancia/notificacao"), notificacao(crianca, esavi, ficha + ", \"gravidade\": \"GRAVE\", \"doseIds\": [\""
                + penta + "\", \"" + vip + "\"], \"dataPrimeirosSintomas\": \"" + dia.plusDays(1) + "\""), 201);
        String notificacaoId = JsonPath.read(n, "$.uuid");
        assertThat((String) JsonPath.read(n, "$.gravidadeEsavi")).isEqualTo("GRAVE");
        assertThat((String) JsonPath.read(n, "$.origem")).isEqualTo("VACINACAO");
        assertThat((List<String>) JsonPath.read(n, "$.doses[*].sigla")).containsExactlyInAnyOrder("Penta", "VIP");

        // A caderneta marca as doses notificadas.
        assertThat((List<String>) JsonPath.read(ler("/api/v1/paciente/" + crianca.getUuid() + "/vacinacao"), "$.dosesNotificadas"))
                .containsExactlyInAnyOrder(penta, vip);

        // O encerramento do ESAVI pede a causalidade e a conduta; a causalidade dá a classificação final.
        String url = "/api/v1/vigilancia/notificacao/" + notificacaoId + "/encerramento";
        enviar(post(url), "{\"evolucao\": \"CURA\"}", 400);
        enviar(post(url), "{\"evolucao\": \"CURA\", \"causalidade\": \"A1_REACAO_PRODUTO\"}", 400);
        String e = enviar(post(url), "{\"evolucao\": \"CURA\", \"causalidade\": \"A1_REACAO_PRODUTO\", \"conduta\": \"CONTRAINDICAR\"}", 200);
        assertThat((String) JsonPath.read(e, "$.classificacaoFinal")).isEqualTo("CONFIRMADO");
        assertThat((String) JsonPath.read(e, "$.condutaEsavi")).isEqualTo("CONTRAINDICAR");

        // As vacinas das doses ficam contraindicadas: a dose seguinte só com a justificativa.
        String caderneta = ler("/api/v1/paciente/" + crianca.getUuid() + "/vacinacao");
        assertThat((List<String>) JsonPath.read(caderneta, "$.contraindicacoes[*].sigla")).containsExactlyInAnyOrder("Penta", "VIP");
        String recusa = enviar(post("/api/v1/vacinacao"), aplicada(crianca, "PENTA", "SEGUNDA", null), 422);
        assertThat(recusa).contains("contraindicada");
        enviar(post("/api/v1/vacinacao"), aplicada(crianca, "PENTA", "SEGUNDA", "Avaliado no CRIE: manter com observação."), 201);

        // A Sala de Situação conta o ESAVI do período, e o grave.
        String s = ler("/api/v1/indicadores-rede/vacinacao?unidadeId=" + ubs.getUuid() + "&de=" + hoje.minusDays(5) + "&ate=" + hoje);
        assertThat((Integer) JsonPath.read(s, "$.esaviNotificados")).isEqualTo(1);
        assertThat((Integer) JsonPath.read(s, "$.esaviGraves")).isEqualTo(1);
    }

    @Test
    void erroDeImunizacaoSugeridoPelaDoseForaDoCalendario() throws Exception {
        // BCG aos 6 anos: acima da idade máxima (59 meses), entra só com a justificativa.
        Paciente crianca = paciente(hoje.minusYears(6));
        String r = enviar(post("/api/v1/vacinacao"), aplicada(crianca, "BCG", "UNICA", "Sem registro anterior."), 201);
        String dose = JsonPath.read(r, "$.uuid");
        String erro = agravo("ERRO_IMUNIZACAO");
        List<String> sugeridas = JsonPath.read(ler("/api/v1/vigilancia/sugestoes?unidadeId=" + ubs.getUuid()),
                "$[?(@.agravoCodigo == 'ERRO_IMUNIZACAO')].referenciaId");
        assertThat(sugeridas).containsExactly(dose);

        String ficha = ", \"respostas\": {\"tipoErro\": \"Fora da idade\", \"houveEvento\": false}, \"doseIds\": [\"" + dose + "\"]";
        // A gravidade é só do ESAVI.
        enviar(post("/api/v1/vigilancia/notificacao"), notificacao(crianca, erro, ficha + ", \"gravidade\": \"NAO_GRAVE\""), 400);
        String n = enviar(post("/api/v1/vigilancia/notificacao"), notificacao(crianca, erro, ficha), 201);
        String notificacaoId = JsonPath.read(n, "$.uuid");
        // Notificada, a dose sai das sugestões.
        assertThat((List<String>) JsonPath.read(ler("/api/v1/vigilancia/sugestoes?unidadeId=" + ubs.getUuid()),
                "$[?(@.agravoCodigo == 'ERRO_IMUNIZACAO')].referenciaId")).isEmpty();

        // O erro de imunização segue o encerramento da Vigilância: sem causalidade, com a classificação.
        String url = "/api/v1/vigilancia/notificacao/" + notificacaoId;
        enviar(post(url + "/encerramento"), "{\"evolucao\": \"CURA\", \"causalidade\": \"C_COINCIDENTE\", \"conduta\": \"MANTER\"}", 400);
        enviar(post(url + "/classificacao"), "{\"classificacao\": \"CONFIRMADO\", \"criterio\": \"CLINICO\"}", 201);
        String e = enviar(post(url + "/encerramento"), "{\"evolucao\": \"CURA\"}", 200);
        assertThat((String) JsonPath.read(e, "$.classificacaoFinal")).isEqualTo("CONFIRMADO");
    }
}
