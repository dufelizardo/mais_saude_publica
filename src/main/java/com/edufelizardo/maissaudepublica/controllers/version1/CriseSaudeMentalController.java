package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.AuditarLeitura;
import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AvaliacaoRiscoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EncerramentoCriseRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EtapaCriseRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EventoCriseRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.NotificacaoCriseRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AcompanhamentoSaudeMentalDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.CrisesSaudeMentalDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.EventoCriseDto;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoCriseSaudeMental;
import com.edufelizardo.maissaudepublica.services.version1.RiscoCriseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/saude-mental/")
@Tag(name = "Saúde Mental · Risco e crise", description = "Risco com histórico no caso e evento de crise com etapas "
        + "(ver docs/adr/0123-saude-mental-risco-crise.md).")
public class CriseSaudeMentalController {

    @Autowired
    private RiscoCriseService service;

    @RequerPermissao({"SAUDE_MENTAL.ACOMPANHAR"})
    @PostMapping(value = "acompanhamento/{uuid}/risco", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Avalia ou reavalia o risco", description = "Registro novo por avaliação; o vigente de cada tipo é o mais recente. "
            + "Moderado ou alto sem medidas, alto sem reavaliação, reavaliação no passado: 400.", tags = "Saúde Mental · Risco e crise")
    @ApiErrorResponsesMutacao
    public ResponseEntity<AcompanhamentoSaudeMentalDto> risco(@PathVariable UUID uuid, @Valid @RequestBody AvaliacaoRiscoRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.avaliarRisco(uuid, dto));
    }

    @RequerPermissao({"SAUDE_MENTAL.CRISE"})
    @PostMapping(value = "crise", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Registra uma crise", description = "Na unidade, com ou sem caso (com caso vigente, liga-se a ele). Autoagressão e tentativa "
            + "de suicídio sugerem a notificação compulsória.", tags = "Saúde Mental · Risco e crise")
    @ApiErrorResponsesMutacao
    public ResponseEntity<EventoCriseDto> registrar(@Valid @RequestBody EventoCriseRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrar(dto));
    }

    @RequerPermissao({"SAUDE_MENTAL.CRISE"})
    @PostMapping(value = "crise/{uuid}/etapa", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Registra uma etapa da crise", description = "Avaliação, intervenção, acionamento da rede, encaminhamento ou pós-crise. Crise "
            + "encerrada: 422.", tags = "Saúde Mental · Risco e crise")
    @ApiErrorResponsesMutacao
    public ResponseEntity<EventoCriseDto> etapa(@PathVariable UUID uuid, @Valid @RequestBody EtapaCriseRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrarEtapa(uuid, dto));
    }

    @RequerPermissao({"SAUDE_MENTAL.CRISE"})
    @PostMapping(value = "crise/{uuid}/encerramento", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Encerra a crise", description = "Com o desfecho; sem a etapa de pós-crise: 422.", tags = "Saúde Mental · Risco e crise")
    @ApiErrorResponsesMutacao
    public ResponseEntity<EventoCriseDto> encerrar(@PathVariable UUID uuid, @Valid @RequestBody EncerramentoCriseRequestDto dto) {
        return ResponseEntity.ok(service.encerrar(uuid, dto));
    }

    @RequerPermissao({"SAUDE_MENTAL.CRISE"})
    @PostMapping(value = "crise/{uuid}/notificacao", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Registra que a notificação foi feita", description = "A notificação compulsória de violência autoprovocada, feita fora do "
            + "sistema até a Vigilância V1. Crise que não pede notificação ou já registrada: 422.", tags = "Saúde Mental · Risco e crise")
    @ApiErrorResponsesMutacao
    public ResponseEntity<EventoCriseDto> notificacao(@PathVariable UUID uuid, @Valid @RequestBody NotificacaoCriseRequestDto dto) {
        return ResponseEntity.ok(service.registrarNotificacao(uuid, dto));
    }

    @AuditarLeitura
    @RequerPermissao({"SAUDE_MENTAL.CRISE", "SAUDE_MENTAL.ACOMPANHAR"})
    @GetMapping(value = "crises", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Crises", description = "Das unidades do escopo, em curso primeiro, com as notificações pendentes. Filtros: unidadeId, "
            + "situacao. Leitura auditada.", tags = "Saúde Mental · Risco e crise")
    @ApiErrorResponsesListagem
    public ResponseEntity<CrisesSaudeMentalDto> crises(@RequestParam(required = false) UUID unidadeId,
                                                       @RequestParam(required = false) SituacaoCriseSaudeMental situacao) {
        return ResponseEntity.ok(service.crises(unidadeId, situacao));
    }

    @AuditarLeitura
    @RequerPermissao({"SAUDE_MENTAL.CRISE", "SAUDE_MENTAL.ACOMPANHAR", "PRONTUARIO.CONSULTAR"})
    @GetMapping(value = "crise/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "A crise", description = "Com as etapas; para quem tem a permissão na unidade dela, a referência do caso ou o acesso "
            + "justificado (403). Leitura auditada.", tags = "Saúde Mental · Risco e crise")
    @ApiErrorResponsesBusca
    public ResponseEntity<EventoCriseDto> crise(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.crise(uuid));
    }
}
