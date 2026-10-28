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
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Imunização, fatia I3 (ADR-0149): a cobertura pelo cadastro (coorte, vacinadas, meta, homogeneidade e abandono) por
 * unidade, equipe e microárea; a busca ativa filtrada com o agente comunitário; a dose da campanha no andamento da meta,
 * com as vacinas da campanha; e a vacinação na Sala de Situação.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CoberturaVacinalControllerTest {

    private static final String PREFIXO = "Cobertura Teste ";
    private static final String MATRICULA = "P-IMU3-TEC";
    private static final String MATRICULA_ACS = "P-IMU3-ACS";

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private UnidadeDeSaudeRepository unidadeDeSaudeRepository;
    @Autowired private PacienteRepository pacienteRepository;
    @Autowired private ProfissionalRepository profissionalRepository;

    private final LocalDate hoje = LocalDate.now();
    private UnidadeDeSaude ubs;
    private UnidadeDeSaude outra;
    private String esfA;
    private String esfB;
    private String microarea;
    private int pacientes;

    @BeforeEach
    void seed() {
        limpar();
        ubs = unidade("UBS");
        outra = unidade("Outra");
        profissional(MATRICULA, "90077700001", "Técnica");
        Profissional acs = profissional(MATRICULA_ACS, "90077700002", "Agente");
        esfA = equipe(ubs, "ESF A");
        esfB = equipe(ubs, "ESF B");
        String segmento = jdbc.queryForObject("insert into tb_segmento_territorial (uuid, codigo, nome, tipo) values (gen_random_uuid(), 'IMU3-SEG', "
                + "'Segmento Teste', 'DISTRITO_SANITARIO') returning cast(uuid as varchar)", String.class);
        String territorio = jdbc.queryForObject("insert into tb_territorio (uuid, segmento_id, nome, finalidade, ativo) values (gen_random_uuid(), "
                + "cast(? as uuid), ?, 'ATENCAO_PRIMARIA', true) returning cast(uuid as varchar)", String.class, segmento, PREFIXO + "Território");
        String area = jdbc.queryForObject("insert into tb_area (uuid, territorio_id, codigo, nome) values (gen_random_uuid(), cast(? as uuid), 'IMU3-AR', "
                + "'Área Teste') returning cast(uuid as varchar)", String.class, territorio);
        microarea = jdbc.queryForObject("insert into tb_microarea (uuid, area_id, codigo, nome) values (gen_random_uuid(), cast(? as uuid), 'IMU3-MA', "
                + "'Microárea 01') returning cast(uuid as varchar)", String.class, area);
        String membro = jdbc.queryForObject("insert into tb_membro_equipe (uuid, equipe_id, profissional_id, funcao, inicio) values (gen_random_uuid(), "
                + "cast(? as uuid), ?, 'ACS', ?) returning cast(uuid as varchar)", String.class, esfA, acs.getUuid(), hoje.minusDays(30));
        jdbc.update("insert into tb_atribuicao_microarea (uuid, membro_id, microarea_id, inicio) values (gen_random_uuid(), cast(? as uuid), cast(? as uuid), ?)",
                membro, microarea, hoje.minusDays(30));
    }

    @AfterEach
    void limpar() {
        String unidades = "(select uuid from tb_unidade_de_saude where nome like '" + PREFIXO + "%')";
        String pacientesSql = "(select uuid from tb_paciente where nome like '" + PREFIXO + "%')";
        String campanhas = "(select uuid from tb_campanha where nome like '" + PREFIXO + "%')";
        jdbc.update("delete from tb_registro_vacinacao where paciente_id in " + pacientesSql);
        jdbc.update("delete from tb_campanha_imunobiologico where campanha_id in " + campanhas);
        jdbc.update("delete from tb_campanha where nome like '" + PREFIXO + "%'");
        jdbc.update("delete from tb_adscricao where paciente_id in " + pacientesSql);
        jdbc.update("delete from tb_atribuicao_microarea where microarea_id in (select uuid from tb_microarea where codigo = 'IMU3-MA')");
        jdbc.update("delete from tb_membro_equipe where equipe_id in (select uuid from tb_equipe where unidade_id in " + unidades + ")");
        jdbc.update("delete from tb_equipe where unidade_id in " + unidades);
        jdbc.update("delete from tb_microarea where codigo = 'IMU3-MA'");
        jdbc.update("delete from tb_area where codigo = 'IMU3-AR'");
        jdbc.update("delete from tb_territorio where nome = '" + PREFIXO + "Território'");
        jdbc.update("delete from tb_segmento_territorial where codigo = 'IMU3-SEG'");
        jdbc.update("delete from tb_paciente where nome like '" + PREFIXO + "%'");
        jdbc.update("delete from tb_profissional where matricula in ('" + MATRICULA + "', '" + MATRICULA_ACS + "')");
        jdbc.update("delete from tb_unidade_de_saude where nome like '" + PREFIXO + "%'");
    }

    private UnidadeDeSaude unidade(String nome) {
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + nome);
        u.setTipo(TipoUnidadeDeSaude.UBS);
        u.setAtivo(true);
        return unidadeDeSaudeRepository.save(u);
    }

    private Profissional profissional(String matricula, String cpf, String nome) {
        Profissional p = new Profissional();
        p.setMatricula(matricula);
        p.setCpf(cpf);
        p.setNome(PREFIXO + nome);
        p.setConselhoClasse("COREN");
        p.setAtivo(true);
        return profissionalRepository.save(p);
    }

    private String equipe(UnidadeDeSaude u, String nome) {
        return jdbc.queryForObject("insert into tb_equipe (uuid, unidade_id, nome, tipo, ativa) values (gen_random_uuid(), ?, ?, 'ESF', true) "
                + "returning cast(uuid as varchar)", String.class, u.getUuid(), PREFIXO + nome);
    }

    /** Uma criança adscrita à equipe (na microárea, se houver). */
    private Paciente crianca(LocalDate nascimento, String equipe, String micro) {
        pacientes++;
        Paciente p = new Paciente();
        p.setNome(PREFIXO + "Criança " + pacientes);
        p.setCpf(String.format("900777%05d", pacientes));
        p.setSexo(Sexo.MASCULINO);
        p.setDataNascimento(nascimento);
        p.setAtivo(true);
        p = pacienteRepository.save(p);
        jdbc.update("insert into tb_adscricao (uuid, paciente_id, equipe_id, microarea_id, origem, inicio) values (gen_random_uuid(), ?, cast(? as uuid), "
                + "cast(? as uuid), 'MANUAL', ?)", p.getUuid(), equipe, micro, hoje.minusDays(5));
        return p;
    }

    private String imuno(String codigo) {
        return jdbc.queryForObject("select cast(uuid as varchar) from tb_imunobiologico where codigo = ?", String.class, codigo);
    }

    private void transcrever(Paciente p, String codigo, String dose, LocalDate data) throws Exception {
        enviar(post("/api/v1/vacinacao"), ("{\"pacienteId\": \"%s\", \"imunobiologicoId\": \"%s\", \"dose\": \"%s\", \"unidadeId\": \"%s\", "
                + "\"origem\": \"TRANSCRITA\", \"aplicadaEm\": \"%s\"}").formatted(p.getUuid(), imuno(codigo), dose, ubs.getUuid(),
                data.atTime(LocalTime.of(9, 0))), 201);
    }

    private String aplicada(Paciente p, String codigo, String dose, String campanha) {
        return ("{\"pacienteId\": \"%s\", \"imunobiologicoId\": \"%s\", \"dose\": \"%s\", \"unidadeId\": \"%s\", \"origem\": \"APLICADA\", "
                + "\"profissionalMatricula\": \"%s\", \"lote\": \"L123\", \"campanhaId\": \"%s\"}")
                .formatted(p.getUuid(), imuno(codigo), dose, ubs.getUuid(), MATRICULA, campanha);
    }

    private String enviar(MockHttpServletRequestBuilder r, String corpo, int esperado) throws Exception {
        return mockMvc.perform(r.contentType(MediaType.APPLICATION_JSON).content(corpo)).andExpect(status().is(esperado)).andReturn().getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
    }

    private String ler(String url) throws Exception {
        return mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    private static Map<String, Object> vacina(String cobertura, String sigla, String dose) {
        List<Map<String, Object>> v = JsonPath.read(cobertura, "$.vacinas[?(@.sigla == '" + sigla + "' && @.dose == '" + dose + "')]");
        assertThat(v).hasSize(1);
        return v.get(0);
    }

    /** Três crianças de 10 meses: a completa, a que parou na 1ª penta e a sem dose; e uma de 30 meses, fora da coorte da penta. */
    private Paciente[] populacao() throws Exception {
        LocalDate dezMeses = hoje.minusMonths(10);
        Paciente completa = crianca(dezMeses, esfA, microarea);
        Paciente parou = crianca(dezMeses, esfA, microarea);
        Paciente semDose = crianca(dezMeses, esfB, null);
        Paciente maior = crianca(hoje.minusMonths(30), esfB, null);
        for (Paciente p : List.of(completa, parou, semDose)) {
            transcrever(p, "BCG", "UNICA", dezMeses.plusDays(1));
        }
        transcrever(completa, "PENTA", "PRIMEIRA", dezMeses.plusMonths(2));
        transcrever(completa, "PENTA", "SEGUNDA", dezMeses.plusMonths(4));
        transcrever(completa, "PENTA", "TERCEIRA", dezMeses.plusMonths(6));
        transcrever(parou, "PENTA", "PRIMEIRA", dezMeses.plusMonths(2));
        return new Paciente[]{completa, parou, semDose, maior};
    }

    @Test
    void coberturaHomogeneidadeEAbandonoPorUnidadeEquipeEMicroarea() throws Exception {
        populacao();
        // O calendário traz as doses de referência com a meta (dado, editável).
        String cal = ler("/api/v1/calendario-vacinal");
        assertThat((List<Object>) JsonPath.read(cal, "$[?(@.sigla == 'Penta' && @.dose == 'TERCEIRA')].metaCobertura")).containsExactly(95.0);
        assertThat((List<Object>) JsonPath.read(cal, "$[?(@.sigla == 'BCG')].medeCobertura")).containsExactly(true);

        // Unidade: a penta tem 3 na coorte (as de 10 meses), 1 vacinada; das 2 com a 1ª dose, 1 parou (abandono alto).
        String u = ler("/api/v1/vacinacao/cobertura?unidadeId=" + ubs.getUuid());
        assertThat((String) JsonPath.read(u, "$.nivel")).isEqualTo("UNIDADE");
        assertThat((String) JsonPath.read(u, "$.recorteRotulo")).isEqualTo("Equipe");
        Map<String, Object> penta = vacina(u, "Penta", "TERCEIRA");
        assertThat(penta).containsEntry("coorte", 3).containsEntry("vacinados", 1).containsEntry("cobertura", 33.3).containsEntry("atingida", false)
                .containsEntry("comPrimeiraDose", 2).containsEntry("abandono", 50.0).containsEntry("classeAbandono", "ALTA")
                .containsEntry("homogeneidadeTerritorios", 0.0);
        Map<String, Object> bcg = vacina(u, "BCG", "UNICA");
        assertThat(bcg).containsEntry("coorte", 3).containsEntry("cobertura", 100.0).containsEntry("atingida", true).containsEntry("abandono", null)
                .containsEntry("homogeneidadeTerritorios", 100.0);
        // A de 30 meses fica fora das coortes da tríplice viral (13 a 24 meses) e do reforço da DTP (16 a 27 meses).
        assertThat(vacina(u, "SCR", "PRIMEIRA")).containsEntry("coorte", 0).containsEntry("cobertura", null);
        assertThat((Integer) JsonPath.read(u, "$.vacinasNaMeta")).isGreaterThanOrEqualTo(1);
        assertThat((Integer) JsonPath.read(u, "$.abandonoAlto")).isGreaterThanOrEqualTo(1);
        assertThat((List<String>) JsonPath.read(u, "$.recortes[*].nome")).containsExactly(PREFIXO + "ESF A", PREFIXO + "ESF B");

        // Equipe A, por microárea; a microárea sozinha não tem recortes.
        String e = ler("/api/v1/vacinacao/cobertura?equipeId=" + esfA);
        assertThat((String) JsonPath.read(e, "$.recorteRotulo")).isEqualTo("Microárea");
        assertThat((List<String>) JsonPath.read(e, "$.recortes[*].nome")).containsExactly("Microárea 01");
        assertThat(vacina(e, "Penta", "TERCEIRA")).containsEntry("coorte", 2).containsEntry("cobertura", 50.0);
        String m = ler("/api/v1/vacinacao/cobertura?equipeId=" + esfA + "&microareaId=" + microarea);
        assertThat((String) JsonPath.read(m, "$.nivel")).isEqualTo("MICROAREA");
        assertThat((List<?>) JsonPath.read(m, "$.recortes")).isEmpty();
        // Equipe B: a criança sem dose e a maior, sem microárea.
        assertThat((List<String>) JsonPath.read(ler("/api/v1/vacinacao/cobertura?equipeId=" + esfB), "$.recortes[*].nome")).containsExactly("Sem microárea");

        // A data de referência volta no tempo: há 8 meses as de 10 meses tinham 2 (fora da coorte da penta).
        String antes = ler("/api/v1/vacinacao/cobertura?unidadeId=" + ubs.getUuid() + "&data=" + hoje.minusMonths(8));
        assertThat(vacina(antes, "Penta", "TERCEIRA")).containsEntry("coorte", 0);

        // Rede: a unidade aparece como recorte.
        assertThat((List<String>) JsonPath.read(ler("/api/v1/vacinacao/cobertura"), "$.recortes[*].nome")).contains(PREFIXO + "UBS");

        // Regras: microárea sem equipe, data no futuro e equipe de outra unidade, 400.
        mockMvc.perform(get("/api/v1/vacinacao/cobertura?microareaId=" + microarea)).andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/vacinacao/cobertura?unidadeId=" + ubs.getUuid() + "&data=" + hoje.plusDays(1))).andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/vacinacao/cobertura?unidadeId=" + outra.getUuid() + "&equipeId=" + esfA)).andExpect(status().isBadRequest());

        // A dose que mede a cobertura pede a meta e o público criança (400).
        String esquema = "{\"imunobiologicoId\": \"%s\", \"dose\": \"ADICIONAL\", \"publico\": \"%s\", \"idadeRecomendadaMeses\": 12, "
                + "\"contaNaSituacao\": false, \"vigenteDesde\": \"%s\", \"medeCobertura\": true%s}";
        enviar(post("/api/v1/esquema-vacinal"), esquema.formatted(imuno("BCG"), "CRIANCA", hoje, ""), 400);
        enviar(post("/api/v1/esquema-vacinal"), esquema.formatted(imuno("BCG"), "ADULTO", hoje, ", \"metaCobertura\": 95"), 400);
    }

    @Test
    void buscaAtivaPorEquipeEMicroareaComOAgente() throws Exception {
        Paciente[] p = populacao();
        String todos = ler("/api/v1/vacinacao/atrasados?unidadeId=" + ubs.getUuid());
        assertThat((List<String>) JsonPath.read(todos, "$[*].pacienteId")).contains(p[0].getUuid().toString(), p[2].getUuid().toString());

        String daMicroarea = ler("/api/v1/vacinacao/atrasados?unidadeId=" + ubs.getUuid() + "&equipeId=" + esfA + "&microareaId=" + microarea);
        assertThat((List<String>) JsonPath.read(daMicroarea, "$[*].pacienteId")).containsExactlyInAnyOrder(p[0].getUuid().toString(), p[1].getUuid().toString());
        assertThat((List<String>) JsonPath.read(daMicroarea, "$[*].acsNome")).containsOnly(PREFIXO + "Agente");
        assertThat((List<String>) JsonPath.read(daMicroarea, "$[*].microareaId")).containsOnly(microarea);

        String daB = ler("/api/v1/vacinacao/atrasados?unidadeId=" + ubs.getUuid() + "&equipeId=" + esfB);
        assertThat((List<String>) JsonPath.read(daB, "$[*].equipeId")).containsOnly(esfB);
        assertThat((List<String>) JsonPath.read(daB, "$[*].acsNome")).containsOnly((String) null);

        // A equipe de outra unidade não serve de filtro (400).
        String outraEquipe = equipe(outra, "ESF Outra");
        mockMvc.perform(get("/api/v1/vacinacao/atrasados?unidadeId=" + ubs.getUuid() + "&equipeId=" + outraEquipe)).andExpect(status().isBadRequest());
    }

    @Test
    void doseDaCampanhaNoAndamentoDaMetaENaSalaDeSituacao() throws Exception {
        Paciente[] p = populacao();
        String influenza = imuno("INFLUENZA");
        // Só a campanha de vacinação tem vacinas (400).
        String campanha = "{\"nome\": \"%s\", \"tipo\": \"%s\", \"inicio\": \"%s\", \"fim\": \"%s\", \"metaQuantidade\": 10, \"imunobiologicoIds\": [\"%s\"]}";
        enviar(post("/api/v1/campanha"), campanha.formatted(PREFIXO + "Rastreio", "RASTREAMENTO", hoje.minusDays(1), hoje.plusDays(10), influenza), 400);
        String c = enviar(post("/api/v1/campanha"), campanha.formatted(PREFIXO + "Gripe", "VACINACAO", hoje.minusDays(1), hoje.plusDays(10), influenza), 201);
        String id = JsonPath.read(c, "$.uuid");
        assertThat((List<String>) JsonPath.read(c, "$.imunobiologicoSiglas")).containsExactly("Influenza");
        assertThat((Integer) JsonPath.read(c, "$.realizado")).isZero();

        // A dose de outra vacina não entra na campanha (422); a de influenza entra e anda a meta.
        enviar(post("/api/v1/vacinacao"), aplicada(p[2], "PENTA", "PRIMEIRA", id), 422);
        enviar(post("/api/v1/vacinacao"), aplicada(p[3], "INFLUENZA", "ANUAL", id), 201);
        assertThat((Integer) JsonPath.read(ler("/api/v1/campanha/" + id), "$.realizado")).isEqualTo(1);

        // Sala de Situação da unidade: a dose do período por estratégia, a cobertura e a campanha com o andamento.
        String s = ler("/api/v1/indicadores-rede/vacinacao?unidadeId=" + ubs.getUuid());
        assertThat((Integer) JsonPath.read(s, "$.dosesAplicadas")).isEqualTo(1);
        assertThat((List<String>) JsonPath.read(s, "$.porEstrategia[*].chave")).containsExactly("CAMPANHA");
        assertThat((List<Integer>) JsonPath.read(s, "$.cobertura[?(@.sigla == 'Penta' && @.dose == 'TERCEIRA')].coorte")).containsExactly(3);
        assertThat((List<Integer>) JsonPath.read(s, "$.campanhas[?(@.uuid == '" + id + "')].realizado")).containsExactly(1);
    }
}
