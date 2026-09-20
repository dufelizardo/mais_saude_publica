package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.repositories.AcidenteTrabalhoRepository;
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
 * Testes do AcidenteTrabalhoController (módulo RH, Fase 7/SST — ver docs/rh/MODELO-RH.md).
 * Fixture de {@link Profissional} criada direto via repositório; limpeza por ID rastreado.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AcidenteTrabalhoControllerTest {

    private static final String BASE_URL = "/api/v1/acidente-trabalho/";
    private static final String PREFIXO_MATRICULA_TESTE = "TESTE-ACIDENTE-";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AcidenteTrabalhoRepository acidenteTrabalhoRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    private final List<UUID> profissionalIdsCriados = new ArrayList<>();

    @AfterEach
    void limparDadosDeTeste() {
        acidenteTrabalhoRepository.findAll().stream()
                .filter(a -> profissionalIdsCriados.contains(a.getProfissional().getUuid()))
                .forEach(a -> acidenteTrabalhoRepository.deleteById(a.getUuid()));
        profissionalRepository.deleteAllById(profissionalIdsCriados);
    }

    private String criarProfissionalFixture(String sufixoMatricula) {
        Profissional profissional = new Profissional();
        String matricula = PREFIXO_MATRICULA_TESTE + sufixoMatricula;
        profissional.setMatricula(matricula);
        profissional.setCpf("00000000000");
        profissional.setNome("Profissional Fixture AcidenteTrabalho");
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
                  "dataHora": "2026-01-15T10:30:00",
                  "descricao": "Corte superficial durante procedimento",
                  "catEmitida": true,
                  "diasAfastamento": 3
                }
                """.formatted(matricula);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Acidente de trabalho registrado com sucesso!"));
    }

    @Test
    void deveRetornarNotFoundAoCriarComProfissionalInexistente() throws Exception {
        String body = """
                {
                  "matriculaProfissional": "%s",
                  "dataHora": "2026-01-15T10:30:00",
                  "catEmitida": false
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
                  "dataHora": "2026-02-01T09:00:00",
                  "catEmitida": true,
                  "diasAfastamento": 10
                }
                """.formatted(matricula);

        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(get(BASE_URL + "profissional/" + matricula))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].catEmitida").value(true))
                .andExpect(jsonPath("$[0].diasAfastamento").value(10));
    }

    @Test
    void deveRetornarNotFoundAoListarHistoricoDeProfissionalSemAcidentes() throws Exception {
        String matricula = criarProfissionalFixture("03");

        mockMvc.perform(get(BASE_URL + "profissional/" + matricula))
                .andExpect(status().isNotFound());
    }
}
