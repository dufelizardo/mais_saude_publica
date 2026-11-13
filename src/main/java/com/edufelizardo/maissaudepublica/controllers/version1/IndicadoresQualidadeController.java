package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AuditoriaProtocoloRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ChecklistProtocoloRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.MetaQualidadeRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.IndicadoresQualidadeDtos;
import com.edufelizardo.maissaudepublica.services.version1.IndicadoresQualidadeService;
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
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/qualidade/")
@Tag(name = "Indicadores de qualidade", description = "Indicadores com metas e auditoria de adesão aos protocolos (ver a ADR-0205).")
public class IndicadoresQualidadeController {

    private static final String TAG = "Indicadores de qualidade";
    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;

    @Autowired
    private IndicadoresQualidadeService service;

    @RequerPermissao({"QUALIDADE.GERIR", "INDICADOR.CONSULTAR"})
    @GetMapping(value = "indicadores", produces = JSON)
    @Operation(summary = "Painel dos indicadores de qualidade", description = "Da competência (AAAA-MM, o mês corrente por padrão), com a meta da unidade e "
            + "a série de 12 meses; sem base, nunca zero.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<IndicadoresQualidadeDtos.Painel> painel(@RequestParam(required = false) UUID unidadeId,
                                                                 @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth competencia) {
        return ResponseEntity.ok(service.painel(unidadeId, competencia));
    }

    @RequerPermissao({"QUALIDADE.GERIR", "INDICADOR.CONSULTAR"})
    @GetMapping(value = "metas", produces = JSON)
    @Operation(summary = "Metas dos indicadores", description = "O histórico de vigência; unidadeId filtra.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<List<IndicadoresQualidadeDtos.Meta>> metas(@RequestParam(required = false) UUID unidadeId) {
        return ResponseEntity.ok(service.metas(unidadeId));
    }

    @RequerPermissao({"QUALIDADE.GERIR"})
    @PostMapping(value = "meta", produces = JSON, consumes = JSON)
    @Operation(summary = "Define a meta", description = "Do mês informado em diante; encerra a anterior na véspera.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<IndicadoresQualidadeDtos.Meta> definirMeta(@Valid @RequestBody MetaQualidadeRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.definirMeta(dto));
    }

    @RequerPermissao({"QUALIDADE.GERIR", "QUALIDADE.ANALISAR", "INDICADOR.CONSULTAR"})
    @GetMapping(value = "checklists", produces = JSON)
    @Operation(summary = "Checklists de protocolo", description = "Com os itens da versão mais recente.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<List<IndicadoresQualidadeDtos.Checklist>> checklists() {
        return ResponseEntity.ok(service.checklists());
    }

    @RequerPermissao({"QUALIDADE.GERIR"})
    @PostMapping(value = "checklist", produces = JSON, consumes = JSON)
    @Operation(summary = "Cadastra o checklist", description = "Código único (409), com a primeira versão.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<IndicadoresQualidadeDtos.Checklist> criarChecklist(@Valid @RequestBody ChecklistProtocoloRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criarChecklist(dto));
    }

    @RequerPermissao({"QUALIDADE.GERIR"})
    @PostMapping(value = "checklist/{uuid}/versao", produces = JSON, consumes = JSON)
    @Operation(summary = "Nova versão do checklist", description = "As auditorias antigas seguem com a versão que responderam.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<IndicadoresQualidadeDtos.Checklist> novaVersao(@PathVariable UUID uuid, @Valid @RequestBody ChecklistProtocoloRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.novaVersaoChecklist(uuid, dto));
    }

    @RequerPermissao({"QUALIDADE.GERIR", "QUALIDADE.ANALISAR"})
    @GetMapping(value = "auditorias-protocolo", produces = JSON)
    @Operation(summary = "Auditorias de protocolo", description = "Os últimos três meses por padrão; unidadeId, de e ate filtram.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<List<IndicadoresQualidadeDtos.Auditoria>> auditorias(@RequestParam(required = false) UUID unidadeId,
                                                                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
                                                                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate) {
        return ResponseEntity.ok(service.auditorias(unidadeId, de, ate));
    }

    @RequerPermissao({"QUALIDADE.GERIR", "QUALIDADE.ANALISAR"})
    @PostMapping(value = "auditoria-protocolo", produces = JSON, consumes = JSON)
    @Operation(summary = "Registra a auditoria de protocolo", description = "Todos os itens respondidos (400); a adesão sai dos totais.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<IndicadoresQualidadeDtos.Auditoria> registrarAuditoria(@Valid @RequestBody AuditoriaProtocoloRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrarAuditoria(dto));
    }
}
