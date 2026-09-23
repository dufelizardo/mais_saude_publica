package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.repositories.CapacidadeAdministrativaRepository;
import com.edufelizardo.maissaudepublica.repositories.ProcessoAdministrativoRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes do ProcessoAdministrativoController (Setor Administrativo Adaptativo, Fase 4 — ver
 * docs/adr/0033-processos-administrativos-por-capacidade.md). Fixture de
 * {@link com.edufelizardo.maissaudepublica.models.CapacidadeAdministrativa} via repositório
 * direto; limpeza por ID rastreado.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProcessoAdministrativoControllerTest {

    private static final String BASE_URL = "/api/v1/processo-administrativo/";
    private static final String PREFIXO_CODIGO_TESTE = "ROBOT-PROC-";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProcessoAdministrativoRepository processoAdministrativoRepository;

    @Autowired
    private CapacidadeAdministrativaRepository capacidadeAdministrativaRepository;

    private final List<UUID> processoIdsCriados = new ArrayList<>();
    private final List<UUID> capacidadeIdsCriadas = new ArrayList<>();

    @AfterEach
    void limparDadosDeTeste() {
        processoAdministrativoRepository.deleteAllById(processoIdsCriados);
        capacidadeAdministrativaRepository.deleteAllById(capacidadeIdsCriadas);
    }

    private UUID criarCapacidadeFixture(String sufixo) {
        com.edufelizardo.maissaudepublica.models.CapacidadeAdministrativa capacidade =
                new com.edufelizardo.maissaudepublica.models.CapacidadeAdministrativa(
                        "FIXTURE-CAP-" + sufixo, "Fixture Capacidade " + sufixo, null, true);
        UUID id = capacidadeAdministrativaRepository.save(capacidade).getUuid();
        capacidadeIdsCriadas.add(id);
        return id;
    }

    @Test
    void deveCriarComSucesso() throws Exception {
        UUID capacidadeId = criarCapacidadeFixture("01");
        String body = """
                {
                  "capacidadeId": "%s",
                  "codigo": "CADASTRO",
                  "nome": "Cadastro de bem",
                  "descricao": "Registro inicial de um bem patrimonial.",
                  "ativo": true
                }
                """.formatted(capacidadeId);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Processo administrativo criado com sucesso!"));

        processoAdministrativoRepository.findAll().stream()
                .filter(p -> p.getCapacidade().getUuid().equals(capacidadeId))
                .forEach(p -> processoIdsCriados.add(p.getUuid()));
    }

    @Test
    void deveRetornarNotFoundAoCriarComCapacidadeInexistente() throws Exception {
        String body = """
                {
                  "capacidadeId": "%s",
                  "codigo": "CADASTRO",
                  "nome": "Cadastro de bem",
                  "ativo": true
                }
                """.formatted(UUID.randomUUID());

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornarBadRequestAoCriarComCamposObrigatoriosEmBranco() throws Exception {
        String body = """
                {
                  "codigo": "",
                  "nome": ""
                }
                """;

        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveBuscarPorId() throws Exception {
        UUID capacidadeId = criarCapacidadeFixture("03");
        String body = """
                {
                  "capacidadeId": "%s",
                  "codigo": "TRANSFERENCIA",
                  "nome": "Transferência de bem",
                  "descricao": "Movimentação de um bem entre setores.",
                  "ativo": true
                }
                """.formatted(capacidadeId);

        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());

        UUID processoId = processoAdministrativoRepository.findAll().stream()
                .filter(p -> p.getCapacidade().getUuid().equals(capacidadeId))
                .findFirst()
                .orElseThrow()
                .getUuid();
        processoIdsCriados.add(processoId);

        mockMvc.perform(get(BASE_URL + processoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Transferência de bem"))
                .andExpect(jsonPath("$.capacidadeUuid").value(capacidadeId.toString()));
    }

    @Test
    void deveRetornarNotFoundAoBuscarIdInexistente() throws Exception {
        mockMvc.perform(get(BASE_URL + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveAtualizarComSucesso() throws Exception {
        UUID capacidadeId = criarCapacidadeFixture("05");
        String bodyCriacao = """
                {
                  "capacidadeId": "%s",
                  "codigo": "INVENTARIO",
                  "nome": "Inventário",
                  "ativo": true
                }
                """.formatted(capacidadeId);

        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(bodyCriacao))
                .andExpect(status().isCreated());

        UUID processoId = processoAdministrativoRepository.findAll().stream()
                .filter(p -> p.getCapacidade().getUuid().equals(capacidadeId))
                .findFirst()
                .orElseThrow()
                .getUuid();
        processoIdsCriados.add(processoId);

        String bodyAtualizacao = """
                {
                  "capacidadeId": "%s",
                  "codigo": "INVENTARIO",
                  "nome": "Inventário Anual",
                  "ativo": false
                }
                """.formatted(capacidadeId);

        mockMvc.perform(patch(BASE_URL + processoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyAtualizacao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Processo administrativo atualizado com sucesso!"));

        mockMvc.perform(get(BASE_URL + processoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Inventário Anual"))
                .andExpect(jsonPath("$.ativo").value(false));
    }

    @Test
    void deveRetornarNotFoundAoAtualizarProcessoInexistente() throws Exception {
        UUID capacidadeId = criarCapacidadeFixture("06");
        String body = """
                {
                  "capacidadeId": "%s",
                  "codigo": "BAIXA",
                  "nome": "Baixa de bem",
                  "ativo": true
                }
                """.formatted(capacidadeId);

        mockMvc.perform(patch(BASE_URL + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }
}
