package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
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
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Segurança do paciente, fatia Q1 da Qualidade (ADR-0203): a notificação anônima ou identificada, a classificação pela ICPS (natureza e
 * dano coerentes), a causa raiz no dano grave, no óbito e no never event, o descarte, o prazo do Notivisa (72 horas no óbito; o 15º dia
 * útil do mês seguinte nos demais) e o painel.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class IncidenteSegurancaControllerTest {

    static final String PREFIXO = "Seguranca Teste ";
    private static final String API = "/api/v1/qualidade/";

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private UnidadeDeSaudeRepository unidadeDeSaudeRepository;
    @Autowired private PacienteRepository pacienteRepository;

    private UnidadeDeSaude ubs;
    private Paciente paciente;

    @BeforeEach
    void seed() {
        limpar();
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + "Hospital");
        u.setTipo(TipoUnidadeDeSaude.HOSPITAL);
        u.setAtivo(true);
        ubs = unidadeDeSaudeRepository.save(u);
        Paciente p = new Paciente();
        p.setNome(PREFIXO + "Paciente");
        p.setCpf("90066600001");
        p.setAtivo(true);
        paciente = pacienteRepository.save(p);
    }

    @AfterEach
    void limpar() {
        limparDados(jdbc, PREFIXO);
    }

    static void limparDados(JdbcTemplate jdbc, String prefixo) {
        jdbc.update("delete from tb_incidente_seguranca where unidade_id in (select uuid from tb_unidade_de_saude where nome like ?)", prefixo + "%");
        jdbc.update("delete from tb_evento_auditoria where paciente_id in (select uuid from tb_paciente where nome like ?)", prefixo + "%");
        jdbc.update("delete from tb_paciente where nome like ?", prefixo + "%");
        jdbc.update("delete from tb_unidade_de_saude where nome like ?", prefixo + "%");
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

    private String notificar(String tipo, Instant ocorrido, boolean anonima) throws Exception {
        return JsonPath.read(enviar(post(API + "incidente"), "{\"unidadeId\": \"" + ubs.getUuid() + "\", \"pacienteId\": \"" + paciente.getUuid()
                + "\", \"ocorridoEm\": \"" + ocorrido + "\", \"local\": \"Enfermaria 2\", \"tipo\": \"" + tipo + "\", \"descricao\": \"Paciente caiu "
                + "ao levantar da cama.\", \"acaoImediata\": \"Avaliação médica.\", \"anonima\": " + anonima + "}", 201), "$.uuid");
    }

    private String acao(String id, String acao, String corpo, int esperado) throws Exception {
        return enviar(post(API + "incidente/" + id + "/" + acao), corpo, esperado);
    }

    @Test
    void notificacaoClassificacaoECausaRaiz() throws Exception {
        Instant ontem = Instant.now().minus(Duration.ofDays(1));
        enviar(post(API + "incidente"), "{\"unidadeId\": \"" + ubs.getUuid() + "\", \"ocorridoEm\": \"" + Instant.now().plus(Duration.ofDays(1))
                + "\", \"local\": \"x\", \"tipo\": \"QUEDA\", \"descricao\": \"x\"}", 400);
        enviar(post(API + "incidente"), "{\"unidadeId\": \"" + ubs.getUuid() + "\", \"pacienteId\": \"" + UUID.randomUUID() + "\", \"ocorridoEm\": \""
                + ontem + "\", \"local\": \"x\", \"tipo\": \"QUEDA\", \"descricao\": \"x\"}", 404);
        String r = enviar(post(API + "incidente"), "{\"unidadeId\": \"" + ubs.getUuid() + "\", \"ocorridoEm\": \"" + ontem + "\", \"local\": \"Posto\", "
                + "\"tipo\": \"MEDICACAO\", \"descricao\": \"Dose dobrada quase administrada.\", \"anonima\": true}", 201);
        assertThat((String) JsonPath.read(r, "$.numero")).matches("\\d{4}/\\d{6}");
        assertThat((Boolean) JsonPath.read(r, "$.anonima")).isTrue();

        String queda = notificar("QUEDA", ontem, false);
        r = ler(API + "incidente/" + queda);
        assertThat((String) JsonPath.read(r, "$.situacao")).isEqualTo("RECEBIDA");
        assertThat((String) JsonPath.read(r, "$.pacienteNome")).isEqualTo(PREFIXO + "Paciente");

        // Natureza e dano coerentes.
        acao(queda, "classificacao", "{\"natureza\": \"EVENTO_ADVERSO\", \"dano\": \"NENHUM\"}", 400);
        acao(queda, "classificacao", "{\"natureza\": \"QUASE_ERRO\", \"dano\": \"LEVE\"}", 400);
        acao(queda, "conclusao", "{\"fatores\": [\"AMBIENTE\"]}", 422);
        r = acao(queda, "classificacao", "{\"natureza\": \"EVENTO_ADVERSO\", \"dano\": \"GRAVE\"}", 200);
        assertThat((String) JsonPath.read(r, "$.situacao")).isEqualTo("EM_ANALISE");
        assertThat((Boolean) JsonPath.read(r, "$.exigeCausaRaiz")).isTrue();
        assertThat((Boolean) JsonPath.read(r, "$.notivisaObrigatoria")).isTrue();

        // Dano grave pede a causa raiz.
        acao(queda, "conclusao", "{\"fatores\": [\"AMBIENTE\", \"TAREFA\"]}", 400);
        r = acao(queda, "conclusao", "{\"fatores\": [\"AMBIENTE\", \"TAREFA\", \"AMBIENTE\"], \"causaRaiz\": \"Grade da cama abaixada e piso molhado.\", "
                + "\"licoes\": \"Checar a grade na troca de plantão.\"}", 200);
        assertThat((String) JsonPath.read(r, "$.situacao")).isEqualTo("CONCLUIDA");
        assertThat((List<String>) JsonPath.read(r, "$.fatores")).containsExactly("AMBIENTE", "TAREFA");
        acao(queda, "classificacao", "{\"natureza\": \"EVENTO_ADVERSO\", \"dano\": \"LEVE\"}", 422);
        acao(queda, "descarte", "{\"motivo\": \"x\"}", 422);

        // O quase erro não vai ao Notivisa; o descartado sai do painel.
        String quase = notificar("IDENTIFICACAO_PACIENTE", ontem, false);
        r = acao(quase, "classificacao", "{\"natureza\": \"QUASE_ERRO\", \"dano\": \"NENHUM\"}", 200);
        assertThat((Boolean) JsonPath.read(r, "$.notivisaObrigatoria")).isFalse();
        acao(quase, "notivisa", "{\"numero\": \"123\"}", 422);
        String dup = notificar("QUEDA", ontem, false);
        r = acao(dup, "descarte", "{\"motivo\": \"Duplicada da queda da enfermaria 2.\"}", 200);
        assertThat((String) JsonPath.read(r, "$.situacao")).isEqualTo("DESCARTADA");

        String painel = ler(API + "seguranca/painel?unidadeId=" + ubs.getUuid());
        assertThat((Integer) JsonPath.read(painel, "$.total")).isEqualTo(3);
        assertThat((Integer) JsonPath.read(painel, "$.eventosAdversos")).isEqualTo(1);
        assertThat((List<Integer>) JsonPath.read(painel, "$.porTipo[?(@.codigo == 'QUEDA')].quantidade")).containsExactly(1);
        assertThat((List<String>) JsonPath.read(ler(API + "incidentes?situacao=DESCARTADA&unidadeId=" + ubs.getUuid()), "$[*].uuid")).containsExactly(dup);
    }

    @Test
    void prazosDoNotivisa() throws Exception {
        // Óbito: 72 horas da ocorrência; vencido sem o registro.
        Instant ocorrido = Instant.now().minus(Duration.ofDays(4)).truncatedTo(java.time.temporal.ChronoUnit.SECONDS);
        String obito = notificar("CIRURGIA", ocorrido, false);
        String r = acao(obito, "classificacao", "{\"natureza\": \"EVENTO_ADVERSO\", \"dano\": \"OBITO\", \"neverEvent\": true}", 200);
        assertThat(Instant.parse(JsonPath.read(r, "$.prazoNotivisa"))).isEqualTo(ocorrido.plus(Duration.ofHours(72)));
        assertThat((Boolean) JsonPath.read(r, "$.notivisaVencida")).isTrue();
        assertThat((Integer) JsonPath.read(ler(API + "seguranca/painel?unidadeId=" + ubs.getUuid()), "$.notivisaVencidas")).isEqualTo(1);
        acao(obito, "notivisa", "{\"numero\": \"\"}", 400);
        acao(obito, "notivisa", "{\"numero\": \"NV-2026-0001\", \"notificadoEm\": \"" + Instant.now().plus(Duration.ofHours(2)) + "\"}", 400);
        r = acao(obito, "notivisa", "{\"numero\": \"NV-2026-0001\"}", 200);
        assertThat((Boolean) JsonPath.read(r, "$.notivisaVencida")).isFalse();
        acao(obito, "notivisa", "{\"numero\": \"NV-2026-0002\"}", 409);

        // Os demais: o fim do 15º dia útil do mês seguinte (fevereiro de 2026 começa num domingo: dia 20).
        String leve = notificar("LESAO_POR_PRESSAO", Instant.parse("2026-01-10T13:00:00Z"), false);
        r = acao(leve, "classificacao", "{\"natureza\": \"EVENTO_ADVERSO\", \"dano\": \"LEVE\"}", 200);
        assertThat((String) JsonPath.read(r, "$.prazoNotivisa")).isEqualTo("2026-02-21T03:00:00Z");
        assertThat((Boolean) JsonPath.read(r, "$.exigeCausaRaiz")).isFalse();
        acao(leve, "conclusao", "{\"fatores\": [\"PACIENTE\"]}", 200);
    }
}
