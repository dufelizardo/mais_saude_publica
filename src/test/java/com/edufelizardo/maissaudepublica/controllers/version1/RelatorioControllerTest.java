package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Relatórios, fatia BI4 (ADR-0167): o catálogo, cada relatório montado pelos domínios, o período e o CSV. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RelatorioControllerTest {

    private static final String URL = "/api/v1/relatorios";
    private static final String PREFIXO = "Relatorio Teste ";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    private UnidadeDeSaude ubs;

    @BeforeEach
    void seed() {
        limpar();
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + "UBS");
        u.setTipo(TipoUnidadeDeSaude.UBS);
        u.setAtivo(true);
        ubs = unidadeDeSaudeRepository.save(u);
    }

    @AfterEach
    void limpar() {
        jdbc.update("delete from tb_unidade_de_saude where nome like ?", PREFIXO + "%");
    }

    @Test
    void catalogoTrazOsCincoRelatorios() throws Exception {
        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].codigo", containsInAnyOrder("PRODUCAO", "SITUACAO", "PROGRAMAS", "VACINACAO", "VIGILANCIA")))
                .andExpect(jsonPath("$[?(@.codigo == 'VACINACAO')].categoria").value("Epidemiológico"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"PRODUCAO", "SITUACAO", "PROGRAMAS", "VACINACAO", "VIGILANCIA"})
    void cadaRelatorioTrazResumoETabelasDaUnidade(String codigo) throws Exception {
        LocalDate hoje = LocalDate.now();
        mockMvc.perform(get(URL + "/" + codigo).param("de", hoje.minusDays(9).toString()).param("ate", hoje.toString()).param("unidadeId", ubs.getUuid().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value(codigo))
                .andExpect(jsonPath("$.de").value(hoje.minusDays(9).toString()))
                .andExpect(jsonPath("$.resumo").isNotEmpty())
                .andExpect(jsonPath("$.secoes").isNotEmpty());
    }

    @Test
    void codigoNaoDiferenciaMaiusculas() throws Exception {
        mockMvc.perform(get(URL + "/producao")).andExpect(status().isOk()).andExpect(jsonPath("$.nome").value("Produção assistencial"));
    }

    @Test
    void codigoDesconhecidoE404() throws Exception {
        mockMvc.perform(get(URL + "/INEXISTENTE")).andExpect(status().isNotFound());
        mockMvc.perform(get(URL + "/INEXISTENTE/exportacao")).andExpect(status().isNotFound());
    }

    @Test
    void periodoInvalidoE400() throws Exception {
        LocalDate hoje = LocalDate.now();
        mockMvc.perform(get(URL + "/PRODUCAO").param("de", hoje.toString()).param("ate", hoje.minusDays(1).toString())).andExpect(status().isBadRequest());
        mockMvc.perform(get(URL + "/VIGILANCIA").param("de", hoje.minusYears(2).toString()).param("ate", hoje.toString())).andExpect(status().isBadRequest());
    }

    @Test
    void exportacaoSaiEmCsvComOsTitulosEOBom() throws Exception {
        byte[] corpo = mockMvc.perform(get(URL + "/PRODUCAO/exportacao").param("unidadeId", ubs.getUuid().toString()))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"relatorio-producao.csv\""))
                .andReturn().getResponse().getContentAsByteArray();
        String csv = new String(corpo, StandardCharsets.UTF_8);
        assertThat(csv).startsWith("\uFEFFProdução assistencial;").contains("Indicador;Valor").contains("Atendimentos concluídos;0").contains("Agenda\n");
    }
}
