package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.TipoBeneficio;
import com.edufelizardo.maissaudepublica.repositories.TipoBeneficioRepository;
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
 * Testes do TipoBeneficioController (módulo RH — ver docs/rh/MODELO-RH.md). Mesmo padrão de
 * engenharia de teste dos demais controllers: SEM {@code @Transactional} na classe, limpeza manual
 * via {@code @AfterEach} por prefixo de nome.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TipoBeneficioControllerTest {

    private static final String BASE_URL = "/api/v1/tipo-beneficio/";
    private static final String PREFIXO_NOME_TESTE = "Beneficio Teste - ";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TipoBeneficioRepository tipoBeneficioRepository;

    @AfterEach
    void limparDadosDeTeste() {
        List<TipoBeneficio> criadosNoTeste = tipoBeneficioRepository.findAll().stream()
                .filter(t -> t.getNome() != null && t.getNome().startsWith(PREFIXO_NOME_TESTE))
                .toList();
        tipoBeneficioRepository.deleteAll(criadosNoTeste);
    }

    private UUID criarTipoBeneficio(String nome) throws Exception {
        String body = """
                {
                  "nome": "%s",
                  "custeio": "EMPRESA"
                }
                """.formatted(nome);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        return tipoBeneficioRepository.findAll().stream()
                .filter(t -> nome.equals(t.getNome()))
                .findFirst()
                .orElseThrow()
                .getUuid();
    }

    @Test
    void deveCriarComSucesso() throws Exception {
        String nome = PREFIXO_NOME_TESTE + "Vale Refeição";
        String body = """
                {
                  "nome": "%s",
                  "custeio": "EMPRESA"
                }
                """.formatted(nome);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Tipo de benefício criado com sucesso!"))
                .andExpect(jsonPath("$.details").value(containsString(nome)));
    }

    @Test
    void deveListarTiposCadastrados() throws Exception {
        String nome = PREFIXO_NOME_TESTE + "Vale Transporte";
        criarTipoBeneficio(nome);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.nome == '%s')]".formatted(nome)).exists());
    }

    @Test
    void deveBuscarPorId() throws Exception {
        String nome = PREFIXO_NOME_TESTE + "Plano de Saúde";
        UUID uuid = criarTipoBeneficio(nome);

        mockMvc.perform(get(BASE_URL + uuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value(nome))
                .andExpect(jsonPath("$.custeio").value("EMPRESA"));
    }

    @Test
    void deveRetornarNotFoundAoBuscarIdInexistente() throws Exception {
        mockMvc.perform(get(BASE_URL + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }
}
