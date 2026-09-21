package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.repositories.CalculoRescisaoRepository;
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
 * Testes do CalculoRescisaoController (módulo RH, Fase 4 — ver docs/rh/MODELO-RH.md). Fixture de
 * {@link Profissional} criada direto via repositório; limpeza por ID rastreado.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CalculoRescisaoControllerTest {

    private static final String BASE_URL = "/api/v1/calculo-rescisao/";
    private static final String PREFIXO_MATRICULA_TESTE = "TESTE-RESCISAO-";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CalculoRescisaoRepository calculoRescisaoRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    private final List<UUID> profissionalIdsCriados = new ArrayList<>();

    @AfterEach
    void limparDadosDeTeste() {
        calculoRescisaoRepository.findAll().stream()
                .filter(c -> profissionalIdsCriados.contains(c.getProfissional().getUuid()))
                .forEach(c -> calculoRescisaoRepository.deleteById(c.getUuid()));
        profissionalRepository.deleteAllById(profissionalIdsCriados);
    }

    private String criarProfissionalFixture(String sufixoMatricula) {
        Profissional profissional = new Profissional();
        String matricula = PREFIXO_MATRICULA_TESTE + sufixoMatricula;
        profissional.setMatricula(matricula);
        profissional.setCpf("00000000000");
        profissional.setNome("Profissional Fixture CalculoRescisao");
        profissional.setAtivo(false);
        profissionalIdsCriados.add(profissionalRepository.save(profissional).getUuid());
        return matricula;
    }

    @Test
    void deveCriarComSucesso() throws Exception {
        String matricula = criarProfissionalFixture("01");
        String body = """
                {
                  "matriculaProfissional": "%s",
                  "tipoDesligamento": "SEM_JUSTA_CAUSA",
                  "avisoPrevio": 3000.00,
                  "feriasVencidas": 0.00,
                  "feriasProporcionais": 1500.00,
                  "decimoTerceiroProporcional": 1000.00,
                  "multaFgts": 1200.00,
                  "total": 6700.00
                }
                """.formatted(matricula);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Cálculo de rescisão registrado com sucesso!"));
    }

    @Test
    void deveRetornarNotFoundAoCriarComProfissionalInexistente() throws Exception {
        String body = """
                {
                  "matriculaProfissional": "%s",
                  "tipoDesligamento": "PEDIDO_DEMISSAO",
                  "total": 500.00
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
                  "tipoDesligamento": "APOSENTADORIA",
                  "feriasProporcionais": 800.00,
                  "total": 800.00,
                  "documentoTrctUrl": "https://storage/trct.pdf"
                }
                """.formatted(matricula);

        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(get(BASE_URL + "profissional/" + matricula))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tipoDesligamento").value("APOSENTADORIA"))
                .andExpect(jsonPath("$[0].total").value(800.00))
                .andExpect(jsonPath("$[0].documentoTrctUrl").value("https://storage/trct.pdf"));
    }

    @Test
    void deveRetornarNotFoundAoListarHistoricoDeProfissionalSemCalculo() throws Exception {
        String matricula = criarProfissionalFixture("03");

        mockMvc.perform(get(BASE_URL + "profissional/" + matricula))
                .andExpect(status().isNotFound());
    }
}
