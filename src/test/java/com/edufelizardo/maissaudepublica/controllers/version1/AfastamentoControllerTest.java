package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.repositories.AfastamentoRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes do AfastamentoController (módulo RH, Fase 1 — ver docs/rh/MODELO-RH.md). Fixture de
 * {@link Profissional} criada direto via repositório; limpeza por ID rastreado.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AfastamentoControllerTest {

    private static final String BASE_URL = "/api/v1/afastamento/";
    private static final String PREFIXO_MATRICULA_TESTE = "TESTE-AFASTAMENTO-";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AfastamentoRepository afastamentoRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    private final List<UUID> profissionalIdsCriados = new ArrayList<>();

    @AfterEach
    void limparDadosDeTeste() {
        afastamentoRepository.findAll().stream()
                .filter(a -> profissionalIdsCriados.contains(a.getProfissional().getUuid()))
                .forEach(a -> afastamentoRepository.deleteById(a.getUuid()));
        profissionalRepository.deleteAllById(profissionalIdsCriados);
    }

    private String criarProfissionalFixture(String sufixoMatricula) {
        Profissional profissional = new Profissional();
        String matricula = PREFIXO_MATRICULA_TESTE + sufixoMatricula;
        profissional.setMatricula(matricula);
        profissional.setCpf("00000000000");
        profissional.setNome("Profissional Fixture Afastamento");
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
                  "tipo": "FERIAS",
                  "dataInicio": "2026-01-01",
                  "dataFim": "2026-01-30",
                  "status": "APROVADO"
                }
                """.formatted(matricula);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Afastamento criado com sucesso!"));
    }

    @Test
    void naoDeveAlterarStatusAtivoDoProfissional() throws Exception {
        String matricula = criarProfissionalFixture("02");
        String body = """
                {
                  "matriculaProfissional": "%s",
                  "tipo": "LICENCA_MEDICA",
                  "dataInicio": "2026-02-01",
                  "dataFim": "2026-02-15",
                  "status": "EM_ANDAMENTO"
                }
                """.formatted(matricula);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        boolean ativo = profissionalRepository.findByMatricula(matricula).orElseThrow().isAtivo();
        assertThat(ativo).isTrue();
    }

    @Test
    void deveRetornarNotFoundAoCriarComProfissionalInexistente() throws Exception {
        String body = """
                {
                  "matriculaProfissional": "%s",
                  "tipo": "FERIAS",
                  "dataInicio": "2026-01-01",
                  "dataFim": "2026-01-30",
                  "status": "APROVADO"
                }
                """.formatted(PREFIXO_MATRICULA_TESTE + "INEXISTENTE");

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveListarHistoricoComMaisDeUmAfastamento() throws Exception {
        String matricula = criarProfissionalFixture("03");
        String bodyFerias = """
                {
                  "matriculaProfissional": "%s",
                  "tipo": "FERIAS",
                  "dataInicio": "2026-01-01",
                  "dataFim": "2026-01-30",
                  "status": "CONCLUIDO"
                }
                """.formatted(matricula);
        String bodyLicenca = """
                {
                  "matriculaProfissional": "%s",
                  "tipo": "LICENCA_PESSOAL",
                  "dataInicio": "2026-03-01",
                  "dataFim": "2026-03-05",
                  "status": "SOLICITADO"
                }
                """.formatted(matricula);

        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(bodyFerias))
                .andExpect(status().isCreated());
        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(bodyLicenca))
                .andExpect(status().isCreated());

        mockMvc.perform(get(BASE_URL + "profissional/" + matricula))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void deveRetornarNotFoundAoListarHistoricoDeProfissionalSemAfastamentos() throws Exception {
        String matricula = criarProfissionalFixture("04");

        mockMvc.perform(get(BASE_URL + "profissional/" + matricula))
                .andExpect(status().isNotFound());
    }
}
