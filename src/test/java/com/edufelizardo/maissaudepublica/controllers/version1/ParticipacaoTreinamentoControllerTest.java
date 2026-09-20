package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.Treinamento;
import com.edufelizardo.maissaudepublica.repositories.ParticipacaoTreinamentoRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.TreinamentoRepository;
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
 * Testes do ParticipacaoTreinamentoController (módulo RH, Fase 6 — ver docs/rh/MODELO-RH.md).
 * Foco principal: o cálculo de {@code dataValidade} a partir de
 * {@code treinamento.validadeMeses}, feito no service, não informado pelo cliente.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ParticipacaoTreinamentoControllerTest {

    private static final String BASE_URL = "/api/v1/participacao-treinamento/";
    private static final String PREFIXO_MATRICULA_TESTE = "TESTE-PARTICIPACAO-";
    private static final String PREFIXO_NOME_TREINAMENTO_TESTE = "Treinamento Fixture Participacao - ";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ParticipacaoTreinamentoRepository participacaoTreinamentoRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private TreinamentoRepository treinamentoRepository;

    private final List<UUID> profissionalIdsCriados = new ArrayList<>();
    private final List<UUID> treinamentoIdsCriados = new ArrayList<>();

    @AfterEach
    void limparDadosDeTeste() {
        participacaoTreinamentoRepository.findAll().stream()
                .filter(p -> profissionalIdsCriados.contains(p.getProfissional().getUuid()))
                .forEach(p -> participacaoTreinamentoRepository.deleteById(p.getUuid()));
        profissionalRepository.deleteAllById(profissionalIdsCriados);
        treinamentoRepository.deleteAllById(treinamentoIdsCriados);
    }

    private String criarProfissionalFixture(String sufixoMatricula) {
        Profissional profissional = new Profissional();
        String matricula = PREFIXO_MATRICULA_TESTE + sufixoMatricula;
        profissional.setMatricula(matricula);
        profissional.setCpf("00000000000");
        profissional.setNome("Profissional Fixture Participacao");
        profissional.setAtivo(true);
        profissionalIdsCriados.add(profissionalRepository.save(profissional).getUuid());
        return matricula;
    }

    private UUID criarTreinamentoFixture(String sufixo, Integer validadeMeses) {
        Treinamento treinamento = new Treinamento();
        treinamento.setNome(PREFIXO_NOME_TREINAMENTO_TESTE + sufixo);
        treinamento.setCargaHoraria(8);
        treinamento.setValidadeMeses(validadeMeses);
        treinamento.setObrigatorio(true);
        UUID id = treinamentoRepository.save(treinamento).getUuid();
        treinamentoIdsCriados.add(id);
        return id;
    }

    @Test
    void deveCalcularDataValidadeCorretamente() throws Exception {
        String matricula = criarProfissionalFixture("01");
        UUID treinamentoId = criarTreinamentoFixture("01", 12);

        String body = """
                {
                  "matriculaProfissional": "%s",
                  "treinamentoId": "%s",
                  "dataConclusao": "2026-01-15"
                }
                """.formatted(matricula, treinamentoId);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Participação em treinamento registrada com sucesso!"));

        mockMvc.perform(get(BASE_URL + "profissional/" + matricula))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].dataConclusao").value("2026-01-15"))
                .andExpect(jsonPath("$[0].dataValidade").value("2027-01-15"));
    }

    @Test
    void deveDeixarDataValidadeNulaQuandoTreinamentoNaoTemValidade() throws Exception {
        String matricula = criarProfissionalFixture("02");
        UUID treinamentoId = criarTreinamentoFixture("02", null);

        String body = """
                {
                  "matriculaProfissional": "%s",
                  "treinamentoId": "%s",
                  "dataConclusao": "2026-01-15"
                }
                """.formatted(matricula, treinamentoId);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(get(BASE_URL + "profissional/" + matricula))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].dataValidade").doesNotExist());
    }

    @Test
    void deveRetornarNotFoundAoCriarComProfissionalInexistente() throws Exception {
        UUID treinamentoId = criarTreinamentoFixture("03", 12);
        String body = """
                {
                  "matriculaProfissional": "%s",
                  "treinamentoId": "%s",
                  "dataConclusao": "2026-01-15"
                }
                """.formatted(PREFIXO_MATRICULA_TESTE + "INEXISTENTE", treinamentoId);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornarNotFoundAoCriarComTreinamentoInexistente() throws Exception {
        String matricula = criarProfissionalFixture("04");
        String body = """
                {
                  "matriculaProfissional": "%s",
                  "treinamentoId": "%s",
                  "dataConclusao": "2026-01-15"
                }
                """.formatted(matricula, UUID.randomUUID());

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornarNotFoundAoListarHistoricoDeProfissionalSemParticipacoes() throws Exception {
        String matricula = criarProfissionalFixture("05");

        mockMvc.perform(get(BASE_URL + "profissional/" + matricula))
                .andExpect(status().isNotFound());
    }
}
