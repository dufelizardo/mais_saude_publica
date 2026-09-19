package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.CategoriaSalarial;
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
 * Testes do CategoriaSalarialController (módulo RH — ver docs/rh/MODELO-RH.md). Mesmo padrão de
 * engenharia de teste dos demais controllers: SEM {@code @Transactional} na classe, limpeza manual
 * via {@code @AfterEach} por prefixo de nome.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CategoriaSalarialControllerTest {

    private static final String BASE_URL = "/api/v1/categoria-salarial/";
    private static final String PREFIXO_NOME_TESTE = "Categoria Teste - ";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoriaSalarialRepository categoriaSalarialRepository;

    @AfterEach
    void limparDadosDeTeste() {
        List<CategoriaSalarial> criadasNoTeste = categoriaSalarialRepository.findAll().stream()
                .filter(c -> c.getNome() != null && c.getNome().startsWith(PREFIXO_NOME_TESTE))
                .toList();
        categoriaSalarialRepository.deleteAll(criadasNoTeste);
    }

    private UUID criarCategoria(String nome) throws Exception {
        String body = """
                {
                  "nome": "%s",
                  "convencaoColetiva": "SINDSAUDE-SP 2026"
                }
                """.formatted(nome);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        return categoriaSalarialRepository.findAll().stream()
                .filter(c -> nome.equals(c.getNome()))
                .findFirst()
                .orElseThrow()
                .getUuid();
    }

    @Test
    void deveCriarComSucesso() throws Exception {
        String nome = PREFIXO_NOME_TESTE + "Enfermagem";
        String body = """
                {
                  "nome": "%s",
                  "convencaoColetiva": "SINDSAUDE-SP 2026"
                }
                """.formatted(nome);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Categoria salarial criada com sucesso!"))
                .andExpect(jsonPath("$.details").value(containsString(nome)));
    }

    @Test
    void deveListarCategoriasCadastradas() throws Exception {
        String nome = PREFIXO_NOME_TESTE + "Medicina";
        criarCategoria(nome);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.nome == '%s')]".formatted(nome)).exists());
    }

    @Test
    void deveBuscarPorId() throws Exception {
        String nome = PREFIXO_NOME_TESTE + "Administrativo";
        UUID uuid = criarCategoria(nome);

        mockMvc.perform(get(BASE_URL + uuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value(nome))
                .andExpect(jsonPath("$.convencaoColetiva").value("SINDSAUDE-SP 2026"));
    }

    @Test
    void deveRetornarNotFoundAoBuscarIdInexistente() throws Exception {
        mockMvc.perform(get(BASE_URL + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornarBadRequestAoCriarSemNome() throws Exception {
        String body = """
                {
                  "convencaoColetiva": "SINDSAUDE-SP 2026"
                }
                """;

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}
