package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Afastamento;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.enuns.StatusAfastamento;
import com.edufelizardo.maissaudepublica.models.enuns.TipoAfastamento;
import com.edufelizardo.maissaudepublica.repositories.AfastamentoRepository;
import com.edufelizardo.maissaudepublica.repositories.LicencaRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes do LicencaController (módulo RH, Fase 3 — ver docs/rh/MODELO-RH.md). Fixtures de
 * {@link Profissional}/{@link Afastamento} via repositório direto; limpeza por ID rastreado.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LicencaControllerTest {

    private static final String BASE_URL = "/api/v1/licenca/";
    private static final String PREFIXO_MATRICULA_TESTE = "TESTE-LICENCA-";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LicencaRepository licencaRepository;

    @Autowired
    private AfastamentoRepository afastamentoRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    private final List<UUID> profissionalIdsCriados = new ArrayList<>();
    private final List<UUID> afastamentoIdsCriados = new ArrayList<>();

    @AfterEach
    void limparDadosDeTeste() {
        licencaRepository.findAll().stream()
                .filter(l -> afastamentoIdsCriados.contains(l.getAfastamento().getUuid()))
                .forEach(l -> licencaRepository.deleteById(l.getUuid()));
        afastamentoRepository.deleteAllById(afastamentoIdsCriados);
        profissionalRepository.deleteAllById(profissionalIdsCriados);
    }

    private UUID criarAfastamentoFixture(String sufixo) {
        Profissional profissional = new Profissional();
        profissional.setMatricula(PREFIXO_MATRICULA_TESTE + sufixo);
        profissional.setCpf("00000000000");
        profissional.setNome("Profissional Fixture Licenca");
        profissional.setAtivo(true);
        profissional = profissionalRepository.save(profissional);
        profissionalIdsCriados.add(profissional.getUuid());

        Afastamento afastamento = new Afastamento(profissional, TipoAfastamento.LICENCA_MEDICA,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 5, 1), StatusAfastamento.EM_ANDAMENTO, null);
        afastamento = afastamentoRepository.save(afastamento);
        afastamentoIdsCriados.add(afastamento.getUuid());
        return afastamento.getUuid();
    }

    @Test
    void deveCriarComSucesso() throws Exception {
        UUID afastamentoId = criarAfastamentoFixture("01");
        String body = """
                {
                  "afastamentoId": "%s",
                  "tipoLegal": "MATERNIDADE",
                  "responsavelPagamento": "INSS"
                }
                """.formatted(afastamentoId);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Licença criada com sucesso!"));
    }

    @Test
    void deveRetornarNotFoundAoCriarComAfastamentoInexistente() throws Exception {
        String body = """
                {
                  "afastamentoId": "%s",
                  "tipoLegal": "DOENCA",
                  "responsavelPagamento": "EMPRESA"
                }
                """.formatted(UUID.randomUUID());

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornarConflictAoCriarSegundaLicencaParaMesmoAfastamento() throws Exception {
        UUID afastamentoId = criarAfastamentoFixture("02");
        String body = """
                {
                  "afastamentoId": "%s",
                  "tipoLegal": "ACIDENTE_DE_TRABALHO",
                  "responsavelPagamento": "MISTO",
                  "documentoUrl": "https://storage/cat.pdf"
                }
                """.formatted(afastamentoId);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void deveBuscarPorAfastamento() throws Exception {
        UUID afastamentoId = criarAfastamentoFixture("03");
        String body = """
                {
                  "afastamentoId": "%s",
                  "tipoLegal": "PATERNIDADE",
                  "responsavelPagamento": "EMPRESA"
                }
                """.formatted(afastamentoId);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(get(BASE_URL + "afastamento/" + afastamentoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipoLegal").value("PATERNIDADE"))
                .andExpect(jsonPath("$.responsavelPagamento").value("EMPRESA"));
    }

    @Test
    void deveRetornarNotFoundAoBuscarAfastamentoSemLicenca() throws Exception {
        UUID afastamentoId = criarAfastamentoFixture("04");

        mockMvc.perform(get(BASE_URL + "afastamento/" + afastamentoId))
                .andExpect(status().isNotFound());
    }
}
