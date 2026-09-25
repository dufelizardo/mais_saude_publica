package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.ResponsabilidadeAdministrativa;
import com.edufelizardo.maissaudepublica.models.Setor;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.TipoSetor;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.ResponsabilidadeAdministrativaRepository;
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes do ResponsabilidadeAdministrativaController (Setor Administrativo Adaptativo, Fase 6 — ver
 * docs/adr/0035-responsabilidade-administrativa-separada-da-lotacao.md). Fixtures de
 * {@link Profissional}/{@link UnidadeDeSaude}/{@link Setor} via repositório direto; limpeza por ID
 * rastreado.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ResponsabilidadeAdministrativaControllerTest {

    private static final String BASE_URL = "/api/v1/responsabilidade-administrativa/";
    private static final String PREFIXO_MATRICULA_TESTE = "TESTE-RESPADM-";
    private static final String PREFIXO_NOME_TESTE = "Fixture RespAdm - ";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ResponsabilidadeAdministrativaRepository responsabilidadeAdministrativaRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private SetorRepository setorRepository;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    private final List<UUID> profissionalIdsCriados = new ArrayList<>();
    private final List<UUID> setorIdsCriados = new ArrayList<>();
    private final List<UUID> unidadeIdsCriadas = new ArrayList<>();

    @AfterEach
    void limparDadosDeTeste() {
        responsabilidadeAdministrativaRepository.findAll().stream()
                .filter(r -> profissionalIdsCriados.contains(r.getProfissional().getUuid()))
                .forEach(r -> responsabilidadeAdministrativaRepository.deleteById(r.getUuid()));
        setorRepository.deleteAllById(setorIdsCriados);
        unidadeDeSaudeRepository.deleteAllById(unidadeIdsCriadas);
        profissionalRepository.deleteAllById(profissionalIdsCriados);
    }

    private String criarProfissionalFixture(String sufixo) {
        Profissional profissional = new Profissional();
        String matricula = PREFIXO_MATRICULA_TESTE + sufixo;
        profissional.setMatricula(matricula);
        profissional.setCpf("00000000000");
        profissional.setNome(PREFIXO_NOME_TESTE + "Profissional " + sufixo);
        profissional.setAtivo(true);
        profissionalIdsCriados.add(profissionalRepository.save(profissional).getUuid());
        return matricula;
    }

    private UUID criarSetorFixture(String sufixo) {
        UnidadeDeSaude unidade = new UnidadeDeSaude();
        unidade.setNome(PREFIXO_NOME_TESTE + "Unidade " + sufixo);
        unidade.setTipo(TipoUnidadeDeSaude.UBS);
        unidade.setAtivo(true);
        unidade = unidadeDeSaudeRepository.save(unidade);
        unidadeIdsCriadas.add(unidade.getUuid());

        Setor setor = new Setor(unidade, PREFIXO_NOME_TESTE + "Setor " + sufixo, "ROBOT-RESPADM-" + sufixo,
                TipoSetor.ADMINISTRATIVO, true, null);
        UUID id = setorRepository.save(setor).getUuid();
        setorIdsCriados.add(id);
        return id;
    }

    private UUID criarResponsabilidadeFixture(String sufixo, LocalDate dataFim) {
        String matricula = criarProfissionalFixture(sufixo);
        Profissional profissional = profissionalRepository.findByMatricula(matricula).orElseThrow();
        UUID setorId = criarSetorFixture(sufixo);
        Setor setor = setorRepository.findById(setorId).orElseThrow();

        ResponsabilidadeAdministrativa responsabilidade = new ResponsabilidadeAdministrativa(
                profissional, setor, "Fiscal de Contrato", null, LocalDate.of(2026, 1, 1));
        responsabilidade.setDataFim(dataFim);
        return responsabilidadeAdministrativaRepository.save(responsabilidade).getUuid();
    }

    @Test
    void deveCriarComSucesso() throws Exception {
        String matricula = criarProfissionalFixture("01");
        UUID setorId = criarSetorFixture("01");
        String body = """
                {
                  "matriculaProfissional": "%s",
                  "setorId": "%s",
                  "tipo": "Fiscal de Contrato",
                  "dataInicio": "2026-01-01"
                }
                """.formatted(matricula, setorId);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Responsabilidade administrativa criada com sucesso!"));
    }

    @Test
    void deveRetornarNotFoundAoCriarComProfissionalInexistente() throws Exception {
        UUID setorId = criarSetorFixture("02");
        String body = """
                {
                  "matriculaProfissional": "%s",
                  "setorId": "%s",
                  "tipo": "Fiscal de Contrato",
                  "dataInicio": "2026-01-01"
                }
                """.formatted(PREFIXO_MATRICULA_TESTE + "INEXISTENTE", setorId);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornarNotFoundAoCriarComSetorInexistente() throws Exception {
        String matricula = criarProfissionalFixture("03");
        String body = """
                {
                  "matriculaProfissional": "%s",
                  "setorId": "%s",
                  "tipo": "Fiscal de Contrato",
                  "dataInicio": "2026-01-01"
                }
                """.formatted(matricula, UUID.randomUUID());

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveManterDuasResponsabilidadesVigentesSimultaneas() throws Exception {
        String matricula = criarProfissionalFixture("04");
        UUID setorFiscal = criarSetorFixture("FISCAL-04");
        UUID setorPatrimonio = criarSetorFixture("PATRIMONIO-04");

        String bodyFiscal = """
                {
                  "matriculaProfissional": "%s",
                  "setorId": "%s",
                  "tipo": "Fiscal de Contrato",
                  "dataInicio": "2026-01-01"
                }
                """.formatted(matricula, setorFiscal);
        String bodyPatrimonio = """
                {
                  "matriculaProfissional": "%s",
                  "setorId": "%s",
                  "tipo": "Gestão de Patrimônio",
                  "dataInicio": "2026-01-01"
                }
                """.formatted(matricula, setorPatrimonio);

        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(bodyFiscal))
                .andExpect(status().isCreated());
        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(bodyPatrimonio))
                .andExpect(status().isCreated());

        mockMvc.perform(get(BASE_URL + "profissional/" + matricula))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].dataFim").doesNotExist())
                .andExpect(jsonPath("$[1].dataFim").doesNotExist());
    }

    @Test
    void deveRetornarNotFoundAoListarHistoricoDeProfissionalSemResponsabilidades() throws Exception {
        String matricula = criarProfissionalFixture("05");

        mockMvc.perform(get(BASE_URL + "profissional/" + matricula))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveEncerrarComSucesso() throws Exception {
        UUID responsabilidadeId = criarResponsabilidadeFixture("06", null);

        mockMvc.perform(patch(BASE_URL + responsabilidadeId + "/encerrar")
                        .param("dataFim", "2026-06-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Responsabilidade administrativa encerrada com sucesso!"));
    }

    @Test
    void deveRetornarConflictAoEncerrarResponsabilidadeJaEncerrada() throws Exception {
        UUID responsabilidadeId = criarResponsabilidadeFixture("07", LocalDate.of(2026, 3, 31));

        mockMvc.perform(patch(BASE_URL + responsabilidadeId + "/encerrar")
                        .param("dataFim", "2026-06-30"))
                .andExpect(status().isConflict());
    }

    @Test
    void deveRetornarNotFoundAoEncerrarResponsabilidadeInexistente() throws Exception {
        mockMvc.perform(patch(BASE_URL + UUID.randomUUID() + "/encerrar")
                        .param("dataFim", "2026-06-30"))
                .andExpect(status().isNotFound());
    }
}
