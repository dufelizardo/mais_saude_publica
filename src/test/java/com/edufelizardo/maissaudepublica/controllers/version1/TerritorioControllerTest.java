package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Cargo;
import com.edufelizardo.maissaudepublica.models.CategoriaSalarial;
import com.edufelizardo.maissaudepublica.models.Lotacao;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.CargoRepository;
import com.edufelizardo.maissaudepublica.repositories.CategoriaSalarialRepository;
import com.edufelizardo.maissaudepublica.repositories.LotacaoRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Território, fatia F1 (ADR-0125): GeoJSON validado, microárea dentro da área e sem sobreposição, equipe da área e ACS
 * da microárea como histórico, Equipes lendo do Território, a migração dos textos livres e as coordenadas da unidade.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TerritorioControllerTest {

    private static final String URL = "/api/v1/territorio/";
    private static final String PREFIXO = "Territorio Teste ";
    private static final String CODIGO = "TSTF1";

    /** Quadrado de 0,01° (cerca de 1,1 km) a partir do canto. */
    static String quadrado(double lon, double lat, double lado) {
        return "{\"type\":\"Polygon\",\"coordinates\":[[[%s,%s],[%s,%s],[%s,%s],[%s,%s],[%s,%s]]]}".formatted(
                lon, lat, lon + lado, lat, lon + lado, lat + lado, lon, lat + lado, lon, lat);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private PlatformTransactionManager transacoes;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private CategoriaSalarialRepository categoriaSalarialRepository;

    @Autowired
    private CargoRepository cargoRepository;

    @Autowired
    private LotacaoRepository lotacaoRepository;

    private UnidadeDeSaude ubs;

    @BeforeEach
    void seed() {
        limpar();
        ubs = unidade("UBS Jardim Esperança");
        CategoriaSalarial categoria = new CategoriaSalarial();
        categoria.setNome(PREFIXO + "Categoria");
        categoria = categoriaSalarialRepository.save(categoria);
        Cargo cargo = cargoRepository.save(new Cargo(categoria, PREFIXO + "Cargo"));
        for (String m : List.of("MED", "ACS1", "ACS2", "ACS3", "ACS4")) {
            lotacaoRepository.save(new Lotacao(profissional(m), ubs, cargo, 40, LocalDate.now().minusYears(1), "admissão"));
        }
    }

    @AfterEach
    void limpar() {
        limparDados(jdbc, PREFIXO, CODIGO);
        List<UUID> unidades = unidadeDeSaudeRepository.findAll().stream()
                .filter(u -> u.getNome() != null && u.getNome().startsWith(PREFIXO)).map(UnidadeDeSaude::getUuid).toList();
        for (UUID u : unidades) {
            jdbc.update("delete from tb_membro_equipe where equipe_id in (select uuid from tb_equipe where unidade_id = ?)", u);
            jdbc.update("delete from tb_equipe where unidade_id = ?", u);
            jdbc.update("delete from tb_lotacao where unidade_id = ?", u);
        }
        unidadeDeSaudeRepository.deleteAllById(unidades);
        profissionalRepository.deleteAll(profissionalRepository.findAll().stream()
                .filter(p -> p.getMatricula() != null && p.getMatricula().startsWith("TER-")).toList());
        jdbc.update("delete from tb_cargo where nome like ?", PREFIXO + "%");
        jdbc.update("delete from tb_categoria_salarial where nome like ?", PREFIXO + "%");
    }

    /** Apaga o território dos testes: os segmentos com o código começando em {@code codigo} e as equipes do prefixo. */
    static void limparDados(JdbcTemplate jdbc, String prefixo, String codigo) {
        String areas = "select a.uuid from tb_area a join tb_territorio t on t.uuid = a.territorio_id "
                + "join tb_segmento_territorial s on s.uuid = t.segmento_id where s.codigo like ?";
        String equipes = "select e.uuid from tb_equipe e join tb_unidade_de_saude u on u.uuid = e.unidade_id where u.nome like ?";
        // Adscrições (ADR-0128) das microáreas e das equipes do teste.
        jdbc.update("delete from tb_adscricao where microarea_id in (select uuid from tb_microarea where area_id in (" + areas + "))"
                + " or equipe_id in (" + equipes + ")", codigo + "%", prefixo + "%");
        jdbc.update("delete from tb_atribuicao_microarea where microarea_id in (select uuid from tb_microarea where area_id in (" + areas + "))"
                + " or membro_id in (select uuid from tb_membro_equipe where equipe_id in (" + equipes + "))", codigo + "%", prefixo + "%");
        jdbc.update("delete from tb_responsabilidade_territorial where area_id in (" + areas + ") or equipe_id in (" + equipes + ")",
                codigo + "%", prefixo + "%");
        // Cadastro territorial (ADR-0127): membros, famílias, domicílios e imóveis das microáreas.
        String imoveis = "select uuid from tb_imovel where microarea_id in (select uuid from tb_microarea where area_id in (" + areas + "))";
        String domicilios = "select uuid from tb_domicilio where imovel_id in (" + imoveis + ")";
        String nucleos = "select uuid from tb_nucleo_familiar where domicilio_id in (" + domicilios + ")";
        jdbc.update("delete from tb_membro_nucleo where nucleo_id in (" + nucleos + ")", codigo + "%");
        jdbc.update("delete from tb_nucleo_familiar where uuid in (" + nucleos + ")", codigo + "%");
        jdbc.update("delete from tb_domicilio where uuid in (" + domicilios + ")", codigo + "%");
        jdbc.update("delete from tb_imovel where uuid in (" + imoveis + ")", codigo + "%");
        jdbc.update("delete from tb_microarea where area_id in (" + areas + ")", codigo + "%");
        jdbc.update("delete from tb_area where uuid in (" + areas + ")", codigo + "%");
        jdbc.update("delete from tb_territorio where segmento_id in (select uuid from tb_segmento_territorial where codigo like ?)", codigo + "%");
        jdbc.update("delete from tb_segmento_territorial where codigo like ?", codigo + "%");
    }

    private UnidadeDeSaude unidade(String nome) {
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + nome);
        u.setTipo(TipoUnidadeDeSaude.UBS);
        u.setAtivo(true);
        return unidadeDeSaudeRepository.save(u);
    }

    private Profissional profissional(String sufixo) {
        Profissional p = new Profissional();
        p.setMatricula("TER-" + sufixo);
        p.setCpf(String.valueOf(91000000000L + Math.abs(sufixo.hashCode() % 999999)));
        p.setNome(PREFIXO + sufixo);
        p.setAtivo(true);
        return profissionalRepository.save(p);
    }

    private String enviar(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder r, String corpo, int esperado) throws Exception {
        return mockMvc.perform(r.contentType(MediaType.APPLICATION_JSON).content(corpo)).andExpect(status().is(esperado))
                .andReturn().getResponse().getContentAsString();
    }

    private UUID equipe(String nome, String microareas) throws Exception {
        String r = enviar(post("/api/v1/equipe/"), "{\"unidadeId\": \"%s\", \"tipo\": \"ESF\", \"nome\": \"%s\", \"microareas\": %s}"
                .formatted(ubs.getUuid(), nome, microareas == null ? "null" : "\"" + microareas + "\""), 201);
        return UUID.fromString(JsonPath.read(r, "$.resumo.uuid"));
    }

    /** Inclui o membro e devolve a participação dele. */
    private UUID membro(UUID equipe, String matricula, String funcao, String microarea) throws Exception {
        String r = enviar(post("/api/v1/equipe/" + equipe + "/membro"), "{\"profissionalMatricula\": \"TER-%s\", \"funcao\": \"%s\", \"microarea\": %s}"
                .formatted(matricula, funcao, microarea == null ? "null" : "\"" + microarea + "\""), 200);
        List<String> ids = JsonPath.read(r, "$.membros[?(@.matricula == 'TER-" + matricula + "')].uuid");
        return UUID.fromString(ids.get(0));
    }

    private UUID segmento(String sufixo) throws Exception {
        String r = enviar(post(URL + "segmento"), "{\"codigo\": \"%s\", \"nome\": \"%s\", \"tipo\": \"DISTRITO_SANITARIO\"}"
                .formatted(CODIGO + sufixo, PREFIXO + "Distrito " + sufixo), 201);
        return UUID.fromString(JsonPath.read(r, "$.uuid"));
    }

    private UUID territorio(UUID segmento) throws Exception {
        String r = enviar(post(URL + "territorio"), "{\"segmentoId\": \"%s\", \"nome\": \"%s\", \"finalidade\": \"ATENCAO_PRIMARIA\"}"
                .formatted(segmento, PREFIXO + "APS"), 201);
        return UUID.fromString(JsonPath.read(r, "$.uuid"));
    }

    private UUID area(UUID territorio, String codigo, String geometria) throws Exception {
        String r = enviar(post(URL + "area"), "{\"territorioId\": \"%s\", \"codigo\": \"%s\", \"nome\": \"Área %s\", \"geometria\": %s}"
                .formatted(territorio, codigo, codigo, texto(geometria)), 201);
        return UUID.fromString(JsonPath.read(r, "$.area.uuid"));
    }

    private ResultActions microarea(UUID area, String codigo, String geometria, Integer populacao) throws Exception {
        return mockMvc.perform(post(URL + "microarea").contentType(MediaType.APPLICATION_JSON).content(
                "{\"areaId\": \"%s\", \"codigo\": \"%s\", \"populacaoReferencia\": %s, \"geometria\": %s}".formatted(area, codigo, populacao,
                        texto(geometria))));
    }

    /** O texto como string JSON (com aspas), ou null. */
    private static String texto(String valor) {
        return valor == null ? "null" : "\"" + valor.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    private UUID microareaId(String areaDetalhe, String codigo) {
        List<String> ids = JsonPath.read(areaDetalhe, "$.area.microareas[?(@.codigo == '" + codigo + "')].uuid");
        return UUID.fromString(ids.get(0));
    }

    @Test
    void cadastroComGeoJsonDentroDaAreaESemSobreposicao() throws Exception {
        UUID s = segmento("A");
        enviar(post(URL + "segmento"), "{\"codigo\": \"%sA\", \"nome\": \"Outro\", \"tipo\": \"OUTRO\"}".formatted(CODIGO), 409);
        UUID t = territorio(s);
        enviar(post(URL + "territorio"), "{\"segmentoId\": \"%s\", \"nome\": \"%s\", \"finalidade\": \"VIGILANCIA\"}".formatted(s, PREFIXO + "APS"), 409);

        // GeoJSON inválido: tipo, anel aberto, fora do WGS84, autointerseção.
        String base = "{\"territorioId\": \"" + t + "\", \"codigo\": \"X\", \"nome\": \"X\", \"geometria\": \"%s\"}";
        enviar(post(URL + "area"), base.formatted("{\\\"type\\\":\\\"Point\\\",\\\"coordinates\\\":[0,0]}"), 400);
        enviar(post(URL + "area"), base.formatted("{\\\"type\\\":\\\"Polygon\\\",\\\"coordinates\\\":[[[0,0],[1,0],[1,1],[0,1]]]}"), 400);
        enviar(post(URL + "area"), base.formatted("{\\\"type\\\":\\\"Polygon\\\",\\\"coordinates\\\":[[[0,0],[200,0],[200,1],[0,0]]]}"), 400);
        enviar(post(URL + "area"), base.formatted("{\\\"type\\\":\\\"Polygon\\\",\\\"coordinates\\\":[[[0,0],[1,1],[1,0],[0,1],[0,0]]]}"), 400);
        enviar(post(URL + "area"), base.formatted("nao e json"), 400);

        UUID a = area(t, "01", quadrado(-47.0, -22.0, 0.02));
        mockMvc.perform(post(URL + "area").contentType(MediaType.APPLICATION_JSON).content("{\"territorioId\": \"%s\", \"codigo\": \"01\", \"nome\": \"Repetida\"}".formatted(t)))
                .andExpect(status().isConflict());

        // Dentro, encostada (aceita), fora e sobreposta (422); a Feature vale como geometria.
        microarea(a, "01", quadrado(-47.0, -22.0, 0.01), 800).andExpect(status().isCreated());
        String feature = "{\"type\":\"Feature\",\"properties\":{},\"geometry\":" + quadrado(-46.99, -22.0, 0.01) + "}";
        microarea(a, "02", feature, 500).andExpect(status().isCreated());
        microarea(a, "03", quadrado(-46.985, -21.985, 0.01), null).andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message", containsString("sai do contorno")));
        microarea(a, "03", quadrado(-46.995, -21.995, 0.004), null).andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message", containsString("sobrepõe")));
        microarea(a, "01", null, null).andExpect(status().isConflict());
        microarea(a, "03", null, null).andExpect(status().isCreated());

        // Encolher a área deixaria a 02 de fora.
        enviar(patch(URL + "area/" + a), "{\"territorioId\": \"%s\", \"codigo\": \"01\", \"nome\": \"Área 01\", \"geometria\": %s}"
                .formatted(t, texto(quadrado(-47.0, -22.0, 0.01))), 422);

        String arvore = mockMvc.perform(get(URL + "arvore")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String micro = "$.segmentos[?(@.codigo == 'TSTF1A')].territorios[*].areas[*].microareas";
        assertThat((List<String>) JsonPath.read(arvore, micro + "[*].codigo")).containsExactly("01", "02", "03");
        assertThat((List<String>) JsonPath.read(arvore, micro + "[?(@.codigo == '01')].avisos[*]"))
                .containsExactly("Sem ACS", "População de referência acima de 750 pessoas (CNES)");
        assertThat((List<String>) JsonPath.read(arvore, micro + "[?(@.codigo == '03')].avisos[*]")).containsExactly("Sem ACS", "Sem desenho");
        assertThat((Integer) JsonPath.read(arvore, "$.indicadores.acimaDaReferencia")).isGreaterThanOrEqualTo(1);
        String geometria = JsonPath.read(mockMvc.perform(get(URL + "area/" + a)).andReturn().getResponse().getContentAsString(),
                "$.area.microareas[1].geometria");
        assertThat(geometria).startsWith("{\"type\":\"Polygon\"");
    }

    @Test
    void equipeDaAreaEAcsDaMicroareaComoHistorico() throws Exception {
        UUID sf = equipe(PREFIXO + "SF-03", null);
        UUID med = membro(sf, "MED", "MEDICO", null);
        UUID acs1 = membro(sf, "ACS1", "ACS", null);
        UUID acs2 = membro(sf, "ACS2", "ACS", null);
        UUID outra = equipe(PREFIXO + "SF-04", null);
        UUID acs3 = membro(outra, "ACS3", "ACS", null);

        UUID t = territorio(segmento("B"));
        UUID a = area(t, "03", null);
        UUID a2 = area(t, "04", null);
        String d = microarea(a, "07", null, null).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        UUID m07 = microareaId(d, "07");
        d = microarea(a, "08", null, null).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        UUID m08 = microareaId(d, "08");

        // ACS antes da equipe: 422.
        enviar(post(URL + "microarea/" + m07 + "/atribuicao"), "{\"membroId\": \"%s\"}".formatted(acs1), 422);
        enviar(post(URL + "area/" + a + "/responsabilidade"), "{\"equipeId\": \"%s\", \"inicio\": \"%s\"}".formatted(sf, LocalDate.now().minusDays(10)), 200);
        enviar(post(URL + "area/" + a + "/responsabilidade"), "{\"equipeId\": \"%s\"}".formatted(sf), 409);
        enviar(post(URL + "area/" + a2 + "/responsabilidade"), "{\"equipeId\": \"%s\"}".formatted(sf), 409);

        // Quem pode ser o ACS: a função ACS, da equipe da área.
        enviar(post(URL + "microarea/" + m07 + "/atribuicao"), "{\"membroId\": \"%s\"}".formatted(med), 422);
        enviar(post(URL + "microarea/" + m07 + "/atribuicao"), "{\"membroId\": \"%s\"}".formatted(acs3), 422);
        enviar(post(URL + "microarea/" + m07 + "/atribuicao"), "{\"membroId\": \"%s\"}".formatted(acs1), 200);
        enviar(post(URL + "microarea/" + m08 + "/atribuicao"), "{\"membroId\": \"%s\"}".formatted(acs1), 409);
        enviar(post(URL + "microarea/" + m07 + "/atribuicao"), "{\"membroId\": \"%s\"}".formatted(acs1), 409);
        // Troca de ACS: com motivo.
        enviar(post(URL + "microarea/" + m07 + "/atribuicao"), "{\"membroId\": \"%s\"}".formatted(acs2), 400);
        String r = enviar(post(URL + "microarea/" + m07 + "/atribuicao"), "{\"membroId\": \"%s\", \"motivo\": \"Redivisão da área\"}".formatted(acs2), 200);
        assertThat((List<?>) JsonPath.read(r, "$.atribuicoes")).hasSize(2);
        assertThat((String) JsonPath.read(r, "$.area.microareas[0].acs.matricula")).isEqualTo("TER-ACS2");
        assertThat((List<String>) JsonPath.read(r, "$.acsSemMicroarea[*].matricula")).containsExactly("TER-ACS1");
        enviar(post(URL + "microarea/" + m08 + "/atribuicao"), "{\"membroId\": \"%s\"}".formatted(acs1), 200);

        // Equipes lê do Território e recusa a escrita do texto.
        mockMvc.perform(get("/api/v1/equipe/" + sf)).andExpect(status().isOk())
                .andExpect(jsonPath("$.resumo.microareas").value("07, 08"))
                .andExpect(jsonPath("$.resumo.microareasPeloTerritorio").value(true))
                .andExpect(jsonPath("$.membros[?(@.matricula == 'TER-ACS2')].microarea").value(hasItem("07")));
        mockMvc.perform(get("/api/v1/territorio/equipe/" + sf)).andExpect(status().isOk()).andExpect(jsonPath("$.area.codigo").value("03"));
        enviar(patch("/api/v1/equipe/" + sf), "{\"unidadeId\": \"%s\", \"tipo\": \"ESF\", \"nome\": \"%s\", \"microareas\": \"07, 09\"}".formatted(ubs.getUuid(), PREFIXO + "SF-03"), 422);
        enviar(patch("/api/v1/equipe/" + sf), "{\"unidadeId\": \"%s\", \"tipo\": \"ESF\", \"nome\": \"%s\", \"microareas\": \"08,07\"}".formatted(ubs.getUuid(), PREFIXO + "SF-03"), 200);
        enviar(post("/api/v1/equipe/" + sf + "/membro"), "{\"profissionalMatricula\": \"TER-ACS4\", \"funcao\": \"ACS\", \"microarea\": \"09\"}", 422);

        // Saída do ACS fecha a microárea; encerrar sem ACS: 422.
        enviar(post("/api/v1/equipe/membro/" + acs1 + "/saida"), "{\"motivo\": \"Exoneração\"}", 200);
        r = mockMvc.perform(get(URL + "area/" + a)).andReturn().getResponse().getContentAsString();
        assertThat((Object) JsonPath.read(r, "$.area.microareas[1].acs")).isNull();
        enviar(post(URL + "microarea/" + m08 + "/atribuicao/encerrar"), "{\"motivo\": \"x\"}", 422);
        enviar(post(URL + "microarea/" + m07 + "/atribuicao/encerrar"), "{\"motivo\": \"Licença longa\"}", 200);

        // Troca de equipe: com motivo; os ACS da anterior saem.
        enviar(post(URL + "microarea/" + m07 + "/atribuicao"), "{\"membroId\": \"%s\"}".formatted(acs2), 200);
        enviar(post(URL + "area/" + a + "/responsabilidade"), "{\"equipeId\": \"%s\"}".formatted(outra), 400);
        r = enviar(post(URL + "area/" + a + "/responsabilidade"), "{\"equipeId\": \"%s\", \"motivo\": \"Reorganização da UBS\"}".formatted(outra), 200);
        assertThat((String) JsonPath.read(r, "$.area.equipe.nome")).isEqualTo(PREFIXO + "SF-04");
        assertThat((Object) JsonPath.read(r, "$.area.microareas[0].acs")).isNull();
        assertThat((List<?>) JsonPath.read(r, "$.responsabilidades")).hasSize(2);
        assertThat((List<String>) JsonPath.read(r, "$.acsSemMicroarea[*].matricula")).containsExactly("TER-ACS3");
        mockMvc.perform(get("/api/v1/equipe/" + sf)).andExpect(jsonPath("$.resumo.microareasPeloTerritorio").value(false));
    }

    @Test
    void migracaoDoTextoLivreViraMicroareasEAtribuicoes() throws Exception {
        UUID sf = equipe(PREFIXO + "SF-09", "14, 17");
        membro(sf, "ACS1", "ACS", "17");
        membro(sf, "ACS2", "ACS", "17");
        UUID acs3 = membro(sf, "ACS3", "ACS", "21");
        enviar(post("/api/v1/equipe/membro/" + acs3 + "/saida"), "{\"motivo\": \"Transferência\"}", 200);

        String sql = new String(new ClassPathResource("db/migration/V19__territorio.sql").getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        String bloco = sql.substring(sql.indexOf("-- ── Migração do texto livre"));
        new TransactionTemplate(transacoes).executeWithoutResult(tx -> {
            try {
                jdbc.execute(bloco);
                assertThat(jdbc.queryForList("select m.codigo from tb_microarea m join tb_responsabilidade_territorial r on r.area_id = m.area_id "
                        + "where r.equipe_id = ? and r.fim is null order by m.codigo", String.class, sf)).containsExactly("14", "17", "21");
                assertThat(jdbc.queryForObject("select count(*) from tb_atribuicao_microarea a join tb_membro_equipe m on m.uuid = a.membro_id "
                        + "where m.equipe_id = ? and a.fim is null", Integer.class, sf)).isEqualTo(1);
                assertThat(jdbc.queryForObject("select motivo_fim from tb_atribuicao_microarea where membro_id = ?", String.class, acs3))
                        .isEqualTo("Saída da equipe: Transferência");
                // Equipes já lê do Território, na mesma transação.
                mockMvc.perform(get("/api/v1/equipe/" + sf)).andExpect(status().isOk())
                        .andExpect(jsonPath("$.resumo.microareas").value("14, 17, 21"))
                        .andExpect(jsonPath("$.resumo.microareasPeloTerritorio").value(true))
                        // Dois ACS vigentes com o mesmo código: fica um, o outro segue sem microárea.
                        .andExpect(jsonPath("$.membros[?(@.funcao == 'ACS')].microarea").value(org.hamcrest.Matchers.containsInAnyOrder("17", null)));
            } catch (Exception e) {
                throw new IllegalStateException(e);
            } finally {
                tx.setRollbackOnly();
            }
        });
    }

    @Test
    void coordenadasDaUnidade() throws Exception {
        enviar(put(URL + "unidade/" + ubs.getUuid() + "/coordenadas"), "{\"latitude\": -22.9}", 400);
        enviar(put(URL + "unidade/" + ubs.getUuid() + "/coordenadas"), "{\"latitude\": -95, \"longitude\": -47}", 400);
        enviar(put(URL + "unidade/" + ubs.getUuid() + "/coordenadas"), "{\"latitude\": -22.9, \"longitude\": -47.06}", 200);
        mockMvc.perform(get(URL + "unidades")).andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.uuid == '" + ubs.getUuid() + "')].latitude").value(hasItem(-22.9)));
    }
}
