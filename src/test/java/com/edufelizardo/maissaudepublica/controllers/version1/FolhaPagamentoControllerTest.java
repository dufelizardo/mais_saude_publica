package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.repositories.FolhaPagamentoRepository;
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
 * Testes do FolhaPagamentoController (módulo RH, Fase 5 — ver docs/rh/MODELO-RH.md). Fixture de
 * {@link Profissional} criada direto via repositório; limpeza por ID rastreado.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FolhaPagamentoControllerTest {

    private static final String BASE_URL = "/api/v1/folha-pagamento/";
    private static final String PREFIXO_MATRICULA_TESTE = "TESTE-FOLHA-";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FolhaPagamentoRepository folhaPagamentoRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    private final List<UUID> profissionalIdsCriados = new ArrayList<>();

    @AfterEach
    void limparDadosDeTeste() {
        folhaPagamentoRepository.findAll().stream()
                .filter(f -> profissionalIdsCriados.contains(f.getProfissional().getUuid()))
                .forEach(f -> folhaPagamentoRepository.deleteById(f.getUuid()));
        profissionalRepository.deleteAllById(profissionalIdsCriados);
    }

    private String criarProfissionalFixture(String sufixoMatricula) {
        Profissional profissional = new Profissional();
        String matricula = PREFIXO_MATRICULA_TESTE + sufixoMatricula;
        profissional.setMatricula(matricula);
        profissional.setCpf("00000000000");
        profissional.setNome("Profissional Fixture FolhaPagamento");
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
                  "competencia": "09/2026",
                  "proventos": 10964.00,
                  "descontos": 2221.32,
                  "encargos": 3069.92,
                  "total": 8742.68
                }
                """.formatted(matricula);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Folha de pagamento registrada com sucesso!"));
    }

    @Test
    void deveRetornarNotFoundAoCriarComProfissionalInexistente() throws Exception {
        String body = """
                {
                  "matriculaProfissional": "%s",
                  "competencia": "09/2026",
                  "proventos": 5000.00,
                  "descontos": 500.00,
                  "encargos": 800.00,
                  "total": 4500.00
                }
                """.formatted(PREFIXO_MATRICULA_TESTE + "INEXISTENTE");

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornarConflictAoCriarSegundaFolhaParaMesmaCompetencia() throws Exception {
        String matricula = criarProfissionalFixture("02");
        String body = """
                {
                  "matriculaProfissional": "%s",
                  "competencia": "09/2026",
                  "proventos": 5000.00,
                  "descontos": 500.00,
                  "encargos": 800.00,
                  "total": 4500.00
                }
                """.formatted(matricula);

        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void deveListarHistorico() throws Exception {
        String matricula = criarProfissionalFixture("03");
        String body = """
                {
                  "matriculaProfissional": "%s",
                  "competencia": "08/2026",
                  "proventos": 5000.00,
                  "descontos": 500.00,
                  "encargos": 800.00,
                  "total": 4500.00
                }
                """.formatted(matricula);

        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(get(BASE_URL + "profissional/" + matricula))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].competencia").value("08/2026"))
                .andExpect(jsonPath("$[0].total").value(4500.00));
    }

    @Test
    void deveRetornarNotFoundAoListarHistoricoDeProfissionalSemFolha() throws Exception {
        String matricula = criarProfissionalFixture("04");

        mockMvc.perform(get(BASE_URL + "profissional/" + matricula))
                .andExpect(status().isNotFound());
    }
}
