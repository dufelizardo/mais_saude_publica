package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.SetorRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
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
 * Testes do SetorController (Setor Administrativo Adaptativo, Fase 1 — ver
 * docs/adr/0030-setor-administrativo-e-relacao-com-unidade-de-saude.md). Fixtures de
 * {@link UnidadeDeSaude} via repositório direto; limpeza por ID rastreado.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SetorControllerTest {

    private static final String BASE_URL = "/api/v1/setor/";
    private static final String PREFIXO_NOME_TESTE = "Fixture Setor - ";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SetorRepository setorRepository;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    private final List<UUID> setorIdsCriados = new ArrayList<>();
    private final List<UUID> unidadeIdsCriadas = new ArrayList<>();
    private final List<UUID> profissionalIdsCriados = new ArrayList<>();

    @AfterEach
    void limparDadosDeTeste() {
        setorRepository.deleteAllById(setorIdsCriados);
        unidadeDeSaudeRepository.deleteAllById(unidadeIdsCriadas);
        profissionalRepository.deleteAllById(profissionalIdsCriados);
    }

    private UUID criarUnidadeFixture(String sufixo) {
        UnidadeDeSaude unidade = new UnidadeDeSaude();
        unidade.setNome(PREFIXO_NOME_TESTE + "Unidade " + sufixo);
        unidade.setTipo(TipoUnidadeDeSaude.UBS);
        unidade.setAtivo(true);
        UUID id = unidadeDeSaudeRepository.save(unidade).getUuid();
        unidadeIdsCriadas.add(id);
        return id;
    }

    private String criarProfissionalFixture(String sufixo) {
        Profissional profissional = new Profissional();
        String matricula = "FIXTURE-SETOR-" + sufixo;
        profissional.setMatricula(matricula);
        profissional.setCpf("00000000000");
        profissional.setNome(PREFIXO_NOME_TESTE + "Profissional " + sufixo);
        profissional.setAtivo(true);
        UUID id = profissionalRepository.save(profissional).getUuid();
        profissionalIdsCriados.add(id);
        return matricula;
    }

    @Test
    void deveCriarComSucesso() throws Exception {
        UUID unidadeId = criarUnidadeFixture("01");
        String body = """
                {
                  "unidadeId": "%s",
                  "nome": "Administração",
                  "codigo": "ADM-01",
                  "tipo": "ADMINISTRATIVO",
                  "ativo": true
                }
                """.formatted(unidadeId);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Setor criado com sucesso!"));

        setorRepository.findAll().forEach(s -> setorIdsCriados.add(s.getUuid()));
    }

    @Test
    void deveRetornarNotFoundAoCriarComUnidadeInexistente() throws Exception {
        String body = """
                {
                  "unidadeId": "%s",
                  "nome": "Administração",
                  "codigo": "ADM-02",
                  "tipo": "ADMINISTRATIVO",
                  "ativo": true
                }
                """.formatted(UUID.randomUUID());

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveBuscarPorId() throws Exception {
        UUID unidadeId = criarUnidadeFixture("03");
        String body = """
                {
                  "unidadeId": "%s",
                  "nome": "Farmácia",
                  "codigo": "FAR-03",
                  "tipo": "ASSISTENCIAL",
                  "ativo": true
                }
                """.formatted(unidadeId);

        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());

        UUID setorId = setorRepository.findAll().stream()
                .filter(s -> s.getCodigo().equals("FAR-03"))
                .findFirst()
                .orElseThrow()
                .getUuid();
        setorIdsCriados.add(setorId);

        mockMvc.perform(get(BASE_URL + setorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Farmácia"))
                .andExpect(jsonPath("$.tipo").value("ASSISTENCIAL"));
    }

    @Test
    void deveRetornarNotFoundAoBuscarIdInexistente() throws Exception {
        mockMvc.perform(get(BASE_URL + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveAtualizarComSucesso() throws Exception {
        UUID unidadeId = criarUnidadeFixture("05");
        String bodyCriacao = """
                {
                  "unidadeId": "%s",
                  "nome": "Administração",
                  "codigo": "ADM-05",
                  "tipo": "ADMINISTRATIVO",
                  "ativo": true
                }
                """.formatted(unidadeId);

        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(bodyCriacao))
                .andExpect(status().isCreated());

        UUID setorId = setorRepository.findAll().stream()
                .filter(s -> s.getCodigo().equals("ADM-05"))
                .findFirst()
                .orElseThrow()
                .getUuid();
        setorIdsCriados.add(setorId);

        String bodyAtualizacao = """
                {
                  "unidadeId": "%s",
                  "nome": "Administração Central",
                  "codigo": "ADM-05",
                  "tipo": "ADMINISTRATIVO",
                  "ativo": false
                }
                """.formatted(unidadeId);

        mockMvc.perform(patch(BASE_URL + setorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyAtualizacao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Setor atualizado com sucesso!"));

        mockMvc.perform(get(BASE_URL + setorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Administração Central"))
                .andExpect(jsonPath("$.ativo").value(false));
    }

    @Test
    void deveCriarComResponsavel() throws Exception {
        UUID unidadeId = criarUnidadeFixture("07");
        String matriculaResponsavel = criarProfissionalFixture("07");
        String body = """
                {
                  "unidadeId": "%s",
                  "nome": "Administração",
                  "codigo": "ADM-07",
                  "tipo": "ADMINISTRATIVO",
                  "ativo": true,
                  "matriculaResponsavel": "%s"
                }
                """.formatted(unidadeId, matriculaResponsavel);

        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());

        UUID setorId = setorRepository.findAll().stream()
                .filter(s -> s.getCodigo().equals("ADM-07"))
                .findFirst()
                .orElseThrow()
                .getUuid();
        setorIdsCriados.add(setorId);

        mockMvc.perform(get(BASE_URL + setorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responsavelMatricula").value(matriculaResponsavel));
    }

    @Test
    void deveRetornarNotFoundAoCriarComResponsavelInexistente() throws Exception {
        UUID unidadeId = criarUnidadeFixture("08");
        String body = """
                {
                  "unidadeId": "%s",
                  "nome": "Administração",
                  "codigo": "ADM-08",
                  "tipo": "ADMINISTRATIVO",
                  "ativo": true,
                  "matriculaResponsavel": "00000000000000-00"
                }
                """.formatted(unidadeId);

        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveAtualizarResponsavel() throws Exception {
        UUID unidadeId = criarUnidadeFixture("09");
        String matriculaResponsavel = criarProfissionalFixture("09");
        String bodyCriacao = """
                {
                  "unidadeId": "%s",
                  "nome": "Administração",
                  "codigo": "ADM-09",
                  "tipo": "ADMINISTRATIVO",
                  "ativo": true
                }
                """.formatted(unidadeId);

        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(bodyCriacao))
                .andExpect(status().isCreated());

        UUID setorId = setorRepository.findAll().stream()
                .filter(s -> s.getCodigo().equals("ADM-09"))
                .findFirst()
                .orElseThrow()
                .getUuid();
        setorIdsCriados.add(setorId);

        String bodyAtualizacao = """
                {
                  "unidadeId": "%s",
                  "nome": "Administração",
                  "codigo": "ADM-09",
                  "tipo": "ADMINISTRATIVO",
                  "ativo": true,
                  "matriculaResponsavel": "%s"
                }
                """.formatted(unidadeId, matriculaResponsavel);

        mockMvc.perform(patch(BASE_URL + setorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyAtualizacao))
                .andExpect(status().isOk());

        mockMvc.perform(get(BASE_URL + setorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responsavelMatricula").value(matriculaResponsavel))
                .andExpect(jsonPath("$.responsavelNome").value(PREFIXO_NOME_TESTE + "Profissional 09"));
    }

    @Test
    void deveRetornarNotFoundAoAtualizarSetorInexistente() throws Exception {
        UUID unidadeId = criarUnidadeFixture("06");
        String body = """
                {
                  "unidadeId": "%s",
                  "nome": "Administração",
                  "codigo": "ADM-06",
                  "tipo": "ADMINISTRATIVO",
                  "ativo": true
                }
                """.formatted(unidadeId);

        mockMvc.perform(patch(BASE_URL + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }
}
