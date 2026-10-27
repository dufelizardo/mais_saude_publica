package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ProducaoAssistencialDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ProducaoUnidadeDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.SerieProducaoDto;
import com.edufelizardo.maissaudepublica.models.enuns.GranularidadeSerie;
import com.edufelizardo.maissaudepublica.services.version1.ProducaoAssistencialService;
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
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/")
@Tag(name = "Produção assistencial", description = "Indicadores de produção por unidade, só agregados "
        + "(ver docs/adr/0143-bi-producao-assistencial.md).")
public class ProducaoAssistencialController {

    private static final String TAG = "Produção assistencial";

    @Autowired
    private ProducaoAssistencialService service;

    @RequerPermissao({"INDICADOR.CONSULTAR"})
    @GetMapping(value = "producao", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Produção no período", description = "Atendimentos concluídos e a agenda, com as distribuições e o período "
            + "anterior de mesma duração. Padrão: do primeiro dia do mês até hoje; até um ano (400). Sem unidade, as unidades do acesso.",
            tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<ProducaoAssistencialDto> producao(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
                                                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate,
                                                            @RequestParam(required = false) UUID unidadeId) {
        return ResponseEntity.ok(service.producao(de, ate, unidadeId));
    }

    @RequerPermissao({"INDICADOR.CONSULTAR"})
    @GetMapping(value = "producao/serie", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Série da produção", description = "Por dia (padrão 30, até 90) ou por mês (padrão 12, até 24), terminando hoje.",
            tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<SerieProducaoDto> serie(@RequestParam(required = false) GranularidadeSerie granularidade,
                                                  @RequestParam(required = false) UUID unidadeId,
                                                  @RequestParam(required = false) Integer pontos) {
        return ResponseEntity.ok(service.serie(granularidade, unidadeId, pontos));
    }

    @RequerPermissao({"INDICADOR.CONSULTAR"})
    @GetMapping(value = "producao/unidades", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Produção por unidade", description = "As unidades do acesso com produção, da maior para a menor, com a variação.",
            tags = TAG)
    @ApiErrorResponsesListagem
    public ResponseEntity<List<ProducaoUnidadeDto>> unidades(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
                                                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate) {
        return ResponseEntity.ok(service.unidades(de, ate));
    }

    @RequerPermissao({"INDICADOR.CONSULTAR"})
    @GetMapping(value = "producao/exportacao", produces = "text/csv")
    @Operation(summary = "Produção em CSV", description = "O mesmo resumo, separado por ponto e vírgula. A exportação entra na trilha de "
            + "auditoria.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<byte[]> exportacao(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
                                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate,
                                             @RequestParam(required = false) UUID unidadeId) {
        byte[] corpo = ("﻿" + service.csv(de, ate, unidadeId)).getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"producao-assistencial.csv\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(corpo);
    }
}
