package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Cargo;
import com.edufelizardo.maissaudepublica.models.CategoriaSalarial;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.Vaga;
import com.edufelizardo.maissaudepublica.models.enuns.StatusVaga;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.CandidatoRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes do CandidatoController (módulo RH, Fase 8/Recrutamento — ver docs/rh/MODELO-RH.md).
 * Fixture de {@link Vaga} (e suas dependências) via repositório direto; limpeza por ID rastreado.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CandidatoControllerTest {

    private static final String BASE_URL = "/api/v1/candidato/";
    private static final String PREFIXO_NOME_TESTE = "Fixture Candidato - ";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CandidatoRepository candidatoRepository;

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
        candidatoRepository.findAll().stream()
                .filter(c -> vagaIdsCriadas.contains(c.getVaga().getUuid()))
                .forEach(c -> candidatoRepository.deleteById(c.getUuid()));
        vagaRepository.deleteAllById(vagaIdsCriadas);
        cargoRepository.deleteAllById(cargoIdsCriados);
        categoriaSalarialRepository.deleteAllById(categoriaIdsCriadas);
        unidadeDeSaudeRepository.deleteAllById(unidadeIdsCriadas);
    }

    private UUID criarVagaFixture(String sufixo) {
        UnidadeDeSaude unidade = new UnidadeDeSaude();
        unidade.setNome(PREFIXO_NOME_TESTE + "Unidade " + sufixo);
        unidade.setTipo(TipoUnidadeDeSaude.UBS);
        unidade.setAtivo(true);
        unidade = unidadeDeSaudeRepository.save(unidade);
        unidadeIdsCriadas.add(unidade.getUuid());

        CategoriaSalarial categoria = new CategoriaSalarial();
        categoria.setNome(PREFIXO_NOME_TESTE + "Categoria " + sufixo);
        categoria = categoriaSalarialRepository.save(categoria);
        categoriaIdsCriadas.add(categoria.getUuid());

        Cargo cargo = cargoRepository.save(new Cargo(categoria, PREFIXO_NOME_TESTE + "Cargo " + sufixo));
        cargoIdsCriados.add(cargo.getUuid());

        Vaga vaga = vagaRepository.save(new Vaga(unidade, cargo, 1, StatusVaga.ABERTA));
        vagaIdsCriadas.add(vaga.getUuid());
        return vaga.getUuid();
    }

    @Test
    void deveCriarComSucesso() throws Exception {
        UUID vagaId = criarVagaFixture("01");
        String body = """
                {
                  "vagaId": "%s",
                  "nome": "Candidato Teste",
                  "cpf": "00000000001",
                  "status": "INSCRITO"
                }
                """.formatted(vagaId);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Candidato inscrito com sucesso!"));
    }

    @Test
    void deveRetornarNotFoundAoCriarComVagaInexistente() throws Exception {
        String body = """
                {
                  "vagaId": "%s",
                  "nome": "Candidato Teste",
                  "cpf": "00000000002",
                  "status": "INSCRITO"
                }
                """.formatted(UUID.randomUUID());

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveListarPorVaga() throws Exception {
        UUID vagaId = criarVagaFixture("02");
        String body = """
                {
                  "vagaId": "%s",
                  "nome": "Candidato Teste 2",
                  "cpf": "00000000003",
                  "curriculoUrl": "https://storage/curriculo.pdf",
                  "status": "TRIAGEM"
                }
                """.formatted(vagaId);

        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(get(BASE_URL + "vaga/" + vagaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Candidato Teste 2"))
                .andExpect(jsonPath("$[0].status").value("TRIAGEM"));
    }

    @Test
    void deveRetornarNotFoundAoListarCandidatosDeVagaSemCandidatos() throws Exception {
        UUID vagaId = criarVagaFixture("03");

        mockMvc.perform(get(BASE_URL + "vaga/" + vagaId))
                .andExpect(status().isNotFound());
    }
}
