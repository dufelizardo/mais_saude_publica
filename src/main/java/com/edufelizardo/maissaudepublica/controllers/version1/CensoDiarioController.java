package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.AuditarLeitura;
import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.CensoDiarioRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.EletivaCensoDtos;
import com.edufelizardo.maissaudepublica.services.version1.CensoDiarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Censo hospitalar diário (ADR-0185). */
@RestController
@RequestMapping(value = "/api/v1/censo-diario/")
@Tag(name = "Censo diário", description = "Prévia, fechamento, reabertura, versões e pendências do censo hospitalar diário (ver docs/adr/0185-internacao-eletiva-e-censo.md).")
public class CensoDiarioController {

    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;
    private static final String TAG = "Censo diário";

    @Autowired
    private CensoDiarioService service;

    @RequerPermissao({"INTERNACAO.CONSULTAR", "INTERNACAO.GERENCIAR", "INTERNACAO.ALTA", "LEITO.GERENCIAR", "CENSO.FECHAR"})
    @AuditarLeitura
    @GetMapping(produces = JSON)
    @Operation(summary = "Censo do dia", description = "O fechado vigente ou a prévia calculada, com o censo nominal. Dia não terminado: 422. Leitura auditada.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<EletivaCensoDtos.Censo> doDia(@RequestParam UUID unidadeId,
                                                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return ResponseEntity.ok(service.doDia(unidadeId, data));
    }

    @RequerPermissao({"INTERNACAO.CONSULTAR", "INTERNACAO.GERENCIAR", "INTERNACAO.ALTA", "LEITO.GERENCIAR", "CENSO.FECHAR"})
    @GetMapping(value = "versoes", produces = JSON)
    @Operation(summary = "Versões do censo do dia", description = "Da mais nova para a mais velha; a reaberta traz o motivo.", tags = TAG)
    @ApiErrorResponsesListagem
    public ResponseEntity<List<EletivaCensoDtos.Censo>> versoes(@RequestParam UUID unidadeId,
                                                                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return ResponseEntity.ok(service.versoes(unidadeId, data));
    }

    @RequerPermissao({"CENSO.FECHAR"})
    @GetMapping(value = "pendencias", produces = JSON)
    @Operation(summary = "Dias sem censo fechado", description = "Dos últimos 30 dias terminados, nas unidades com leito do escopo.", tags = TAG)
    @ApiErrorResponsesListagem
    public ResponseEntity<List<EletivaCensoDtos.Pendencia>> pendencias(@RequestParam(required = false) UUID unidadeId) {
        return ResponseEntity.ok(service.pendencias(unidadeId));
    }

    @RequerPermissao({"CENSO.FECHAR"})
    @PostMapping(value = "fechamento", produces = JSON, consumes = JSON)
    @Operation(summary = "Fecha o censo do dia", description = "Só dia terminado (422); já fechado: 409.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<EletivaCensoDtos.Censo> fechar(@Valid @RequestBody CensoDiarioRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.fechar(dto.getUnidadeId(), dto.getData()));
    }

    @RequerPermissao({"CENSO.FECHAR"})
    @PostMapping(value = "reabertura", produces = JSON, consumes = JSON)
    @Operation(summary = "Reabre o censo do dia", description = "Com motivo (400). Sem censo fechado: 422. Devolve a prévia para fechar de novo.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<EletivaCensoDtos.Censo> reabrir(@Valid @RequestBody CensoDiarioRequestDto dto) {
        return ResponseEntity.ok(service.reabrir(dto.getUnidadeId(), dto.getData(), dto.getMotivo()));
    }
}
