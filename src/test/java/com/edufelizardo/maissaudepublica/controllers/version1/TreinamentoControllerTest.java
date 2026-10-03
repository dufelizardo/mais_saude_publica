package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Treinamento;
import com.edufelizardo.maissaudepublica.repositories.TreinamentoRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes do TreinamentoController (módulo RH, Fase 6 — ver docs/rh/MODELO-RH.md). Mesmo padrão de
 * engenharia de teste dos demais controllers de catálogo: limpeza manual via {@code @AfterEach}
 * por prefixo de nome.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TreinamentoControllerTest {

    private static final String BASE_URL = "/api/v1/treinamento/";
    private static final String PREFIXO_NOME_TESTE = "Treinamento Teste - ";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TreinamentoRepository treinamentoRepository;

    @AfterEach
    void limparDadosDeTeste() {
        List<Treinamento> criadosNoTeste = treinamentoRepository.findAll().stream()
                .filter(t -> t.getNome() != null && t.getNome().startsWith(PREFIXO_NOME_TESTE))
                .toList();
        treinamentoRepository.deleteAll(criadosNoTeste);
    }

    private UUID criarTreinamento(String nome) throws Exception {
        String body = """
                {
                  "nome": "%s",
                  "cargaHoraria": 8,
                  "validadeMeses": 12,
                  "obrigatorio": true
                }
                """.formatted(nome);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        return treinamentoRepository.findAll().stream()
                .filter(t -> nome.equals(t.getNome()))
                .findFirst()
                .orElseThrow()
                .getUuid();
    }

    @Test
    void deveCriarComSucesso() throws Exception {
        String nome = PREFIXO_NOME_TESTE + "NR-32";
        String body = """
                {
                  "nome": "%s",
                  "cargaHoraria": 8,
                  "validadeMeses": 12,
                  "obrigatorio": true
                }
                """.formatted(nome);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Treinamento criado com sucesso!"))
                .andExpect(jsonPath("$.details").value(containsString(nome)));
    }

    @Test
    void deveListarTreinamentosCadastrados() throws Exception {
        String nome = PREFIXO_NOME_TESTE + "Suporte Básico de Vida";
        criarTreinamento(nome);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.nome == '%s')]".formatted(nome)).exists());
    }

    @Test
    void deveBuscarPorId() throws Exception {
        String nome = PREFIXO_NOME_TESTE + "Brigada de Incêndio";
        UUID uuid = criarTreinamento(nome);

        mockMvc.perform(get(BASE_URL + uuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value(nome))
                .andExpect(jsonPath("$.cargaHoraria").value(8))
                .andExpect(jsonPath("$.validadeMeses").value(12))
                .andExpect(jsonPath("$.obrigatorio").value(true));
    }

    @Test
    void deveRetornarNotFoundAoBuscarIdInexistente() throws Exception {
        mockMvc.perform(get(BASE_URL + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    // ── Edição (ADR-0083) ──────────────────────────────────────────────────────────────────────

    @Test
    void deveAtualizarOTreinamento() throws Exception {
        UUID uuid = criarTreinamento(PREFIXO_NOME_TESTE + "NR-10");
        String nome = PREFIXO_NOME_TESTE + "NR-10 reciclagem";
        mockMvc.perform(patch(BASE_URL + uuid).contentType(MediaType.APPLICATION_JSON).content("""
                        {"nome": "%s", "cargaHoraria": 16, "validadeMeses": 24, "obrigatorio": false}
                        """.formatted(nome)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Treinamento atualizado com sucesso!"));
        mockMvc.perform(get(BASE_URL + uuid))
                .andExpect(jsonPath("$.nome").value(nome))
                .andExpect(jsonPath("$.cargaHoraria").value(16))
                .andExpect(jsonPath("$.validadeMeses").value(24))
                .andExpect(jsonPath("$.obrigatorio").value(false));
    }

    @Test
    void atualizarTreinamentoInexistenteDa404ENomeRepetidoDa409() throws Exception {
        mockMvc.perform(patch(BASE_URL + UUID.randomUUID()).contentType(MediaType.APPLICATION_JSON).content("""
                        {"nome": "%s", "cargaHoraria": 8, "validadeMeses": 12, "obrigatorio": true}
                        """.formatted(PREFIXO_NOME_TESTE + "Fantasma")))
                .andExpect(status().isNotFound());
        criarTreinamento(PREFIXO_NOME_TESTE + "Primeiro");
        UUID segundo = criarTreinamento(PREFIXO_NOME_TESTE + "Segundo");
        mockMvc.perform(patch(BASE_URL + segundo).contentType(MediaType.APPLICATION_JSON).content("""
                        {"nome": "%s", "cargaHoraria": 8, "validadeMeses": 12, "obrigatorio": true}
                        """.formatted(PREFIXO_NOME_TESTE + "Primeiro")))
                .andExpect(status().isConflict());
    }
}
