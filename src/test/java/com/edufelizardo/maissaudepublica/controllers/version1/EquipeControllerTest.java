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
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Equipes (ADR-0103): cadastro com nome e INE únicos, membros com lotação na unidade, composição mínima da eSF, uma eSF por
 * profissional, saída como histórico, coordenação e apoio da eMulti.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EquipeControllerTest {

    private static final String URL = "/api/v1/equipe/";
    private static final String PREFIXO = "Equipe Teste ";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

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
        ubs = unidade("UBS Vila Esperança");
        UnidadeDeSaude outra = unidade("UBS Centro");
        CategoriaSalarial categoria = new CategoriaSalarial();
        categoria.setNome(PREFIXO + "Categoria");
        categoria = categoriaSalarialRepository.save(categoria);
        Cargo cargo = cargoRepository.save(new Cargo(categoria, PREFIXO + "Cargo"));
        for (String m : List.of("MED", "ENF", "TEC", "ACS1", "ACS2", "MED2")) {
            lotacaoRepository.save(new Lotacao(profissional(m), ubs, cargo, 40, LocalDate.now().minusYears(1), "admissão"));
        }
        lotacaoRepository.save(new Lotacao(profissional("OUTRA"), outra, cargo, 40, LocalDate.now().minusYears(1), "admissão"));
    }

    @AfterEach
    void limpar() {
        List<UUID> unidades = unidadeDeSaudeRepository.findAll().stream()
                .filter(u -> u.getNome() != null && u.getNome().startsWith(PREFIXO)).map(UnidadeDeSaude::getUuid).toList();
        for (UUID u : unidades) {
            jdbc.update("delete from tb_equipe_apoio where equipe_id in (select uuid from tb_equipe where unidade_id = ?) "
                    + "or apoiada_id in (select uuid from tb_equipe where unidade_id = ?)", u, u);
            jdbc.update("delete from tb_membro_equipe where equipe_id in (select uuid from tb_equipe where unidade_id = ?)", u);
            jdbc.update("delete from tb_equipe where unidade_id = ?", u);
            jdbc.update("delete from tb_lotacao where unidade_id = ?", u);
        }
        unidadeDeSaudeRepository.deleteAllById(unidades);
        profissionalRepository.deleteAll(profissionalRepository.findAll().stream()
                .filter(p -> p.getMatricula() != null && p.getMatricula().startsWith("EQ-")).toList());
        jdbc.update("delete from tb_cargo where nome like ?", PREFIXO + "%");
        jdbc.update("delete from tb_categoria_salarial where nome like ?", PREFIXO + "%");
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
        p.setMatricula("EQ-" + sufixo);
        p.setCpf(String.valueOf(90000000000L + Math.abs(sufixo.hashCode() % 999999)));
        p.setNome(PREFIXO + sufixo);
        p.setAtivo(true);
        return profissionalRepository.save(p);
    }

    private ResultActions criar(String nome, String tipo, String ine, String apoiadas) throws Exception {
        return mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("""
                {"unidadeId": "%s", "tipo": "%s", "nome": "%s", "ine": %s, "microareas": "14, 17, 18", "apoiadasIds": [%s]}
                """.formatted(ubs.getUuid(), tipo, nome, ine == null ? "null" : "\"" + ine + "\"", apoiadas)));
    }

    private UUID criada(String nome, String tipo) throws Exception {
        String corpo = criar(nome, tipo, null, "").andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return UUID.fromString(JsonPath.read(corpo, "$.resumo.uuid"));
    }

    private ResultActions membro(UUID equipe, String matricula, String funcao) throws Exception {
        return mockMvc.perform(post(URL + equipe + "/membro").contentType(MediaType.APPLICATION_JSON).content("""
                {"profissionalMatricula": "EQ-%s", "funcao": "%s", "microarea": "14"}""".formatted(matricula, funcao)));
    }

    @Test
    void cadastroComNomeEIneUnicos() throws Exception {
        criar("SF-12", "ESF", "0001234567", "").andExpect(status().isCreated())
                .andExpect(jsonPath("$.resumo.ativa").value(true))
                .andExpect(jsonPath("$.resumo.completa").value(false))
                .andExpect(jsonPath("$.resumo.faltando.length()").value(4));
        criar("SF-12", "ESF", null, "").andExpect(status().isConflict());
        criar("SF-15", "ESF", "0001234567", "").andExpect(status().isConflict());
        criar("SF-16", "ESF", "123", "").andExpect(status().isBadRequest());
    }

    @Test
    void membrosComposicaoMinimaExclusividadeESaida() throws Exception {
        UUID sf12 = criada("SF-12", "ESF");
        membro(sf12, "MED", "MEDICO").andExpect(status().isOk());
        membro(sf12, "ENF", "ENFERMEIRO").andExpect(status().isOk());
        membro(sf12, "TEC", "TECNICO_ENFERMAGEM").andExpect(status().isOk())
                .andExpect(jsonPath("$.resumo.completa").value(false))
                .andExpect(jsonPath("$.resumo.faltando", contains("ACS")));
        String corpo = membro(sf12, "ACS1", "ACS").andExpect(status().isOk())
                .andExpect(jsonPath("$.resumo.completa").value(true))
                .andExpect(jsonPath("$.resumo.agentesComunitarios").value(1))
                .andReturn().getResponse().getContentAsString();
        String acs = JsonPath.read(corpo, "$.membros[?(@.matricula == 'EQ-ACS1')].uuid").toString().replaceAll("[\\[\\]\"]", "");

        membro(sf12, "OUTRA", "MEDICO").andExpect(status().isUnprocessableEntity());
        membro(sf12, "MED", "MEDICO").andExpect(status().isConflict());
        UUID sf15 = criada("SF-15", "ESF");
        membro(sf15, "MED", "MEDICO").andExpect(status().isConflict());
        membro(sf15, "MED2", "MEDICO").andExpect(status().isOk());

        mockMvc.perform(get(URL).param("unidadeId", ubs.getUuid().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.equipes").value(2))
                .andExpect(jsonPath("$.profissionaisVinculados").value(5))
                .andExpect(jsonPath("$.incompletas").value(1))
                .andExpect(jsonPath("$.porTipo.ESF").value(2));

        // Coordenação: só membro vigente.
        String edicao = """
                {"unidadeId": "%s", "tipo": "%s", "nome": "SF-12", "coordenadorMatricula": "%s", "reuniaoDia": "MONDAY",
                 "reuniaoInicio": "07:00", "reuniaoFim": "08:30", "reuniaoLocal": "Sala 2"}""";
        mockMvc.perform(patch(URL + sf12).contentType(MediaType.APPLICATION_JSON).content(edicao.formatted(ubs.getUuid(), "ESF", "EQ-OUTRA")))
                .andExpect(status().isUnprocessableEntity());
        mockMvc.perform(patch(URL + sf12).contentType(MediaType.APPLICATION_JSON).content(edicao.formatted(ubs.getUuid(), "EAB", "EQ-ENF")))
                .andExpect(status().isUnprocessableEntity());
        mockMvc.perform(patch(URL + sf12).contentType(MediaType.APPLICATION_JSON).content(edicao.formatted(ubs.getUuid(), "ESF", "EQ-ENF")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resumo.coordenadorNome").value(PREFIXO + "ENF"))
                .andExpect(jsonPath("$.reuniaoLocal").value("Sala 2"));

        String saida = "{\"motivo\": \"%s\"}";
        mockMvc.perform(post(URL + "membro/" + acs + "/saida").contentType(MediaType.APPLICATION_JSON).content(saida.formatted("Transferida para outra UBS")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resumo.completa").value(false))
                .andExpect(jsonPath("$.antigos[0].matricula").value("EQ-ACS1"))
                .andExpect(jsonPath("$.antigos[0].motivoSaida").value("Transferida para outra UBS"));
        mockMvc.perform(post(URL + "membro/" + acs + "/saida").contentType(MediaType.APPLICATION_JSON).content(saida.formatted("De novo")))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void emultiApoiaSoEsfEEab() throws Exception {
        UUID sf12 = criada("SF-12", "ESF");
        UUID esb = criada("SB-01", "ESB");
        criar("NASF Centro", "EMULTI", null, "\"" + esb + "\"").andExpect(status().isUnprocessableEntity());
        criar("SB-02", "ESB", null, "\"" + sf12 + "\"").andExpect(status().isUnprocessableEntity());
        criar("eMulti Centro", "EMULTI", null, "\"" + sf12 + "\"").andExpect(status().isCreated())
                .andExpect(jsonPath("$.apoiadas[0].nome").value("SF-12"))
                .andExpect(jsonPath("$.resumo.completa").value(true));
        mockMvc.perform(get(URL + sf12)).andExpect(jsonPath("$.apoiadaPor[*].nome", hasItem("eMulti Centro")));
    }
}
