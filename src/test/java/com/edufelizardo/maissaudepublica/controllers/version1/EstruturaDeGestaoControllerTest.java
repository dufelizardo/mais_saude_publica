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
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Coordenações e gerência, fatia BI5 (ADR-0175): hierarquia, regional, quem responde como histórico e os indicadores somados por regional. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EstruturaDeGestaoControllerTest {

    private static final String URL = "/api/v1/gestao/";
    private static final String PREFIXO = "Gestao Teste ";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private UnidadeDeSaudeRepository unidadeRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    private UnidadeDeSaude regional;
    private UnidadeDeSaude ubsDaRegional;
    private UnidadeDeSaude ubsFora;
    private final LocalDate hoje = LocalDate.now();

    @BeforeEach
    void seed() {
        limpar();
        regional = unidade("Regional Centro", TipoUnidadeDeSaude.REGIONAL, null);
        ubsDaRegional = unidade("UBS Centro", TipoUnidadeDeSaude.UBS, regional);
        ubsFora = unidade("UBS Fora", TipoUnidadeDeSaude.UBS, null);
        profissional("TESTE-BI5-1", true);
        profissional("TESTE-BI5-2", true);
        profissional("TESTE-BI5-3", false);
    }

    @AfterEach
    void limpar() {
        jdbc.update("delete from tb_titularidade_area_gestao where area_id in (select uuid from tb_area_gestao where nome like ?)", PREFIXO + "%");
        jdbc.update("update tb_area_gestao set superior_id = null where nome like ?", PREFIXO + "%");
        jdbc.update("delete from tb_area_gestao where nome like ?", PREFIXO + "%");
        jdbc.update("delete from tb_notificacao where unidade_id in (select uuid from tb_unidade_de_saude where nome like ?)", PREFIXO + "%");
        jdbc.update("delete from tb_paciente where nome like ?", PREFIXO + "%");
        jdbc.update("delete from tb_profissional where matricula like 'TESTE-BI5-%'");
        jdbc.update("update tb_unidade_de_saude set supervisao_regional_id = null where nome like ?", PREFIXO + "%");
        jdbc.update("delete from tb_unidade_de_saude where nome like ?", PREFIXO + "%");
    }

    private UnidadeDeSaude unidade(String nome, TipoUnidadeDeSaude tipo, UnidadeDeSaude supervisao) {
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + nome);
        u.setTipo(tipo);
        u.setAtivo(true);
        u.setSupervisaoRegional(supervisao);
        return unidadeRepository.save(u);
    }

    private void profissional(String matricula, boolean ativo) {
        Profissional p = new Profissional();
        p.setMatricula(matricula);
        p.setCpf("00000000000");
        p.setNome(PREFIXO + matricula);
        p.setAtivo(ativo);
        profissionalRepository.save(p);
    }

    private ResultActions criar(String tipo, String nome, Object superior, Object regionalId) throws Exception {
        String corpo = "{\"tipo\": \"%s\", \"tema\": \"ATENCAO_BASICA\", \"nome\": \"%s\"%s%s}".formatted(tipo, PREFIXO + nome,
                superior == null ? "" : ", \"superiorId\": \"" + superior + "\"", regionalId == null ? "" : ", \"regionalId\": \"" + regionalId + "\"");
        return mockMvc.perform(post(URL + "area").contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    private String id(ResultActions r) throws Exception {
        return JsonPath.read(r.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString(), "$.uuid");
    }

    private ResultActions designar(String area, String matricula, String papel, LocalDate inicio) throws Exception {
        return mockMvc.perform(post(URL + "area/" + area + "/titularidade").contentType(MediaType.APPLICATION_JSON)
                .content("{\"matriculaProfissional\": \"%s\", \"papel\": \"%s\", \"inicio\": \"%s\", \"ato\": \"Portaria 12/2026\"}".formatted(matricula, papel, inicio)));
    }

    @Test
    void hierarquiaERegional() throws Exception {
        String gerencia = id(criar("GERENCIA", "Atenção Básica", null, null));
        criar("GERENCIA", "Com superior", gerencia, null).andExpect(status().isUnprocessableEntity());
        criar("GERENCIA", "Com regional", null, regional.getUuid()).andExpect(status().isUnprocessableEntity());
        String coordenacao = JsonPath.read(criar("COORDENACAO", "Regional Centro", gerencia, regional.getUuid()).andExpect(status().isCreated())
                .andExpect(jsonPath("$.unidades").value(1))
                .andExpect(jsonPath("$.unidadesDaRegional[0].nome").value(PREFIXO + "UBS Centro"))
                .andReturn().getResponse().getContentAsString(), "$.uuid");
        criar("COORDENACAO", "Outra do Centro", gerencia, regional.getUuid()).andExpect(status().isConflict());
        criar("COORDENACAO", "Na UBS", gerencia, ubsFora.getUuid()).andExpect(status().isUnprocessableEntity());
        criar("SUPERVISAO", "Sem superior", null, null).andExpect(status().isUnprocessableEntity());
        criar("SUPERVISAO", "Vigilância Centro", coordenacao, null).andExpect(status().isCreated())
                .andExpect(jsonPath("$.regionalNome").value(PREFIXO + "Regional Centro"));
        mockMvc.perform(patch(URL + "area/" + gerencia).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipo\": \"GERENCIA\", \"tema\": \"ATENCAO_BASICA\", \"nome\": \"%sAtenção Básica\", \"ativa\": false}".formatted(PREFIXO)))
                .andExpect(status().isUnprocessableEntity());
        mockMvc.perform(patch(URL + "area/" + gerencia).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipo\": \"COORDENACAO\", \"tema\": \"ATENCAO_BASICA\", \"nome\": \"%sX\"}".formatted(PREFIXO)))
                .andExpect(status().isUnprocessableEntity());
        mockMvc.perform(get(URL + "regional")).andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.nome == '" + PREFIXO + "Regional Centro')]").isNotEmpty());
    }

    @Test
    void quemRespondeFicaComoHistorico() throws Exception {
        String area = id(criar("GERENCIA", "Urgência", null, null));
        designar(area, "TESTE-BI5-1", "TITULAR", hoje.minusDays(30)).andExpect(status().isOk())
                .andExpect(jsonPath("$.titular.matricula").value("TESTE-BI5-1"));
        designar(area, "TESTE-BI5-1", "SUBSTITUTO", hoje.minusDays(10)).andExpect(status().isUnprocessableEntity());
        designar(area, "TESTE-BI5-3", "SUBSTITUTO", hoje).andExpect(status().isUnprocessableEntity());
        designar(area, "TESTE-BI5-2", "TITULAR", hoje.minusDays(40)).andExpect(status().isUnprocessableEntity());
        String r = designar(area, "TESTE-BI5-2", "TITULAR", hoje).andExpect(status().isOk())
                .andExpect(jsonPath("$.titular.matricula").value("TESTE-BI5-2"))
                .andExpect(jsonPath("$.historico.length()").value(2))
                .andExpect(jsonPath("$.historico[1].fim").value(hoje.minusDays(1).toString()))
                .andReturn().getResponse().getContentAsString();
        String vigente = JsonPath.read(r, "$.historico[0].uuid");
        mockMvc.perform(post(URL + "titularidade/" + vigente + "/encerramento").contentType(MediaType.APPLICATION_JSON).content("{\"fim\": \"" + hoje.minusDays(5) + "\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post(URL + "titularidade/" + vigente + "/encerramento").contentType(MediaType.APPLICATION_JSON).content("{\"fim\": \"" + hoje + "\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(post(URL + "titularidade/" + vigente + "/encerramento").contentType(MediaType.APPLICATION_JSON).content("{\"fim\": \"" + hoje + "\"}"))
                .andExpect(status().isUnprocessableEntity());
        designar(area, "NAO-EXISTE", "TITULAR", hoje.plusDays(1)).andExpect(status().isNotFound());
    }

    @Test
    void indicadoresDaRegionalSomamAsUnidadesAbaixo() throws Exception {
        Paciente p = new Paciente();
        p.setNome(PREFIXO + "Paciente");
        p.setCpf("93200000175");
        p.setDataNascimento(LocalDate.of(1980, 1, 1));
        p.setSexo(Sexo.FEMININO);
        p.setAtivo(true);
        UUID paciente = pacienteRepository.save(p).getUuid();
        String agravo = jdbc.queryForObject("select cast(uuid as varchar) from tb_agravo where not sensivel order by codigo limit 1", String.class);
        String n = "insert into tb_notificacao (uuid, agravo_id, paciente_id, unidade_id, data_notificacao, prazo_encerramento, registrada_em, origem, situacao) "
                + "values (gen_random_uuid(), cast(? as uuid), ?, ?, ?, ?, now(), 'ATENDIMENTO', 'NOTIFICADA')";
        jdbc.update(n, agravo, paciente, ubsDaRegional.getUuid(), hoje.minusDays(1), hoje.plusDays(30));
        jdbc.update(n, agravo, paciente, ubsFora.getUuid(), hoje.minusDays(1), hoje.plusDays(30));

        mockMvc.perform(get("/api/v1/indicadores-rede/vigilancia").param("de", hoje.minusDays(7).toString()).param("ate", hoje.toString())
                        .param("unidadeId", regional.getUuid().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notificadas").value(1))
                .andExpect(jsonPath("$.abrangencia").value(PREFIXO + "Regional Centro"));
        mockMvc.perform(get("/api/v1/indicadores-rede/vigilancia").param("de", hoje.minusDays(7).toString()).param("ate", hoje.toString())
                        .param("unidadeId", ubsFora.getUuid().toString()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.notificadas").value(1));
    }
}
