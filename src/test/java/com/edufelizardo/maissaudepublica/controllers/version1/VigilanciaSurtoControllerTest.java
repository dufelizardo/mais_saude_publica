package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.AgravoRepository;
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

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Vigilância, fatia V2b (ADR-0131): o surto (abertura com casos, mesmo agravo, um surto por caso, retirada, ciclo até o
 * encerramento), as medidas de controle do surto e do caso, a linha do tempo e o mapa filtrado pelo surto.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class VigilanciaSurtoControllerTest {

    private static final String PREFIXO = "VigV2b Teste ";
    private static final String CODIGO = "TSTV2B";
    private static final String URL = "/api/v1/vigilancia/";

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private UnidadeDeSaudeRepository unidadeRepository;
    @Autowired private PacienteRepository pacienteRepository;
    @Autowired private AgravoRepository agravoRepository;

    private UnidadeDeSaude ubs;
    private Paciente maria;
    private Paciente jose;
    private Paciente ana;
    private String sarampo;
    private String dengue;

    @BeforeEach
    void seed() {
        limpar();
        ubs = new UnidadeDeSaude();
        ubs.setNome(PREFIXO + "UBS");
        ubs.setTipo(TipoUnidadeDeSaude.UBS);
        ubs.setAtivo(true);
        ubs = unidadeRepository.save(ubs);
        maria = paciente("Maria", "93800000001");
        jose = paciente("José", "93800000002");
        ana = paciente("Ana", "93800000003");
        sarampo = agravoRepository.findByCodigo("SARAMPO").orElseThrow().getUuid().toString();
        dengue = agravoRepository.findByCodigo("DENGUE").orElseThrow().getUuid().toString();
    }

    @AfterEach
    void limpar() {
        // Os surtos saem antes do território (apontam as microáreas).
        VigilanciaControllerTest.limparSurtos(jdbc, PREFIXO, CODIGO);
        String unidades = "(select uuid from tb_unidade_de_saude where nome like '" + PREFIXO + "%')";
        String notificacoes = "(select uuid from tb_notificacao where unidade_id in " + unidades + ")";
        jdbc.update("delete from tb_classificacao_caso where notificacao_id in " + notificacoes);
        jdbc.update("delete from tb_notificacao where unidade_id in " + unidades);
        TerritorioControllerTest.limparDados(jdbc, PREFIXO, CODIGO);
        VigilanciaControllerTest.limparDados(jdbc, PREFIXO, CODIGO);
    }

    private Paciente paciente(String nome, String cpf) {
        Paciente p = new Paciente();
        p.setNome(PREFIXO + nome);
        p.setCpf(cpf);
        p.setAtivo(true);
        return pacienteRepository.save(p);
    }

    private String enviar(MockHttpServletRequestBuilder r, String corpo, int esperado) throws Exception {
        return mockMvc.perform(r.contentType(MediaType.APPLICATION_JSON).content(corpo)).andExpect(status().is(esperado))
                .andReturn().getResponse().getContentAsString();
    }

    private String ler(String url) throws Exception {
        return mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
    }

    private String notificar(Paciente p, String agravo, String extra) throws Exception {
        String r = enviar(post(URL + "notificacao"), "{\"pacienteId\": \"%s\", \"agravoId\": \"%s\", \"unidadeId\": \"%s\", \"origem\": \"ATENDIMENTO\"%s}"
                .formatted(p.getUuid(), agravo, ubs.getUuid(), extra), 201);
        return JsonPath.read(r, "$.uuid");
    }

    private String surto(String extra, String casos, int esperado) throws Exception {
        return enviar(post(URL + "surto"), ("{\"agravoId\": \"%s\", \"unidadeId\": \"%s\", \"nome\": \"Sarampo na escola\", \"inicio\": \"%s\"%s, "
                + "\"notificacoesIds\": [%s]}").formatted(sarampo, ubs.getUuid(), LocalDate.now().minusDays(10), extra, casos), esperado);
    }

    private static String ids(String... ids) {
        return String.join(", ", java.util.Arrays.stream(ids).map(i -> "\"" + i + "\"").toList());
    }

    @Test
    void surtoComCasosMedidasEEncerramento() throws Exception {
        String casoMaria = notificar(maria, sarampo, "");
        String casoJose = notificar(jose, sarampo, "");
        String casoAna = notificar(ana, sarampo, "");
        String casoDengue = notificar(ana, dengue, ", \"respostas\": {\"febre\": true}");

        String institucional = ", \"tipo\": \"INSTITUCIONAL\", \"tipoInstituicao\": \"ESCOLA\"";
        surto(institucional, "", 400);
        String r = surto(institucional + ", \"nomeInstituicao\": \"Escola Municipal\"", ids(casoMaria, casoJose), 201);
        String id = JsonPath.read(r, "$.uuid");
        assertThat(JsonPath.<String>read(r, "$.situacao")).isEqualTo("SUSPEITO");
        assertThat(JsonPath.<Integer>read(r, "$.casos")).isEqualTo(2);
        // Um surto por caso; do mesmo agravo.
        surto(", \"tipo\": \"COMUNITARIO\"", ids(casoJose), 409);
        enviar(post(URL + "surto/" + id + "/casos"), "{\"notificacoesIds\": [%s]}".formatted(ids(casoDengue)), 422);
        r = enviar(post(URL + "surto/" + id + "/casos"), "{\"notificacoesIds\": [%s]}".formatted(ids(casoAna)), 200);
        assertThat(JsonPath.<Integer>read(r, "$.casos")).isEqualTo(3);
        r = enviar(delete(URL + "surto/" + id + "/casos/" + casoAna), "", 200);
        assertThat(JsonPath.<Integer>read(r, "$.casos")).isEqualTo(2);
        assertThat((Object) JsonPath.read(ler(URL + "notificacao/" + casoAna), "$.surtoId")).isNull();

        // Medida do surto com o prazo vencido conta nos indicadores.
        String m = enviar(post(URL + "surto/" + id + "/medida"), "{\"tipo\": \"BLOQUEIO_VACINAL\", \"descricao\": \"Vacinar a escola\", \"prazo\": \"%s\"}"
                .formatted(LocalDate.now().minusDays(1)), 201);
        String medida = JsonPath.read(m, "$.uuid");
        assertThat(JsonPath.<Boolean>read(m, "$.vencida")).isTrue();
        assertThat(JsonPath.<Integer>read(ler(URL + "surtos?unidadeId=" + ubs.getUuid()), "$.medidasVencidas")).isEqualTo(1);

        // Ciclo: encerrar pede confirmado ou descartado, a conclusão e nenhuma medida em aberto.
        enviar(post(URL + "surto/" + id + "/situacao"), "{\"situacao\": \"ENCERRADO\", \"conclusao\": \"Fim\"}", 422);
        enviar(post(URL + "surto/" + id + "/situacao"), "{\"situacao\": \"EM_INVESTIGACAO\"}", 200);
        enviar(post(URL + "surto/" + id + "/situacao"), "{\"situacao\": \"CONFIRMADO\"}", 200);
        enviar(post(URL + "surto/" + id + "/situacao"), "{\"situacao\": \"ENCERRADO\"}", 400);
        enviar(post(URL + "surto/" + id + "/situacao"), "{\"situacao\": \"ENCERRADO\", \"conclusao\": \"Fim\"}", 422);
        enviar(patch(URL + "medida/" + medida), "{\"tipo\": \"BLOQUEIO_VACINAL\", \"descricao\": \"Vacinar a escola\", \"situacao\": \"CONCLUIDA\"}", 400);
        m = enviar(patch(URL + "medida/" + medida), "{\"tipo\": \"BLOQUEIO_VACINAL\", \"descricao\": \"Vacinar a escola\", \"situacao\": \"CONCLUIDA\", "
                + "\"resultado\": \"312 doses\"}", 200);
        assertThat(JsonPath.<Boolean>read(m, "$.vencida")).isFalse();
        enviar(patch(URL + "medida/" + medida), "{\"tipo\": \"BLOQUEIO_VACINAL\", \"descricao\": \"Outra\", \"situacao\": \"EM_ANDAMENTO\"}", 422);
        r = enviar(post(URL + "surto/" + id + "/situacao"), "{\"situacao\": \"ENCERRADO\", \"conclusao\": \"Transmissão interrompida.\"}", 200);
        assertThat(JsonPath.<String>read(r, "$.fim")).isEqualTo(LocalDate.now().toString());
        enviar(put(URL + "surto/" + id), "{\"agravoId\": \"%s\", \"unidadeId\": \"%s\", \"nome\": \"X\", \"tipo\": \"COMUNITARIO\", \"inicio\": \"%s\"}"
                .formatted(sarampo, ubs.getUuid(), LocalDate.now()), 422);

        // Detalhe: casos, curva e a linha do tempo (da trilha de auditoria).
        String d = ler(URL + "surto/" + id);
        assertThat((List<?>) JsonPath.read(d, "$.listaCasos")).hasSize(2);
        assertThat((List<?>) JsonPath.read(d, "$.curva")).isNotEmpty();
        assertThat((List<String>) JsonPath.read(d, "$.linhaDoTempo[*].titulo")).contains("Surto aberto", "Situação", "Medida", "Casos incluídos",
                "Caso retirado");
        // O caso aponta o surto, com a inclusão na linha do tempo.
        String n = ler(URL + "notificacao/" + casoMaria);
        assertThat(JsonPath.<String>read(n, "$.surtoNome")).isEqualTo("Sarampo na escola");
        assertThat((List<String>) JsonPath.read(n, "$.linhaDoTempo[*].titulo")).contains("Notificação", "Incluído no surto");
    }

    @Test
    void medidaDoCasoEMapaDoSurto() throws Exception {
        String caso = notificar(maria, sarampo, "");
        enviar(post(URL + "notificacao/" + caso + "/medida"), "{\"tipo\": \"ISOLAMENTO\"}", 400);
        enviar(post(URL + "notificacao/" + caso + "/medida"), "{\"tipo\": \"ISOLAMENTO\", \"descricao\": \"Isolamento domiciliar\"}", 201);
        String n = ler(URL + "notificacao/" + caso);
        assertThat((List<String>) JsonPath.read(n, "$.medidas[*].situacao")).containsExactly("PLANEJADA");
        assertThat((List<String>) JsonPath.read(n, "$.linhaDoTempo[*].titulo")).contains("Medida");

        // Surto comunitário com a microárea declarada; o mapa do surto traz só os casos dele e a microárea em destaque.
        String s = enviar(post("/api/v1/territorio/segmento"), "{\"codigo\": \"" + CODIGO + "\", \"nome\": \"D\", \"tipo\": \"OUTRO\"}", 201);
        String t = enviar(post("/api/v1/territorio/territorio"), "{\"segmentoId\": \"%s\", \"nome\": \"APS\", \"finalidade\": \"ATENCAO_PRIMARIA\"}"
                .formatted(JsonPath.<String>read(s, "$.uuid")), 201);
        String a = enviar(post("/api/v1/territorio/area"), "{\"territorioId\": \"%s\", \"codigo\": \"01\", \"nome\": \"A\", \"geometria\": %s}"
                .formatted(JsonPath.<String>read(t, "$.uuid"), CadastroTerritorialControllerTest.texto(TerritorioControllerTest.quadrado(-57.0, -27.0, 0.02))), 201);
        String m = enviar(post("/api/v1/territorio/microarea"), "{\"areaId\": \"%s\", \"codigo\": \"07\", \"geometria\": %s}"
                .formatted(JsonPath.<String>read(a, "$.area.uuid"), CadastroTerritorialControllerTest.texto(TerritorioControllerTest.quadrado(-57.0, -27.0, 0.01))), 201);
        String micro = JsonPath.read(m, "$.area.microareas[0].uuid");
        notificar(jose, sarampo, "");
        String r = surto(", \"tipo\": \"COMUNITARIO\", \"microareasIds\": [\"" + micro + "\"]", ids(caso), 201);
        String mapa = ler(URL + "mapa?surtoId=" + JsonPath.read(r, "$.uuid"));
        assertThat(JsonPath.<Integer>read(mapa, "$.total")).isEqualTo(1);
        assertThat((List<Boolean>) JsonPath.read(mapa, "$.microareas[?(@.codigo == '07')].doSurto")).containsExactly(true);
    }
}
