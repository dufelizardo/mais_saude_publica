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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Vigilância, fatia V2a (ADR-0130): contatos do caso (com e sem cadastro, monitoramento pelo agravo, o contato que vira
 * caso), o local do caso (pelo domicílio do cadastro territorial ou marcado no mapa) e o mapa dos casos (microáreas,
 * pontos, curva, agravo sensível sem ponto).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class VigilanciaContatosControllerTest {

    private static final String PREFIXO = "VigV2a Teste ";
    private static final String CODIGO = "TSTV2A";
    private static final String URL = "/api/v1/vigilancia/";

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private UnidadeDeSaudeRepository unidadeRepository;
    @Autowired private PacienteRepository pacienteRepository;
    @Autowired private AgravoRepository agravoRepository;

    private UnidadeDeSaude ubs;
    private Paciente maria;
    private Paciente jose;
    private String sarampo;

    @BeforeEach
    void seed() {
        limpar();
        ubs = new UnidadeDeSaude();
        ubs.setNome(PREFIXO + "UBS");
        ubs.setTipo(TipoUnidadeDeSaude.UBS);
        ubs.setAtivo(true);
        ubs = unidadeRepository.save(ubs);
        maria = paciente("Maria", "93700000001");
        jose = paciente("José", "93700000002");
        sarampo = agravoRepository.findByCodigo("SARAMPO").orElseThrow().getUuid().toString();
    }

    @AfterEach
    void limpar() {
        // As notificações saem antes do território (apontam a microárea); o território antes dos pacientes.
        String unidades = "(select uuid from tb_unidade_de_saude where nome like '" + PREFIXO + "%')";
        String notificacoes = "(select uuid from tb_notificacao where unidade_id in " + unidades + ")";
        jdbc.update("delete from tb_contato_epidemiologico where notificacao_id in " + notificacoes + " or notificacao_gerada_id in " + notificacoes);
        jdbc.update("delete from tb_classificacao_caso where notificacao_id in " + notificacoes);
        jdbc.update("delete from tb_notificacao where unidade_id in " + unidades);
        jdbc.update("delete from tb_adscricao where paciente_id in (select uuid from tb_paciente where nome like ?)", PREFIXO + "%");
        TerritorioControllerTest.limparDados(jdbc, PREFIXO, CODIGO);
        jdbc.update("delete from tb_equipe where unidade_id in " + unidades);
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

    private String notificar(Paciente p, String agravo, String extra, int esperado) throws Exception {
        return enviar(post(URL + "notificacao"), "{\"pacienteId\": \"%s\", \"agravoId\": \"%s\", \"unidadeId\": \"%s\", \"origem\": \"ATENDIMENTO\"%s}"
                .formatted(p.getUuid(), agravo, ubs.getUuid(), extra), esperado);
    }

    @Test
    void contatosComMonitoramentoEOContatoQueViraCaso() throws Exception {
        String caso = JsonPath.read(notificar(maria, sarampo, "", 201), "$.uuid");
        enviar(post(URL + "notificacao/" + caso + "/contato"), "{\"tipoExposicao\": \"COMUNITARIO\"}", 400);
        enviar(post(URL + "notificacao/" + caso + "/contato"), "{\"pacienteId\": \"%s\", \"tipoExposicao\": \"DOMICILIAR\"}".formatted(maria.getUuid()), 422);
        String r = enviar(post(URL + "notificacao/" + caso + "/contato"), "{\"nome\": \"Vizinha\", \"telefone\": \"19 99999-0000\", \"tipoExposicao\": \"COMUNITARIO\", \"dataExposicao\": \"%s\"}"
                .formatted(LocalDate.now().minusDays(25)), 201);
        List<String> vizinha = JsonPath.read(r, "$.contatos[?(@.nome == 'Vizinha')].uuid");
        assertThat((List<Boolean>) JsonPath.read(r, "$.contatos[?(@.nome == 'Vizinha')].monitoramentoVencido")).containsExactly(true);
        assertThat((List<String>) JsonPath.read(r, "$.contatos[?(@.nome == 'Vizinha')].fimMonitoramento"))
                .containsExactly(LocalDate.now().minusDays(4).toString());
        r = enviar(post(URL + "notificacao/" + caso + "/contato"), "{\"pacienteId\": \"%s\", \"tipoExposicao\": \"DOMICILIAR\"}".formatted(jose.getUuid()), 201);
        List<String> doJose = JsonPath.read(r, "$.contatos[?(@.pacienteId == '" + jose.getUuid() + "')].uuid");
        assertThat(JsonPath.<Integer>read(ler(URL + "notificacoes?unidadeId=" + ubs.getUuid()), "$.contatosVencidos")).isGreaterThanOrEqualTo(1);

        // Virar caso só notificando o contato; a notificação dele fica ligada ao caso índice.
        enviar(patch(URL + "contato/" + doJose.get(0)), "{\"tipoExposicao\": \"DOMICILIAR\", \"situacao\": \"VIROU_CASO\"}", 422);
        String secundario = JsonPath.read(notificar(jose, sarampo, ", \"contatoId\": \"" + doJose.get(0) + "\"", 201), "$.uuid");
        notificar(jose, sarampo, ", \"contatoId\": \"" + doJose.get(0) + "\"", 409);
        r = ler(URL + "notificacao/" + caso);
        assertThat((List<String>) JsonPath.read(r, "$.contatos[?(@.pacienteId == '" + jose.getUuid() + "')].situacao")).containsExactly("VIROU_CASO");
        assertThat((List<String>) JsonPath.read(r, "$.contatos[?(@.pacienteId == '" + jose.getUuid() + "')].notificacaoGeradaId")).containsExactly(secundario);
        enviar(patch(URL + "contato/" + doJose.get(0)), "{\"tipoExposicao\": \"DOMICILIAR\", \"situacao\": \"LIBERADO\"}", 422);
        r = enviar(patch(URL + "contato/" + vizinha.get(0)), "{\"nome\": \"Vizinha\", \"tipoExposicao\": \"COMUNITARIO\", \"situacao\": \"LIBERADO\"}", 200);
        assertThat((List<String>) JsonPath.read(r, "$.contatos[?(@.nome == 'Vizinha')].situacao")).containsExactly("LIBERADO");
    }

    @Test
    void localPeloDomicilioOuMarcadoEMapaDosCasos() throws Exception {
        // Território: área com equipe e a microárea 07 desenhada; Maria mora num domicílio com ponto lá.
        String s = enviar(post("/api/v1/territorio/segmento"), "{\"codigo\": \"" + CODIGO + "\", \"nome\": \"D\", \"tipo\": \"OUTRO\"}", 201);
        String t = enviar(post("/api/v1/territorio/territorio"), "{\"segmentoId\": \"%s\", \"nome\": \"APS\", \"finalidade\": \"ATENCAO_PRIMARIA\"}"
                .formatted(JsonPath.<String>read(s, "$.uuid")), 201);
        double lon = -58.0;
        double lat = -28.0;
        String a = enviar(post("/api/v1/territorio/area"), "{\"territorioId\": \"%s\", \"codigo\": \"01\", \"nome\": \"A\", \"geometria\": %s}"
                .formatted(JsonPath.<String>read(t, "$.uuid"), CadastroTerritorialControllerTest.texto(TerritorioControllerTest.quadrado(lon, lat, 0.02))), 201);
        String area = JsonPath.read(a, "$.area.uuid");
        String m = enviar(post("/api/v1/territorio/microarea"), "{\"areaId\": \"%s\", \"codigo\": \"07\", \"geometria\": %s}"
                .formatted(area, CadastroTerritorialControllerTest.texto(TerritorioControllerTest.quadrado(lon, lat, 0.01))), 201);
        String micro = JsonPath.read(m, "$.area.microareas[0].uuid");
        String e = enviar(post("/api/v1/equipe/"), "{\"unidadeId\": \"%s\", \"tipo\": \"ESF\", \"nome\": \"%sSF\"}".formatted(ubs.getUuid(), PREFIXO), 201);
        enviar(post("/api/v1/territorio/area/" + area + "/responsabilidade"), "{\"equipeId\": \"%s\"}".formatted(JsonPath.<String>read(e, "$.resumo.uuid")), 200);
        String i = enviar(post("/api/v1/territorio/imovel"), "{\"microareaId\": \"%s\", \"tipo\": \"DOMICILIO\", \"endereco\": %s, \"latitude\": %s, \"longitude\": %s}"
                .formatted(micro, CadastroTerritorialControllerTest.endereco("Rua A", "1"), lat + 0.005, lon + 0.005), 201);
        String d = enviar(post("/api/v1/territorio/domicilio"), "{\"imovelId\": \"%s\"}".formatted(JsonPath.<String>read(i, "$.imovel.uuid")), 201);
        enviar(post("/api/v1/territorio/familia"), "{\"domicilioId\": \"%s\", \"responsavelId\": \"%s\"}"
                .formatted(JsonPath.<String>read(d, "$.domicilios[0].uuid"), maria.getUuid()), 201);

        String r = notificar(maria, sarampo, "", 201);
        assertThat(JsonPath.<String>read(r, "$.origemLocal")).isEqualTo("DOMICILIO");
        assertThat(JsonPath.<String>read(r, "$.microareaCodigo")).isEqualTo("07");
        r = notificar(jose, sarampo, "", 201);
        assertThat((Object) JsonPath.read(r, "$.origemLocal")).isNull();
        String doJose = JsonPath.read(r, "$.uuid");
        enviar(post(URL + "notificacao/" + doJose + "/local"), "{\"latitude\": %s}".formatted(lat), 400);
        r = enviar(post(URL + "notificacao/" + doJose + "/local"), "{\"latitude\": %s, \"longitude\": %s}".formatted(lat + 0.002, lon + 0.002), 200);
        assertThat(JsonPath.<String>read(r, "$.origemLocal")).isEqualTo("MARCADO");
        assertThat(JsonPath.<String>read(r, "$.microareaCodigo")).isEqualTo("07");

        // Mapa do sarampo na unidade: a microárea com 2 casos, os 2 pontos e a curva.
        String mapa = ler(URL + "mapa?agravoId=" + sarampo + "&unidadeId=" + ubs.getUuid());
        assertThat(JsonPath.<Integer>read(mapa, "$.total")).isEqualTo(2);
        assertThat((List<Integer>) JsonPath.read(mapa, "$.microareas[?(@.codigo == '07')].casos")).containsExactly(2);
        assertThat((List<?>) JsonPath.read(mapa, "$.pontos")).hasSize(2);
        assertThat((List<?>) JsonPath.read(mapa, "$.curva")).hasSizeGreaterThanOrEqualTo(12);
        assertThat(JsonPath.<Integer>read(mapa, "$.curva[-1].casos")).isEqualTo(2);

        // Agravo sensível: conta na microárea, nunca vira ponto.
        String violencia = agravoRepository.findByCodigo("VIOLENCIA_INTERPESSOAL_AUTOPROVOCADA").orElseThrow().getUuid().toString();
        notificar(jose, violencia, ", \"latitude\": %s, \"longitude\": %s, \"respostas\": {\"tipoViolencia\": \"Física\", \"lesaoAutoprovocada\": false}"
                .formatted(lat + 0.003, lon + 0.003), 201);
        mapa = ler(URL + "mapa?agravoId=" + violencia + "&unidadeId=" + ubs.getUuid());
        assertThat((List<Integer>) JsonPath.read(mapa, "$.microareas[?(@.codigo == '07')].casos")).containsExactly(1);
        assertThat((List<?>) JsonPath.read(mapa, "$.pontos")).isEmpty();
    }
}
