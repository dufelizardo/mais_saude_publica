package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.AuditarLeitura;
import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ClassificacaoIncidenteRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ConclusaoIncidenteRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.IncidenteSegurancaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.MotivoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.NotivisaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.IncidenteSegurancaDtos;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoIncidente;
import com.edufelizardo.maissaudepublica.services.version1.IncidenteSegurancaService;
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

@RestController
@RequestMapping(value = "/api/v1/qualidade/")
@Tag(name = "Segurança do paciente", description = "Notificação de incidentes, análise pelo Núcleo de Segurança do Paciente e Notivisa (ver a ADR-0203).")
public class IncidenteSegurancaController {

    private static final String TAG = "Segurança do paciente";
    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;

    @Autowired
    private IncidenteSegurancaService service;

    @RequerPermissao({"QUALIDADE.NOTIFICAR"})
    @PostMapping(value = "incidente", produces = JSON, consumes = JSON)
    @Operation(summary = "Notifica um incidente", description = "Anônima ou identificada; devolve o protocolo. Ocorrência futura, 400.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<IncidenteSegurancaDtos.Protocolo> notificar(@Valid @RequestBody IncidenteSegurancaRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.notificar(dto));
    }

    @RequerPermissao({"QUALIDADE.ANALISAR"})
    @AuditarLeitura
    @GetMapping(value = "incidentes", produces = JSON)
    @Operation(summary = "Incidentes", description = "Das unidades em que se analisa; unidadeId e situacao filtram.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<List<IncidenteSegurancaDtos.Incidente>> incidentes(@RequestParam(required = false) UUID unidadeId,
                                                                           @RequestParam(required = false) SituacaoIncidente situacao) {
        return ResponseEntity.ok(service.incidentes(unidadeId, situacao));
    }

    @RequerPermissao({"QUALIDADE.ANALISAR"})
    @AuditarLeitura
    @GetMapping(value = "incidente/{uuid}", produces = JSON)
    @Operation(summary = "Incidente", description = "Com a classificação, a análise e o prazo do Notivisa.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<IncidenteSegurancaDtos.Incidente> incidente(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.incidente(uuid));
    }

    @RequerPermissao({"QUALIDADE.ANALISAR"})
    @PostMapping(value = "incidente/{uuid}/classificacao", produces = JSON, consumes = JSON)
    @Operation(summary = "Classifica o incidente", description = "Natureza e dano (ICPS); põe em análise.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<IncidenteSegurancaDtos.Incidente> classificar(@PathVariable UUID uuid, @Valid @RequestBody ClassificacaoIncidenteRequestDto dto) {
        return ResponseEntity.ok(service.classificar(uuid, dto));
    }

    @RequerPermissao({"QUALIDADE.ANALISAR"})
    @PostMapping(value = "incidente/{uuid}/conclusao", produces = JSON, consumes = JSON)
    @Operation(summary = "Conclui a análise", description = "Fatores, causa raiz (dano grave, óbito, never event) e lições.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<IncidenteSegurancaDtos.Incidente> concluir(@PathVariable UUID uuid, @Valid @RequestBody ConclusaoIncidenteRequestDto dto) {
        return ResponseEntity.ok(service.concluir(uuid, dto));
    }

    @RequerPermissao({"QUALIDADE.ANALISAR"})
    @PostMapping(value = "incidente/{uuid}/descarte", produces = JSON, consumes = JSON)
    @Operation(summary = "Descarta a notificação", description = "Duplicada ou fora do escopo, com o motivo.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<IncidenteSegurancaDtos.Incidente> descartar(@PathVariable UUID uuid, @Valid @RequestBody MotivoRequestDto dto) {
        return ResponseEntity.ok(service.descartar(uuid, dto.getMotivo()));
    }

    @RequerPermissao({"QUALIDADE.ANALISAR"})
    @PostMapping(value = "incidente/{uuid}/notivisa", produces = JSON, consumes = JSON)
    @Operation(summary = "Registra a notificação ao Notivisa", description = "Só do evento adverso (422), uma vez (409).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<IncidenteSegurancaDtos.Incidente> notivisa(@PathVariable UUID uuid, @Valid @RequestBody NotivisaRequestDto dto) {
        return ResponseEntity.ok(service.registrarNotivisa(uuid, dto));
    }

    @RequerPermissao({"QUALIDADE.ANALISAR", "INDICADOR.CONSULTAR"})
    @GetMapping(value = "seguranca/painel", produces = JSON)
    @Operation(summary = "Painel da segurança do paciente", description = "Só totais: por tipo, dano e natureza; análises e Notivisa pendentes.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<IncidenteSegurancaDtos.Painel> painel(@RequestParam(required = false) UUID unidadeId,
                                                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
                                                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate) {
        return ResponseEntity.ok(service.painel(unidadeId, de, ate));
    }
}
