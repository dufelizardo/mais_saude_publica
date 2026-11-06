package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ProgramaPorUnidadeDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ProgramasNaRedeDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.VacinacaoNaRedeDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.VigilanciaNaRedeDto;
import com.edufelizardo.maissaudepublica.services.version1.CoberturaVacinalService;
import com.edufelizardo.maissaudepublica.services.version1.IndicadoresDaRedeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/")
@Tag(name = "Indicadores da rede", description = "Programas de saúde e vigilância consolidados por unidade, território e rede, só agregados "
        + "(ver docs/adr/0146-bi-indicadores-da-rede.md).")
public class IndicadoresDaRedeController {

    private static final String TAG = "Indicadores da rede";

    @Autowired
    private IndicadoresDaRedeService service;

    @Autowired
    private CoberturaVacinalService coberturaService;

    @RequerPermissao({"INDICADOR.CONSULTAR"})
    @GetMapping(value = "indicadores-rede/vacinacao", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Vacinação na rede", description = "As doses aplicadas no período por estratégia e por vacina, a cobertura das "
            + "vacinas de referência hoje (pelo cadastro), a homogeneidade, o abandono e as campanhas de vacinação do período com o andamento "
            + "(ADR-0149).", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<VacinacaoNaRedeDto> vacinacao(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
                                                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate,
                                                        @RequestParam(required = false) UUID unidadeId) {
        return ResponseEntity.ok(coberturaService.naRede(de, ate, unidadeId));
    }

    @RequerPermissao({"INDICADOR.CONSULTAR"})
    @GetMapping(value = "indicadores-rede/programas", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Programas na rede", description = "Cada programa ativo: inscritos, novos, saídas, faltosos, acompanhados em dia e "
            + "a situação das metas. Programa sensível só com PROGRAMA.ACOMPANHAR. Padrão: o mês corrente; até um ano (400).", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<ProgramasNaRedeDto> programas(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
                                                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate,
                                                        @RequestParam(required = false) UUID unidadeId) {
        return ResponseEntity.ok(service.programas(de, ate, unidadeId));
    }

    @RequerPermissao({"INDICADOR.CONSULTAR"})
    @GetMapping(value = "indicadores-rede/programa/{uuid}/unidades", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Programa por unidade", description = "Os indicadores do programa em cada unidade do acesso que o oferece, com a "
            + "meta da unidade ou da rede; as abaixo da meta primeiro.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<ProgramaPorUnidadeDto> programaPorUnidade(@PathVariable UUID uuid,
                                                                    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
                                                                    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate) {
        return ResponseEntity.ok(service.programaPorUnidade(uuid, de, ate));
    }

    @RequerPermissao({"INDICADOR.CONSULTAR"})
    @GetMapping(value = "indicadores-rede/vigilancia", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Vigilância na rede", description = "Notificações por agravo com a oportunidade de encerramento, as últimas 12 "
            + "semanas epidemiológicas, a regularidade da última semana e o território. Agravo sensível sem o nome sem VIGILANCIA.INVESTIGAR.",
            tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<VigilanciaNaRedeDto> vigilancia(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
                                                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate,
                                                          @RequestParam(required = false) UUID unidadeId) {
        return ResponseEntity.ok(service.vigilancia(de, ate, unidadeId));
    }

    @RequerPermissao({"INDICADOR.CONSULTAR"})
    @GetMapping(value = "indicadores-rede/exportacao", produces = "text/csv")
    @Operation(summary = "Indicadores da rede em CSV", description = "Programas e vigilância do período. A exportação entra na trilha.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<byte[]> exportacao(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
                                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate,
                                             @RequestParam(required = false) UUID unidadeId) {
        byte[] corpo = ("﻿" + service.csv(de, ate, unidadeId)).getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"indicadores-da-rede.csv\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(corpo);
    }
}
