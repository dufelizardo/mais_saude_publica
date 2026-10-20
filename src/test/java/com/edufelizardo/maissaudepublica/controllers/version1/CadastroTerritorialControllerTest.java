package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Endereco;
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

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Cadastro territorial, fatia F3a (ADR-0127): imóvel com o ponto na microárea, domicílio só em imóvel de moradia, família
 * com responsável, uma família vigente por paciente, troca de responsável, saída, mudança com histórico e o endereço do
 * paciente levado do imóvel só por ação explícita.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CadastroTerritorialControllerTest {

    private static final String URL = "/api/v1/territorio/";
    static final String PREFIXO = "CadTer Teste ";
    static final String CODIGO = "TSTF3A";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    private UnidadeDeSaude ubs;
    private UUID area;
    private UUID m07;

    @BeforeEach
    void seed() throws Exception {
        limpar();
        ubs = new UnidadeDeSaude();
        ubs.setNome(PREFIXO + "UBS");
        ubs.setTipo(TipoUnidadeDeSaude.UBS);
        ubs.setAtivo(true);
        ubs = unidadeDeSaudeRepository.save(ubs);
        String s = enviar(post(URL + "segmento"), "{\"codigo\": \"" + CODIGO + "\", \"nome\": \"Distrito\", \"tipo\": \"DISTRITO_SANITARIO\"}", 201);
        String t = enviar(post(URL + "territorio"), "{\"segmentoId\": \"%s\", \"nome\": \"APS\", \"finalidade\": \"ATENCAO_PRIMARIA\"}"
                .formatted(JsonPath.<String>read(s, "$.uuid")), 201);
        String a = enviar(post(URL + "area"), "{\"territorioId\": \"%s\", \"codigo\": \"01\", \"nome\": \"Área 01\", \"geometria\": %s}"
                .formatted(JsonPath.<String>read(t, "$.uuid"), texto(TerritorioControllerTest.quadrado(-47.0, -22.0, 0.02))), 201);
        area = UUID.fromString(JsonPath.read(a, "$.area.uuid"));
        String m = enviar(post(URL + "microarea"), "{\"areaId\": \"%s\", \"codigo\": \"07\", \"geometria\": %s}"
                .formatted(area, texto(TerritorioControllerTest.quadrado(-47.0, -22.0, 0.01))), 201);
        List<String> ids = JsonPath.read(m, "$.area.microareas[?(@.codigo == '07')].uuid");
        m07 = UUID.fromString(ids.get(0));
    }

    @AfterEach
    void limpar() {
        limparDados(jdbc);
    }

    /** O território dos testes, as equipes, os pacientes e a unidade do prefixo. */
    static void limparDados(JdbcTemplate jdbc) {
        TerritorioControllerTest.limparDados(jdbc, PREFIXO, CODIGO);
        jdbc.update("delete from tb_membro_nucleo where paciente_id in (select uuid from tb_paciente where nome like ?)", PREFIXO + "%");
        jdbc.update("delete from tb_nucleo_familiar where responsavel_id in (select uuid from tb_paciente where nome like ?)", PREFIXO + "%");
        jdbc.update("delete from tb_evento_auditoria where paciente_id in (select uuid from tb_paciente where nome like ?)", PREFIXO + "%");
        jdbc.update("delete from tb_telefones_paciente where paciente_uuid in (select uuid from tb_paciente where nome like ?)", PREFIXO + "%");
        jdbc.update("delete from tb_paciente where nome like ?", PREFIXO + "%");
        jdbc.update("delete from tb_equipe where unidade_id in (select uuid from tb_unidade_de_saude where nome like ?)", PREFIXO + "%");
        jdbc.update("delete from tb_unidade_de_saude where nome like ?", PREFIXO + "%");
    }

    static String texto(String valor) {
        return valor == null ? "null" : "\"" + valor.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    static String endereco(String logradouro, String numero) {
        return "{\"cep\": \"13000-000\", \"logradouro\": \"%s\", \"numeroLogradouro\": \"%s\", \"bairro\": \"Centro\", \"cidade\": \"Campinas\", \"estado\": \"SP\"}"
                .formatted(logradouro, numero);
    }

    private String enviar(MockHttpServletRequestBuilder r, String corpo, int esperado) throws Exception {
        return mockMvc.perform(r.contentType(MediaType.APPLICATION_JSON).content(corpo)).andExpect(status().is(esperado))
                .andReturn().getResponse().getContentAsString();
    }

    private String ler(String url) throws Exception {
        return mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
    }

    private void comEquipe() throws Exception {
        String e = enviar(post("/api/v1/equipe/"), "{\"unidadeId\": \"%s\", \"tipo\": \"ESF\", \"nome\": \"%sSF\"}".formatted(ubs.getUuid(), PREFIXO), 201);
        enviar(post(URL + "area/" + area + "/responsabilidade"), "{\"equipeId\": \"%s\"}".formatted(JsonPath.<String>read(e, "$.resumo.uuid")), 200);
    }

    private UUID paciente(String nome) {
        Paciente p = new Paciente();
        p.setNome(PREFIXO + nome);
        p.setCpf(String.valueOf(92000000000L + Math.abs(nome.hashCode() % 999999)));
        p.setAtivo(true);
        p.setEndereco(new Endereco("13000-000", "Rua Antiga", "9", null, "Centro", "Campinas", "SP", null));
        return pacienteRepository.save(p).getUuid();
    }

    /** Imóvel de moradia com um domicílio dentro da microárea 07; devolve o domicílio. */
    private UUID domicilio(String logradouro, String numero) throws Exception {
        String i = enviar(post(URL + "imovel"), "{\"microareaId\": \"%s\", \"tipo\": \"DOMICILIO\", \"endereco\": %s, \"latitude\": -21.995, \"longitude\": -46.995}"
                .formatted(m07, endereco(logradouro, numero)), 201);
        String d = enviar(post(URL + "domicilio"), "{\"imovelId\": \"%s\", \"situacao\": \"PROPRIO\", \"comodos\": 4, \"abastecimentoAgua\": \"REDE_ENCANADA\"}"
                .formatted(JsonPath.<String>read(i, "$.imovel.uuid")), 201);
        return UUID.fromString(JsonPath.read(d, "$.domicilios[0].uuid"));
    }

    @Test
    void imovelComPontoNaMicroareaEDomicilioSoEmMoradia() throws Exception {
        String base = "{\"microareaId\": %s, \"tipo\": \"%s\", \"endereco\": " + endereco("Rua A", "100") + ", \"latitude\": %s, \"longitude\": %s}";
        // O cadastro é da equipe.
        enviar(post(URL + "imovel"), base.formatted("\"" + m07 + "\"", "DOMICILIO", "-21.995", "-46.995"), 422);
        comEquipe();
        // Fora da microárea (dentro da área): 422. Sem microárea, o ponto a encontra; sem ponto nem microárea, 400.
        enviar(post(URL + "imovel"), base.formatted("\"" + m07 + "\"", "DOMICILIO", "-21.985", "-46.985"), 422);
        String i = enviar(post(URL + "imovel"), base.formatted("null", "DOMICILIO", "-21.995", "-46.995"), 201);
        assertThat(JsonPath.<String>read(i, "$.imovel.microareaCodigo")).isEqualTo("07");
        enviar(post(URL + "imovel"), base.formatted("null", "DOMICILIO", "null", "null"), 400);
        enviar(post(URL + "imovel"), base.formatted("null", "DOMICILIO", "-10.0", "-40.0"), 400);
        enviar(post(URL + "imovel"), base.formatted("\"" + m07 + "\"", "DOMICILIO", "-21.995", "null"), 400);

        // Domicílio só em imóvel de moradia; identificação repetida, 409.
        String comercio = enviar(post(URL + "imovel"), base.formatted("\"" + m07 + "\"", "COMERCIO", "null", "null"), 201);
        enviar(post(URL + "domicilio"), "{\"imovelId\": \"%s\"}".formatted(JsonPath.<String>read(comercio, "$.imovel.uuid")), 422);
        String imovel = JsonPath.read(i, "$.imovel.uuid");
        enviar(post(URL + "domicilio"), "{\"imovelId\": \"%s\", \"identificacao\": \"Apto 12\", \"localizacao\": \"URBANA\"}".formatted(imovel), 201);
        enviar(post(URL + "domicilio"), "{\"imovelId\": \"%s\", \"identificacao\": \"apto 12\"}".formatted(imovel), 409);
        String d = enviar(post(URL + "domicilio"), "{\"imovelId\": \"%s\", \"identificacao\": \"Apto 13\"}".formatted(imovel), 201);
        assertThat((List<?>) JsonPath.read(d, "$.domicilios")).hasSize(2);
        // Imóvel com domicílio não deixa de ser de moradia.
        enviar(patch(URL + "imovel/" + imovel), base.formatted("\"" + m07 + "\"", "COMERCIO", "null", "null"), 422);

        String lista = ler(URL + "area/" + area + "/imoveis");
        assertThat((List<Integer>) JsonPath.read(lista, "$[?(@.uuid == '" + imovel + "')].domicilios")).containsExactly(2);
        assertThat((List<?>) JsonPath.read(lista, "$")).hasSize(2);
    }

    @Test
    void familiaComResponsavelSaidaMudancaEHistorico() throws Exception {
        comEquipe();
        UUID casa = domicilio("Rua A", "100");
        UUID outraCasa = domicilio("Rua B", "200");
        UUID ana = paciente("Ana");
        UUID bia = paciente("Bia");
        UUID caio = paciente("Caio");

        String r = enviar(post(URL + "familia"), "{\"domicilioId\": \"%s\", \"responsavelId\": \"%s\", \"rendaFamiliar\": \"ATE_UM_SM\", \"membros\": [{\"pacienteId\": \"%s\", \"parentesco\": \"FILHO\"}]}"
                .formatted(casa, ana, bia), 201);
        String familia = JsonPath.read(r, "$.domicilios[0].familias[0].uuid");
        assertThat((List<?>) JsonPath.read(r, "$.domicilios[0].familias[0].membros")).hasSize(2);
        assertThat(JsonPath.<Boolean>read(r, "$.domicilios[0].familias[0].membros[0].enderecoDiverge")).isTrue();
        // Uma família vigente por pessoa; dois responsáveis, 400.
        enviar(post(URL + "familia"), "{\"domicilioId\": \"%s\", \"responsavelId\": \"%s\"}".formatted(outraCasa, bia), 409);
        enviar(post(URL + "familia/" + familia + "/membro"), "{\"pacienteId\": \"%s\", \"parentesco\": \"RESPONSAVEL\"}".formatted(caio), 400);
        r = enviar(post(URL + "familia/" + familia + "/membro"), "{\"pacienteId\": \"%s\", \"parentesco\": \"NETO\"}".formatted(caio), 200);
        List<String> membroAna = JsonPath.read(r, "$.domicilios[0].familias[0].membros[?(@.nome == '" + PREFIXO + "Ana')].uuid");
        List<String> membroBia = JsonPath.read(r, "$.domicilios[0].familias[0].membros[?(@.nome == '" + PREFIXO + "Bia')].uuid");

        // O responsável sai por último: antes, troca.
        enviar(post(URL + "familia/membro/" + membroAna.get(0) + "/saida"), "{\"motivo\": \"Mudou de cidade\"}", 422);
        enviar(post(URL + "familia/" + familia + "/responsavel"), "{\"membroId\": \"%s\", \"parentescoDoAnterior\": \"PAI_OU_MAE\"}".formatted(membroBia.get(0)), 200);
        r = enviar(post(URL + "familia/membro/" + membroAna.get(0) + "/saida"), "{\"motivo\": \"Mudou de cidade\"}", 200);
        assertThat(JsonPath.<String>read(r, "$.domicilios[0].familias[0].responsavelNome")).isEqualTo(PREFIXO + "Bia");
        assertThat((List<?>) JsonPath.read(r, "$.domicilios[0].familias[0].antigos")).hasSize(1);

        // Mudança da família inteira: a de origem se encerra e a nova começa no destino.
        r = enviar(post(URL + "familia/" + familia + "/mudanca"), "{\"domicilioId\": \"%s\", \"motivo\": \"Aluguel\"}".formatted(outraCasa), 200);
        assertThat((List<?>) JsonPath.read(r, "$.domicilios[0].familias[0].membros")).hasSize(2);
        enviar(post(URL + "familia/" + familia + "/membro"), "{\"pacienteId\": \"%s\", \"parentesco\": \"FILHO\"}".formatted(ana), 422);
        String daBia = ler(URL + "paciente/" + bia + "/familia");
        assertThat((List<?>) JsonPath.read(daBia, "$.moradias")).hasSize(2);
        assertThat(JsonPath.<String>read(daBia, "$.enderecoTexto")).startsWith("Rua B, 200");
        assertThat(JsonPath.<String>read(daBia, "$.microareaCodigo")).isEqualTo("07");

        // O endereço do cadastro só muda por ação explícita.
        List<String> membroBiaNova = JsonPath.read(r, "$.domicilios[0].familias[0].membros[?(@.nome == '" + PREFIXO + "Bia')].uuid");
        r = enviar(post(URL + "familia/membro/" + membroBiaNova.get(0) + "/endereco-do-imovel"), "", 200);
        assertThat((List<Boolean>) JsonPath.read(r, "$.domicilios[0].familias[0].membros[?(@.nome == '" + PREFIXO + "Bia')].enderecoDiverge"))
                .containsExactly(false);
        assertThat(pacienteRepository.findById(bia).orElseThrow().getEndereco().getLogradouro()).isEqualTo("Rua B");

        // Contagens na árvore.
        String arvore = ler(URL + "arvore");
        String micro = "$.segmentos[?(@.codigo == '" + CODIGO + "')].territorios[*].areas[*].microareas[?(@.codigo == '07')]";
        assertThat((List<Integer>) JsonPath.read(arvore, micro + ".imoveis")).containsExactly(2);
        assertThat((List<Integer>) JsonPath.read(arvore, micro + ".familias")).containsExactly(1);
        assertThat((List<Integer>) JsonPath.read(arvore, micro + ".pessoas")).containsExactly(2);
    }

    @Test
    void saidaDoUltimoEncerraAFamilia() throws Exception {
        comEquipe();
        UUID casa = domicilio("Rua C", "1");
        UUID dani = paciente("Dani");
        String r = enviar(post(URL + "familia"), "{\"domicilioId\": \"%s\", \"responsavelId\": \"%s\"}".formatted(casa, dani), 201);
        String membro = JsonPath.read(r, "$.domicilios[0].familias[0].membros[0].uuid");
        r = enviar(post(URL + "familia/membro/" + membro + "/saida"), "{\"motivo\": \"Óbito\"}", 200);
        assertThat((List<?>) JsonPath.read(r, "$.domicilios[0].familias")).isEmpty();
        assertThat(JsonPath.<String>read(r, "$.domicilios[0].anteriores[0].motivoFim")).isEqualTo("Família sem membros: Óbito");
        String daDani = ler(URL + "paciente/" + dani + "/familia");
        assertThat((Object) JsonPath.read(daDani, "$.familia")).isNull();
    }
}
