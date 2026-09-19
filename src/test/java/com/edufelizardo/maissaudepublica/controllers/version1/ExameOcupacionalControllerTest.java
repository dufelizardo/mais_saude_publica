package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.repositories.ExameOcupacionalRepository;
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
 * Testes do ExameOcupacionalController (módulo RH, Fase 7/SST — ver docs/rh/MODELO-RH.md).
 * Fixture de {@link Profissional} criada direto via repositório; limpeza por ID rastreado.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ExameOcupacionalControllerTest {

    private static final String BASE_URL = "/api/v1/exame-ocupacional/";
    private static final String PREFIXO_MATRICULA_TESTE = "TESTE-EXAME-";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ExameOcupacionalRepository exameOcupacionalRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    private final List<UUID> profissionalIdsCriados = new ArrayList<>();

    @AfterEach
    void limparDadosDeTeste() {
        exameOcupacionalRepository.findAll().stream()
                .filter(e -> profissionalIdsCriados.contains(e.getProfissional().getUuid()))
                .forEach(e -> exameOcupacionalRepository.deleteById(e.getUuid()));
        profissionalRepository.deleteAllById(profissionalIdsCriados);
    }

    private String criarProfissionalFixture(String sufixoMatricula) {
        Profissional profissional = new Profissional();
        String matricula = PREFIXO_MATRICULA_TESTE + sufixoMatricula;
        profissional.setMatricula(matricula);
        profissional.setCpf("00000000000");
        profissional.setNome("Profissional Fixture ExameOcupacional");
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
                  "tipo": "PERIODICO",
                  "dataRealizacao": "2026-01-15",
                  "dataValidade": "2027-01-15",
                  "resultado": "APTO"
                }
                """.formatted(matricula);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Exame ocupacional registrado com sucesso!"));
    }

    @Test
    void deveRetornarNotFoundAoCriarComProfissionalInexistente() throws Exception {
        String body = """
                {
                  "matriculaProfissional": "%s",
                  "tipo": "ADMISSIONAL",
                  "dataRealizacao": "2026-01-15",
                  "resultado": "APTO"
                }
                """.formatted(PREFIXO_MATRICULA_TESTE + "INEXISTENTE");

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveListarHistorico() throws Exception {
        String matricula = criarProfissionalFixture("02");
        String body = """
                {
                  "matriculaProfissional": "%s",
                  "tipo": "DEMISSIONAL",
                  "dataRealizacao": "2026-02-01",
                  "resultado": "APTO_COM_RESTRICAO"
                }
                """.formatted(matricula);

        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(get(BASE_URL + "profissional/" + matricula))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tipo").value("DEMISSIONAL"))
                .andExpect(jsonPath("$[0].resultado").value("APTO_COM_RESTRICAO"));
    }

    @Test
    void deveRetornarNotFoundAoListarHistoricoDeProfissionalSemExames() throws Exception {
        String matricula = criarProfissionalFixture("03");

        mockMvc.perform(get(BASE_URL + "profissional/" + matricula))
                .andExpect(status().isNotFound());
    }
}
