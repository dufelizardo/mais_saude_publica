package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Paciente;
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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** O local de realização do atendimento (ADR-0166): sem local, na unidade; ponto da própria unidade; domicílio cadastrado ou descrito. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LocalRealizacaoAtendimentoControllerTest {

    private static final String UNIDADE = "/api/v1/unidade-saude/id/";
    private static final String ATENDIMENTO = "/api/v1/atendimento/";
    private static final String TERRITORIO = "/api/v1/territorio/";
    private static final String PREFIXO = "LocalAt Teste ";
    private static final String CODIGO = "TESTE_LOCALAT";
    private static final String CPF_PROFISSIONAL = "93100000166";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    private UnidadeDeSaude ubs;
    private UUID paciente;
    private String matricula;

    @BeforeEach
    void seed() throws Exception {
        limpar();
        ubs = unidade("UBS");
        Paciente p = new Paciente();
        p.setNome(PREFIXO + "Ana");
        p.setCpf("93100000167");
        p.setDataNascimento(LocalDate.of(1950, 3, 1));
        p.setSexo(Sexo.FEMININO);
        p.setAtivo(true);
        paciente = pacienteRepository.save(p).getUuid();
        mockMvc.perform(post("/api/v1/profissional/").contentType(MediaType.APPLICATION_JSON).content("""
                {"cpf": "%s", "nome": "%sProfissional", "endereco": {"cep": "01310-100", "logradouro": "Avenida Paulista", "numeroLogradouro": "1000",
                 "bairro": "Bela Vista", "cidade": "São Paulo", "estado": "SP"}, "telefones": ["011-2063-7185"], "email": "local@saude.sp.gov.br"}
                """.formatted(CPF_PROFISSIONAL, PREFIXO))).andExpect(status().isCreated());
        matricula = profissionalRepository.findByCpfAndAtivoTrue(CPF_PROFISSIONAL).orElseThrow().getMatricula();
    }

    @AfterEach
    void limpar() {
        jdbc.update("delete from tb_atendimento where paciente_id in (select uuid from tb_paciente where nome like ?)", PREFIXO + "%");
        jdbc.update("delete from tb_ponto_operacional where unidade_id in (select uuid from tb_unidade_de_saude where nome like ?)", PREFIXO + "%");
        TerritorioControllerTest.limparDados(jdbc, PREFIXO, CODIGO);
        jdbc.update("delete from tb_membro_nucleo where paciente_id in (select uuid from tb_paciente where nome like ?)", PREFIXO + "%");
        jdbc.update("delete from tb_nucleo_familiar where responsavel_id in (select uuid from tb_paciente where nome like ?)", PREFIXO + "%");
        jdbc.update("delete from tb_evento_auditoria where paciente_id in (select uuid from tb_paciente where nome like ?)", PREFIXO + "%");
        jdbc.update("delete from tb_paciente where nome like ?", PREFIXO + "%");
        profissionalRepository.findByCpfAndAtivoTrue(CPF_PROFISSIONAL).ifPresent(profissionalRepository::delete);
        jdbc.update("delete from tb_equipe where unidade_id in (select uuid from tb_unidade_de_saude where nome like ?)", PREFIXO + "%");
        jdbc.update("delete from tb_unidade_de_saude where nome like ?", PREFIXO + "%");
    }

    private UnidadeDeSaude unidade(String nome) {
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + nome);
        u.setTipo(TipoUnidadeDeSaude.UBS);
        u.setAtivo(true);
        return unidadeDeSaudeRepository.save(u);
    }

    private String ponto(UnidadeDeSaude u, String nome) throws Exception {
        String r = enviar(post(UNIDADE + u.getUuid() + "/ponto"), "{\"tipo\": \"PONTO_FIXO\", \"nome\": \"%s\"}".formatted(nome), 200);
        return JsonPath.read(r, "$.pontos[0].uuid");
    }

    private String enviar(MockHttpServletRequestBuilder r, String corpo, int esperado) throws Exception {
        return mockMvc.perform(r.contentType(MediaType.APPLICATION_JSON).content(corpo)).andExpect(status().is(esperado))
                .andReturn().getResponse().getContentAsString();
    }

    private String corpo(String local) {
        return """
                {"pacienteId": "%s", "profissionalMatricula": "%s", "unidadeId": "%s", "tipo": "CONSULTA", "status": "EM_ANDAMENTO",
                 "dataHora": "2026-01-01T08:00:00"%s}
                """.formatted(paciente, matricula, ubs.getUuid(), local.isEmpty() ? "" : ", " + local);
    }

    private ResultActions abrir(String local) throws Exception {
        return mockMvc.perform(post(ATENDIMENTO).contentType(MediaType.APPLICATION_JSON).content(corpo(local)));
    }

    private String idDoAtendimento() {
        return jdbc.queryForObject("select uuid::text from tb_atendimento where paciente_id = ?", String.class, paciente);
    }

    private UUID domicilioDoPaciente() throws Exception {
        String s = enviar(post(TERRITORIO + "segmento"), "{\"codigo\": \"" + CODIGO + "\", \"nome\": \"D\", \"tipo\": \"OUTRO\"}", 201);
        String t = enviar(post(TERRITORIO + "territorio"), "{\"segmentoId\": \"%s\", \"nome\": \"APS\", \"finalidade\": \"ATENCAO_PRIMARIA\"}"
                .formatted(JsonPath.<String>read(s, "$.uuid")), 201);
        String a = enviar(post(TERRITORIO + "area"), "{\"territorioId\": \"%s\", \"codigo\": \"01\", \"nome\": \"A\", \"geometria\": %s}"
                .formatted(JsonPath.<String>read(t, "$.uuid"), CadastroTerritorialControllerTest.texto(TerritorioControllerTest.quadrado(-47.0, -22.0, 0.02))), 201);
        String e = enviar(post("/api/v1/equipe/"), "{\"unidadeId\": \"%s\", \"tipo\": \"ESF\", \"nome\": \"%sSF\"}".formatted(ubs.getUuid(), PREFIXO), 201);
        enviar(post(TERRITORIO + "area/" + JsonPath.<String>read(a, "$.area.uuid") + "/responsabilidade"), "{\"equipeId\": \"%s\", \"inicio\": \"%s\"}"
                .formatted(JsonPath.<String>read(e, "$.resumo.uuid"), LocalDate.now().minusDays(30)), 200);
        String m = enviar(post(TERRITORIO + "microarea"), "{\"areaId\": \"%s\", \"codigo\": \"07\", \"geometria\": %s}"
                .formatted(JsonPath.<String>read(a, "$.area.uuid"), CadastroTerritorialControllerTest.texto(TerritorioControllerTest.quadrado(-47.0, -22.0, 0.01))), 201);
        List<String> micro = JsonPath.read(m, "$.area.microareas[?(@.codigo == '07')].uuid");
        String i = enviar(post(TERRITORIO + "imovel"), "{\"microareaId\": \"%s\", \"tipo\": \"DOMICILIO\", \"endereco\": %s, \"latitude\": -21.995, \"longitude\": -46.995}"
                .formatted(micro.get(0), CadastroTerritorialControllerTest.endereco("Rua A", "10")), 201);
        String d = enviar(post(TERRITORIO + "domicilio"), "{\"imovelId\": \"%s\"}".formatted(JsonPath.<String>read(i, "$.imovel.uuid")), 201);
        UUID domicilio = UUID.fromString(JsonPath.read(d, "$.domicilios[0].uuid"));
        enviar(post(TERRITORIO + "familia"), "{\"domicilioId\": \"%s\", \"responsavelId\": \"%s\"}".formatted(domicilio, paciente), 201);
        return domicilio;
    }

    @Test
    void semLocalFoiNaUnidade() throws Exception {
        abrir("").andExpect(status().isCreated());
        mockMvc.perform(get(ATENDIMENTO + idDoAtendimento()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.localRealizacao").doesNotExist());
    }

    @Test
    void localGuardaADescricaoEAPosicao() throws Exception {
        abrir("\"localRealizacao\": \"VIA_PUBLICA\", \"localDescricao\": \"Praça da Sé\", \"localLatitude\": -23.55, \"localLongitude\": -46.63")
                .andExpect(status().isCreated());
        mockMvc.perform(get(ATENDIMENTO + idDoAtendimento()))
                .andExpect(jsonPath("$.localRealizacao").value("VIA_PUBLICA"))
                .andExpect(jsonPath("$.localDescricao").value("Praça da Sé"))
                .andExpect(jsonPath("$.localLatitude").value(-23.55));
    }

    @Test
    void semDescricaoOuComSoALatitudeE400() throws Exception {
        abrir("\"localRealizacao\": \"ESCOLA\"").andExpect(status().isBadRequest());
        abrir("\"localRealizacao\": \"DOMICILIO\"").andExpect(status().isBadRequest());
        abrir("\"localRealizacao\": \"ESCOLA\", \"localDescricao\": \"EMEF\", \"localLatitude\": -23.55").andExpect(status().isBadRequest());
    }

    @Test
    void pontoDaUnidadeExigePontoAtivoDaPropriaUnidade() throws Exception {
        abrir("\"localRealizacao\": \"PONTO_DA_UNIDADE\"").andExpect(status().isBadRequest());
        String alheio = ponto(unidade("Outra"), "Praça");
        abrir("\"localRealizacao\": \"PONTO_DA_UNIDADE\", \"pontoOperacionalId\": \"%s\"".formatted(alheio)).andExpect(status().isUnprocessableEntity());

        String id = ponto(ubs, "Escola Rui Barbosa");
        abrir("\"localRealizacao\": \"PONTO_DA_UNIDADE\", \"pontoOperacionalId\": \"%s\"".formatted(id)).andExpect(status().isCreated());
        mockMvc.perform(get(ATENDIMENTO + idDoAtendimento()))
                .andExpect(jsonPath("$.pontoOperacionalNome").value("Escola Rui Barbosa"));
    }

    @Test
    void pontoEncerradoNaoEntraMasOQueJaEstavaContinua() throws Exception {
        String id = ponto(ubs, "Escola");
        String local = "\"localRealizacao\": \"PONTO_DA_UNIDADE\", \"pontoOperacionalId\": \"%s\"".formatted(id);
        abrir(local).andExpect(status().isCreated());
        String atendimento = idDoAtendimento();
        enviar(post(UNIDADE + ubs.getUuid() + "/ponto/" + id + "/encerramento"), "{\"motivo\": \"Fim\"}", 200);

        abrir(local).andExpect(status().isUnprocessableEntity());
        mockMvc.perform(patch(ATENDIMENTO + atendimento).contentType(MediaType.APPLICATION_JSON).content(corpo(local))).andExpect(status().isOk());
    }

    @Test
    void domicilioDoCadastroVemNosLocaisEEntraNoAtendimento() throws Exception {
        UUID domicilio = domicilioDoPaciente();
        ponto(ubs, "Escola");
        mockMvc.perform(get(UNIDADE + ubs.getUuid() + "/locais-de-realizacao").param("pacienteId", paciente.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.domicilioId").value(domicilio.toString()))
                .andExpect(jsonPath("$.domicilioDescricao").value(org.hamcrest.Matchers.containsString("Rua A")))
                .andExpect(jsonPath("$.pontos[0].nome").value("Escola"));

        abrir("\"localRealizacao\": \"DOMICILIO\", \"domicilioId\": \"%s\"".formatted(domicilio)).andExpect(status().isCreated());
        mockMvc.perform(get(ATENDIMENTO + idDoAtendimento())).andExpect(jsonPath("$.domicilioId").value(domicilio.toString()));
    }

    @Test
    void domicilioInexistenteE404() throws Exception {
        abrir("\"localRealizacao\": \"DOMICILIO\", \"domicilioId\": \"%s\"".formatted(UUID.randomUUID())).andExpect(status().isNotFound());
    }

    @Test
    void editarSemLocalVoltaParaAUnidade() throws Exception {
        abrir("\"localRealizacao\": \"ESCOLA\", \"localDescricao\": \"EMEF\"").andExpect(status().isCreated());
        String id = idDoAtendimento();
        mockMvc.perform(patch(ATENDIMENTO + id).contentType(MediaType.APPLICATION_JSON).content(corpo(""))).andExpect(status().isOk());
        mockMvc.perform(get(ATENDIMENTO + id)).andExpect(jsonPath("$.localDescricao").doesNotExist());
    }
}
