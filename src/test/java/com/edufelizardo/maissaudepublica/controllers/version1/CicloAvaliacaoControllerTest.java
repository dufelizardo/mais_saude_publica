package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.CicloAvaliacao;
import com.edufelizardo.maissaudepublica.repositories.CicloAvaliacaoRepository;
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
 * Testes do CicloAvaliacaoController (módulo RH, Fase 9 — ver docs/rh/MODELO-RH.md). Mesmo padrão
 * de engenharia de teste dos demais controllers de catálogo: limpeza manual via
 * {@code @AfterEach} por prefixo de nome.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CicloAvaliacaoControllerTest {

    private static final String BASE_URL = "/api/v1/ciclo-avaliacao/";
    private static final String PREFIXO_NOME_TESTE = "Ciclo Teste - ";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CicloAvaliacaoRepository cicloAvaliacaoRepository;

    @AfterEach
    void limparDadosDeTeste() {
        List<CicloAvaliacao> criadosNoTeste = cicloAvaliacaoRepository.findAll().stream()
                .filter(c -> c.getNome() != null && c.getNome().startsWith(PREFIXO_NOME_TESTE))
                .toList();
        cicloAvaliacaoRepository.deleteAll(criadosNoTeste);
    }

    private UUID criarCiclo(String nome) throws Exception {
        String body = """
                {
                  "nome": "%s",
                  "dataInicio": "2027-01-01",
                  "dataFim": "2027-06-30"
                }
                """.formatted(nome);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        return cicloAvaliacaoRepository.findAll().stream()
                .filter(c -> nome.equals(c.getNome()))
                .findFirst()
                .orElseThrow()
                .getUuid();
    }

    @Test
    void deveCriarComSucesso() throws Exception {
        String nome = PREFIXO_NOME_TESTE + "2027-S1";
        String body = """
                {
                  "nome": "%s",
                  "dataInicio": "2027-01-01",
                  "dataFim": "2027-06-30"
                }
                """.formatted(nome);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Ciclo de avaliação criado com sucesso!"))
                .andExpect(jsonPath("$.details").value(containsString(nome)));
    }

    @Test
    void deveListarCiclosCadastrados() throws Exception {
        String nome = PREFIXO_NOME_TESTE + "2027-S2";
        criarCiclo(nome);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.nome == '%s')]".formatted(nome)).exists());
    }

    @Test
    void deveBuscarPorId() throws Exception {
        String nome = PREFIXO_NOME_TESTE + "2028-S1";
        UUID uuid = criarCiclo(nome);

        mockMvc.perform(get(BASE_URL + uuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value(nome))
                .andExpect(jsonPath("$.dataInicio").value("2027-01-01"));
    }

    @Test
    void deveRetornarNotFoundAoBuscarIdInexistente() throws Exception {
        mockMvc.perform(get(BASE_URL + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }
}
