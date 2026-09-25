package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.repositories.CapacidadeAdministrativaRepository;
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
 * Testes do CapacidadeAdministrativaController (Setor Administrativo Adaptativo, Fase 2 — ver
 * docs/adr/0032-catalogo-de-capacidades-administrativas.md). Catálogo sem FK — fixtures são
 * puramente locais, limpeza por ID rastreado.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CapacidadeAdministrativaControllerTest {

    private static final String BASE_URL = "/api/v1/capacidade-administrativa/";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CapacidadeAdministrativaRepository capacidadeAdministrativaRepository;

    private final List<UUID> capacidadeIdsCriadas = new ArrayList<>();

    @AfterEach
    void limparDadosDeTeste() {
        capacidadeAdministrativaRepository.deleteAllById(capacidadeIdsCriadas);
    }

    @Test
    void deveCriarComSucesso() throws Exception {
        String body = """
                {
                  "codigo": "PATRIMONIO-01",
                  "nome": "Patrimônio",
                  "descricao": "Cadastro e controle de bens.",
                  "ativo": true
                }
                """;

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Capacidade administrativa criada com sucesso!"));

        capacidadeAdministrativaRepository.findAll().stream()
                .filter(c -> c.getCodigo().equals("PATRIMONIO-01"))
                .forEach(c -> capacidadeIdsCriadas.add(c.getUuid()));
    }

    @Test
    void deveRetornarConflictAoCriarComCodigoDuplicado() throws Exception {
        String body = """
                {
                  "codigo": "ESTOQUE-02",
                  "nome": "Estoque",
                  "ativo": true
                }
                """;

        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());

        capacidadeAdministrativaRepository.findAll().stream()
                .filter(c -> c.getCodigo().equals("ESTOQUE-02"))
                .forEach(c -> capacidadeIdsCriadas.add(c.getUuid()));

        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
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
        String body = """
                {
                  "codigo": "COMPRAS-03",
                  "nome": "Compras",
                  "descricao": "Solicitação, cotação, aprovação, pedido, recebimento e encerramento.",
                  "ativo": true
                }
                """;

        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());

        UUID capacidadeId = capacidadeAdministrativaRepository.findAll().stream()
                .filter(c -> c.getCodigo().equals("COMPRAS-03"))
                .findFirst()
                .orElseThrow()
                .getUuid();
        capacidadeIdsCriadas.add(capacidadeId);

        mockMvc.perform(get(BASE_URL + capacidadeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Compras"))
                .andExpect(jsonPath("$.descricao").value("Solicitação, cotação, aprovação, pedido, recebimento e encerramento."));
    }

    @Test
    void deveRetornarNotFoundAoBuscarIdInexistente() throws Exception {
        mockMvc.perform(get(BASE_URL + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveAtualizarComSucesso() throws Exception {
        String bodyCriacao = """
                {
                  "codigo": "CONTRATOS-04",
                  "nome": "Contratos",
                  "ativo": true
                }
                """;

        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(bodyCriacao))
                .andExpect(status().isCreated());

        UUID capacidadeId = capacidadeAdministrativaRepository.findAll().stream()
                .filter(c -> c.getCodigo().equals("CONTRATOS-04"))
                .findFirst()
                .orElseThrow()
                .getUuid();
        capacidadeIdsCriadas.add(capacidadeId);

        String bodyAtualizacao = """
                {
                  "codigo": "CONTRATOS-04",
                  "nome": "Gestão de Contratos",
                  "ativo": false
                }
                """;

        mockMvc.perform(patch(BASE_URL + capacidadeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyAtualizacao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Capacidade administrativa atualizada com sucesso!"));

        mockMvc.perform(get(BASE_URL + capacidadeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Gestão de Contratos"))
                .andExpect(jsonPath("$.ativo").value(false));
    }

    @Test
    void deveRetornarNotFoundAoAtualizarCapacidadeInexistente() throws Exception {
        String body = """
                {
                  "codigo": "MANUTENCAO-05",
                  "nome": "Manutenção",
                  "ativo": true
                }
                """;

        mockMvc.perform(patch(BASE_URL + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }
}
