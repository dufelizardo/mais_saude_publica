package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
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
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Emergências, fatia E6a (ADR-0160): o perfil do tipo, a emergência planejada do evento de massa (dados, início da
 * operação, cancelamento), as estruturas temporárias com os boletins e o reflexo na desmobilização e no relatório.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EventosEspecializadosControllerTest {

    private static final String PREFIXO = "EvEsp Teste ";

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    private String eventoDeMassa;
    private String inundacao;
    private String nivel1;
    private UnidadeDeSaude upa;
    private final LocalDate hoje = LocalDate.now();

    @BeforeEach
    void seed() {
        limpar();
        eventoDeMassa = jdbc.queryForObject("select cast(uuid as varchar) from tb_tipo_emergencia where codigo = 'EVENTO_DE_MASSA'", String.class);
        inundacao = jdbc.queryForObject("select cast(uuid as varchar) from tb_tipo_emergencia where codigo = 'INUNDACAO'", String.class);
        nivel1 = jdbc.queryForObject("select cast(uuid as varchar) from tb_nivel_emergencia where ordem = 1", String.class);
        upa = new UnidadeDeSaude();
        upa.setNome(PREFIXO + "UPA");
        upa.setTipo(TipoUnidadeDeSaude.UPA);
        upa.setAtivo(true);
        upa = unidadeDeSaudeRepository.save(upa);
    }

    @AfterEach
    void limpar() {
        String emergencias = "(select e.uuid from tb_emergencia e join tb_evento_emergencial v on v.uuid = e.evento_id where v.descricao like '" + PREFIXO + "%')";
        jdbc.update("delete from tb_boletim_estrutura where estrutura_id in (select uuid from tb_estrutura_temporaria where emergencia_id in " + emergencias + ")");
        jdbc.update("delete from tb_estrutura_temporaria where emergencia_id in " + emergencias);
        jdbc.update("delete from tb_evento_massa where emergencia_id in " + emergencias);
        jdbc.update("delete from tb_recomendacao_relatorio where relatorio_id in (select uuid from tb_relatorio_pos_evento where emergencia_id in "
                + emergencias + ")");
        jdbc.update("delete from tb_relatorio_pos_evento where emergencia_id in " + emergencias);
        jdbc.update("delete from tb_desmobilizacao where emergencia_id in " + emergencias);
        jdbc.update("delete from tb_acao_emergencial where emergencia_id in " + emergencias);
        jdbc.update("delete from tb_mudanca_nivel_emergencia where emergencia_id in " + emergencias);
        jdbc.update("delete from tb_emergencia where uuid in " + emergencias);
        jdbc.update("delete from tb_evento_emergencial where descricao like '" + PREFIXO + "%'");
        jdbc.update("delete from tb_tipo_emergencia where codigo like 'EVESP_%'");
        jdbc.update("delete from tb_unidade_de_saude where nome like '" + PREFIXO + "%'");
    }

    private String enviar(MockHttpServletRequestBuilder r, String corpo, int esperado) throws Exception {
        return mockMvc.perform(r.contentType(MediaType.APPLICATION_JSON).content(corpo)).andExpect(status().is(esperado)).andReturn().getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
    }

    private String ler(String url) throws Exception {
        return mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    private String evento(String tipo, String descricao) throws Exception {
        return JsonPath.read(enviar(post("/api/v1/evento-emergencial"), "{\"tipoId\": \"" + tipo + "\", \"fonte\": \"GESTOR\", \"descricao\": \"" + PREFIXO
                + descricao + "\"}", 201), "$.uuid");
    }

    private String converter(String evento, boolean planejada, int esperado) throws Exception {
        return enviar(post("/api/v1/evento-emergencial/" + evento + "/emergencia"), "{\"nome\": \"Carnaval EvEsp\", \"nivelId\": \"" + nivel1
                + "\", \"justificativa\": \"Público acima de 50 mil\", \"planejada\": " + planejada + "}", esperado);
    }

    private String dadosDoEvento(int estimado, Integer maximo, Instant inicio, Instant fim) {
        return "{\"natureza\": \"FESTA_POPULAR\", \"local\": \"Avenida Central\", \"inicioPrevisto\": \"" + inicio + "\", \"fimPrevisto\": \"" + fim
                + "\", \"publicoEstimado\": " + estimado + (maximo != null ? ", \"publicoMaximo\": " + maximo : "")
                + ", \"organizador\": \"Liga das escolas\", \"contato\": \"(11) 3333-0000\", \"risco\": \"ALTO\"}";
    }

    private String estrutura(String tipo, String nome) {
        return "{\"tipo\": \"" + tipo + "\", \"nome\": \"" + nome + "\", \"local\": \"Praça da Sé\", \"latitude\": -23.55, \"longitude\": -46.63, "
                + "\"unidadeReferenciaId\": \"" + upa.getUuid() + "\", \"leitos\": 6, \"atendimentosDia\": 300, \"ambulancias\": 2, \"horario\": \"18h às 6h\"}";
    }

    @Test
    void perfilDoTipo() throws Exception {
        String tipos = ler("/api/v1/emergencias/tipos");
        assertThat((List<String>) JsonPath.read(tipos, "$[?(@.codigo == 'EVENTO_DE_MASSA')].perfil")).containsExactly("EVENTO_DE_MASSA");
        assertThat((List<String>) JsonPath.read(tipos, "$[?(@.codigo == 'EPIDEMIA')].perfil")).containsExactly("EPIDEMIA");
        assertThat((List<String>) JsonPath.read(tipos, "$[?(@.codigo == 'PRODUTO_PERIGOSO')].perfil")).containsExactly("DESASTRE_AMBIENTAL");
        assertThat((List<String>) JsonPath.read(tipos, "$[?(@.codigo == 'INUNDACAO')].perfil")).containsExactly("GERAL");
        // Sem o perfil, nasce geral; no ajuste sem ele, mantém.
        String t = enviar(post("/api/v1/emergencias/tipo"), "{\"codigo\": \"EVESP_SHOW\", \"nome\": \"Show\", \"categoria\": \"SAUDE\"}", 201);
        assertThat((String) JsonPath.read(t, "$.perfil")).isEqualTo("GERAL");
        String id = JsonPath.read(t, "$.uuid");
        t = enviar(put("/api/v1/emergencias/tipo/" + id), "{\"codigo\": \"EVESP_SHOW\", \"nome\": \"Show\", \"categoria\": \"SAUDE\", \"perfil\": \"EVENTO_DE_MASSA\"}", 200);
        assertThat((String) JsonPath.read(t, "$.perfil")).isEqualTo("EVENTO_DE_MASSA");
        t = enviar(put("/api/v1/emergencias/tipo/" + id), "{\"codigo\": \"EVESP_SHOW\", \"nome\": \"Show grande\", \"categoria\": \"SAUDE\"}", 200);
        assertThat((String) JsonPath.read(t, "$.perfil")).isEqualTo("EVENTO_DE_MASSA");
    }

    @Test
    void eventoDeMassaPlanejadoAteORelatorio() throws Exception {
        String em = JsonPath.read(converter(evento(eventoDeMassa, "Carnaval"), true, 201), "$.uuid");
        String base = "/api/v1/emergencia/" + em;
        String r = ler(base);
        assertThat((String) JsonPath.read(r, "$.situacao")).isEqualTo("PLANEJADA");
        assertThat((String) JsonPath.read(r, "$.perfil")).isEqualTo("EVENTO_DE_MASSA");

        // Planejada: nada da resposta ainda; a operação pede os dados do evento.
        enviar(post(base + "/acao"), "{\"descricao\": \"Abrir posto\", \"responsavel\": \"X\", \"prioridade\": \"ALTA\"}", 422);
        enviar(post(base + "/avaliacao-impacto"), "{\"nivelImpacto\": \"BAIXO\"}", 422);
        enviar(post(base + "/operacao"), "{\"justificativa\": \"Abertura dos portões\"}", 422);
        Instant inicio = Instant.now().plus(3, ChronoUnit.DAYS).truncatedTo(ChronoUnit.SECONDS);
        enviar(put(base + "/evento-massa"), dadosDoEvento(80000, null, inicio, inicio.minus(1, ChronoUnit.HOURS)), 400);
        enviar(put(base + "/evento-massa"), dadosDoEvento(80000, 50000, inicio, inicio.plus(12, ChronoUnit.HOURS)), 400);
        r = enviar(put(base + "/evento-massa"), dadosDoEvento(80000, 120000, inicio, inicio.plus(12, ChronoUnit.HOURS)), 200);
        assertThat((Integer) JsonPath.read(r, "$.eventoMassa.publicoEstimado")).isEqualTo(80000);
        assertThat((String) JsonPath.read(r, "$.eventoMassa.risco")).isEqualTo("ALTO");
        // Na lista, os dados do evento (a Sala de Situação avisa o que vem).
        assertThat((List<Integer>) JsonPath.read(ler("/api/v1/emergencias?situacao=PLANEJADA"), "$[?(@.uuid == '" + em + "')].eventoMassa.publicoEstimado"))
                .containsExactly(80000);

        // A estrutura nasce planejada; abre só com a operação iniciada; o boletim, só depois de aberta.
        enviar(post(base + "/estrutura"), estrutura("POSTO_MEDICO", "Posto 1").replace("\"longitude\": -46.63, ", ""), 400);
        r = enviar(post(base + "/estrutura"), estrutura("POSTO_MEDICO", "Posto 1"), 201);
        assertThat((List<String>) JsonPath.read(r, "$.estruturas[*].situacao")).containsExactly("PLANEJADA");
        assertThat((List<String>) JsonPath.read(r, "$.estruturas[*].unidadeReferenciaNome")).containsExactly(PREFIXO + "UPA");
        String posto = JsonPath.read(r, "$.estruturas[0].uuid");
        String est = base + "/estrutura/" + posto;
        enviar(post(est + "/situacao"), "{\"situacao\": \"EM_OPERACAO\"}", 422);
        enviar(post(est + "/boletim"), "{\"data\": \"" + hoje + "\", \"atendimentos\": 10}", 422);

        r = enviar(post(base + "/operacao"), "{\"justificativa\": \"Abertura dos portões\"}", 200);
        assertThat((String) JsonPath.read(r, "$.situacao")).isEqualTo("ATIVA");
        enviar(post(base + "/operacao"), "{\"justificativa\": \"De novo\"}", 422);
        r = enviar(post(est + "/situacao"), "{\"situacao\": \"EM_OPERACAO\"}", 200);
        assertThat((String) JsonPath.read(r, "$.estruturas[0].situacao")).isEqualTo("EM_OPERACAO");
        enviar(post(est + "/situacao"), "{\"situacao\": \"EM_OPERACAO\"}", 422);

        enviar(post(est + "/boletim"), "{\"data\": \"" + hoje.plusDays(1) + "\", \"atendimentos\": 10}", 400);
        enviar(post(est + "/boletim"), "{\"data\": \"" + hoje.minusDays(1) + "\", \"atendimentos\": 10}", 422);
        enviar(post(est + "/boletim"), "{\"data\": \"" + hoje + "\", \"atendimentos\": 120, \"remocoes\": 4, \"casosGraves\": 2}", 201);
        r = enviar(post(est + "/boletim"), "{\"data\": \"" + hoje + "\", \"atendimentos\": 80, \"remocoes\": 1, \"obitos\": 1, \"observacao\": \"Madrugada\"}", 201);
        assertThat((Integer) JsonPath.read(r, "$.estruturas[0].atendimentos")).isEqualTo(200);
        assertThat((Integer) JsonPath.read(r, "$.estruturas[0].remocoes")).isEqualTo(5);
        assertThat((List<Object>) JsonPath.read(r, "$.estruturas[0].boletins")).hasSize(2);
        // Uma segunda estrutura, ainda planejada, sai na desmobilização.
        enviar(post(base + "/estrutura"), estrutura("TENDA_HIDRATACAO", "Tenda 2"), 201);

        r = enviar(post(base + "/desmobilizacao"), "{\"responsavel\": \"Coordenação\", \"justificativa\": \"Fim do desfile\"}", 200);
        assertThat((Integer) JsonPath.read(r, "$.desmobilizacao.estruturasDesativadas")).isEqualTo(2);
        assertThat((List<String>) JsonPath.read(r, "$.estruturas[*].situacao")).containsOnly("DESATIVADA");
        // Nova estrutura não entra; o boletim do último dia ainda entra.
        enviar(post(base + "/estrutura"), estrutura("POSTO_MEDICO", "Posto 3"), 422);
        enviar(post(est + "/boletim"), "{\"data\": \"" + hoje + "\", \"atendimentos\": 5}", 201);

        String rel = ler(base + "/relatorio");
        assertThat((Integer) JsonPath.read(rel, "$.indicadores.estruturas")).isEqualTo(1);
        assertThat((Integer) JsonPath.read(rel, "$.indicadores.atendimentosEstruturas")).isEqualTo(205);
        assertThat((Integer) JsonPath.read(rel, "$.indicadores.remocoesEstruturas")).isEqualTo(5);
        assertThat((Integer) JsonPath.read(rel, "$.indicadores.casosGravesEstruturas")).isEqualTo(2);
        assertThat((Integer) JsonPath.read(rel, "$.indicadores.obitosEstruturas")).isEqualTo(1);
        assertThat((Integer) JsonPath.read(rel, "$.indicadores.publicoEstimado")).isEqualTo(80000);
        String csv = mockMvc.perform(get(base + "/relatorio.csv")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(csv).contains("Atendimentos nas estruturas;205").contains("Público estimado;80000");
    }

    @Test
    void planejadaSoNoEventoDeMassaECancelamento() throws Exception {
        converter(evento(inundacao, "Cheia"), true, 422);
        String enchente = JsonPath.read(converter(evento(inundacao, "Cheia 2"), false, 201), "$.uuid");
        Instant inicio = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        enviar(put("/api/v1/emergencia/" + enchente + "/evento-massa"), dadosDoEvento(1000, null, inicio, inicio.plus(1, ChronoUnit.HOURS)), 422);
        enviar(post("/api/v1/emergencia/" + enchente + "/operacao"), "{\"justificativa\": \"J\"}", 422);

        // O hospital de campanha no desastre: abre direto na emergência ativa; desativar pede o motivo.
        String r = enviar(post("/api/v1/emergencia/" + enchente + "/estrutura"), estrutura("HOSPITAL_DE_CAMPANHA", "Hospital de campanha Norte"), 201);
        String hc = "/api/v1/emergencia/" + enchente + "/estrutura/" + JsonPath.read(r, "$.estruturas[0].uuid");
        enviar(post(hc + "/situacao"), "{\"situacao\": \"EM_OPERACAO\"}", 200);
        enviar(post(hc + "/situacao"), "{\"situacao\": \"DESATIVADA\"}", 400);
        r = enviar(post(hc + "/situacao"), "{\"situacao\": \"DESATIVADA\", \"motivo\": \"Hospital regional reaberto\"}", 200);
        assertThat((String) JsonPath.read(r, "$.estruturas[0].motivoDesativacao")).isEqualTo("Hospital regional reaberto");
        enviar(put(hc), estrutura("HOSPITAL_DE_CAMPANHA", "Outro nome"), 422);
        enviar(post(hc + "/situacao"), "{\"situacao\": \"EM_OPERACAO\"}", 422);

        // A planejada cancelada: encerra sem operação; o relatório só começa depois.
        String show = JsonPath.read(converter(evento(eventoDeMassa, "Show"), true, 201), "$.uuid");
        enviar(put("/api/v1/emergencia/" + show + "/relatorio"), "{\"resumo\": \"R\"}", 422);
        r = enviar(post("/api/v1/emergencia/" + show + "/encerramento"), "{\"motivo\": \"Show cancelado pela organização\"}", 200);
        assertThat((String) JsonPath.read(r, "$.situacao")).isEqualTo("ENCERRADA");
        enviar(post("/api/v1/emergencia/" + show + "/operacao"), "{\"justificativa\": \"J\"}", 422);
    }
}
