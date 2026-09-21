package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.AdesaoBeneficio;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.TipoBeneficio;
import com.edufelizardo.maissaudepublica.models.enuns.CusteioBeneficio;
import com.edufelizardo.maissaudepublica.repositories.AdesaoBeneficioRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.TipoBeneficioRepository;
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
 * Testes do AdesaoBeneficioController (módulo RH — ver docs/rh/MODELO-RH.md). Fixtures de
 * {@link Profissional}/{@link TipoBeneficio} via repositório; limpeza por ID rastreado.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdesaoBeneficioControllerTest {

    private static final String BASE_URL = "/api/v1/adesao-beneficio/";
    private static final String PREFIXO_MATRICULA_TESTE = "TESTE-ADESAO-";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AdesaoBeneficioRepository adesaoBeneficioRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private TipoBeneficioRepository tipoBeneficioRepository;

    private final List<UUID> profissionalIdsCriados = new ArrayList<>();
    private final List<UUID> tipoIdsCriados = new ArrayList<>();

    @AfterEach
    void limparDadosDeTeste() {
        adesaoBeneficioRepository.findAll().stream()
                .filter(a -> profissionalIdsCriados.contains(a.getProfissional().getUuid()))
                .forEach(a -> adesaoBeneficioRepository.deleteById(a.getUuid()));
        profissionalRepository.deleteAllById(profissionalIdsCriados);
        tipoBeneficioRepository.deleteAllById(tipoIdsCriados);
    }

    private String criarProfissionalFixture(String sufixoMatricula) {
        Profissional profissional = new Profissional();
        String matricula = PREFIXO_MATRICULA_TESTE + sufixoMatricula;
        profissional.setMatricula(matricula);
        profissional.setCpf("00000000000");
        profissional.setNome("Profissional Fixture AdesaoBeneficio");
        profissional.setAtivo(true);
        profissionalIdsCriados.add(profissionalRepository.save(profissional).getUuid());
        return matricula;
    }

    private UUID criarTipoFixture(String sufixo) {
        TipoBeneficio tipoBeneficio = new TipoBeneficio();
        tipoBeneficio.setNome("Beneficio Fixture AdesaoBeneficio - " + sufixo);
        tipoBeneficio.setCusteio(CusteioBeneficio.EMPRESA);
        UUID id = tipoBeneficioRepository.save(tipoBeneficio).getUuid();
        tipoIdsCriados.add(id);
        return id;
    }

    private UUID criarAdesaoFixture(String sufixo, LocalDate dataFim) {
        String matricula = criarProfissionalFixture(sufixo);
        Profissional profissional = profissionalRepository.findByMatricula(matricula).orElseThrow();
        UUID tipoId = criarTipoFixture(sufixo);
        TipoBeneficio tipoBeneficio = tipoBeneficioRepository.findById(tipoId).orElseThrow();

        AdesaoBeneficio adesaoBeneficio = new AdesaoBeneficio(profissional, tipoBeneficio, LocalDate.of(2026, 1, 1), null);
        adesaoBeneficio.setDataFim(dataFim);
        return adesaoBeneficioRepository.save(adesaoBeneficio).getUuid();
    }

    @Test
    void deveCriarComSucesso() throws Exception {
        String matricula = criarProfissionalFixture("01");
        UUID tipoId = criarTipoFixture("01");
        String body = """
                {
                  "matriculaProfissional": "%s",
                  "tipoBeneficioId": "%s",
                  "dataInicio": "2026-01-01"
                }
                """.formatted(matricula, tipoId);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Adesão de benefício criada com sucesso!"));
    }

    @Test
    void deveRetornarNotFoundAoCriarComProfissionalInexistente() throws Exception {
        UUID tipoId = criarTipoFixture("02");
        String body = """
                {
                  "matriculaProfissional": "%s",
                  "tipoBeneficioId": "%s",
                  "dataInicio": "2026-01-01"
                }
                """.formatted(PREFIXO_MATRICULA_TESTE + "INEXISTENTE", tipoId);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornarNotFoundAoCriarComTipoInexistente() throws Exception {
        String matricula = criarProfissionalFixture("03");
        String body = """
                {
                  "matriculaProfissional": "%s",
                  "tipoBeneficioId": "%s",
                  "dataInicio": "2026-01-01"
                }
                """.formatted(matricula, UUID.randomUUID());

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveManterDuasAdesoesVigentesSimultaneas() throws Exception {
        String matricula = criarProfissionalFixture("04");
        UUID tipoVT = criarTipoFixture("VT-04");
        UUID tipoVR = criarTipoFixture("VR-04");

        String bodyVT = """
                {
                  "matriculaProfissional": "%s",
                  "tipoBeneficioId": "%s",
                  "dataInicio": "2026-01-01"
                }
                """.formatted(matricula, tipoVT);
        String bodyVR = """
                {
                  "matriculaProfissional": "%s",
                  "tipoBeneficioId": "%s",
                  "dataInicio": "2026-01-01",
                  "quantidadeDependentes": 2
                }
                """.formatted(matricula, tipoVR);

        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(bodyVT))
                .andExpect(status().isCreated());
        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(bodyVR))
                .andExpect(status().isCreated());

        mockMvc.perform(get(BASE_URL + "profissional/" + matricula))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].dataFim").doesNotExist())
                .andExpect(jsonPath("$[1].dataFim").doesNotExist());
    }

    @Test
    void deveRetornarNotFoundAoListarHistoricoDeProfissionalSemAdesoes() throws Exception {
        String matricula = criarProfissionalFixture("05");

        mockMvc.perform(get(BASE_URL + "profissional/" + matricula))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveEncerrarComSucesso() throws Exception {
        UUID adesaoId = criarAdesaoFixture("06", null);

        mockMvc.perform(patch(BASE_URL + adesaoId + "/encerrar")
                        .param("dataFim", "2026-06-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Adesão de benefício encerrada com sucesso!"));
    }

    @Test
    void deveRetornarConflictAoEncerrarAdesaoJaEncerrada() throws Exception {
        UUID adesaoId = criarAdesaoFixture("07", LocalDate.of(2026, 3, 31));

        mockMvc.perform(patch(BASE_URL + adesaoId + "/encerrar")
                        .param("dataFim", "2026-06-30"))
                .andExpect(status().isConflict());
    }

    @Test
    void deveRetornarNotFoundAoEncerrarAdesaoInexistente() throws Exception {
        mockMvc.perform(patch(BASE_URL + UUID.randomUUID() + "/encerrar")
                        .param("dataFim", "2026-06-30"))
                .andExpect(status().isNotFound());
    }
}
