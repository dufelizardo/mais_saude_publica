package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.repositories.AjusteIndividualRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
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
 * Testes do AjusteIndividualController (módulo RH — ver docs/rh/MODELO-RH.md). Fixture de
 * {@link Profissional} criada direto via repositório; limpeza por ID rastreado.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AjusteIndividualControllerTest {

    private static final String BASE_URL = "/api/v1/ajuste-individual/";
    private static final String PREFIXO_MATRICULA_TESTE = "TESTE-AJUSTE-";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AjusteIndividualRepository ajusteIndividualRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    private final List<UUID> profissionalIdsCriados = new ArrayList<>();

    @AfterEach
    void limparDadosDeTeste() {
        ajusteIndividualRepository.findAll().stream()
                .filter(a -> profissionalIdsCriados.contains(a.getProfissional().getUuid()))
                .forEach(a -> ajusteIndividualRepository.deleteById(a.getUuid()));
        profissionalRepository.deleteAllById(profissionalIdsCriados);
    }

    private String criarProfissionalFixture(String sufixoMatricula) {
        Profissional profissional = new Profissional();
        String matricula = PREFIXO_MATRICULA_TESTE + sufixoMatricula;
        profissional.setMatricula(matricula);
        profissional.setCpf("00000000000");
        profissional.setNome("Profissional Fixture AjusteIndividual");
        profissional.setAtivo(true);
        profissionalIdsCriados.add(profissionalRepository.save(profissional).getUuid());
        return matricula;
    }

    @Test
    void deveCriarComSucesso() throws Exception {
        String matricula = criarProfissionalFixture("01");
        String body = """
                {
                  "matriculaProfissional": "%s",
                  "valor": 300.00,
                  "dataInicio": "2026-01-01",
                  "motivo": "GRATIFICACAO_PESSOAL"
                }
                """.formatted(matricula);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Ajuste individual criado com sucesso!"));
    }

    @Test
    void deveCriarComReferenciaParaEquiparacaoJudicial() throws Exception {
        String matricula = criarProfissionalFixture("02");
        String body = """
                {
                  "matriculaProfissional": "%s",
                  "valor": 800.00,
                  "dataInicio": "2026-01-01",
                  "motivo": "EQUIPARACAO_JUDICIAL",
                  "referencia": "Processo 0001234-56.2026.5.02.0001"
                }
                """.formatted(matricula);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(get(BASE_URL + "profissional/" + matricula))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].referencia").value("Processo 0001234-56.2026.5.02.0001"));
    }

    @Test
    void deveRetornarNotFoundAoCriarComProfissionalInexistente() throws Exception {
        String body = """
                {
                  "matriculaProfissional": "%s",
                  "valor": 300.00,
                  "dataInicio": "2026-01-01",
                  "motivo": "GRATIFICACAO_PESSOAL"
                }
                """.formatted(PREFIXO_MATRICULA_TESTE + "INEXISTENTE");

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveListarHistoricoComMaisDeUmAjusteVigente() throws Exception {
        String matricula = criarProfissionalFixture("03");
        String bodyGratificacao = """
                {
                  "matriculaProfissional": "%s",
                  "valor": 300.00,
                  "dataInicio": "2026-01-01",
                  "motivo": "GRATIFICACAO_PESSOAL"
                }
                """.formatted(matricula);
        String bodyEquiparacao = """
                {
                  "matriculaProfissional": "%s",
                  "valor": 800.00,
                  "dataInicio": "2026-02-01",
                  "motivo": "EQUIPARACAO_JUDICIAL",
                  "referencia": "Processo 0001234-56.2026.5.02.0001"
                }
                """.formatted(matricula);

        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(bodyGratificacao))
                .andExpect(status().isCreated());
        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(bodyEquiparacao))
                .andExpect(status().isCreated());

        mockMvc.perform(get(BASE_URL + "profissional/" + matricula))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void deveRetornarNotFoundAoListarHistoricoDeProfissionalSemAjustes() throws Exception {
        String matricula = criarProfissionalFixture("04");

        mockMvc.perform(get(BASE_URL + "profissional/" + matricula))
                .andExpect(status().isNotFound());
    }
}
