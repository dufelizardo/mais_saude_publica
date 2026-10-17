package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Equipe;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.TipoEquipe;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.CategoriaProgramaRepository;
import com.edufelizardo.maissaudepublica.repositories.EquipeRepository;
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
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Catálogo de programas de saúde, fatia P1 (ADR-0118): carga inicial, cadastro com público-alvo e suas validações,
 * categorias, participação de unidades e equipes como histórico, e a consulta pública.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProgramaControllerTest {

    private static final String URL = "/api/v1/programa/";
    private static final String CATEGORIA = "/api/v1/categoria-programa/";
    private static final String PREFIXO = "Programa Teste ";
    private static final String CODIGO = "TESTE_PG_";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @Autowired
    private EquipeRepository equipeRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private CategoriaProgramaRepository categoriaRepository;

    private UnidadeDeSaude ubs;
    private UUID cronicos;

    @BeforeEach
    void seed() {
        limpar();
        ubs = unidade("UBS Vila Esperança", TipoUnidadeDeSaude.UBS);
        cronicos = categoriaRepository.findByNomeIgnoreCase("Crônicos").orElseThrow().getUuid();
        Profissional p = new Profissional();
        p.setMatricula("PG-COORD");
        p.setCpf("90011122233");
        p.setNome(PREFIXO + "Dra. Helena");
        p.setAtivo(true);
        profissionalRepository.save(p);
    }

    @AfterEach
    void limpar() {
        jdbc.update("delete from tb_participacao_programa where programa_id in (select uuid from tb_programa where codigo like ?)", CODIGO + "%");
        List<UUID> unidades = unidadeDeSaudeRepository.findAll().stream()
                .filter(u -> u.getNome() != null && u.getNome().startsWith(PREFIXO)).map(UnidadeDeSaude::getUuid).toList();
        for (UUID u : unidades) {
            jdbc.update("delete from tb_participacao_programa where unidade_id = ?", u);
            jdbc.update("delete from tb_equipe where unidade_id = ?", u);
        }
        jdbc.update("delete from tb_programa_condicao where programa_id in (select uuid from tb_programa where codigo like ?)", CODIGO + "%");
        jdbc.update("delete from tb_programa where codigo like ?", CODIGO + "%");
        jdbc.update("delete from tb_categoria_programa where nome like ?", PREFIXO + "%");
        unidadeDeSaudeRepository.deleteAllById(unidades);
        profissionalRepository.findByMatricula("PG-COORD").ifPresent(profissionalRepository::delete);
    }

    private UnidadeDeSaude unidade(String nome, TipoUnidadeDeSaude tipo) {
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + nome);
        u.setTipo(tipo);
        u.setAtivo(true);
        return unidadeDeSaudeRepository.save(u);
    }

    private Equipe equipe(UnidadeDeSaude u, String nome) {
        Equipe e = new Equipe();
        e.setUnidade(u);
        e.setTipo(TipoEquipe.ESF);
        e.setNome(nome);
        e.setAtiva(true);
        return equipeRepository.save(e);
    }

    private String programa(String codigo, String extra) {
        return """
                {"codigo": "%s", "nome": "%s", "categoriaId": "%s"%s}""".formatted(codigo, PREFIXO + codigo, cronicos, extra);
    }

    private ResultActions criar(String corpo) throws Exception {
        return mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    private ResultActions participar(String programa, UUID unidade, UUID equipe) throws Exception {
        return mockMvc.perform(post(URL + programa + "/participacao").contentType(MediaType.APPLICATION_JSON)
                .content("{\"unidadeId\": \"%s\", \"equipeId\": %s}".formatted(unidade, equipe == null ? "null" : "\"" + equipe + "\"")));
    }

    @Test
    void cargaInicialTrazQuatroCategoriasETrezeProgramas() throws Exception {
        mockMvc.perform(get(CATEGORIA)).andExpect(status().isOk())
                .andExpect(jsonPath("$[*].nome", hasItems("Atenção Básica", "Crônicos", "Ciclos de vida", "Saúde Mental")));
        mockMvc.perform(get(URL)).andExpect(status().isOk())
                .andExpect(jsonPath("$[*].codigo", hasItems("SAUDE_DA_FAMILIA", "HIPERDIA", "PRE_NATAL", "SAUDE_DO_IDOSO", "SAUDE_MENTAL", "TABAGISMO")))
                .andExpect(jsonPath("$[?(@.codigo == 'HIPERDIA')].condicoes[0]", hasItem("I10")))
                .andExpect(jsonPath("$[?(@.codigo == 'SAUDE_DO_IDOSO')].publico", hasItem("60 anos ou mais")))
                .andExpect(jsonPath("$[?(@.codigo == 'PRE_NATAL')].publico", hasItem("Gestantes")))
                .andExpect(jsonPath("$[?(@.codigo == 'SAUDE_MENTAL')].sensivel", hasItem(true)));
    }

    @Test
    void cadastroComPublicoAlvoEValidacoes() throws Exception {
        String corpo = criar(programa("TESTE_PG_dpoc", ", \"coordenadorMatricula\": \"PG-COORD\", \"periodicidade\": \"TRIMESTRAL\", "
                + "\"idadeMinima\": 40, \"condicoes\": [\"j44\", \"R95\", \"J44\"], \"sensivel\": false"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.resumo.codigo").value("TESTE_PG_DPOC"))
                .andExpect(jsonPath("$.resumo.situacao").value("ATIVO"))
                .andExpect(jsonPath("$.resumo.abrangencia").value("MUNICIPAL"))
                .andExpect(jsonPath("$.resumo.coordenadorNome").value(PREFIXO + "Dra. Helena"))
                .andExpect(jsonPath("$.resumo.condicoes", contains("J44", "R95")))
                .andExpect(jsonPath("$.resumo.publico").value("40 anos ou mais · condições J44, R95"))
                .andReturn().getResponse().getContentAsString();
        String id = JsonPath.read(corpo, "$.resumo.uuid");

        criar(programa("TESTE_PG_DPOC", "")).andExpect(status().isConflict());
        criar(programa("TESTE_PG_X1", ", \"condicoes\": [\"hipertensao\"]")).andExpect(status().isBadRequest());
        criar(programa("TESTE_PG_X2", ", \"idadeMinima\": 60, \"idadeMaxima\": 10")).andExpect(status().isBadRequest());
        criar(programa("TESTE_PG_X3", ", \"situacao\": \"ENCERRADO\"")).andExpect(status().isBadRequest());
        criar(programa("TESTE_PG_X4", ", \"sexo\": \"IGNORADO\"")).andExpect(status().isBadRequest());
        criar(programa("TESTE_PG_X5", ", \"coordenadorMatricula\": \"NAO-EXISTE\"")).andExpect(status().isNotFound());
        criar(programa("TESTE PG", "")).andExpect(status().isBadRequest());

        mockMvc.perform(patch(URL + id).contentType(MediaType.APPLICATION_JSON)
                        .content(programa("TESTE_PG_DPOC", ", \"sexo\": \"FEMININO\", \"idadeMinima\": 25, \"idadeMaxima\": 64")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resumo.publico").value("Mulheres de 25 a 64 anos"))
                .andExpect(jsonPath("$.resumo.coordenadorNome").doesNotExist());
    }

    @Test
    void categoriaComNomeUnicoEInativaSemProgramaNovo() throws Exception {
        String corpo = mockMvc.perform(post(CATEGORIA).contentType(MediaType.APPLICATION_JSON).content("{\"nome\": \"" + PREFIXO + "Saúde do Trabalhador\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ativa").value(true))
                .andReturn().getResponse().getContentAsString();
        String id = JsonPath.read(corpo, "$.uuid");
        mockMvc.perform(post(CATEGORIA).contentType(MediaType.APPLICATION_JSON).content("{\"nome\": \"" + PREFIXO.toUpperCase() + "SAÚDE DO TRABALHADOR\"}"))
                .andExpect(status().isConflict());
        mockMvc.perform(patch(CATEGORIA + id).contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\": \"" + PREFIXO + "Saúde do Trabalhador\", \"ativa\": false}")).andExpect(status().isOk());
        criar("""
                {"codigo": "TESTE_PG_LER", "nome": "LER/DORT", "categoriaId": "%s"}""".formatted(id)).andExpect(status().isUnprocessableEntity());
    }

    @Test
    void participacaoDeUnidadeEEquipeComoHistorico() throws Exception {
        String corpo = criar(programa("TESTE_PG_HD", "")).andReturn().getResponse().getContentAsString();
        String id = JsonPath.read(corpo, "$.resumo.uuid");
        Equipe sf12 = equipe(ubs, "SF-12");
        UnidadeDeSaude outra = unidade("UBS Centro", TipoUnidadeDeSaude.UBS);
        Equipe deOutra = equipe(outra, "SF-01");
        UnidadeDeSaude municipio = unidade("Município", TipoUnidadeDeSaude.MUNICIPAL);

        participar(id, ubs.getUuid(), null).andExpect(status().isOk())
                .andExpect(jsonPath("$.resumo.unidades").value(1));
        participar(id, ubs.getUuid(), null).andExpect(status().isConflict());
        String comEquipe = participar(id, ubs.getUuid(), sf12.getUuid()).andExpect(status().isOk())
                .andExpect(jsonPath("$.participacoes.length()").value(2))
                .andExpect(jsonPath("$.resumo.unidades").value(1))
                .andReturn().getResponse().getContentAsString();
        participar(id, ubs.getUuid(), deOutra.getUuid()).andExpect(status().isUnprocessableEntity());
        participar(id, municipio.getUuid(), null).andExpect(status().isUnprocessableEntity());

        mockMvc.perform(get(URL).param("unidadeId", ubs.getUuid().toString()))
                .andExpect(jsonPath("$[*].codigo", contains("TESTE_PG_HD")));

        List<String> ids = JsonPath.read(comEquipe, "$.participacoes[?(@.equipeNome == 'SF-12')].uuid");
        String encerrar = URL + id + "/participacao/" + ids.get(0) + "/encerramento";
        mockMvc.perform(post(encerrar).contentType(MediaType.APPLICATION_JSON).content("{\"motivo\": \"\"}")).andExpect(status().isBadRequest());
        mockMvc.perform(post(encerrar).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\": \"Equipe desfeita\", \"fim\": \"%s\"}".formatted(LocalDate.now().plusDays(1))))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post(encerrar).contentType(MediaType.APPLICATION_JSON).content("{\"motivo\": \"Equipe desfeita\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.participacoes.length()").value(1))
                .andExpect(jsonPath("$.participacoesEncerradas[0].motivoFim").value("Equipe desfeita"));
        mockMvc.perform(post(encerrar).contentType(MediaType.APPLICATION_JSON).content("{\"motivo\": \"De novo\"}"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void consultaPublicaSoComProgramasAtivosESemCodigos() throws Exception {
        String corpo = criar(programa("TESTE_PG_PUB", ", \"idadeMinima\": 60, \"condicoes\": [\"I10\"]")).andReturn().getResponse().getContentAsString();
        String id = JsonPath.read(corpo, "$.resumo.uuid");
        participar(id, ubs.getUuid(), null).andExpect(status().isOk());
        criar(programa("TESTE_PG_SUSP", ", \"situacao\": \"SUSPENSO\"")).andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/publico/programa")).andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.nome == '%s')].publico".formatted(PREFIXO + "TESTE_PG_PUB"), hasItem("60 anos ou mais")))
                .andExpect(jsonPath("$[?(@.nome == '%s')].unidades[0]".formatted(PREFIXO + "TESTE_PG_PUB"), hasItem(PREFIXO + "UBS Vila Esperança")))
                .andExpect(jsonPath("$[*].nome", not(hasItem(PREFIXO + "TESTE_PG_SUSP"))))
                .andExpect(jsonPath("$[0].codigo").doesNotExist())
                .andExpect(jsonPath("$[0].condicoes").doesNotExist());
    }
}
