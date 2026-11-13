package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.DeslocadoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.RetornoEncaminhamentoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.PessoasEmergenciaDtos;
import com.edufelizardo.maissaudepublica.models.enuns.MarcadorVulnerabilidade;
import com.edufelizardo.maissaudepublica.services.version1.PessoasEmergenciaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/")
@Tag(name = "Emergências · pessoas", description = "Vulneráveis da área afetada, pacientes deslocados, abrigos e o encaminhamento intersetorial "
        + "(ver docs/adr/0193-emergencias-vulneraveis-e-deslocados.md).")
public class PessoasEmergenciaController {

    private static final String TAG = "Emergências · pessoas";
    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;

    @Autowired
    private PessoasEmergenciaService service;

    @RequerPermissao({"EMERGENCIA.COORDENAR"})
    @GetMapping(value = "emergencia/{uuid}/vulneraveis", produces = JSON)
    @Operation(summary = "Vulneráveis da área afetada", description = "As pessoas dos domicílios nas microáreas afetadas ou dentro do polígono, com os "
            + "marcadores (idade, gestante, programas não sensíveis, diálise, oxigênio). Pede também PACIENTE.CONSULTAR.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<PessoasEmergenciaDtos.Vulneraveis> vulneraveis(@PathVariable UUID uuid, @RequestParam(defaultValue = "false") boolean todos,
                                                                        @RequestParam(required = false) MarcadorVulnerabilidade marcador,
                                                                        @RequestParam(required = false) UUID microareaId) {
        return ResponseEntity.ok(service.vulneraveis(uuid, todos, marcador, microareaId));
    }

    @RequerPermissao({"EMERGENCIA.COORDENAR"})
    @GetMapping(value = "emergencia/{uuid}/vulneraveis.csv", produces = "text/csv")
    @Operation(summary = "Vulneráveis em CSV", description = "A mesma lista, para a busca ativa em campo.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<byte[]> vulneraveisCsv(@PathVariable UUID uuid, @RequestParam(defaultValue = "false") boolean todos,
                                                 @RequestParam(required = false) MarcadorVulnerabilidade marcador,
                                                 @RequestParam(required = false) UUID microareaId) {
        byte[] corpo = ("﻿" + service.vulneraveisCsv(uuid, todos, marcador, microareaId)).getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=vulneraveis.csv")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8)).body(corpo);
    }

    @RequerPermissao({"EMERGENCIA.COORDENAR"})
    @GetMapping(value = "emergencia/{uuid}/deslocados", produces = JSON)
    @Operation(summary = "Pacientes deslocados", description = "Os em aberto primeiro, com o destino, o transporte e os encaminhamentos.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<List<PessoasEmergenciaDtos.Deslocado>> deslocados(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.deslocados(uuid));
    }

    @RequerPermissao({"EMERGENCIA.COORDENAR"})
    @PostMapping(value = "emergencia/{uuid}/deslocado", produces = JSON, consumes = JSON)
    @Operation(summary = "Registra o paciente deslocado", description = "Emergência ativa (422); já deslocado nela, 409; o destino com o que ele "
            + "pede (400); transporte de outro paciente, 422.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<List<PessoasEmergenciaDtos.Deslocado>> registrar(@PathVariable UUID uuid, @Valid @RequestBody DeslocadoRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrar(uuid, dto));
    }

    @RequerPermissao({"EMERGENCIA.COORDENAR"})
    @PostMapping(value = "emergencia/{uuid}/deslocado/{deslocadoId}/realocacao", produces = JSON, consumes = JSON)
    @Operation(summary = "Realoca o deslocado", description = "Novo destino; o registro atual fica realocado e o novo aponta para ele.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<List<PessoasEmergenciaDtos.Deslocado>> realocar(@PathVariable UUID uuid, @PathVariable UUID deslocadoId,
                                                                          @Valid @RequestBody DeslocadoRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.realocar(uuid, deslocadoId, dto));
    }

    @RequerPermissao({"EMERGENCIA.COORDENAR"})
    @PostMapping(value = "emergencia/{uuid}/deslocado/{deslocadoId}/retorno", produces = JSON, consumes = JSON)
    @Operation(summary = "Retorno para casa", description = "Só o deslocado (422); vale também na desmobilização.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<List<PessoasEmergenciaDtos.Deslocado>> retorno(@PathVariable UUID uuid, @PathVariable UUID deslocadoId,
                                                                         @Valid @RequestBody DeslocadoRequestDto.Fim dto) {
        return ResponseEntity.ok(service.retornar(uuid, deslocadoId, dto));
    }

    @RequerPermissao({"EMERGENCIA.COORDENAR"})
    @PostMapping(value = "emergencia/{uuid}/deslocado/{deslocadoId}/encerramento", produces = JSON, consumes = JSON)
    @Operation(summary = "Encerra o deslocamento", description = "Sem retorno, com o motivo (400).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<List<PessoasEmergenciaDtos.Deslocado>> encerrar(@PathVariable UUID uuid, @PathVariable UUID deslocadoId,
                                                                          @Valid @RequestBody DeslocadoRequestDto.Fim dto) {
        return ResponseEntity.ok(service.encerrar(uuid, deslocadoId, dto));
    }

    @RequerPermissao({"EMERGENCIA.COORDENAR"})
    @PutMapping(value = "emergencia/{uuid}/deslocado/{deslocadoId}/transporte", produces = JSON, consumes = JSON)
    @Operation(summary = "Liga o transporte", description = "A solicitação de transporte do mesmo paciente (outro, 422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<List<PessoasEmergenciaDtos.Deslocado>> transporte(@PathVariable UUID uuid, @PathVariable UUID deslocadoId,
                                                                            @Valid @RequestBody DeslocadoRequestDto.Transporte dto) {
        return ResponseEntity.ok(service.ligarTransporte(uuid, deslocadoId, dto));
    }

    @RequerPermissao({"EMERGENCIA.COORDENAR"})
    @PostMapping(value = "emergencia/{uuid}/deslocado/{deslocadoId}/encaminhamento", produces = JSON, consumes = JSON)
    @Operation(summary = "Encaminha à rede intersetorial", description = "As regras da ADR-0154, com a unidade que encaminha.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<List<PessoasEmergenciaDtos.Deslocado>> encaminhar(@PathVariable UUID uuid, @PathVariable UUID deslocadoId,
                                                                            @Valid @RequestBody DeslocadoRequestDto.Encaminhamento dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.encaminhar(uuid, deslocadoId, dto));
    }

    @RequerPermissao({"EMERGENCIA.COORDENAR"})
    @PostMapping(value = "emergencia/{uuid}/deslocado/{deslocadoId}/encaminhamento/{encaminhamentoId}/recebimento", produces = JSON, consumes = JSON)
    @Operation(summary = "A instituição recebeu", description = "Só do enviado (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<List<PessoasEmergenciaDtos.Deslocado>> receber(@PathVariable UUID uuid, @PathVariable UUID deslocadoId,
                                                                         @PathVariable UUID encaminhamentoId, @RequestBody RetornoEncaminhamentoRequestDto dto) {
        return ResponseEntity.ok(service.receberEncaminhamento(uuid, deslocadoId, encaminhamentoId, dto));
    }

    @RequerPermissao({"EMERGENCIA.COORDENAR"})
    @PostMapping(value = "emergencia/{uuid}/deslocado/{deslocadoId}/encaminhamento/{encaminhamentoId}/retorno", produces = JSON, consumes = JSON)
    @Operation(summary = "Retorno da instituição", description = "Concluído ou recusado, com o texto (400).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<List<PessoasEmergenciaDtos.Deslocado>> retornoEncaminhamento(@PathVariable UUID uuid, @PathVariable UUID deslocadoId,
                                                                                       @PathVariable UUID encaminhamentoId,
                                                                                       @RequestBody RetornoEncaminhamentoRequestDto dto) {
        return ResponseEntity.ok(service.retornoEncaminhamento(uuid, deslocadoId, encaminhamentoId, dto));
    }

    @RequerPermissao({"EMERGENCIA.COORDENAR"})
    @PostMapping(value = "emergencia/{uuid}/deslocado/{deslocadoId}/encaminhamento/{encaminhamentoId}/cancelamento", produces = JSON, consumes = JSON)
    @Operation(summary = "Cancela o encaminhamento", description = "Em aberto (422), com o motivo (400).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<List<PessoasEmergenciaDtos.Deslocado>> cancelarEncaminhamento(@PathVariable UUID uuid, @PathVariable UUID deslocadoId,
                                                                                        @PathVariable UUID encaminhamentoId,
                                                                                        @Valid @RequestBody DeslocadoRequestDto.Fim dto) {
        return ResponseEntity.ok(service.cancelarEncaminhamento(uuid, deslocadoId, encaminhamentoId, dto));
    }

    @RequerPermissao({"PACIENTE.CONSULTAR", "PRONTUARIO.CONSULTAR"})
    @GetMapping(value = "emergencias/deslocamentos", produces = JSON)
    @Operation(summary = "Deslocamentos em aberto do paciente", description = "Para o aviso no prontuário.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<List<PessoasEmergenciaDtos.DeslocamentoAberto>> deslocamentos(@RequestParam UUID pacienteId) {
        return ResponseEntity.ok(service.deslocamentosDoPaciente(pacienteId));
    }
}
