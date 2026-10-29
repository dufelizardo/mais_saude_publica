package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Leito;
import com.edufelizardo.maissaudepublica.models.Setor;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.SexoLeito;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoLeito;
import com.edufelizardo.maissaudepublica.models.enuns.TipoLeito;
import com.edufelizardo.maissaudepublica.models.enuns.TipoSetor;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.LeitoRepository;
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
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Emergências e Desastres, fatia E1 (ADR-0156): catálogos de tipo e nível, o evento (monitorar, descartar, converter), a
 * emergência com o nível em histórico, o território (polígono e microáreas atingidas), as referências legais, a
 * avaliação de impacto com as sugestões dos donos e o encerramento.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EmergenciaControllerTest {

    private static final String PREFIXO = "Emerg Teste ";
    /** Um quadrado longe das unidades de outros testes (Mato Grosso). */
    private static final String AREA = poligono(-55.40, -10.20, -55.30, -10.10);

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private UnidadeDeSaudeRepository unidadeDeSaudeRepository;
    @Autowired private SetorRepository setorRepository;
    @Autowired private LeitoRepository leitoRepository;

    private String tipoInundacao;
    private String nivel1;
    private String nivel2;
    private String microareaDentro;
    private String microareaFora;
    private UnidadeDeSaude ubs;

    @BeforeEach
    void seed() {
        limpar();
        tipoInundacao = jdbc.queryForObject("select cast(uuid as varchar) from tb_tipo_emergencia where codigo = 'INUNDACAO'", String.class);
        nivel1 = jdbc.queryForObject("select cast(uuid as varchar) from tb_nivel_emergencia where ordem = 1", String.class);
        nivel2 = jdbc.queryForObject("select cast(uuid as varchar) from tb_nivel_emergencia where ordem = 2", String.class);
        String segmento = jdbc.queryForObject("insert into tb_segmento_territorial (uuid, codigo, nome, tipo) values (gen_random_uuid(), 'EMG1-SEG', "
                + "'Segmento Teste', 'DISTRITO_SANITARIO') returning cast(uuid as varchar)", String.class);
        String territorio = jdbc.queryForObject("insert into tb_territorio (uuid, segmento_id, nome, finalidade, ativo) values (gen_random_uuid(), "
                + "cast(? as uuid), ?, 'ATENCAO_PRIMARIA', true) returning cast(uuid as varchar)", String.class, segmento, PREFIXO + "Território");
        String area = jdbc.queryForObject("insert into tb_area (uuid, territorio_id, codigo, nome) values (gen_random_uuid(), cast(? as uuid), 'EMG1-AR', "
                + "'Área Norte') returning cast(uuid as varchar)", String.class, territorio);
        microareaDentro = jdbc.queryForObject("insert into tb_microarea (uuid, area_id, codigo, nome, geometria, populacao_referencia) values "
                + "(gen_random_uuid(), cast(? as uuid), 'EMG1-01', 'Ribeirinha', ?, 500) returning cast(uuid as varchar)", String.class, area,
                poligono(-55.38, -10.18, -55.32, -10.12));
        microareaFora = jdbc.queryForObject("insert into tb_microarea (uuid, area_id, codigo, nome, geometria, populacao_referencia) values "
                + "(gen_random_uuid(), cast(? as uuid), 'EMG1-02', 'Serra', ?, 300) returning cast(uuid as varchar)", String.class, area,
                poligono(-55.10, -10.00, -55.00, -9.90));
        ubs = new UnidadeDeSaude();
        ubs.setNome(PREFIXO + "UBS Ribeirinha");
        ubs.setTipo(TipoUnidadeDeSaude.UBS);
        ubs.setAtivo(true);
        ubs.setLatitude(-10.15);
        ubs.setLongitude(-55.35);
        ubs = unidadeDeSaudeRepository.save(ubs);
        Setor setor = setorRepository.save(new Setor(ubs, PREFIXO + "Observação", "EMG1-OBS", TipoSetor.ASSISTENCIAL, true, null));
        leito(setor, "OBS 01", SituacaoLeito.BLOQUEADO);
        leito(setor, "OBS 02", SituacaoLeito.LIVRE);
    }

    @AfterEach
    void limpar() {
        String emergencias = "(select e.uuid from tb_emergencia e join tb_evento_emergencial v on v.uuid = e.evento_id where v.descricao like '" + PREFIXO + "%')";
        jdbc.update("delete from tb_avaliacao_impacto_unidade where avaliacao_id in (select uuid from tb_avaliacao_impacto where emergencia_id in "
                + emergencias + ")");
        jdbc.update("delete from tb_avaliacao_impacto where emergencia_id in " + emergencias);
        jdbc.update("delete from tb_mudanca_nivel_emergencia where emergencia_id in " + emergencias);
        jdbc.update("delete from tb_emergencia_microarea where emergencia_id in " + emergencias);
        jdbc.update("delete from tb_emergencia where uuid in " + emergencias);
        jdbc.update("delete from tb_evento_emergencial where descricao like '" + PREFIXO + "%'");
        jdbc.update("delete from tb_tipo_emergencia where codigo like 'EMG1_%'");
        jdbc.update("delete from tb_nivel_emergencia where ordem = 9");
        jdbc.update("delete from tb_microarea where codigo like 'EMG1-%'");
        jdbc.update("delete from tb_area where codigo = 'EMG1-AR'");
        jdbc.update("delete from tb_territorio where nome = '" + PREFIXO + "Território'");
        jdbc.update("delete from tb_segmento_territorial where codigo = 'EMG1-SEG'");
        String unidades = "(select uuid from tb_unidade_de_saude where nome like '" + PREFIXO + "%')";
        jdbc.update("delete from tb_evento_leito where leito_id in (select uuid from tb_leito where unidade_id in " + unidades + ")");
        jdbc.update("delete from tb_leito where unidade_id in " + unidades);
        jdbc.update("delete from tb_setor where unidade_id in " + unidades);
        jdbc.update("delete from tb_unidade_de_saude where nome like '" + PREFIXO + "%'");
    }

    private static String poligono(double x1, double y1, double x2, double y2) {
        return "{\"type\":\"Polygon\",\"coordinates\":[[[" + x1 + "," + y1 + "],[" + x2 + "," + y1 + "],[" + x2 + "," + y2 + "],[" + x1 + "," + y2 + "],["
                + x1 + "," + y1 + "]]]}";
    }

    private void leito(Setor setor, String nome, SituacaoLeito situacao) {
        Leito l = new Leito();
        l.setUnidade(ubs);
        l.setSetor(setor);
        l.setIdentificacao(nome);
        l.setTipo(TipoLeito.CLINICO);
        l.setSexo(SexoLeito.MISTO);
        l.setSituacao(situacao);
        l.setAtivo(true);
        leitoRepository.save(l);
    }

    private String enviar(MockHttpServletRequestBuilder r, String corpo, int esperado) throws Exception {
        return mockMvc.perform(r.contentType(MediaType.APPLICATION_JSON).content(corpo)).andExpect(status().is(esperado)).andReturn().getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
    }

    private String ler(String url) throws Exception {
        return mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    private static String json(String s) {
        return s.replace("\"", "\\\"");
    }

    private String evento(String geometria) throws Exception {
        String corpo = "{\"tipoId\": \"" + tipoInundacao + "\", \"fonte\": \"DEFESA_CIVIL\", \"local\": \"Bairro Ribeirinho\", \"descricao\": \"" + PREFIXO
                + "Rio transbordou\"" + (geometria != null ? ", \"geometria\": \"" + json(geometria) + "\"" : "") + "}";
        return JsonPath.read(enviar(post("/api/v1/evento-emergencial"), corpo, 201), "$.uuid");
    }

    @Test
    void catalogosDeTipoENivel() throws Exception {
        List<Map<String, Object>> inundacao = JsonPath.read(ler("/api/v1/emergencias/tipos"), "$[?(@.codigo == 'INUNDACAO')]");
        assertThat(inundacao.get(0)).containsEntry("cobrade", "1.2.1.0.0").containsEntry("categoria", "DESASTRE");
        assertThat((List<Integer>) JsonPath.read(ler("/api/v1/emergencias/niveis"), "$[*].ordem")).containsSequence(0, 1, 2, 3);

        String r = enviar(post("/api/v1/emergencias/tipo"), "{\"codigo\": \"emg1_vendaval\", \"nome\": \"Vendaval\", \"categoria\": \"DESASTRE\", "
                + "\"cobrade\": \"1.3.2.1.5\"}", 201);
        assertThat((String) JsonPath.read(r, "$.codigo")).isEqualTo("EMG1_VENDAVAL");
        enviar(post("/api/v1/emergencias/tipo"), "{\"codigo\": \"EMG1_VENDAVAL\", \"nome\": \"Outro\", \"categoria\": \"DESASTRE\"}", 409);
        String id = JsonPath.read(r, "$.uuid");
        enviar(put("/api/v1/emergencias/tipo/" + id), "{\"codigo\": \"EMG1_OUTRO\", \"nome\": \"Vendaval\", \"categoria\": \"DESASTRE\"}", 422);
        r = enviar(put("/api/v1/emergencias/tipo/" + id), "{\"codigo\": \"EMG1_VENDAVAL\", \"nome\": \"Vendaval forte\", \"categoria\": \"DESASTRE\", "
                + "\"ativo\": false}", 200);
        assertThat((Boolean) JsonPath.read(r, "$.ativo")).isFalse();
        // Tipo inativo não recebe evento novo.
        enviar(post("/api/v1/evento-emergencial"), "{\"tipoId\": \"" + id + "\", \"fonte\": \"GESTOR\", \"descricao\": \"" + PREFIXO + "x\"}", 422);

        enviar(post("/api/v1/emergencias/nivel"), "{\"ordem\": 1, \"nome\": \"Repetido\"}", 409);
        enviar(post("/api/v1/emergencias/nivel"), "{\"ordem\": 9, \"nome\": \"Nível 9\"}", 201);
    }

    @Test
    void eventoMonitoradoDescartadoOuConvertido() throws Exception {
        enviar(post("/api/v1/evento-emergencial"), "{\"tipoId\": \"" + tipoInundacao + "\", \"fonte\": \"GESTOR\", \"descricao\": \"" + PREFIXO
                + "Futuro\", \"ocorridoEm\": \"" + Instant.now().plusSeconds(3600) + "\"}", 400);
        enviar(post("/api/v1/evento-emergencial"), "{\"tipoId\": \"" + tipoInundacao + "\", \"fonte\": \"GESTOR\", \"descricao\": \"" + PREFIXO
                + "Área ruim\", \"geometria\": \"{\\\"type\\\":\\\"Polygon\\\"}\"}", 400);

        String descartado = evento(null);
        enviar(post("/api/v1/evento-emergencial/" + descartado + "/situacao"), "{\"situacao\": \"DESCARTADO\"}", 400);
        enviar(post("/api/v1/evento-emergencial/" + descartado + "/situacao"), "{\"situacao\": \"DESCARTADO\", \"motivo\": \"Alarme falso\"}", 200);
        enviar(post("/api/v1/evento-emergencial/" + descartado + "/emergencia"), "{\"nome\": \"X\", \"nivelId\": \"" + nivel1
                + "\", \"justificativa\": \"J\"}", 422);

        String id = evento(null);
        String r = enviar(post("/api/v1/evento-emergencial/" + id + "/situacao"), "{\"situacao\": \"MONITORADO\"}", 200);
        assertThat((String) JsonPath.read(r, "$.situacao")).isEqualTo("MONITORADO");
        r = enviar(post("/api/v1/evento-emergencial/" + id + "/emergencia"), "{\"nome\": \"Enchente Norte\", \"nivelId\": \"" + nivel1
                + "\", \"justificativa\": \"Três bairros alagados\"}", 201);
        assertThat((String) JsonPath.read(r, "$.situacao")).isEqualTo("ATIVA");
        assertThat((String) JsonPath.read(r, "$.tipoNome")).isEqualTo("Inundação");
        assertThat((List<Object>) JsonPath.read(r, "$.niveis")).hasSize(1);
        assertThat((Object) JsonPath.read(r, "$.niveis[0].nivelAnterior")).isNull();
        String emergencia = JsonPath.read(r, "$.uuid");

        r = ler("/api/v1/evento-emergencial/" + id);
        assertThat((String) JsonPath.read(r, "$.situacao")).isEqualTo("CONVERTIDO");
        assertThat((String) JsonPath.read(r, "$.emergenciaId")).isEqualTo(emergencia);
        enviar(post("/api/v1/evento-emergencial/" + id + "/emergencia"), "{\"nome\": \"De novo\", \"nivelId\": \"" + nivel1
                + "\", \"justificativa\": \"J\"}", 409);
        enviar(post("/api/v1/evento-emergencial/" + id + "/situacao"), "{\"situacao\": \"MONITORADO\"}", 422);
        assertThat((List<String>) JsonPath.read(ler("/api/v1/emergencias?situacao=ATIVA"), "$[*].uuid")).contains(emergencia);
    }

    @Test
    void nivelTerritorioImpactoEEncerramento() throws Exception {
        String id = evento(AREA);
        String r = enviar(post("/api/v1/evento-emergencial/" + id + "/emergencia"), "{\"nome\": \"Enchente Ribeirinha\", \"nivelId\": \"" + nivel1
                + "\", \"justificativa\": \"Rio acima da cota\"}", 201);
        String em = JsonPath.read(r, "$.uuid");
        // Sem território próprio, a área do evento e as microáreas que ela atinge.
        assertThat((List<String>) JsonPath.read(r, "$.microareas[*].uuid")).contains(microareaDentro).doesNotContain(microareaFora);

        String s = ler("/api/v1/emergencia/" + em + "/sugestao-impacto");
        assertThat((Integer) JsonPath.read(s, "$.populacaoAfetada")).isEqualTo(500);
        List<Map<String, Object>> naArea = JsonPath.read(s, "$.unidades[?(@.uuid == '" + ubs.getUuid() + "')]");
        assertThat(naArea.get(0)).containsEntry("leitos", 2).containsEntry("leitosIndisponiveis", 1).containsEntry("situacaoOperacional", "EM_OPERACAO");

        // A lista explícita vale sobre a sugestão.
        r = enviar(put("/api/v1/emergencia/" + em + "/territorio"), "{\"geometria\": \"" + json(AREA) + "\", \"microareaIds\": [\"" + microareaFora
                + "\"]}", 200);
        assertThat((List<String>) JsonPath.read(r, "$.microareas[*].uuid")).containsExactly(microareaFora);

        r = enviar(post("/api/v1/emergencia/" + em + "/nivel"), "{\"nivelId\": \"" + nivel2 + "\", \"justificativa\": \"Hospital atingido\"}", 200);
        assertThat((Integer) JsonPath.read(r, "$.nivelOrdem")).isEqualTo(2);
        assertThat((String) JsonPath.read(r, "$.niveis[0].nivelAnterior")).isEqualTo("Nível 1 · Resposta local");
        enviar(post("/api/v1/emergencia/" + em + "/nivel"), "{\"nivelId\": \"" + nivel2 + "\", \"justificativa\": \"De novo\"}", 422);

        enviar(put("/api/v1/emergencia/" + em + "/referencias-legais"), "{\"espinNumero\": \"1/2026\"}", 400);
        enviar(put("/api/v1/emergencia/" + em + "/referencias-legais"), "{\"decretoTipo\": \"SITUACAO_DE_EMERGENCIA\"}", 400);
        r = enviar(put("/api/v1/emergencia/" + em + "/referencias-legais"), "{\"decretoTipo\": \"SITUACAO_DE_EMERGENCIA\", \"decretoNumero\": \"123/2026\", "
                + "\"decretoData\": \"" + LocalDate.now() + "\"}", 200);
        assertThat((String) JsonPath.read(r, "$.decretoNumero")).isEqualTo("123/2026");

        r = enviar(post("/api/v1/emergencia/" + em + "/avaliacao-impacto"), "{\"populacaoAfetada\": 18000, \"unidadeIds\": [\"" + ubs.getUuid()
                + "\"], \"leitosIndisponiveis\": 1, \"medicamentosCriticos\": \"Soro de reidratação\", \"nivelImpacto\": \"ALTO\"}", 201);
        assertThat((String) JsonPath.read(r, "$.avaliacoes[0].nivelImpacto")).isEqualTo("ALTO");
        assertThat((List<String>) JsonPath.read(r, "$.avaliacoes[0].unidadesAfetadas")).containsExactly(PREFIXO + "UBS Ribeirinha");
        enviar(post("/api/v1/emergencia/" + em + "/avaliacao-impacto"), "{\"populacaoAfetada\": -1, \"nivelImpacto\": \"ALTO\"}", 400);
        List<Map<String, Object>> naLista = JsonPath.read(ler("/api/v1/emergencias"), "$[?(@.uuid == '" + em + "')]");
        assertThat(naLista.get(0)).containsEntry("ultimoImpacto", "ALTO");

        enviar(post("/api/v1/emergencia/" + em + "/encerramento"), "{}", 400);
        r = enviar(post("/api/v1/emergencia/" + em + "/encerramento"), "{\"motivo\": \"Águas baixaram; rede normalizada\"}", 200);
        assertThat((String) JsonPath.read(r, "$.situacao")).isEqualTo("ENCERRADA");
        enviar(post("/api/v1/emergencia/" + em + "/nivel"), "{\"nivelId\": \"" + nivel1 + "\", \"justificativa\": \"J\"}", 422);
        enviar(post("/api/v1/emergencia/" + em + "/avaliacao-impacto"), "{\"nivelImpacto\": \"BAIXO\"}", 422);
    }
}
