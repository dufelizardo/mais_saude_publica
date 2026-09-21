package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.TipoBeneficio;
import com.edufelizardo.maissaudepublica.models.ValorBeneficio;
import com.edufelizardo.maissaudepublica.models.enuns.CusteioBeneficio;
import com.edufelizardo.maissaudepublica.repositories.TipoBeneficioRepository;
import com.edufelizardo.maissaudepublica.repositories.ValorBeneficioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes do ValorBeneficioController (módulo RH — ver docs/rh/MODELO-RH.md). Fixture de
 * {@link TipoBeneficio} criada direto via repositório; limpeza por ID rastreado.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ValorBeneficioControllerTest {

    private static final String BASE_URL = "/api/v1/valor-beneficio/";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ValorBeneficioRepository valorBeneficioRepository;

    @Autowired
    private TipoBeneficioRepository tipoBeneficioRepository;

    private final List<UUID> valorIdsCriados = new ArrayList<>();
    private final List<UUID> tipoIdsCriados = new ArrayList<>();

    @AfterEach
    void limparDadosDeTeste() {
        valorBeneficioRepository.deleteAllById(valorIdsCriados);
        tipoBeneficioRepository.deleteAllById(tipoIdsCriados);
    }

    private TipoBeneficio criarTipoFixture(String sufixo) {
        TipoBeneficio tipoBeneficio = new TipoBeneficio();
        tipoBeneficio.setNome("Beneficio Fixture ValorBeneficio - " + sufixo);
        tipoBeneficio.setCusteio(CusteioBeneficio.EMPRESA);
        tipoBeneficio = tipoBeneficioRepository.save(tipoBeneficio);
        tipoIdsCriados.add(tipoBeneficio.getUuid());
        return tipoBeneficio;
    }

    @Test
    void deveCriarComSucesso() throws Exception {
        TipoBeneficio tipo = criarTipoFixture("01");
        String body = """
                {
                  "tipoBeneficioId": "%s",
                  "valor": 35.00,
                  "dataVigencia": "2026-01-01",
                  "motivo": "Reajuste"
                }
                """.formatted(tipo.getUuid());

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Valor de benefício criado com sucesso!"));

        valorBeneficioRepository.findByTipoBeneficio_UuidOrderByDataVigenciaDesc(tipo.getUuid())
                .forEach(v -> valorIdsCriados.add(v.getUuid()));
    }

    @Test
    void deveRetornarNotFoundAoCriarComTipoInexistente() throws Exception {
        String body = """
                {
                  "tipoBeneficioId": "%s",
                  "valor": 35.00,
                  "dataVigencia": "2026-01-01"
                }
                """.formatted(UUID.randomUUID());

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveBuscarValorVigenteNaData() throws Exception {
        TipoBeneficio tipo = criarTipoFixture("02");
        ValorBeneficio antigo = valorBeneficioRepository.save(
                new ValorBeneficio(tipo, new BigDecimal("30.00"), LocalDate.of(2025, 1, 1), "Reajuste"));
        ValorBeneficio novo = valorBeneficioRepository.save(
                new ValorBeneficio(tipo, new BigDecimal("35.00"), LocalDate.of(2026, 1, 1), "Reajuste"));
        valorIdsCriados.add(antigo.getUuid());
        valorIdsCriados.add(novo.getUuid());

        mockMvc.perform(get(BASE_URL + "tipo/" + tipo.getUuid() + "/vigente").param("data", "2026-06-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valor").value(35.00));

        mockMvc.perform(get(BASE_URL + "tipo/" + tipo.getUuid() + "/vigente").param("data", "2025-06-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valor").value(30.00));
    }

    @Test
    void deveRetornarNotFoundAoBuscarVigenteSemValorCadastrado() throws Exception {
        TipoBeneficio tipo = criarTipoFixture("03");

        mockMvc.perform(get(BASE_URL + "tipo/" + tipo.getUuid() + "/vigente"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveListarHistoricoDoTipo() throws Exception {
        TipoBeneficio tipo = criarTipoFixture("04");
        ValorBeneficio registro = valorBeneficioRepository.save(
                new ValorBeneficio(tipo, new BigDecimal("35.00"), LocalDate.of(2026, 1, 1), "Reajuste"));
        valorIdsCriados.add(registro.getUuid());

        mockMvc.perform(get(BASE_URL + "tipo/" + tipo.getUuid()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].valor").value(35.00));
    }
}
