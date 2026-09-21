package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.CicloAvaliacao;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.repositories.AvaliacaoRepository;
import com.edufelizardo.maissaudepublica.repositories.CicloAvaliacaoRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes do AvaliacaoController (módulo RH, Fase 9 — última fase — ver docs/rh/MODELO-RH.md).
 * Fixtures de {@link Profissional}/{@link CicloAvaliacao} via repositório direto; limpeza por ID
 * rastreado.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AvaliacaoControllerTest {

    private static final String BASE_URL = "/api/v1/avaliacao/";
    private static final String PREFIXO_MATRICULA_TESTE = "TESTE-AVALIACAO-";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AvaliacaoRepository avaliacaoRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private CicloAvaliacaoRepository cicloAvaliacaoRepository;

    private final List<UUID> profissionalIdsCriados = new ArrayList<>();
    private final List<UUID> cicloIdsCriados = new ArrayList<>();

    @AfterEach
    void limparDadosDeTeste() {
        avaliacaoRepository.findAll().stream()
                .filter(a -> profissionalIdsCriados.contains(a.getProfissional().getUuid()))
                .forEach(a -> avaliacaoRepository.deleteById(a.getUuid()));
        profissionalRepository.deleteAllById(profissionalIdsCriados);
        cicloAvaliacaoRepository.deleteAllById(cicloIdsCriados);
    }

    private String criarProfissionalFixture(String sufixoMatricula) {
        Profissional profissional = new Profissional();
        String matricula = PREFIXO_MATRICULA_TESTE + sufixoMatricula;
        profissional.setMatricula(matricula);
        profissional.setCpf("00000000000");
        profissional.setNome("Profissional Fixture Avaliacao");
        profissional.setAtivo(true);
        profissionalIdsCriados.add(profissionalRepository.save(profissional).getUuid());
        return matricula;
    }

    private UUID criarCicloFixture(String sufixo) {
        CicloAvaliacao ciclo = new CicloAvaliacao();
        ciclo.setNome("Ciclo Fixture Avaliacao - " + sufixo);
        ciclo.setDataInicio(LocalDate.of(2027, 1, 1));
        ciclo.setDataFim(LocalDate.of(2027, 6, 30));
        UUID id = cicloAvaliacaoRepository.save(ciclo).getUuid();
        cicloIdsCriados.add(id);
        return id;
    }

    @Test
    void deveCriarComSucesso() throws Exception {
        String matricula = criarProfissionalFixture("01");
        UUID cicloId = criarCicloFixture("01");
        String body = """
                {
                  "matriculaProfissional": "%s",
                  "cicloId": "%s",
                  "avaliador": "Gestor Fulano",
                  "nota": 8.5
                }
                """.formatted(matricula, cicloId);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Avaliação registrada com sucesso!"));
    }

    @Test
    void deveRetornarNotFoundAoCriarComProfissionalInexistente() throws Exception {
        UUID cicloId = criarCicloFixture("02");
        String body = """
                {
                  "matriculaProfissional": "%s",
                  "cicloId": "%s",
                  "avaliador": "Gestor Fulano"
                }
                """.formatted(PREFIXO_MATRICULA_TESTE + "INEXISTENTE", cicloId);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornarNotFoundAoCriarComCicloInexistente() throws Exception {
        String matricula = criarProfissionalFixture("03");
        String body = """
                {
                  "matriculaProfissional": "%s",
                  "cicloId": "%s",
                  "avaliador": "Gestor Fulano"
                }
                """.formatted(matricula, UUID.randomUUID());

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveListarHistorico() throws Exception {
        String matricula = criarProfissionalFixture("04");
        UUID cicloId = criarCicloFixture("04");
        String body = """
                {
                  "matriculaProfissional": "%s",
                  "cicloId": "%s",
                  "avaliador": "Gestora Beltrana",
                  "nota": 9.0,
                  "observacao": "Excelente desempenho no período"
                }
                """.formatted(matricula, cicloId);

        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(get(BASE_URL + "profissional/" + matricula))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].avaliador").value("Gestora Beltrana"))
                .andExpect(jsonPath("$[0].nota").value(9.0))
                .andExpect(jsonPath("$[0].observacao").value("Excelente desempenho no período"));
    }

    @Test
    void deveRetornarNotFoundAoListarHistoricoDeProfissionalSemAvaliacoes() throws Exception {
        String matricula = criarProfissionalFixture("05");

        mockMvc.perform(get(BASE_URL + "profissional/" + matricula))
                .andExpect(status().isNotFound());
    }
}
