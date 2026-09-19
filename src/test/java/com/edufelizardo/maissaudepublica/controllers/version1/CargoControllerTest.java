package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Cargo;
import com.edufelizardo.maissaudepublica.models.CategoriaSalarial;
import com.edufelizardo.maissaudepublica.repositories.CargoRepository;
import com.edufelizardo.maissaudepublica.repositories.CategoriaSalarialRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes do CargoController (módulo RH — ver docs/rh/MODELO-RH.md). A fixture de
 * {@link CategoriaSalarial} é criada direto via repositório (já coberta pelos próprios testes de
 * CategoriaSalarialControllerTest) — aqui o foco é só o comportamento de Cargo.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CargoControllerTest {

    private static final String BASE_URL = "/api/v1/cargo/";
    private static final String PREFIXO_NOME_TESTE = "Cargo Teste - ";
    private static final String PREFIXO_CATEGORIA_TESTE = "Categoria Fixture Cargo - ";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CargoRepository cargoRepository;

    @Autowired
    private CategoriaSalarialRepository categoriaSalarialRepository;

    @AfterEach
    void limparDadosDeTeste() {
        List<Cargo> cargosCriados = cargoRepository.findAll().stream()
                .filter(c -> c.getNome() != null && c.getNome().startsWith(PREFIXO_NOME_TESTE))
                .toList();
        cargoRepository.deleteAll(cargosCriados);

        List<CategoriaSalarial> categoriasCriadas = categoriaSalarialRepository.findAll().stream()
                .filter(c -> c.getNome() != null && c.getNome().startsWith(PREFIXO_CATEGORIA_TESTE))
                .toList();
        categoriaSalarialRepository.deleteAll(categoriasCriadas);
    }

    private UUID criarCategoriaFixture(String nome) {
        CategoriaSalarial categoria = new CategoriaSalarial();
        categoria.setNome(nome);
        return categoriaSalarialRepository.save(categoria).getUuid();
    }

    @Test
    void deveCriarComSucesso() throws Exception {
        UUID categoriaId = criarCategoriaFixture(PREFIXO_CATEGORIA_TESTE + "Enfermagem 1");
        String nome = PREFIXO_NOME_TESTE + "Enfermeiro";
        String body = """
                {
                  "categoriaId": "%s",
                  "nome": "%s"
                }
                """.formatted(categoriaId, nome);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Cargo criado com sucesso!"))
                .andExpect(jsonPath("$.details").value(containsString(nome)));
    }

    @Test
    void deveRetornarNotFoundAoCriarComCategoriaInexistente() throws Exception {
        String body = """
                {
                  "categoriaId": "%s",
                  "nome": "%s"
                }
                """.formatted(UUID.randomUUID(), PREFIXO_NOME_TESTE + "Sem Categoria");

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveListarCargosCadastrados() throws Exception {
        UUID categoriaId = criarCategoriaFixture(PREFIXO_CATEGORIA_TESTE + "Enfermagem 2");
        String nome = PREFIXO_NOME_TESTE + "Técnico de Enfermagem";
        Cargo cargo = new Cargo(categoriaSalarialRepository.findById(categoriaId).orElseThrow(), nome);
        cargoRepository.save(cargo);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.nome == '%s')]".formatted(nome)).exists());
    }

    @Test
    void deveBuscarPorId() throws Exception {
        UUID categoriaId = criarCategoriaFixture(PREFIXO_CATEGORIA_TESTE + "Enfermagem 3");
        String nome = PREFIXO_NOME_TESTE + "Enfermeiro Chefe";
        Cargo cargo = new Cargo(categoriaSalarialRepository.findById(categoriaId).orElseThrow(), nome);
        UUID cargoId = cargoRepository.save(cargo).getUuid();

        mockMvc.perform(get(BASE_URL + cargoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value(nome))
                .andExpect(jsonPath("$.categoriaUuid").value(categoriaId.toString()));
    }

    @Test
    void deveRetornarNotFoundAoBuscarIdInexistente() throws Exception {
        mockMvc.perform(get(BASE_URL + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }
}
