package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.AuditarLeitura;
import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AgendamentoInternacaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EletivaLeitoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.EletivaCensoDtos;
import com.edufelizardo.maissaudepublica.services.version1.InternacaoEletivaService;
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

/** Internação eletiva agendada com reserva de leito (ADR-0185). */
@RestController
@RequestMapping(value = "/api/v1/internacao-eletiva/")
@Tag(name = "Internação eletiva", description = "Agendamento, reserva do leito, admissão, falta, remarcação e cancelamento (ver docs/adr/0185-internacao-eletiva-e-censo.md).")
public class InternacaoEletivaController {

    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;
    private static final String TAG = "Internação eletiva";

    @Autowired
    private InternacaoEletivaService service;

    @RequerPermissao({"INTERNACAO.GERENCIAR"})
    @PostMapping(produces = JSON, consumes = JSON)
    @Operation(summary = "Agenda uma internação eletiva", description = "Data no passado ou paciente inativo: 422. Já há eletiva ativa do paciente: 409.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<EletivaCensoDtos.Agendamento> agendar(@Valid @RequestBody AgendamentoInternacaoRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.agendar(dto));
    }

    @RequerPermissao({"INTERNACAO.CONSULTAR", "INTERNACAO.GERENCIAR", "INTERNACAO.ALTA", "LEITO.GERENCIAR"})
    @GetMapping(produces = JSON)
    @Operation(summary = "Agenda de eletivas", description = "Do período (padrão: hoje e os próximos seis dias, até 62) e as passadas ainda sem desfecho.", tags = TAG)
    @ApiErrorResponsesListagem
    public ResponseEntity<List<EletivaCensoDtos.Agendamento>> agenda(@RequestParam(required = false) UUID unidadeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate) {
        return ResponseEntity.ok(service.agenda(unidadeId, de, ate));
    }

    @RequerPermissao({"INTERNACAO.CONSULTAR", "INTERNACAO.GERENCIAR", "INTERNACAO.ALTA", "LEITO.GERENCIAR"})
    @AuditarLeitura
    @GetMapping(value = "{uuid}", produces = JSON)
    @Operation(summary = "Detalhe da eletiva", description = "Com os eventos. Leitura auditada.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<EletivaCensoDtos.Agendamento> buscarPorId(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.buscarPorId(uuid));
    }

    @RequerPermissao({"INTERNACAO.GERENCIAR"})
    @PostMapping(value = "{uuid}/reserva", produces = JSON, consumes = JSON)
    @Operation(summary = "Reserva o leito da eletiva", description = "Leito livre compatível da unidade, de dois dias antes até a data prevista (422 fora). "
            + "Vale até o fim do dia previsto.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<EletivaCensoDtos.Agendamento> reservar(@PathVariable UUID uuid, @Valid @RequestBody EletivaLeitoRequestDto dto) {
        return ResponseEntity.ok(service.reservar(uuid, dto));
    }

    @RequerPermissao({"INTERNACAO.GERENCIAR"})
    @PostMapping(value = "{uuid}/admissao", produces = JSON, consumes = JSON)
    @Operation(summary = "Admite o paciente da eletiva", description = "No leito reservado; sem reserva, num leito livre da unidade (400 sem ele).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<EletivaCensoDtos.Agendamento> admitir(@PathVariable UUID uuid, @Valid @RequestBody EletivaLeitoRequestDto dto) {
        return ResponseEntity.ok(service.admitir(uuid, dto));
    }

    @RequerPermissao({"INTERNACAO.GERENCIAR"})
    @PostMapping(value = "{uuid}/falta", produces = JSON, consumes = JSON)
    @Operation(summary = "Registra a falta", description = "A partir do dia previsto (422 antes), com motivo (400). O leito reservado volta a livre.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<EletivaCensoDtos.Agendamento> falta(@PathVariable UUID uuid, @Valid @RequestBody EletivaLeitoRequestDto dto) {
        return ResponseEntity.ok(service.registrarFalta(uuid, dto));
    }

    @RequerPermissao({"INTERNACAO.GERENCIAR"})
    @PostMapping(value = "{uuid}/remarcacao", produces = JSON, consumes = JSON)
    @Operation(summary = "Remarca a eletiva", description = "Nova data (não no passado, 422) e motivo (400); desfaz a reserva.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<EletivaCensoDtos.Agendamento> remarcar(@PathVariable UUID uuid, @Valid @RequestBody EletivaLeitoRequestDto dto) {
        return ResponseEntity.ok(service.remarcar(uuid, dto));
    }

    @RequerPermissao({"INTERNACAO.GERENCIAR"})
    @PostMapping(value = "{uuid}/cancelamento", produces = JSON, consumes = JSON)
    @Operation(summary = "Cancela a eletiva", description = "Com motivo (400); desfaz a reserva.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<EletivaCensoDtos.Agendamento> cancelar(@PathVariable UUID uuid, @Valid @RequestBody EletivaLeitoRequestDto dto) {
        return ResponseEntity.ok(service.cancelar(uuid, dto));
    }
}
