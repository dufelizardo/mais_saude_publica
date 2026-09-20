package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Cargo;
import com.edufelizardo.maissaudepublica.models.CategoriaSalarial;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.CargoRepository;
import com.edufelizardo.maissaudepublica.repositories.CategoriaSalarialRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import com.edufelizardo.maissaudepublica.repositories.VagaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes do VagaController (módulo RH, Fase 8/Recrutamento — ver docs/rh/MODELO-RH.md). Fixtures
 * de {@link UnidadeDeSaude}/{@link Cargo}/{@link CategoriaSalarial} via repositório direto;
 * limpeza por ID rastreado.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class VagaControllerTest {

    private static final String BASE_URL = "/api/v1/vaga/";
    private static final String PREFIXO_NOME_TESTE = "Fixture Vaga - ";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private VagaRepository vagaRepository;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @Autowired
    private CargoRepository cargoRepository;

    @Autowired
    private CategoriaSalarialRepository categoriaSalarialRepository;

    private final List<UUID> vagaIdsCriadas = new ArrayList<>();
    private final List<UUID> unidadeIdsCriadas = new ArrayList<>();
    private final List<UUID> cargoIdsCriados = new ArrayList<>();
    private final List<UUID> categoriaIdsCriadas = new ArrayList<>();

    @AfterEach
    void limparDadosDeTeste() {
        vagaRepository.deleteAllById(vagaIdsCriadas);
        cargoRepository.deleteAllById(cargoIdsCriados);
        categoriaSalarialRepository.deleteAllById(categoriaIdsCriadas);
        unidadeDeSaudeRepository.deleteAllById(unidadeIdsCriadas);
    }

    private UUID criarUnidadeFixture(String sufixo) {
        UnidadeDeSaude unidade = new UnidadeDeSaude();
        unidade.setNome(PREFIXO_NOME_TESTE + "Unidade " + sufixo);
        unidade.setTipo(TipoUnidadeDeSaude.UBS);
        unidade.setAtivo(true);
        UUID id = unidadeDeSaudeRepository.save(unidade).getUuid();
        unidadeIdsCriadas.add(id);
        return id;
    }

    private UUID criarCargoFixture(String sufixo) {
        CategoriaSalarial categoria = new CategoriaSalarial();
        categoria.setNome(PREFIXO_NOME_TESTE + "Categoria " + sufixo);
        categoria = categoriaSalarialRepository.save(categoria);
        categoriaIdsCriadas.add(categoria.getUuid());

        Cargo cargo = cargoRepository.save(new Cargo(categoria, PREFIXO_NOME_TESTE + "Cargo " + sufixo));
        cargoIdsCriados.add(cargo.getUuid());
        return cargo.getUuid();
    }

    @Test
    void deveCriarComSucesso() throws Exception {
        UUID unidadeId = criarUnidadeFixture("01");
        UUID cargoId = criarCargoFixture("01");
        String body = """
                {
                  "unidadeId": "%s",
                  "cargoId": "%s",
                  "quantidade": 2,
                  "status": "ABERTA"
                }
                """.formatted(unidadeId, cargoId);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Vaga criada com sucesso!"));

        vagaRepository.findAll().forEach(v -> vagaIdsCriadas.add(v.getUuid()));
    }

    @Test
    void deveRetornarNotFoundAoCriarComUnidadeInexistente() throws Exception {
        UUID cargoId = criarCargoFixture("02");
        String body = """
                {
                  "unidadeId": "%s",
                  "cargoId": "%s",
                  "quantidade": 1,
                  "status": "ABERTA"
                }
                """.formatted(UUID.randomUUID(), cargoId);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornarNotFoundAoCriarComCargoInexistente() throws Exception {
        UUID unidadeId = criarUnidadeFixture("03");
        String body = """
                {
                  "unidadeId": "%s",
                  "cargoId": "%s",
                  "quantidade": 1,
                  "status": "ABERTA"
                }
                """.formatted(unidadeId, UUID.randomUUID());

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveBuscarPorId() throws Exception {
        UUID unidadeId = criarUnidadeFixture("04");
        UUID cargoId = criarCargoFixture("04");
        String body = """
                {
                  "unidadeId": "%s",
                  "cargoId": "%s",
                  "quantidade": 3,
                  "status": "EM_ANDAMENTO"
                }
                """.formatted(unidadeId, cargoId);

        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());

        UUID vagaId = vagaRepository.findAll().stream()
                .filter(v -> v.getQuantidade() == 3)
                .findFirst()
                .orElseThrow()
                .getUuid();
        vagaIdsCriadas.add(vagaId);

        mockMvc.perform(get(BASE_URL + vagaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidade").value(3))
                .andExpect(jsonPath("$.status").value("EM_ANDAMENTO"));
    }

    @Test
    void deveRetornarNotFoundAoBuscarIdInexistente() throws Exception {
        mockMvc.perform(get(BASE_URL + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveAtualizarComSucesso() throws Exception {
        UUID unidadeId = criarUnidadeFixture("05");
        UUID cargoId = criarCargoFixture("05");
        String bodyCriacao = """
                {
                  "unidadeId": "%s",
                  "cargoId": "%s",
                  "quantidade": 5,
                  "status": "ABERTA"
                }
                """.formatted(unidadeId, cargoId);

        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(bodyCriacao))
                .andExpect(status().isCreated());

        UUID vagaId = vagaRepository.findAll().stream()
                .filter(v -> v.getQuantidade() == 5)
                .findFirst()
                .orElseThrow()
                .getUuid();
        vagaIdsCriadas.add(vagaId);

        String bodyAtualizacao = """
                {
                  "unidadeId": "%s",
                  "cargoId": "%s",
                  "quantidade": 5,
                  "status": "FECHADA"
                }
                """.formatted(unidadeId, cargoId);

        mockMvc.perform(patch(BASE_URL + vagaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyAtualizacao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Vaga atualizada com sucesso!"));

        mockMvc.perform(get(BASE_URL + vagaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FECHADA"));
    }

    @Test
    void deveHerdarDescricaoDoCargo() throws Exception {
        UUID unidadeId = criarUnidadeFixture("07");
        UUID cargoId = criarCargoFixture("07");
        Cargo cargo = cargoRepository.findById(cargoId).orElseThrow();
        cargo.setDescricao("Atuação em plantões noturnos, 12x36.");
        cargoRepository.save(cargo);

        String body = """
                {
                  "unidadeId": "%s",
                  "cargoId": "%s",
                  "quantidade": 1,
                  "status": "ABERTA"
                }
                """.formatted(unidadeId, cargoId);

        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());

        UUID vagaId = vagaRepository.findAll().stream()
                .filter(v -> v.getCargo().getUuid().equals(cargoId))
                .findFirst()
                .orElseThrow()
                .getUuid();
        vagaIdsCriadas.add(vagaId);

        mockMvc.perform(get(BASE_URL + vagaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cargoDescricao").value("Atuação em plantões noturnos, 12x36."));
    }

    @Test
    void deveRetornarNotFoundAoAtualizarVagaInexistente() throws Exception {
        UUID unidadeId = criarUnidadeFixture("06");
        UUID cargoId = criarCargoFixture("06");
        String body = """
                {
                  "unidadeId": "%s",
                  "cargoId": "%s",
                  "quantidade": 1,
                  "status": "FECHADA"
                }
                """.formatted(unidadeId, cargoId);

        mockMvc.perform(patch(BASE_URL + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }
}
