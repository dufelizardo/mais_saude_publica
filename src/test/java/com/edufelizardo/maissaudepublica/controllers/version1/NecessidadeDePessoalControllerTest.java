package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Cargo;
import com.edufelizardo.maissaudepublica.models.CategoriaSalarial;
import com.edufelizardo.maissaudepublica.models.Setor;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.Vaga;
import com.edufelizardo.maissaudepublica.models.enuns.StatusVaga;
import com.edufelizardo.maissaudepublica.models.enuns.TipoSetor;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.CargoRepository;
import com.edufelizardo.maissaudepublica.repositories.CategoriaSalarialRepository;
import com.edufelizardo.maissaudepublica.repositories.NecessidadeDePessoalRepository;
import com.edufelizardo.maissaudepublica.repositories.SetorRepository;
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
 * Testes do NecessidadeDePessoalController (Setor Administrativo Adaptativo, Fase 6 — ver
 * docs/adr/0036-necessidade-de-pessoal-encaminhada-ao-rh.md). Fixtures de
 * {@link UnidadeDeSaude}/{@link Setor}/{@link Cargo}/{@link Vaga} via repositório direto; limpeza
 * por ID rastreado.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NecessidadeDePessoalControllerTest {

    private static final String BASE_URL = "/api/v1/necessidade-de-pessoal/";
    private static final String PREFIXO_NOME_TESTE = "Fixture NecPessoal - ";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NecessidadeDePessoalRepository necessidadeDePessoalRepository;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @Autowired
    private SetorRepository setorRepository;

    @Autowired
    private CargoRepository cargoRepository;

    @Autowired
    private CategoriaSalarialRepository categoriaSalarialRepository;

    @Autowired
    private VagaRepository vagaRepository;

    private final List<UUID> necessidadeIdsCriadas = new ArrayList<>();
    private final List<UUID> vagaIdsCriadas = new ArrayList<>();
    private final List<UUID> setorIdsCriados = new ArrayList<>();
    private final List<UUID> cargoIdsCriados = new ArrayList<>();
    private final List<UUID> categoriaIdsCriadas = new ArrayList<>();
    private final List<UUID> unidadeIdsCriadas = new ArrayList<>();

    @AfterEach
    void limparDadosDeTeste() {
        necessidadeDePessoalRepository.deleteAllById(necessidadeIdsCriadas);
        vagaRepository.deleteAllById(vagaIdsCriadas);
        setorRepository.deleteAllById(setorIdsCriados);
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

    private UUID criarSetorFixture(String sufixo, UUID unidadeId) {
        UnidadeDeSaude unidade = unidadeDeSaudeRepository.findById(unidadeId).orElseThrow();
        Setor setor = new Setor(unidade, PREFIXO_NOME_TESTE + "Setor " + sufixo, "ROBOT-NECPESSOAL-" + sufixo,
                TipoSetor.ADMINISTRATIVO, true, null);
        UUID id = setorRepository.save(setor).getUuid();
        setorIdsCriados.add(id);
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

    private UUID criarVagaFixture(String sufixo, UUID unidadeId, UUID cargoId) {
        UnidadeDeSaude unidade = unidadeDeSaudeRepository.findById(unidadeId).orElseThrow();
        Cargo cargo = cargoRepository.findById(cargoId).orElseThrow();
        Vaga vaga = vagaRepository.save(new Vaga(unidade, cargo, 1, StatusVaga.ABERTA));
        vagaIdsCriadas.add(vaga.getUuid());
        return vaga.getUuid();
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
                  "jornadaSemanalHoras": 40,
                  "justificativa": "Reposição de quadro"
                }
                """.formatted(unidadeId, cargoId);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Necessidade de pessoal criada com sucesso!"));

        necessidadeDePessoalRepository.findAll().forEach(n -> necessidadeIdsCriadas.add(n.getUuid()));
    }

    @Test
    void deveRetornarNotFoundAoCriarComUnidadeInexistente() throws Exception {
        UUID cargoId = criarCargoFixture("02");
        String body = """
                {
                  "unidadeId": "%s",
                  "cargoId": "%s",
                  "quantidade": 1,
                  "jornadaSemanalHoras": 40
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
                  "jornadaSemanalHoras": 40
                }
                """.formatted(unidadeId, UUID.randomUUID());

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornarNotFoundAoCriarComSetorInexistente() throws Exception {
        UUID unidadeId = criarUnidadeFixture("04");
        UUID cargoId = criarCargoFixture("04");
        String body = """
                {
                  "unidadeId": "%s",
                  "setorId": "%s",
                  "cargoId": "%s",
                  "quantidade": 1,
                  "jornadaSemanalHoras": 40
                }
                """.formatted(unidadeId, UUID.randomUUID(), cargoId);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveBuscarPorIdComSetorInformado() throws Exception {
        UUID unidadeId = criarUnidadeFixture("05");
        UUID setorId = criarSetorFixture("05", unidadeId);
        UUID cargoId = criarCargoFixture("05");
        String body = """
                {
                  "unidadeId": "%s",
                  "setorId": "%s",
                  "cargoId": "%s",
                  "quantidade": 3,
                  "jornadaSemanalHoras": 30,
                  "competenciasNecessarias": "Experiência em atenção primária"
                }
                """.formatted(unidadeId, setorId, cargoId);

        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());

        UUID necessidadeId = necessidadeDePessoalRepository.findAll().stream()
                .filter(n -> n.getQuantidade() == 3)
                .findFirst()
                .orElseThrow()
                .getUuid();
        necessidadeIdsCriadas.add(necessidadeId);

        mockMvc.perform(get(BASE_URL + necessidadeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.setorUuid").value(setorId.toString()))
                .andExpect(jsonPath("$.competenciasNecessarias").value("Experiência em atenção primária"));
    }

    @Test
    void deveRetornarNotFoundAoBuscarIdInexistente() throws Exception {
        mockMvc.perform(get(BASE_URL + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveAtualizarComSucesso() throws Exception {
        UUID unidadeId = criarUnidadeFixture("06");
        UUID cargoId = criarCargoFixture("06");
        String bodyCriacao = """
                {
                  "unidadeId": "%s",
                  "cargoId": "%s",
                  "quantidade": 5,
                  "jornadaSemanalHoras": 40
                }
                """.formatted(unidadeId, cargoId);

        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(bodyCriacao))
                .andExpect(status().isCreated());

        UUID necessidadeId = necessidadeDePessoalRepository.findAll().stream()
                .filter(n -> n.getQuantidade() == 5)
                .findFirst()
                .orElseThrow()
                .getUuid();
        necessidadeIdsCriadas.add(necessidadeId);

        String bodyAtualizacao = """
                {
                  "unidadeId": "%s",
                  "cargoId": "%s",
                  "quantidade": 7,
                  "jornadaSemanalHoras": 20,
                  "justificativa": "Ajuste de escala"
                }
                """.formatted(unidadeId, cargoId);

        mockMvc.perform(patch(BASE_URL + necessidadeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyAtualizacao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Necessidade de pessoal atualizada com sucesso!"));

        mockMvc.perform(get(BASE_URL + necessidadeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidade").value(7))
                .andExpect(jsonPath("$.jornadaSemanalHoras").value(20));
    }

    @Test
    void deveRetornarNotFoundAoAtualizarNecessidadeInexistente() throws Exception {
        UUID unidadeId = criarUnidadeFixture("07");
        UUID cargoId = criarCargoFixture("07");
        String body = """
                {
                  "unidadeId": "%s",
                  "cargoId": "%s",
                  "quantidade": 1,
                  "jornadaSemanalHoras": 40
                }
                """.formatted(unidadeId, cargoId);

        mockMvc.perform(patch(BASE_URL + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveVincularVagaComSucesso() throws Exception {
        UUID unidadeId = criarUnidadeFixture("08");
        UUID cargoId = criarCargoFixture("08");
        String body = """
                {
                  "unidadeId": "%s",
                  "cargoId": "%s",
                  "quantidade": 1,
                  "jornadaSemanalHoras": 40
                }
                """.formatted(unidadeId, cargoId);

        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());

        UUID necessidadeId = necessidadeDePessoalRepository.findAll().stream()
                .filter(n -> n.getUnidade().getUuid().equals(unidadeId))
                .findFirst()
                .orElseThrow()
                .getUuid();
        necessidadeIdsCriadas.add(necessidadeId);

        UUID vagaId = criarVagaFixture("08", unidadeId, cargoId);

        mockMvc.perform(patch(BASE_URL + necessidadeId + "/vincular-vaga")
                        .param("vagaId", vagaId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Necessidade de pessoal vinculada à vaga com sucesso!"));

        mockMvc.perform(get(BASE_URL + necessidadeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vagaAssociadaUuid").value(vagaId.toString()));
    }

    @Test
    void deveRetornarConflictAoVincularVagaJaVinculada() throws Exception {
        UUID unidadeId = criarUnidadeFixture("09");
        UUID cargoId = criarCargoFixture("09");
        String body = """
                {
                  "unidadeId": "%s",
                  "cargoId": "%s",
                  "quantidade": 1,
                  "jornadaSemanalHoras": 40
                }
                """.formatted(unidadeId, cargoId);

        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());

        UUID necessidadeId = necessidadeDePessoalRepository.findAll().stream()
                .filter(n -> n.getUnidade().getUuid().equals(unidadeId))
                .findFirst()
                .orElseThrow()
                .getUuid();
        necessidadeIdsCriadas.add(necessidadeId);

        UUID primeiraVagaId = criarVagaFixture("09-A", unidadeId, cargoId);
        UUID segundaVagaId = criarVagaFixture("09-B", unidadeId, cargoId);

        mockMvc.perform(patch(BASE_URL + necessidadeId + "/vincular-vaga")
                        .param("vagaId", primeiraVagaId.toString()))
                .andExpect(status().isOk());

        mockMvc.perform(patch(BASE_URL + necessidadeId + "/vincular-vaga")
                        .param("vagaId", segundaVagaId.toString()))
                .andExpect(status().isConflict());
    }

    @Test
    void deveRetornarNotFoundAoVincularVagaInexistente() throws Exception {
        UUID unidadeId = criarUnidadeFixture("10");
        UUID cargoId = criarCargoFixture("10");
        String body = """
                {
                  "unidadeId": "%s",
                  "cargoId": "%s",
                  "quantidade": 1,
                  "jornadaSemanalHoras": 40
                }
                """.formatted(unidadeId, cargoId);

        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());

        UUID necessidadeId = necessidadeDePessoalRepository.findAll().stream()
                .filter(n -> n.getUnidade().getUuid().equals(unidadeId))
                .findFirst()
                .orElseThrow()
                .getUuid();
        necessidadeIdsCriadas.add(necessidadeId);

        mockMvc.perform(patch(BASE_URL + necessidadeId + "/vincular-vaga")
                        .param("vagaId", UUID.randomUUID().toString()))
                .andExpect(status().isNotFound());
    }
}
