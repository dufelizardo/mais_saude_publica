package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.AuditarLeitura;
import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.PaginaAuditoriaResponseDto;
import com.edufelizardo.maissaudepublica.models.enuns.AcaoAuditoria;
import com.edufelizardo.maissaudepublica.models.enuns.ResultadoAuditoria;
import com.edufelizardo.maissaudepublica.services.version1.ConsultaAuditoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/auditoria/")
@Tag(name = "Auditoria", description = "Trilha de auditoria — quem acessou e alterou o quê (ver docs/adr/0070-trilha-de-auditoria.md e 0071).")
public class AuditoriaController {

    @Autowired
    private ConsultaAuditoriaService service;

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
}
