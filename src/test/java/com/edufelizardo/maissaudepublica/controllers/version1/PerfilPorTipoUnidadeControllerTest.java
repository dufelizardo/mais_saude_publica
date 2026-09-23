package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.PerfilAdministrativo;
import com.edufelizardo.maissaudepublica.repositories.PerfilAdministrativoRepository;
import com.edufelizardo.maissaudepublica.repositories.PerfilPorTipoUnidadeRepository;
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
 * Testes do PerfilPorTipoUnidadeController (Setor Administrativo Adaptativo, Fase 3 — ver
 * docs/adr/0031-perfil-administrativo-por-tipo-de-unidade.md). Cada teste usa um
 * {@link com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude} diferente pra não
 * colidir com a constraint de unicidade de {@code tipo} entre execuções.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PerfilPorTipoUnidadeControllerTest {

    private static final String BASE_URL = "/api/v1/perfil-por-tipo-unidade/";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PerfilPorTipoUnidadeRepository perfilPorTipoUnidadeRepository;

    @Autowired
    private PerfilAdministrativoRepository perfilAdministrativoRepository;

    private final List<UUID> associacaoIdsCriadas = new ArrayList<>();
    private final List<UUID> perfilIdsCriados = new ArrayList<>();

    @AfterEach
    void limparDadosDeTeste() {
        perfilPorTipoUnidadeRepository.deleteAllById(associacaoIdsCriadas);
        perfilAdministrativoRepository.deleteAllById(perfilIdsCriados);
    }

    private UUID criarPerfilFixture(String sufixo) {
        PerfilAdministrativo perfil = new PerfilAdministrativo(
                "FIXTURE-PERFIL-" + sufixo, "Fixture Perfil " + sufixo, null, true);
        UUID id = perfilAdministrativoRepository.save(perfil).getUuid();
        perfilIdsCriados.add(id);
        return id;
    }

    @Test
    void deveCriarComSucesso() throws Exception {
        UUID perfilId = criarPerfilFixture("01");
        String body = """
                {
                  "tipo": "CAPS",
                  "perfilAdministrativoId": "%s"
                }
                """.formatted(perfilId);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Associação perfil/tipo de unidade criada com sucesso!"));

        perfilPorTipoUnidadeRepository.findAll().stream()
                .filter(a -> a.getPerfilAdministrativo().getUuid().equals(perfilId))
                .forEach(a -> associacaoIdsCriadas.add(a.getUuid()));
    }

    @Test
    void deveRetornarNotFoundAoCriarComPerfilInexistente() throws Exception {
        String body = """
                {
                  "tipo": "POLICLINICA",
                  "perfilAdministrativoId": "%s"
                }
                """.formatted(UUID.randomUUID());

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornarConflictAoCriarComTipoDuplicado() throws Exception {
        UUID perfilId = criarPerfilFixture("02");
        String body = """
                {
                  "tipo": "CENTRO_REABILITACAO",
                  "perfilAdministrativoId": "%s"
                }
                """.formatted(perfilId);

        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());

        perfilPorTipoUnidadeRepository.findAll().stream()
                .filter(a -> a.getPerfilAdministrativo().getUuid().equals(perfilId))
                .forEach(a -> associacaoIdsCriadas.add(a.getUuid()));

        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void deveBuscarPorTipo() throws Exception {
        UUID perfilId = criarPerfilFixture("03");
        String body = """
                {
                  "tipo": "CENTRO_ESPECIALIDADES",
                  "perfilAdministrativoId": "%s"
                }
                """.formatted(perfilId);

        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());

        perfilPorTipoUnidadeRepository.findAll().stream()
                .filter(a -> a.getPerfilAdministrativo().getUuid().equals(perfilId))
                .forEach(a -> associacaoIdsCriadas.add(a.getUuid()));

        mockMvc.perform(get(BASE_URL + "tipo/CENTRO_ESPECIALIDADES"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.perfilAdministrativoUuid").value(perfilId.toString()));
    }

    @Test
    void deveRetornarNotFoundAoBuscarPorTipoNaoConfigurado() throws Exception {
        mockMvc.perform(get(BASE_URL + "tipo/UPA"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveAtualizarComSucesso() throws Exception {
        UUID perfilOriginal = criarPerfilFixture("04a");
        UUID perfilNovo = criarPerfilFixture("04b");
        String bodyCriacao = """
                {
                  "tipo": "LABORATORIO",
                  "perfilAdministrativoId": "%s"
                }
                """.formatted(perfilOriginal);

        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(bodyCriacao))
                .andExpect(status().isCreated());

        UUID associacaoId = perfilPorTipoUnidadeRepository.findAll().stream()
                .filter(a -> a.getPerfilAdministrativo().getUuid().equals(perfilOriginal))
                .findFirst()
                .orElseThrow()
                .getUuid();
        associacaoIdsCriadas.add(associacaoId);

        String bodyAtualizacao = """
                {
                  "tipo": "LABORATORIO",
                  "perfilAdministrativoId": "%s"
                }
                """.formatted(perfilNovo);

        mockMvc.perform(patch(BASE_URL + associacaoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyAtualizacao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Associação perfil/tipo de unidade atualizada com sucesso!"));

        mockMvc.perform(get(BASE_URL + "tipo/LABORATORIO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.perfilAdministrativoUuid").value(perfilNovo.toString()));
    }
}
