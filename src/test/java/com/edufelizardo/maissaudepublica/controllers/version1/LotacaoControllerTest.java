package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Cargo;
import com.edufelizardo.maissaudepublica.models.CategoriaSalarial;
import com.edufelizardo.maissaudepublica.models.Lotacao;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.CargoRepository;
import com.edufelizardo.maissaudepublica.repositories.CategoriaSalarialRepository;
import com.edufelizardo.maissaudepublica.repositories.LotacaoRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes do LotacaoController (módulo RH — ver docs/rh/MODELO-RH.md). Fixtures de
 * {@link Profissional}, {@link UnidadeDeSaude}, {@link CategoriaSalarial} e {@link Cargo} são
 * criadas direto via repositório (já cobertas pelos próprios testes de cada uma) — aqui o foco é
 * só o comportamento de Lotacao, principalmente a regra de "no máximo uma vigente por vez".
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LotacaoControllerTest {

    private static final String BASE_URL = "/api/v1/lotacao/";
    private static final String PREFIXO_MATRICULA_TESTE = "TESTE-LOTACAO-";
    private static final String PREFIXO_NOME_UNIDADE_TESTE = "Unidade Fixture Lotacao - ";
    private static final String PREFIXO_NOME_CATEGORIA_TESTE = "Categoria Fixture Lotacao - ";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LotacaoRepository lotacaoRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @Autowired
    private CargoRepository cargoRepository;

    @Autowired
    private CategoriaSalarialRepository categoriaSalarialRepository;

    /**
     * IDs rastreados na criação das fixtures, em vez de re-buscar depois e filtrar por campo de
     * relacionamento LAZY (ex.: {@code cargo.getCategoria().getNome()} fora de uma requisição HTTP
     * lançaria {@code LazyInitializationException} — já aconteceu várias vezes nesta sessão).
     */
    private final List<UUID> profissionalIdsCriados = new ArrayList<>();
    private final List<UUID> unidadeIdsCriadas = new ArrayList<>();
    private final List<UUID> cargoIdsCriados = new ArrayList<>();
    private final List<UUID> categoriaIdsCriadas = new ArrayList<>();

    @AfterEach
    void limparDadosDeTeste() {
        lotacaoRepository.findAll().stream()
                .filter(l -> profissionalIdsCriados.contains(l.getProfissional().getUuid()))
                .forEach(l -> lotacaoRepository.deleteById(l.getUuid()));
        profissionalRepository.deleteAllById(profissionalIdsCriados);
        cargoRepository.deleteAllById(cargoIdsCriados);
        categoriaSalarialRepository.deleteAllById(categoriaIdsCriadas);
        unidadeDeSaudeRepository.deleteAllById(unidadeIdsCriadas);
    }

    private Profissional criarProfissionalFixture(String sufixoMatricula) {
        Profissional profissional = new Profissional();
        profissional.setMatricula(PREFIXO_MATRICULA_TESTE + sufixoMatricula);
        profissional.setCpf("00000000000");
        profissional.setNome("Profissional Fixture Lotacao");
        profissional.setAtivo(true);
        profissional = profissionalRepository.save(profissional);
        profissionalIdsCriados.add(profissional.getUuid());
        return profissional;
    }

    private UnidadeDeSaude criarUnidadeFixture(String sufixo) {
        UnidadeDeSaude unidade = new UnidadeDeSaude();
        unidade.setNome(PREFIXO_NOME_UNIDADE_TESTE + sufixo);
        unidade.setTipo(TipoUnidadeDeSaude.UBS);
        unidade.setAtivo(true);
        unidade = unidadeDeSaudeRepository.save(unidade);
        unidadeIdsCriadas.add(unidade.getUuid());
        return unidade;
    }

    private Cargo criarCargoFixture(String sufixo) {
        CategoriaSalarial categoria = new CategoriaSalarial();
        categoria.setNome(PREFIXO_NOME_CATEGORIA_TESTE + sufixo);
        categoria = categoriaSalarialRepository.save(categoria);
        categoriaIdsCriadas.add(categoria.getUuid());

        Cargo cargo = cargoRepository.save(new Cargo(categoria, "Cargo Fixture " + sufixo));
        cargoIdsCriados.add(cargo.getUuid());
        return cargo;
    }

    @Test
    void deveCriarPrimeiraLotacao() throws Exception {
        Profissional profissional = criarProfissionalFixture("01");
        UnidadeDeSaude unidade = criarUnidadeFixture("01");
        Cargo cargo = criarCargoFixture("01");

        String body = """
                {
                  "matriculaProfissional": "%s",
                  "unidadeId": "%s",
                  "cargoId": "%s",
                  "jornadaSemanalHoras": 40,
                  "dataInicio": "2026-01-01",
                  "motivo": "Admissão"
                }
                """.formatted(profissional.getMatricula(), unidade.getUuid(), cargo.getUuid());

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Lotação criada com sucesso!"));

        mockMvc.perform(get(BASE_URL + "profissional/" + profissional.getMatricula() + "/atual"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unidadeNome").value(unidade.getNome()))
                .andExpect(jsonPath("$.cargoNome").value(cargo.getNome()))
                .andExpect(jsonPath("$.dataFim").doesNotExist());
    }

    @Test
    void deveFecharLotacaoVigenteAoTransferir() throws Exception {
        Profissional profissional = criarProfissionalFixture("02");
        UnidadeDeSaude unidadeOrigem = criarUnidadeFixture("Origem 02");
        UnidadeDeSaude unidadeDestino = criarUnidadeFixture("Destino 02");
        Cargo cargo = criarCargoFixture("02");

        String bodyOrigem = """
                {
                  "matriculaProfissional": "%s",
                  "unidadeId": "%s",
                  "cargoId": "%s",
                  "dataInicio": "2026-01-01",
                  "motivo": "Admissão"
                }
                """.formatted(profissional.getMatricula(), unidadeOrigem.getUuid(), cargo.getUuid());

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyOrigem))
                .andExpect(status().isCreated());

        String bodyTransferencia = """
                {
                  "matriculaProfissional": "%s",
                  "unidadeId": "%s",
                  "cargoId": "%s",
                  "dataInicio": "2026-06-01",
                  "motivo": "Transferência"
                }
                """.formatted(profissional.getMatricula(), unidadeDestino.getUuid(), cargo.getUuid());

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyTransferencia))
                .andExpect(status().isCreated());

        mockMvc.perform(get(BASE_URL + "profissional/" + profissional.getMatricula()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].unidadeNome").value(unidadeDestino.getNome()))
                .andExpect(jsonPath("$[0].dataFim").doesNotExist())
                .andExpect(jsonPath("$[1].unidadeNome").value(unidadeOrigem.getNome()))
                .andExpect(jsonPath("$[1].dataFim").value("2026-05-31"));

        mockMvc.perform(get(BASE_URL + "profissional/" + profissional.getMatricula() + "/atual"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unidadeNome").value(unidadeDestino.getNome()));
    }

    @Test
    void deveRetornarNotFoundAoCriarComProfissionalInexistente() throws Exception {
        UnidadeDeSaude unidade = criarUnidadeFixture("03");
        Cargo cargo = criarCargoFixture("03");

        String body = """
                {
                  "matriculaProfissional": "%s",
                  "unidadeId": "%s",
                  "cargoId": "%s",
                  "dataInicio": "2026-01-01"
                }
                """.formatted(PREFIXO_MATRICULA_TESTE + "INEXISTENTE", unidade.getUuid(), cargo.getUuid());

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornarNotFoundAoBuscarVigenteDeProfissionalSemLotacao() throws Exception {
        Profissional profissional = criarProfissionalFixture("04");

        mockMvc.perform(get(BASE_URL + "profissional/" + profissional.getMatricula() + "/atual"))
                .andExpect(status().isNotFound());
    }
}
