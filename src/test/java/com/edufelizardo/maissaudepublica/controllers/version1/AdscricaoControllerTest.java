package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.CategoriaPrograma;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.ParticipacaoPrograma;
import com.edufelizardo.maissaudepublica.models.Programa;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.AbrangenciaPrograma;
import com.edufelizardo.maissaudepublica.models.enuns.PeriodicidadeAcompanhamento;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoPrograma;
import com.edufelizardo.maissaudepublica.models.enuns.Sexo;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.CategoriaProgramaRepository;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
import com.edufelizardo.maissaudepublica.repositories.ParticipacaoProgramaRepository;
import com.edufelizardo.maissaudepublica.repositories.ProgramaRepository;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Adscrição e cobertura, fatia F3b (ADR-0128): a adscrição segue o domicílio (entrada, mudança de microárea, saída) e a
 * equipe da área; a manual não é trocada pela calculada e, encerrada, volta a do território; cobertura por microárea e
 * os elegíveis de um programa entre os adscritos.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdscricaoControllerTest {

    private static final String URL = "/api/v1/territorio/";
    private static final String PREFIXO = "Adscr Teste ";
    private static final String CODIGO = "TSTF3B";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private ProgramaRepository programaRepository;

    @Autowired
    private CategoriaProgramaRepository categoriaRepository;

    @Autowired
    private ParticipacaoProgramaRepository participacaoRepository;

    private UnidadeDeSaude ubs;
    private UUID area;
    private UUID m07;
    private UUID m08;
    private String equipeA;
    private String equipeB;

    @BeforeEach
    void seed() throws Exception {
        limpar();
        ubs = new UnidadeDeSaude();
        ubs.setNome(PREFIXO + "UBS");
        ubs.setTipo(TipoUnidadeDeSaude.UBS);
        ubs.setAtivo(true);
        ubs = unidadeDeSaudeRepository.save(ubs);
        String s = enviar(post(URL + "segmento"), "{\"codigo\": \"" + CODIGO + "\", \"nome\": \"D\", \"tipo\": \"OUTRO\"}", 201);
        String t = enviar(post(URL + "territorio"), "{\"segmentoId\": \"%s\", \"nome\": \"APS\", \"finalidade\": \"ATENCAO_PRIMARIA\"}"
                .formatted(JsonPath.<String>read(s, "$.uuid")), 201);
        String a = enviar(post(URL + "area"), "{\"territorioId\": \"%s\", \"codigo\": \"01\", \"nome\": \"A\", \"geometria\": %s}"
                .formatted(JsonPath.<String>read(t, "$.uuid"), CadastroTerritorialControllerTest.texto(TerritorioControllerTest.quadrado(-47.0, -22.0, 0.02))), 201);
        area = UUID.fromString(JsonPath.read(a, "$.area.uuid"));
        m07 = microarea("07", -47.0);
        m08 = microarea("08", -46.99);
        equipeA = equipe("SF-A");
        equipeB = equipe("SF-B");
        enviar(post(URL + "area/" + area + "/responsabilidade"), "{\"equipeId\": \"%s\", \"inicio\": \"%s\"}".formatted(equipeA, LocalDate.now().minusDays(30)), 200);
    }

    @AfterEach
    void limpar() {
        jdbc.update("delete from tb_evento_acompanhamento where acompanhamento_id in (select a.uuid from tb_acompanhamento_programatico a "
                + "join tb_programa p on p.uuid = a.programa_id where p.codigo like ?)", CODIGO + "%");
        jdbc.update("delete from tb_acompanhamento_programatico where programa_id in (select uuid from tb_programa where codigo like ?)", CODIGO + "%");
        jdbc.update("delete from tb_participacao_programa where programa_id in (select uuid from tb_programa where codigo like ?)", CODIGO + "%");
        jdbc.update("delete from tb_programa_condicao where programa_id in (select uuid from tb_programa where codigo like ?)", CODIGO + "%");
        jdbc.update("delete from tb_programa where codigo like ?", CODIGO + "%");
        jdbc.update("delete from tb_adscricao where paciente_id in (select uuid from tb_paciente where nome like ?)", PREFIXO + "%");
        TerritorioControllerTest.limparDados(jdbc, PREFIXO, CODIGO);
        jdbc.update("delete from tb_membro_nucleo where paciente_id in (select uuid from tb_paciente where nome like ?)", PREFIXO + "%");
        jdbc.update("delete from tb_nucleo_familiar where responsavel_id in (select uuid from tb_paciente where nome like ?)", PREFIXO + "%");
        jdbc.update("delete from tb_evento_auditoria where paciente_id in (select uuid from tb_paciente where nome like ?)", PREFIXO + "%");
        jdbc.update("delete from tb_paciente where nome like ?", PREFIXO + "%");
        jdbc.update("delete from tb_equipe where unidade_id in (select uuid from tb_unidade_de_saude where nome like ?)", PREFIXO + "%");
        jdbc.update("delete from tb_unidade_de_saude where nome like ?", PREFIXO + "%");
    }

    private String enviar(MockHttpServletRequestBuilder r, String corpo, int esperado) throws Exception {
        return mockMvc.perform(r.contentType(MediaType.APPLICATION_JSON).content(corpo)).andExpect(status().is(esperado))
                .andReturn().getResponse().getContentAsString();
    }

    private String ler(String url) throws Exception {
        return mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
    }

    private UUID microarea(String codigo, double lon) throws Exception {
        String m = enviar(post(URL + "microarea"), "{\"areaId\": \"%s\", \"codigo\": \"%s\", \"geometria\": %s}"
                .formatted(area, codigo, CadastroTerritorialControllerTest.texto(TerritorioControllerTest.quadrado(lon, -22.0, 0.01))), 201);
        List<String> ids = JsonPath.read(m, "$.area.microareas[?(@.codigo == '" + codigo + "')].uuid");
        return UUID.fromString(ids.get(0));
    }

    private String equipe(String nome) throws Exception {
        String e = enviar(post("/api/v1/equipe/"), "{\"unidadeId\": \"%s\", \"tipo\": \"ESF\", \"nome\": \"%s%s\"}".formatted(ubs.getUuid(), PREFIXO, nome), 201);
        return JsonPath.read(e, "$.resumo.uuid");
    }

    private UUID paciente(String nome, LocalDate nascimento, Sexo sexo) {
        Paciente p = new Paciente();
        p.setNome(PREFIXO + nome);
        p.setCpf(String.valueOf(93000000000L + Math.abs(nome.hashCode() % 999999)));
        p.setDataNascimento(nascimento);
        p.setSexo(sexo);
        p.setAtivo(true);
        return pacienteRepository.save(p).getUuid();
    }

    /** Domicílio num imóvel com o ponto dentro da microárea; devolve o domicílio. */
    private UUID domicilio(UUID microarea, double lon, String numero) throws Exception {
        String i = enviar(post(URL + "imovel"), "{\"microareaId\": \"%s\", \"tipo\": \"DOMICILIO\", \"endereco\": %s, \"latitude\": -21.995, \"longitude\": %s}"
                .formatted(microarea, CadastroTerritorialControllerTest.endereco("Rua A", numero), lon), 201);
        String d = enviar(post(URL + "domicilio"), "{\"imovelId\": \"%s\"}".formatted(JsonPath.<String>read(i, "$.imovel.uuid")), 201);
        return UUID.fromString(JsonPath.read(d, "$.domicilios[0].uuid"));
    }

    private String familia(UUID domicilio, UUID responsavel, UUID outro) throws Exception {
        String membros = outro == null ? "" : ", \"membros\": [{\"pacienteId\": \"%s\", \"parentesco\": \"CONJUGE\"}]".formatted(outro);
        String r = enviar(post(URL + "familia"), "{\"domicilioId\": \"%s\", \"responsavelId\": \"%s\"%s}".formatted(domicilio, responsavel, membros), 201);
        return JsonPath.read(r, "$.domicilios[0].familias[0].uuid");
    }

    private String adscricao(UUID paciente) throws Exception {
        return ler(URL + "paciente/" + paciente + "/adscricao");
    }

    @Test
    void adscricaoSegueODomicilioEAEquipeDaArea() throws Exception {
        UUID ana = paciente("Ana", LocalDate.of(1950, 3, 1), Sexo.FEMININO);
        UUID caio = paciente("Caio", LocalDate.of(1955, 6, 1), Sexo.MASCULINO);
        UUID x = domicilio(m07, -46.995, "1");
        UUID y = domicilio(m08, -46.985, "2");
        assertThat((Object) JsonPath.read(adscricao(ana), "$.atual")).isNull();

        String familia = familia(x, ana, caio);
        String r = adscricao(ana);
        assertThat(JsonPath.<String>read(r, "$.atual.origem")).isEqualTo("TERRITORIO");
        assertThat(JsonPath.<String>read(r, "$.atual.equipeId")).isEqualTo(equipeA);
        assertThat(JsonPath.<String>read(r, "$.atual.microareaCodigo")).isEqualTo("07");
        assertThat(JsonPath.<String>read(r, "$.domicilio")).startsWith("Rua A, 1");

        // Mudança para a microárea 08: fecha e reabre.
        enviar(post(URL + "familia/" + familia + "/mudanca"), "{\"domicilioId\": \"%s\"}".formatted(y), 200);
        r = adscricao(ana);
        assertThat(JsonPath.<String>read(r, "$.atual.microareaCodigo")).isEqualTo("08");
        assertThat((List<?>) JsonPath.read(r, "$.historico")).hasSize(1);

        // Troca de equipe da área: os moradores vão para a nova.
        enviar(post(URL + "area/" + area + "/responsabilidade"), "{\"equipeId\": \"%s\", \"motivo\": \"Redivisão\"}".formatted(equipeB), 200);
        r = adscricao(ana);
        assertThat(JsonPath.<String>read(r, "$.atual.equipeId")).isEqualTo(equipeB);
        assertThat((List<String>) JsonPath.read(r, "$.historico[*].motivoFim")).anyMatch(m -> m.startsWith("Equipe responsável pela área"));

        // Saída da família: sem domicílio, sem adscrição.
        String imovel = ler(URL + "paciente/" + caio + "/familia");
        List<String> membro = JsonPath.read(imovel, "$.familia.membros[?(@.pacienteId == '" + caio + "')].uuid");
        enviar(post(URL + "familia/membro/" + membro.get(0) + "/saida"), "{\"motivo\": \"Mudou de cidade\"}", 200);
        assertThat((Object) JsonPath.read(adscricao(caio), "$.atual")).isNull();
    }

    @Test
    void manualNaoETrocadaPelaCalculada() throws Exception {
        UUID bia = paciente("Bia", LocalDate.of(2000, 1, 1), Sexo.FEMININO);
        enviar(post(URL + "paciente/" + bia + "/adscricao"), "{\"equipeId\": \"%s\"}".formatted(equipeB), 400);
        String r = enviar(post(URL + "paciente/" + bia + "/adscricao"), "{\"equipeId\": \"%s\", \"motivo\": \"Situação de rua\"}".formatted(equipeB), 200);
        assertThat(JsonPath.<String>read(r, "$.atual.origem")).isEqualTo("MANUAL");

        // Entrou numa família da área da equipe A: a manual fica.
        familia(domicilio(m07, -46.995, "3"), bia, null);
        r = adscricao(bia);
        assertThat(JsonPath.<String>read(r, "$.atual.origem")).isEqualTo("MANUAL");
        assertThat(JsonPath.<String>read(r, "$.atual.equipeId")).isEqualTo(equipeB);

        // Encerrada a manual, volta a do território; a do território não se encerra por aqui.
        r = enviar(post(URL + "paciente/" + bia + "/adscricao/encerrar"), "{\"motivo\": \"Agora tem casa\"}", 200);
        assertThat(JsonPath.<String>read(r, "$.atual.origem")).isEqualTo("TERRITORIO");
        assertThat(JsonPath.<String>read(r, "$.atual.equipeId")).isEqualTo(equipeA);
        enviar(post(URL + "paciente/" + bia + "/adscricao/encerrar"), "{\"motivo\": \"x\"}", 422);
    }

    @Test
    void coberturaEElegiveisDoPrograma() throws Exception {
        UUID ana = paciente("Ana", LocalDate.of(1950, 3, 1), Sexo.FEMININO);
        UUID caio = paciente("Caio", LocalDate.of(1955, 6, 1), Sexo.MASCULINO);
        UUID jovem = paciente("Jovem", LocalDate.of(2005, 6, 1), Sexo.FEMININO);
        enviar(patch(URL + "microarea/" + m07), "{\"areaId\": \"%s\", \"codigo\": \"07\", \"populacaoReferencia\": 8, \"geometria\": %s}"
                .formatted(area, CadastroTerritorialControllerTest.texto(TerritorioControllerTest.quadrado(-47.0, -22.0, 0.01))), 200);
        familia(domicilio(m07, -46.995, "1"), ana, caio);
        familia(domicilio(m07, -46.996, "2"), jovem, null);

        String c = ler(URL + "equipe/" + equipeA + "/cobertura");
        assertThat(JsonPath.<Integer>read(c, "$.pessoasAdscritas")).isEqualTo(3);
        assertThat(JsonPath.<Integer>read(c, "$.familias")).isEqualTo(2);
        assertThat(JsonPath.<Integer>read(c, "$.microareasSemCadastro")).isEqualTo(1);
        assertThat((List<Integer>) JsonPath.read(c, "$.microareas[?(@.codigo == '07')].cobertura")).containsExactly(38);

        // Idosos (60+): Ana e Caio; com a Ana inscrita, só o Caio.
        CategoriaPrograma cronicos = categoriaRepository.findByNomeIgnoreCase("Crônicos").orElseThrow();
        Programa idoso = new Programa();
        idoso.setCodigo(CODIGO + "IDOSO");
        idoso.setNome(PREFIXO + "Idoso");
        idoso.setCategoria(cronicos);
        idoso.setAbrangencia(AbrangenciaPrograma.MUNICIPAL);
        idoso.setSituacao(SituacaoPrograma.ATIVO);
        idoso.setPeriodicidade(PeriodicidadeAcompanhamento.SEMESTRAL);
        idoso.setIdadeMinima(60);
        idoso.getCondicoes().add("I10");
        idoso = programaRepository.save(idoso);
        participacaoRepository.save(new ParticipacaoPrograma(null, idoso, ubs, null, LocalDate.now().minusYears(1), null, null, null));
        String el = ler("/api/v1/programa/" + idoso.getUuid() + "/elegiveis?equipeId=" + equipeA);
        assertThat((List<String>) JsonPath.read(el, "$.elegiveis[*].nome")).containsExactly(PREFIXO + "Ana", PREFIXO + "Caio");
        assertThat((List<String>) JsonPath.read(el, "$.criteriosNaoAvaliados")).containsExactly("condição (I10)");
        enviar(post("/api/v1/programa/" + idoso.getUuid() + "/acompanhamento"), "{\"pacienteId\": \"%s\", \"unidadeId\": \"%s\"}".formatted(ana, ubs.getUuid()), 201);
        el = ler("/api/v1/programa/" + idoso.getUuid() + "/elegiveis?equipeId=" + equipeA + "&microareaId=" + m07);
        assertThat((List<String>) JsonPath.read(el, "$.elegiveis[*].nome")).containsExactly(PREFIXO + "Caio");
        el = ler("/api/v1/programa/" + idoso.getUuid() + "/elegiveis?equipeId=" + equipeA + "&microareaId=" + m08);
        assertThat((List<?>) JsonPath.read(el, "$.elegiveis")).isEmpty();
    }
}
