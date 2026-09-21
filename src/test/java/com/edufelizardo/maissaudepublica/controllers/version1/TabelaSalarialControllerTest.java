package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Cargo;
import com.edufelizardo.maissaudepublica.models.CategoriaSalarial;
import com.edufelizardo.maissaudepublica.models.TabelaSalarial;
import com.edufelizardo.maissaudepublica.models.enuns.MotivoTabelaSalarial;
import com.edufelizardo.maissaudepublica.repositories.CargoRepository;
import com.edufelizardo.maissaudepublica.repositories.CategoriaSalarialRepository;
import com.edufelizardo.maissaudepublica.repositories.TabelaSalarialRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes do TabelaSalarialController (módulo RH — ver docs/rh/MODELO-RH.md). Fixtures de
 * {@link Cargo}/{@link CategoriaSalarial} criadas direto via repositório; limpeza por ID
 * rastreado, sem acessar relacionamentos LAZY fora de uma requisição HTTP.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TabelaSalarialControllerTest {

    private static final String BASE_URL = "/api/v1/tabela-salarial/";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TabelaSalarialRepository tabelaSalarialRepository;

    @Autowired
    private CargoRepository cargoRepository;

    @Autowired
    private CategoriaSalarialRepository categoriaSalarialRepository;

    private final List<UUID> tabelaSalarialIdsCriadas = new ArrayList<>();
    private final List<UUID> cargoIdsCriados = new ArrayList<>();
    private final List<UUID> categoriaIdsCriadas = new ArrayList<>();

    @AfterEach
    void limparDadosDeTeste() {
        tabelaSalarialRepository.deleteAllById(tabelaSalarialIdsCriadas);
        cargoRepository.deleteAllById(cargoIdsCriados);
        categoriaSalarialRepository.deleteAllById(categoriaIdsCriadas);
    }

    private Cargo criarCargoFixture(String sufixo) {
        CategoriaSalarial categoria = new CategoriaSalarial();
        categoria.setNome("Categoria Fixture TabelaSalarial - " + sufixo);
        categoria = categoriaSalarialRepository.save(categoria);
        categoriaIdsCriadas.add(categoria.getUuid());

        Cargo cargo = cargoRepository.save(new Cargo(categoria, "Cargo Fixture TabelaSalarial - " + sufixo));
        cargoIdsCriados.add(cargo.getUuid());
        return cargo;
    }

    @Test
    void deveCriarComSucesso() throws Exception {
        Cargo cargo = criarCargoFixture("01");
        String body = """
                {
                  "cargoId": "%s",
                  "valorBase": 5000.00,
                  "dataVigencia": "2026-01-01",
                  "motivo": "DISSIDIO"
                }
                """.formatted(cargo.getUuid());

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Valor de tabela salarial criado com sucesso!"));

        tabelaSalarialRepository.findByCargo_UuidOrderByDataVigenciaDesc(cargo.getUuid())
                .forEach(t -> tabelaSalarialIdsCriadas.add(t.getUuid()));
    }

    @Test
    void deveRetornarNotFoundAoCriarComCargoInexistente() throws Exception {
        String body = """
                {
                  "cargoId": "%s",
                  "valorBase": 5000.00,
                  "dataVigencia": "2026-01-01",
                  "motivo": "DISSIDIO"
                }
                """.formatted(UUID.randomUUID());

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveBuscarValorVigenteNaData() throws Exception {
        Cargo cargo = criarCargoFixture("02");
        TabelaSalarial antigo = tabelaSalarialRepository.save(
                new TabelaSalarial(cargo, new BigDecimal("4500.00"), java.time.LocalDate.of(2025, 1, 1), MotivoTabelaSalarial.REVISAO_PLANO_CARGOS_SALARIOS));
        TabelaSalarial novo = tabelaSalarialRepository.save(
                new TabelaSalarial(cargo, new BigDecimal("5000.00"), java.time.LocalDate.of(2026, 1, 1), MotivoTabelaSalarial.DISSIDIO));
        tabelaSalarialIdsCriadas.add(antigo.getUuid());
        tabelaSalarialIdsCriadas.add(novo.getUuid());

        mockMvc.perform(get(BASE_URL + "cargo/" + cargo.getUuid() + "/vigente").param("data", "2026-06-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valorBase").value(5000.00));

        mockMvc.perform(get(BASE_URL + "cargo/" + cargo.getUuid() + "/vigente").param("data", "2025-06-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valorBase").value(4500.00));
    }

    @Test
    void deveRetornarNotFoundAoBuscarVigenteSemValorCadastrado() throws Exception {
        Cargo cargo = criarCargoFixture("03");

        mockMvc.perform(get(BASE_URL + "cargo/" + cargo.getUuid() + "/vigente"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveListarHistoricoDoCargo() throws Exception {
        Cargo cargo = criarCargoFixture("04");
        TabelaSalarial registro = tabelaSalarialRepository.save(
                new TabelaSalarial(cargo, new BigDecimal("5000.00"), java.time.LocalDate.of(2026, 1, 1), MotivoTabelaSalarial.DISSIDIO));
        tabelaSalarialIdsCriadas.add(registro.getUuid());

        mockMvc.perform(get(BASE_URL + "cargo/" + cargo.getUuid()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].valorBase").value(5000.00));
    }
}
