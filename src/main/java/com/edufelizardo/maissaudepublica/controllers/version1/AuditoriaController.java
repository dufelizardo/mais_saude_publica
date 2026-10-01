package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.AuditarLeitura;
import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.PaginaAuditoriaResponseDto;
import com.edufelizardo.maissaudepublica.models.enuns.AcaoAuditoria;
import com.edufelizardo.maissaudepublica.models.enuns.ResultadoAuditoria;
import com.edufelizardo.maissaudepublica.services.version1.ConsultaAuditoriaService;
import com.edufelizardo.maissaudepublica.services.version1.RetencaoAuditoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/auditoria/")
@Tag(name = "Auditoria", description = "Trilha de auditoria — quem acessou e alterou o quê (ver docs/adr/0070-trilha-de-auditoria.md e 0071).")
public class AuditoriaController {

    @Autowired
    private ConsultaAuditoriaService service;

    @Autowired
    private RetencaoAuditoriaService retencaoService;

    @RequerPermissao({"AUDITORIA.CONSULTAR"})
    // Consultar a auditoria também fica na trilha: quem olhou o quê.
    @AuditarLeitura
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Consulta a trilha de auditoria",
            description = "Filtros opcionais (usuário por CPF, paciente, registro, unidade, ação, resultado e período em dias), "
                    + "do mais recente ao mais antigo, paginada (tamanho até 100). Traz o total e um resumo do filtro inteiro. "
                    + "Respeita o escopo de AUDITORIA.CONSULTAR. Período invertido → 400. Sem resultado → página vazia (200).",
            tags = "Auditoria")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = PaginaAuditoriaResponseDto.class))
    })
    @ApiErrorResponsesListagem
    public ResponseEntity<PaginaAuditoriaResponseDto> consultar(
            @RequestParam(required = false) String usuarioCpf,
            @RequestParam(required = false) UUID pacienteId,
            @RequestParam(required = false) UUID registroId,
            @RequestParam(required = false) UUID unidadeId,
            @RequestParam(required = false) AcaoAuditoria acao,
            @RequestParam(required = false) ResultadoAuditoria resultado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "" + ConsultaAuditoriaService.TAMANHO_PADRAO) int tamanho) {
        var filtro = new ConsultaAuditoriaService.Filtro(usuarioCpf, pacienteId, registroId, unidadeId, acao, resultado, desde, ate);
        return ResponseEntity.ok(service.consultar(filtro, pagina, tamanho));
    }

    @RequerPermissao({"AUDITORIA.CONSULTAR"})
    @GetMapping(value = "exportacao", produces = "text/csv")
    @Operation(summary = "Exporta a trilha de auditoria em CSV",
            description = "Mesmos filtros e escopo da consulta (ADR-0082). CSV com \";\" e BOM (abre no Excel em português), datas no "
                    + "fuso de Brasília, até 50.000 eventos (acima → 422, refinar o filtro). Filtrar por paciente gera o relatório "
                    + "de acessos para o titular dos dados. A exportação entra na própria trilha (ação EXPORTACAO).",
            tags = "Auditoria")
    @ApiErrorResponsesListagem
    public ResponseEntity<byte[]> exportar(
            @RequestParam(required = false) String usuarioCpf,
            @RequestParam(required = false) UUID pacienteId,
            @RequestParam(required = false) UUID registroId,
            @RequestParam(required = false) UUID unidadeId,
            @RequestParam(required = false) AcaoAuditoria acao,
            @RequestParam(required = false) ResultadoAuditoria resultado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate) {
        var filtro = new ConsultaAuditoriaService.Filtro(usuarioCpf, pacienteId, registroId, unidadeId, acao, resultado, desde, ate);
        byte[] csv = service.exportarCsv(filtro).getBytes(StandardCharsets.UTF_8);
        String nome = "auditoria-" + LocalDateTime.now(ZoneId.of("America/Sao_Paulo"))
                .format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmm")) + ".csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nome + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv);
    }

    @RequerPermissao({"AUDITORIA.CONSULTAR"})
    @GetMapping(value = "politica", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Informa a política de retenção da trilha",
            description = "Prazo de guarda em anos (0 = indefinido) e a data a partir da qual os eventos são guardados (ADR-0082).",
            tags = "Auditoria")
    public ResponseEntity<Map<String, Object>> politica() {
        Instant desde = retencaoService.guardadosDesde();
        return ResponseEntity.ok(desde == null
                ? Map.of("retencaoAnos", retencaoService.retencaoAnos())
                : Map.of("retencaoAnos", retencaoService.retencaoAnos(), "guardadosDesde", desde));
    }
}
