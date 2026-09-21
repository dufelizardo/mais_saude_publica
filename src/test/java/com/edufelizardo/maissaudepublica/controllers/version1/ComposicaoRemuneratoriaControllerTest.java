package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.AjusteIndividual;
import com.edufelizardo.maissaudepublica.models.Cargo;
import com.edufelizardo.maissaudepublica.models.CategoriaSalarial;
import com.edufelizardo.maissaudepublica.models.Lotacao;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.RegraAnuenio;
import com.edufelizardo.maissaudepublica.models.TabelaSalarial;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.MotivoAjusteIndividual;
import com.edufelizardo.maissaudepublica.models.enuns.MotivoTabelaSalarial;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.AjusteIndividualRepository;
import com.edufelizardo.maissaudepublica.repositories.CargoRepository;
import com.edufelizardo.maissaudepublica.repositories.CategoriaSalarialRepository;
import com.edufelizardo.maissaudepublica.repositories.LotacaoRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.RegraAnuenioRepository;
import com.edufelizardo.maissaudepublica.repositories.TabelaSalarialRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes do ComposicaoRemuneratoriaController (módulo RH — ver docs/rh/MODELO-RH.md seção 2.4).
 * Endpoint de leitura calculada — sem POST/PATCH pra testar, o foco é a fórmula (valor base +
 * anuênio + ajustes vigentes).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ComposicaoRemuneratoriaControllerTest {

    private static final String BASE_URL = "/api/v1/composicao-remuneratoria/";
    private static final String PREFIXO_MATRICULA_TESTE = "TESTE-COMPOSICAO-";
    private static final String PREFIXO_NOME_CATEGORIA_TESTE = "Categoria Fixture Composicao - ";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @Autowired
    private CategoriaSalarialRepository categoriaSalarialRepository;

    @Autowired
    private CargoRepository cargoRepository;

    @Autowired
    private TabelaSalarialRepository tabelaSalarialRepository;

    @Autowired
    private RegraAnuenioRepository regraAnuenioRepository;

    @Autowired
    private LotacaoRepository lotacaoRepository;

    @Autowired
    private AjusteIndividualRepository ajusteIndividualRepository;

    private final List<UUID> profissionalIdsCriados = new ArrayList<>();
    private final List<UUID> unidadeIdsCriadas = new ArrayList<>();
    private final List<UUID> categoriaIdsCriadas = new ArrayList<>();
    private final List<UUID> cargoIdsCriados = new ArrayList<>();

    @AfterEach
    void limparDadosDeTeste() {
        ajusteIndividualRepository.findAll().stream()
                .filter(a -> profissionalIdsCriados.contains(a.getProfissional().getUuid()))
                .forEach(a -> ajusteIndividualRepository.deleteById(a.getUuid()));
        lotacaoRepository.findAll().stream()
                .filter(l -> profissionalIdsCriados.contains(l.getProfissional().getUuid()))
                .forEach(l -> lotacaoRepository.deleteById(l.getUuid()));
        profissionalRepository.deleteAllById(profissionalIdsCriados);
        tabelaSalarialRepository.findAll().stream()
                .filter(t -> cargoIdsCriados.contains(t.getCargo().getUuid()))
                .forEach(t -> tabelaSalarialRepository.deleteById(t.getUuid()));
        regraAnuenioRepository.findAll().stream()
                .filter(r -> categoriaIdsCriadas.contains(r.getCategoria().getUuid()))
                .forEach(r -> regraAnuenioRepository.deleteById(r.getUuid()));
        cargoRepository.deleteAllById(cargoIdsCriados);
        categoriaSalarialRepository.deleteAllById(categoriaIdsCriadas);
        unidadeDeSaudeRepository.deleteAllById(unidadeIdsCriadas);
    }

    private Profissional criarProfissionalFixture(String sufixo, LocalDate dataAdmissao) {
        Profissional profissional = new Profissional();
        profissional.setMatricula(PREFIXO_MATRICULA_TESTE + sufixo);
        profissional.setCpf("00000000000");
        profissional.setNome("Profissional Fixture Composicao");
        profissional.setAtivo(true);
        profissional.setDataAdmissao(dataAdmissao);
        profissional = profissionalRepository.save(profissional);
        profissionalIdsCriados.add(profissional.getUuid());
        return profissional;
    }

    private UnidadeDeSaude criarUnidadeFixture(String sufixo) {
        UnidadeDeSaude unidade = new UnidadeDeSaude();
        unidade.setNome("Unidade Fixture Composicao - " + sufixo);
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

        Cargo cargo = cargoRepository.save(new Cargo(categoria, "Cargo Fixture Composicao " + sufixo));
        cargoIdsCriados.add(cargo.getUuid());
        return cargo;
    }

    private void criarTabelaSalarialFixture(Cargo cargo, BigDecimal valorBase) {
        tabelaSalarialRepository.save(new TabelaSalarial(cargo, valorBase, LocalDate.of(2020, 1, 1), MotivoTabelaSalarial.REVISAO_PLANO_CARGOS_SALARIOS));
    }

    private void criarLotacaoFixture(Profissional profissional, UnidadeDeSaude unidade, Cargo cargo) {
        lotacaoRepository.save(new Lotacao(profissional, unidade, cargo, 40, LocalDate.of(2020, 1, 1), "Admissão"));
    }

    @Test
    void deveCalcularSemAnuenioESemAjustes() throws Exception {
        Profissional profissional = criarProfissionalFixture("01", LocalDate.now());
        UnidadeDeSaude unidade = criarUnidadeFixture("01");
        Cargo cargo = criarCargoFixture("01");
        criarTabelaSalarialFixture(cargo, new BigDecimal("4000.00"));
        criarLotacaoFixture(profissional, unidade, cargo);

        mockMvc.perform(get(BASE_URL + "profissional/" + profissional.getMatricula()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valorBase").value(4000.00))
                .andExpect(jsonPath("$.valorAnuenio").value(0.00))
                .andExpect(jsonPath("$.totalAjustesIndividuais").value(0.00))
                .andExpect(jsonPath("$.total").value(4000.00));
    }

    @Test
    void deveCalcularComAnuenio() throws Exception {
        Profissional profissional = criarProfissionalFixture("02", LocalDate.now().minusYears(3));
        UnidadeDeSaude unidade = criarUnidadeFixture("02");
        Cargo cargo = criarCargoFixture("02");
        criarTabelaSalarialFixture(cargo, new BigDecimal("4000.00"));
        criarLotacaoFixture(profissional, unidade, cargo);
        regraAnuenioRepository.save(new RegraAnuenio(cargo.getCategoria(), new BigDecimal("1.00"), 25));

        // 3 anos completos x 1% x 4000.00 = 120.00
        mockMvc.perform(get(BASE_URL + "profissional/" + profissional.getMatricula()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.anosCompletos").value(3))
                .andExpect(jsonPath("$.valorAnuenio").value(120.00))
                .andExpect(jsonPath("$.total").value(4120.00));
    }

    @Test
    void deveIncluirSoAjustesVigentes() throws Exception {
        Profissional profissional = criarProfissionalFixture("03", LocalDate.now());
        UnidadeDeSaude unidade = criarUnidadeFixture("03");
        Cargo cargo = criarCargoFixture("03");
        criarTabelaSalarialFixture(cargo, new BigDecimal("4000.00"));
        criarLotacaoFixture(profissional, unidade, cargo);

        ajusteIndividualRepository.save(new AjusteIndividual(profissional, new BigDecimal("300.00"),
                LocalDate.now().minusMonths(1), null, MotivoAjusteIndividual.GRATIFICACAO_PESSOAL, null));
        ajusteIndividualRepository.save(new AjusteIndividual(profissional, new BigDecimal("500.00"),
                LocalDate.now().minusYears(2), LocalDate.now().minusYears(1), MotivoAjusteIndividual.EQUIPARACAO_JUDICIAL, "processo expirado"));

        mockMvc.perform(get(BASE_URL + "profissional/" + profissional.getMatricula()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ajustesIndividuaisVigentes.length()").value(1))
                .andExpect(jsonPath("$.totalAjustesIndividuais").value(300.00))
                .andExpect(jsonPath("$.total").value(4300.00));
    }

    @Test
    void deveRetornarNotFoundSemLotacaoVigente() throws Exception {
        Profissional profissional = criarProfissionalFixture("04", LocalDate.now());

        mockMvc.perform(get(BASE_URL + "profissional/" + profissional.getMatricula()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornarNotFoundParaProfissionalInexistente() throws Exception {
        mockMvc.perform(get(BASE_URL + "profissional/" + PREFIXO_MATRICULA_TESTE + "INEXISTENTE"))
                .andExpect(status().isNotFound());
    }
}
