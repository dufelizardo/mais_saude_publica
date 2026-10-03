package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.EventoAuditoria;
import com.edufelizardo.maissaudepublica.models.enuns.AcaoAuditoria;
import com.edufelizardo.maissaudepublica.models.enuns.ResultadoAuditoria;
import com.edufelizardo.maissaudepublica.repositories.EventoAuditoriaRepository;
import com.edufelizardo.maissaudepublica.services.version1.RetencaoAuditoriaService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Exportação da trilha em CSV e política de retenção (ADR-0082). Toggle de segurança desligado, como a maior parte
 * da suíte: o escopo por unidade já é coberto pelo AutorizacaoControllerTest.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuditoriaExportacaoControllerTest {

    private static final String RECURSO = "TESTE_EXPORTACAO";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventoAuditoriaRepository auditoriaRepository;

    @Autowired
    private RetencaoAuditoriaService retencaoService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @AfterEach
    void limpar() {
        jdbcTemplate.update("delete from tb_evento_auditoria where recurso = ? or rota like '/api/v1/auditoria%' and detalhe like '%teste-exportacao%'",
                RECURSO);
        ReflectionTestUtils.setField(retencaoService, "retencaoAnos", 20);
    }

    private boolean existe(UUID id) {
        return jdbcTemplate.queryForObject("select count(*) from tb_evento_auditoria where uuid = ?", Integer.class, id) > 0;
    }

    private EventoAuditoria evento(UUID paciente, Instant quando, String detalhe) {
        EventoAuditoria e = new EventoAuditoria();
        e.setOcorridoEm(quando);
        e.setUsuarioCpf("52998224725");
        e.setAcao(AcaoAuditoria.LEITURA);
        e.setResultado(ResultadoAuditoria.PERMITIDO);
        e.setRecurso(RECURSO);
        e.setMetodo("GET");
        e.setRota("/api/v1/prontuario/{pacienteId}");
        e.setStatusHttp(200);
        e.setPacienteId(paciente);
        e.setDetalhe(detalhe);
        return auditoriaRepository.save(e);
    }

    @Test
    void exportaOFiltroEmCsvProntoParaPlanilhaEEntraNaTrilha() throws Exception {
        UUID paciente = UUID.randomUUID();
        evento(paciente, Instant.now().minus(2, ChronoUnit.HOURS), "=SOMA(A1)");
        evento(paciente, Instant.now().minus(1, ChronoUnit.HOURS), "com; ponto e vírgula e \"aspas\"");
        evento(UUID.randomUUID(), Instant.now(), "outro paciente");

        MvcResult r = mockMvc.perform(get("/api/v1/auditoria/exportacao").param("pacienteId", paciente.toString()))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.startsWith("attachment; filename=\"auditoria-")))
                .andExpect(header().string("Content-Type", org.hamcrest.Matchers.startsWith("text/csv")))
                .andReturn();
        String csv = new String(r.getResponse().getContentAsByteArray(), StandardCharsets.UTF_8);
        List<String> linhas = csv.lines().toList();

        assertThat(csv).startsWith("﻿Data e hora (Brasília);Usuário (CPF);");
        assertThat(linhas).hasSize(3);
        // Mais recente primeiro; fórmula neutralizada; campo com ";" entre aspas.
        assertThat(linhas.get(1)).contains("\"com; ponto e vírgula e \"\"aspas\"\"\"");
        assertThat(linhas.get(2)).contains(";'=SOMA(A1)").contains(paciente.toString());

        EventoAuditoria exportacao = auditoriaRepository.findByPacienteIdOrderByOcorridoEmDesc(paciente).stream()
                .filter(e -> e.getAcao() == AcaoAuditoria.EXPORTACAO).findFirst().orElseThrow();
        assertThat(exportacao.getDetalhe()).isEqualTo("2 eventos exportados; filtros: paciente.");
    }

    @Test
    void periodoInvertidoDa400() throws Exception {
        mockMvc.perform(get("/api/v1/auditoria/exportacao").param("desde", "2026-09-30").param("ate", "2026-09-01"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void retencaoApagaSoOQuePassouDoPrazoERegistraNaTrilha() {
        UUID paciente = UUID.randomUUID();
        EventoAuditoria antigo = evento(paciente, ZonedDateTime.now().minusYears(21).toInstant(), "teste-exportacao antigo");
        EventoAuditoria recente = evento(paciente, Instant.now().minus(1, ChronoUnit.DAYS), "teste-exportacao recente");

        long apagados = retencaoService.aplicar();

        assertThat(apagados).isGreaterThanOrEqualTo(1);
        assertThat(existe(antigo.getUuid())).isFalse();
        assertThat(existe(recente.getUuid())).isTrue();
        assertThat(jdbcTemplate.queryForObject(
                "select count(*) from tb_evento_auditoria where rota = 'retencao-da-auditoria' and detalhe like 'Retenção de 20 anos: %'",
                Integer.class)).isPositive();
        jdbcTemplate.update("delete from tb_evento_auditoria where rota = 'retencao-da-auditoria'");
    }

    @Test
    void comPrazoZeroNadaEApagado() {
        ReflectionTestUtils.setField(retencaoService, "retencaoAnos", 0);
        EventoAuditoria antigo = evento(UUID.randomUUID(), ZonedDateTime.now().minusYears(30).toInstant(), "teste-exportacao");

        assertThat(retencaoService.aplicar()).isZero();
        assertThat(existe(antigo.getUuid())).isTrue();
    }

    @Test
    void politicaInformaOPrazo() throws Exception {
        mockMvc.perform(get("/api/v1/auditoria/politica"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.retencaoAnos").value(20))
                .andExpect(jsonPath("$.guardadosDesde").exists());
    }
}
