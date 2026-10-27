package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.LiberadoParaAutenticados;
import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.MetaProgramaticaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.IndicadorApuradoDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.MetaProgramaticaDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.SerieIndicadorDto;
import com.edufelizardo.maissaudepublica.models.enuns.IndicadorProgramatico;
import com.edufelizardo.maissaudepublica.services.version1.IndicadorProgramaticoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/")
@Tag(name = "Indicadores programáticos", description = "Indicadores calculados e metas dos programas de saúde, só com números "
        + "(ver docs/adr/0141-programas-metas-e-indicadores.md).")
public class IndicadorProgramaticoController {

    private static final String TAG = "Indicadores programáticos";

    @Autowired
    private IndicadorProgramaticoService service;

    @LiberadoParaAutenticados
    @GetMapping(value = "programa/{uuid}/indicadores", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Indicadores do programa no período", description = "Padrão: do primeiro dia do mês até hoje, na rede (ou na "
            + "unidade). Programa sensível: PROGRAMA.ACOMPANHAR (403). Cada indicador traz a meta vigente e a situação.", tags = TAG)
    @ApiErrorResponsesListagem
    public ResponseEntity<List<IndicadorApuradoDto>> indicadores(@PathVariable UUID uuid,
                                                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
                                                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate,
                                                                 @RequestParam(required = false) UUID unidadeId) {
        return ResponseEntity.ok(service.indicadores(uuid, de, ate, unidadeId));
    }

    @LiberadoParaAutenticados
    @GetMapping(value = "programa/{uuid}/indicador/{indicador}/serie", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Evolução mensal de um indicador", description = "Os últimos meses (padrão 12, até 24), na rede ou na unidade.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<SerieIndicadorDto> serie(@PathVariable UUID uuid, @PathVariable IndicadorProgramatico indicador,
                                                   @RequestParam(required = false) UUID unidadeId, @RequestParam(required = false) Integer meses) {
        return ResponseEntity.ok(service.serie(uuid, indicador, unidadeId, meses));
    }

    @LiberadoParaAutenticados
    @GetMapping(value = "programa/{uuid}/indicadores.csv", produces = "text/csv")
    @Operation(summary = "Relatório dos indicadores em CSV", description = "O mesmo do painel, separado por ponto e vírgula.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<byte[]> csv(@PathVariable UUID uuid,
                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate,
                                      @RequestParam(required = false) UUID unidadeId) {
        byte[] corpo = ("﻿" + service.csv(uuid, de, ate, unidadeId)).getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"indicadores-programa.csv\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(corpo);
    }

    @LiberadoParaAutenticados
    @GetMapping(value = "programa/{uuid}/metas", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Metas do programa", description = "A mais recente primeiro.", tags = TAG)
    @ApiErrorResponsesListagem
    public ResponseEntity<List<MetaProgramaticaDto>> metas(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.metas(uuid));
    }

    @RequerPermissao({"PROGRAMA.GERENCIAR"})
    @PostMapping(value = "programa/{uuid}/meta", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cadastra uma meta", description = "Indicador do programa (422), período válido (400), percentual até 100 (400), exame e "
            + "limite no exame no alvo (400). Uma por indicador e unidade em cada período: períodos que se cruzam dão 409.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<MetaProgramaticaDto> criar(@PathVariable UUID uuid, @Valid @RequestBody MetaProgramaticaRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criarMeta(uuid, dto));
    }

    @RequerPermissao({"PROGRAMA.GERENCIAR"})
    @PutMapping(value = "meta-programatica/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Ajusta uma meta", description = "O alvo, o período e o exame; o indicador e a unidade não mudam (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<MetaProgramaticaDto> atualizar(@PathVariable UUID uuid, @Valid @RequestBody MetaProgramaticaRequestDto dto) {
        return ResponseEntity.ok(service.atualizarMeta(uuid, dto));
    }

    @RequerPermissao({"PROGRAMA.GERENCIAR"})
    @DeleteMapping(value = "meta-programatica/{uuid}")
    @Operation(summary = "Exclui uma meta", description = "A meta é configuração, e não registro clínico.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<Void> excluir(@PathVariable UUID uuid) {
        service.excluirMeta(uuid);
        return ResponseEntity.noContent().build();
    }
}
