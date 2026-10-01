package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Afastamento;
import com.edufelizardo.maissaudepublica.models.Cargo;
import com.edufelizardo.maissaudepublica.models.CategoriaSalarial;
import com.edufelizardo.maissaudepublica.models.Lotacao;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.StatusAfastamento;
import com.edufelizardo.maissaudepublica.models.enuns.TipoAfastamento;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.AfastamentoRepository;
import com.edufelizardo.maissaudepublica.repositories.CargoRepository;
import com.edufelizardo.maissaudepublica.repositories.CategoriaSalarialRepository;
import com.edufelizardo.maissaudepublica.repositories.LotacaoRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Quadro de profissionais com lotação e afastamento vigentes (ADR-0072). */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class QuadroProfissionalControllerTest {

    private static final String URL = "/api/v1/profissional/quadro";
    private static final String PREFIXO = "Quadro Teste ";

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
    private LotacaoRepository lotacaoRepository;

    @Autowired
    private AfastamentoRepository afastamentoRepository;

    @BeforeEach
    void seed() {
        limpar();
        UnidadeDeSaude ubs = new UnidadeDeSaude();
        ubs.setNome(PREFIXO + "UBS");
        ubs.setTipo(TipoUnidadeDeSaude.UBS);
        ubs.setAtivo(true);
        ubs = unidadeDeSaudeRepository.save(ubs);
        CategoriaSalarial categoria = new CategoriaSalarial();
        categoria.setNome(PREFIXO + "Categoria");
        categoria = categoriaSalarialRepository.save(categoria);
        Cargo cargo = cargoRepository.save(new Cargo(categoria, PREFIXO + "Enfermeiro"));

        Profissional lotado = profissional("QT-001", "36543247027", PREFIXO + "Ana Lotada");
        Lotacao antiga = new Lotacao(lotado, ubs, cargo, 30, LocalDate.now().minusYears(2), "admissão");
        antiga.setDataFim(LocalDate.now().minusYears(1));
        lotacaoRepository.save(antiga);
        lotacaoRepository.save(new Lotacao(lotado, ubs, cargo, 40, LocalDate.now().minusYears(1), "transferência"));

        Profissional ferias = profissional("QT-002", "02918637002", PREFIXO + "Bruno Ferias");
        afastamentoRepository.save(new Afastamento(ferias, TipoAfastamento.FERIAS, LocalDate.now().minusDays(3),
                LocalDate.now().plusDays(10), StatusAfastamento.APROVADO, null));
        // Solicitado e já concluído não contam como afastamento em curso.
        afastamentoRepository.save(new Afastamento(ferias, TipoAfastamento.LICENCA_PESSOAL, LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(1), StatusAfastamento.SOLICITADO, null));

        profissional("QT-003", "11122233396", PREFIXO + "Carla Sem Lotacao");
    }

    private Profissional profissional(String matricula, String cpf, String nome) {
        Profissional p = new Profissional();
        p.setMatricula(matricula);
        p.setCpf(cpf);
        p.setNome(nome);
        p.setConselhoClasse("COREN");
        p.setNumeroConselho("SP-" + matricula);
        p.setAtivo(true);
        return profissionalRepository.save(p);
    }

    @AfterEach
    void limpar() {
        // Pelo id: o profissional das lotações/afastamentos é um proxy preguiçoso, sem sessão aqui.
        java.util.Set<java.util.UUID> nossos = profissionalRepository.findAll().stream()
                .filter(p -> p.getNome() != null && p.getNome().startsWith(PREFIXO))
                .map(Profissional::getUuid).collect(java.util.stream.Collectors.toSet());
        afastamentoRepository.deleteAll(afastamentoRepository.findAll().stream()
                .filter(a -> nossos.contains(a.getProfissional().getUuid())).toList());
        lotacaoRepository.deleteAll(lotacaoRepository.findAll().stream()
                .filter(l -> nossos.contains(l.getProfissional().getUuid())).toList());
        profissionalRepository.deleteAll(profissionalRepository.findAll().stream()
                .filter(p -> p.getNome() != null && p.getNome().startsWith(PREFIXO)).toList());
        cargoRepository.deleteAll(cargoRepository.findAll().stream().filter(c -> c.getNome().startsWith(PREFIXO)).toList());
        categoriaSalarialRepository.deleteAll(categoriaSalarialRepository.findAll().stream()
                .filter(c -> c.getNome().startsWith(PREFIXO)).toList());
        unidadeDeSaudeRepository.deleteAll(unidadeDeSaudeRepository.findAll().stream()
                .filter(u -> u.getNome().startsWith(PREFIXO)).toList());
    }

    @Test
    void deveTrazerLotacaoVigenteEAfastamentoEmCurso() throws Exception {
        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.profissional.matricula=='QT-001')].lotacao.jornadaSemanalHoras", hasItem(40)))
                .andExpect(jsonPath("$[?(@.profissional.matricula=='QT-001')].lotacao.cargoNome", hasItem(PREFIXO + "Enfermeiro")))
                .andExpect(jsonPath("$[?(@.profissional.matricula=='QT-001')].lotacao.unidadeNome", hasItem(PREFIXO + "UBS")))
                .andExpect(jsonPath("$[?(@.profissional.matricula=='QT-002')].afastamento.tipo", hasItem("FERIAS")))
                .andExpect(jsonPath("$[?(@.profissional.matricula=='QT-003')].profissional.nome", hasItem(PREFIXO + "Carla Sem Lotacao")));
    }

    @Test
    void semLotacaoOuAfastamentoOsCamposVemNulos() throws Exception {
        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.profissional.matricula=='QT-003' && @.lotacao == null && @.afastamento == null)]").isNotEmpty())
                .andExpect(jsonPath("$[?(@.profissional.matricula=='QT-001' && @.afastamento == null)]").isNotEmpty());
    }
}
