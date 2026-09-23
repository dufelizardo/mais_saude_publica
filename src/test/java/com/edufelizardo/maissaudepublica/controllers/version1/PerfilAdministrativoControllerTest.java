package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.repositories.PerfilAdministrativoRepository;
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
 * Testes do PerfilAdministrativoController (Setor Administrativo Adaptativo, Fase 3 — ver
 * docs/adr/0031-perfil-administrativo-por-tipo-de-unidade.md). Catálogo sem FK — fixtures são
 * puramente locais, limpeza por ID rastreado.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PerfilAdministrativoControllerTest {

    private static final String BASE_URL = "/api/v1/perfil-administrativo/";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PerfilAdministrativoRepository perfilAdministrativoRepository;

    private final List<UUID> perfilIdsCriados = new ArrayList<>();

    @AfterEach
    void limparDadosDeTeste() {
        perfilAdministrativoRepository.deleteAllById(perfilIdsCriados);
    }

    @Test
    void deveCriarComSucesso() throws Exception {
        String body = """
                {
                  "codigo": "ADMIN_UBS-01",
                  "nome": "Perfil UBS",
                  "descricao": "Perfil padrão de UBS.",
                  "ativo": true
                }
                """;

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Perfil administrativo criado com sucesso!"));

        perfilAdministrativoRepository.findAll().stream()
                .filter(p -> p.getCodigo().equals("ADMIN_UBS-01"))
                .forEach(p -> perfilIdsCriados.add(p.getUuid()));
    }

    @Test
    void deveRetornarConflictAoCriarComCodigoDuplicado() throws Exception {
        String body = """
                {
                  "codigo": "ADMIN_HOSPITAL-02",
                  "nome": "Perfil Hospital",
                  "ativo": true
                }
                """;

        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());

        perfilAdministrativoRepository.findAll().stream()
                .filter(p -> p.getCodigo().equals("ADMIN_HOSPITAL-02"))
                .forEach(p -> perfilIdsCriados.add(p.getUuid()));

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
                  "codigo": "ADMIN_LABORATORIO-03",
                  "nome": "Perfil Laboratório",
                  "descricao": "Perfil padrão de laboratório clínico.",
                  "ativo": true
                }
                """;

        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());

        UUID perfilId = perfilAdministrativoRepository.findAll().stream()
                .filter(p -> p.getCodigo().equals("ADMIN_LABORATORIO-03"))
                .findFirst()
                .orElseThrow()
                .getUuid();
        perfilIdsCriados.add(perfilId);

        mockMvc.perform(get(BASE_URL + perfilId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Perfil Laboratório"));
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
                  "codigo": "ADMIN_CAPS-04",
                  "nome": "Perfil CAPS",
                  "ativo": true
                }
                """;

        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(bodyCriacao))
                .andExpect(status().isCreated());

        UUID perfilId = perfilAdministrativoRepository.findAll().stream()
                .filter(p -> p.getCodigo().equals("ADMIN_CAPS-04"))
                .findFirst()
                .orElseThrow()
                .getUuid();
        perfilIdsCriados.add(perfilId);

        String bodyAtualizacao = """
                {
                  "codigo": "ADMIN_CAPS-04",
                  "nome": "Perfil CAPS Atualizado",
                  "ativo": false
                }
                """;

        mockMvc.perform(patch(BASE_URL + perfilId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyAtualizacao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Perfil administrativo atualizado com sucesso!"));

        mockMvc.perform(get(BASE_URL + perfilId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Perfil CAPS Atualizado"))
                .andExpect(jsonPath("$.ativo").value(false));
    }

    @Test
    void deveRetornarNotFoundAoAtualizarPerfilInexistente() throws Exception {
        String body = """
                {
                  "codigo": "ADMIN_POLICLINICA-05",
                  "nome": "Perfil Policlínica",
                  "ativo": true
                }
                """;

        mockMvc.perform(patch(BASE_URL + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }
}
