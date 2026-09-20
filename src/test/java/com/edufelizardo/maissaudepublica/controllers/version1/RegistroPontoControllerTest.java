package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.RegistroPonto;
import com.edufelizardo.maissaudepublica.models.enuns.TipoRegistroPonto;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.RegistroPontoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes do RegistroPontoController (módulo RH, Fase 2 — ver docs/rh/MODELO-RH.md). Fixture de
 * {@link Profissional} criada direto via repositório; limpeza por ID rastreado.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RegistroPontoControllerTest {

    private static final String BASE_URL = "/api/v1/registro-ponto/";
    private static final String PREFIXO_MATRICULA_TESTE = "TESTE-PONTO-";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RegistroPontoRepository registroPontoRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    private final List<UUID> profissionalIdsCriados = new ArrayList<>();

    @AfterEach
    void limparDadosDeTeste() {
        registroPontoRepository.findAll().stream()
                .filter(r -> profissionalIdsCriados.contains(r.getProfissional().getUuid()))
                .forEach(r -> registroPontoRepository.deleteById(r.getUuid()));
        profissionalRepository.deleteAllById(profissionalIdsCriados);
    }

    private String criarProfissionalFixture(String sufixoMatricula) {
        Profissional profissional = new Profissional();
        String matricula = PREFIXO_MATRICULA_TESTE + sufixoMatricula;
        profissional.setMatricula(matricula);
        profissional.setCpf("00000000000");
        profissional.setNome("Profissional Fixture RegistroPonto");
        profissional.setAtivo(true);
        profissionalIdsCriados.add(profissionalRepository.save(profissional).getUuid());
        return matricula;
    }

    private UUID criarRegistroFixture(String sufixo) {
        String matricula = criarProfissionalFixture(sufixo);
        Profissional profissional = profissionalRepository.findByMatricula(matricula).orElseThrow();
        RegistroPonto registroPonto = new RegistroPonto(profissional, LocalDateTime.of(2026, 1, 5, 8, 0), TipoRegistroPonto.ENTRADA, "app");
        return registroPontoRepository.save(registroPonto).getUuid();
    }

    @Test
    void deveCriarComSucesso() throws Exception {
        String matricula = criarProfissionalFixture("01");
        String body = """
                {
                  "matriculaProfissional": "%s",
                  "dataHora": "2026-01-05T08:00:00",
                  "tipo": "ENTRADA",
                  "origem": "app"
                }
                """.formatted(matricula);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Registro de ponto criado com sucesso!"));
    }

    @Test
    void deveRetornarNotFoundAoCriarComProfissionalInexistente() throws Exception {
        String body = """
                {
                  "matriculaProfissional": "%s",
                  "dataHora": "2026-01-05T08:00:00",
                  "tipo": "ENTRADA"
                }
                """.formatted(PREFIXO_MATRICULA_TESTE + "INEXISTENTE");

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveListarHistoricoComEntradaESaida() throws Exception {
        String matricula = criarProfissionalFixture("02");
        String bodyEntrada = """
                {
                  "matriculaProfissional": "%s",
                  "dataHora": "2026-01-05T08:00:00",
                  "tipo": "ENTRADA"
                }
                """.formatted(matricula);
        String bodySaida = """
                {
                  "matriculaProfissional": "%s",
                  "dataHora": "2026-01-05T17:00:00",
                  "tipo": "SAIDA"
                }
                """.formatted(matricula);

        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(bodyEntrada))
                .andExpect(status().isCreated());
        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(bodySaida))
                .andExpect(status().isCreated());

        mockMvc.perform(get(BASE_URL + "profissional/" + matricula))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].tipo").value("SAIDA"))
                .andExpect(jsonPath("$[1].tipo").value("ENTRADA"));
    }

    @Test
    void deveRetornarNotFoundAoListarHistoricoDeProfissionalSemRegistros() throws Exception {
        String matricula = criarProfissionalFixture("03");

        mockMvc.perform(get(BASE_URL + "profissional/" + matricula))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveFiltrarHistoricoPorPeriodo() throws Exception {
        String matricula = criarProfissionalFixture("04");
        String bodyJaneiro = """
                {
                  "matriculaProfissional": "%s",
                  "dataHora": "2026-01-05T08:00:00",
                  "tipo": "ENTRADA"
                }
                """.formatted(matricula);
        String bodyFevereiro = """
                {
                  "matriculaProfissional": "%s",
                  "dataHora": "2026-02-05T08:00:00",
                  "tipo": "ENTRADA"
                }
                """.formatted(matricula);

        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(bodyJaneiro))
                .andExpect(status().isCreated());
        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(bodyFevereiro))
                .andExpect(status().isCreated());

        mockMvc.perform(get(BASE_URL + "profissional/" + matricula)
                        .param("dataInicio", "2026-01-01")
                        .param("dataFim", "2026-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].dataHora").value("2026-01-05T08:00:00"));
    }

    @Test
    void deveSolicitarCorrecaoComSucesso() throws Exception {
        UUID registroId = criarRegistroFixture("05");
        String body = """
                {
                  "dataHoraProposta": "2026-01-05T08:03:00",
                  "tipoProposto": "ENTRADA",
                  "justificativa": "Relógio de ponto marcou errado"
                }
                """;

        mockMvc.perform(patch(BASE_URL + registroId + "/solicitar-correcao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Correção de ponto solicitada com sucesso!"));
    }

    @Test
    void deveRetornarConflictAoSolicitarCorrecaoJaPendente() throws Exception {
        UUID registroId = criarRegistroFixture("06");
        String body = """
                {
                  "dataHoraProposta": "2026-01-05T08:03:00",
                  "tipoProposto": "ENTRADA",
                  "justificativa": "Relógio de ponto marcou errado"
                }
                """;

        mockMvc.perform(patch(BASE_URL + registroId + "/solicitar-correcao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
        mockMvc.perform(patch(BASE_URL + registroId + "/solicitar-correcao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void deveRetornarNotFoundAoSolicitarCorrecaoDeRegistroInexistente() throws Exception {
        String body = """
                {
                  "dataHoraProposta": "2026-01-05T08:03:00",
                  "tipoProposto": "ENTRADA",
                  "justificativa": "Relógio de ponto marcou errado"
                }
                """;

        mockMvc.perform(patch(BASE_URL + UUID.randomUUID() + "/solicitar-correcao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveAprovarCorrecaoEAplicarNoRegistro() throws Exception {
        UUID registroId = criarRegistroFixture("07");
        String body = """
                {
                  "dataHoraProposta": "2026-01-05T08:03:00",
                  "tipoProposto": "SAIDA",
                  "justificativa": "Relógio de ponto marcou errado"
                }
                """;
        mockMvc.perform(patch(BASE_URL + registroId + "/solicitar-correcao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        mockMvc.perform(patch(BASE_URL + registroId + "/aprovar-correcao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Correção de ponto aprovada com sucesso!"));

        RegistroPonto atualizado = registroPontoRepository.findById(registroId).orElseThrow();
        Assertions.assertEquals(LocalDateTime.of(2026, 1, 5, 8, 3), atualizado.getDataHora());
        Assertions.assertEquals(TipoRegistroPonto.SAIDA, atualizado.getTipo());
        Assertions.assertNull(atualizado.getDataHoraProposta());
    }

    @Test
    void deveRetornarConflictAoAprovarCorrecaoSemPendencia() throws Exception {
        UUID registroId = criarRegistroFixture("08");

        mockMvc.perform(patch(BASE_URL + registroId + "/aprovar-correcao"))
                .andExpect(status().isConflict());
    }

    @Test
    void deveRejeitarCorrecaoEManterRegistroOriginal() throws Exception {
        UUID registroId = criarRegistroFixture("09");
        String body = """
                {
                  "dataHoraProposta": "2026-01-05T08:03:00",
                  "tipoProposto": "SAIDA",
                  "justificativa": "Relógio de ponto marcou errado"
                }
                """;
        mockMvc.perform(patch(BASE_URL + registroId + "/solicitar-correcao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        mockMvc.perform(patch(BASE_URL + registroId + "/rejeitar-correcao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Correção de ponto rejeitada com sucesso!"));

        RegistroPonto atualizado = registroPontoRepository.findById(registroId).orElseThrow();
        Assertions.assertEquals(LocalDateTime.of(2026, 1, 5, 8, 0), atualizado.getDataHora());
        Assertions.assertEquals(TipoRegistroPonto.ENTRADA, atualizado.getTipo());
        Assertions.assertNull(atualizado.getDataHoraProposta());
    }

    @Test
    void deveRetornarConflictAoRejeitarCorrecaoSemPendencia() throws Exception {
        UUID registroId = criarRegistroFixture("10");

        mockMvc.perform(patch(BASE_URL + registroId + "/rejeitar-correcao"))
                .andExpect(status().isConflict());
    }
}
