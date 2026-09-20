package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.CategoriaSalarial;
import com.edufelizardo.maissaudepublica.repositories.CategoriaSalarialRepository;
import com.edufelizardo.maissaudepublica.repositories.RegraAnuenioRepository;
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
 * Testes do RegraAnuenioController (módulo RH — ver docs/rh/MODELO-RH.md). Só uma regra por
 * categoria — foco principal aqui é o 409 na segunda tentativa.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RegraAnuenioControllerTest {

    private static final String BASE_URL = "/api/v1/regra-anuenio/";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RegraAnuenioRepository regraAnuenioRepository;

    @Autowired
    private CategoriaSalarialRepository categoriaSalarialRepository;

    private final List<UUID> categoriaIdsCriadas = new ArrayList<>();

    @AfterEach
    void limparDadosDeTeste() {
        regraAnuenioRepository.findAll().stream()
                .filter(r -> categoriaIdsCriadas.contains(r.getCategoria().getUuid()))
                .forEach(r -> regraAnuenioRepository.deleteById(r.getUuid()));
        categoriaSalarialRepository.deleteAllById(categoriaIdsCriadas);
    }

    private UUID criarCategoriaFixture(String sufixo) {
        CategoriaSalarial categoria = new CategoriaSalarial();
        categoria.setNome("Categoria Fixture RegraAnuenio - " + sufixo);
        UUID id = categoriaSalarialRepository.save(categoria).getUuid();
        categoriaIdsCriadas.add(id);
        return id;
    }

    @Test
    void deveListarRegrasCadastradas() throws Exception {
        UUID categoriaId = criarCategoriaFixture("07");
        String body = """
                {
                  "categoriaId": "%s",
                  "percentualPorAno": 2.00,
                  "tetoAnos": 15
                }
                """.formatted(categoriaId);

        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.percentualPorAno == 2.00)]").exists());
    }

    @Test
    void deveCriarComSucesso() throws Exception {
        UUID categoriaId = criarCategoriaFixture("01");
        String body = """
                {
                  "categoriaId": "%s",
                  "percentualPorAno": 1.00,
                  "tetoAnos": 25
                }
                """.formatted(categoriaId);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Regra de anuênio criada com sucesso!"));
    }

    @Test
    void deveRetornarConflictAoCriarSegundaRegraParaMesmaCategoria() throws Exception {
        UUID categoriaId = criarCategoriaFixture("02");
        String body = """
                {
                  "categoriaId": "%s",
                  "percentualPorAno": 1.00,
                  "tetoAnos": 25
                }
                """.formatted(categoriaId);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void deveRetornarNotFoundAoCriarComCategoriaInexistente() throws Exception {
        String body = """
                {
                  "categoriaId": "%s",
                  "percentualPorAno": 1.00
                }
                """.formatted(UUID.randomUUID());

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveBuscarPorCategoria() throws Exception {
        UUID categoriaId = criarCategoriaFixture("03");
        String body = """
                {
                  "categoriaId": "%s",
                  "percentualPorAno": 1.50,
                  "tetoAnos": 20
                }
                """.formatted(categoriaId);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(get(BASE_URL + "categoria/" + categoriaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.percentualPorAno").value(1.50))
                .andExpect(jsonPath("$.tetoAnos").value(20));
    }

    @Test
    void deveRetornarNotFoundAoBuscarCategoriaSemRegra() throws Exception {
        UUID categoriaId = criarCategoriaFixture("04");

        mockMvc.perform(get(BASE_URL + "categoria/" + categoriaId))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveAtualizarPercentualETeto() throws Exception {
        UUID categoriaId = criarCategoriaFixture("05");
        String bodyCriar = """
                {
                  "categoriaId": "%s",
                  "percentualPorAno": 1.00,
                  "tetoAnos": 25
                }
                """.formatted(categoriaId);

        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(bodyCriar))
                .andExpect(status().isCreated());

        UUID regraId = regraAnuenioRepository.findByCategoria_Uuid(categoriaId).orElseThrow().getUuid();
        String bodyAtualizar = """
                {
                  "categoriaId": "%s",
                  "percentualPorAno": 1.50,
                  "tetoAnos": 30
                }
                """.formatted(categoriaId);

        mockMvc.perform(patch(BASE_URL + regraId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyAtualizar))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Regra de anuênio atualizada com sucesso!"));

        mockMvc.perform(get(BASE_URL + "categoria/" + categoriaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.percentualPorAno").value(1.50))
                .andExpect(jsonPath("$.tetoAnos").value(30));
    }

    @Test
    void deveRetornarNotFoundAoAtualizarIdInexistente() throws Exception {
        UUID categoriaId = criarCategoriaFixture("06");
        String body = """
                {
                  "categoriaId": "%s",
                  "percentualPorAno": 1.00
                }
                """.formatted(categoriaId);

        mockMvc.perform(patch(BASE_URL + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }
}
