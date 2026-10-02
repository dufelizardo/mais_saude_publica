package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.AuditarLeitura;
import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ExampleConstants;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.SuccessResponseDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AgendamentoRegulacaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AutorizacaoRegulacaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ComplementoRegulacaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.MotivoRegulacaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.RealizacaoRegulacaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ReclassificacaoRegulacaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.SolicitacaoRegulacaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.SolicitacaoRegulacaoResponseDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.SolicitacaoRegulacaoResumoDto;
import com.edufelizardo.maissaudepublica.models.enuns.PrioridadeRegulacao;
import com.edufelizardo.maissaudepublica.models.enuns.StatusSolicitacaoRegulacao;
import com.edufelizardo.maissaudepublica.services.version1.SolicitacaoRegulacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/solicitacao-regulacao/")
@Tag(name = "Regulação", description = "Regulação do acesso: a unidade de origem solicita, a Central autoriza com a vaga, devolve para complementar ou nega; fila por prioridade e hora do pedido (ver docs/adr/0087-regulacao-do-acesso-backend.md).")
public class SolicitacaoRegulacaoController {

    @Autowired
    private SolicitacaoRegulacaoService service;

    @RequerPermissao({"REGULACAO.SOLICITAR"})
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Solicita a regulação de um procedimento para um paciente",
            description = "Entra na fila como SOLICITADA. Procedimento fora de uso ou paciente inativo: 422. Já existe "
                    + "solicitação em andamento do mesmo procedimento para o paciente: 409. CID-10 inválido: 400.",
            tags = "Regulação")
    @ApiResponse(responseCode = "201", description = "Success:", content = {
            @Content(mediaType = "application/json",
                    schema = @Schema(implementation = SuccessResponseDto.class),
                    examples = @ExampleObject(name = "Success", summary = "SuccessResponse",
                            value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> create(@Valid @RequestBody SolicitacaoRegulacaoRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sucesso("Solicitação de regulação registrada com sucesso!",
                service.solicitar(dto)));
    }

    @RequerPermissao({"REGULACAO.SOLICITAR"})
    @PostMapping(value = "{uuid}/complemento", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Complementa uma solicitação devolvida",
            description = "Volta à fila na posição original. Só solicitações DEVOLVIDA (422).",
            tags = "Regulação")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json",
                    schema = @Schema(implementation = SuccessResponseDto.class),
                    examples = @ExampleObject(name = "Success", summary = "SuccessResponse",
                            value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> complementar(@PathVariable UUID uuid, @Valid @RequestBody ComplementoRegulacaoRequestDto dto) {
        return ResponseEntity.ok(sucesso("Solicitação complementada e devolvida à fila!", service.complementar(uuid, dto)));
    }


    @RequerPermissao({"REGULACAO.REGULAR"})
    @PostMapping(value = "{uuid}/reclassificacao", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Muda a prioridade de uma solicitação na fila",
            description = "Motivo obrigatório. Só solicitações SOLICITADA (422).",
            tags = "Regulação")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json",
                    schema = @Schema(implementation = SuccessResponseDto.class),
                    examples = @ExampleObject(name = "Success", summary = "SuccessResponse",
                            value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> reclassificar(@PathVariable UUID uuid, @Valid @RequestBody ReclassificacaoRegulacaoRequestDto dto) {
        return ResponseEntity.ok(sucesso("Solicitação reclassificada com sucesso!", service.reclassificar(uuid, dto)));
    }


    @RequerPermissao({"REGULACAO.REGULAR"})
    @PostMapping(value = "{uuid}/autorizacao", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Autoriza a solicitação com a vaga",
            description = "Unidade executante e data e hora da vaga (não no passado: 422). Quem solicitou não autoriza (422). Só SOLICITADA (422).",
            tags = "Regulação")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json",
                    schema = @Schema(implementation = SuccessResponseDto.class),
                    examples = @ExampleObject(name = "Success", summary = "SuccessResponse",
                            value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> autorizar(@PathVariable UUID uuid, @Valid @RequestBody AutorizacaoRegulacaoRequestDto dto) {
        return ResponseEntity.ok(sucesso("Solicitação autorizada com sucesso!", service.autorizar(uuid, dto)));
    }


    @RequerPermissao({"REGULACAO.REGULAR"})
    @PostMapping(value = "{uuid}/devolucao", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Devolve a solicitação ao solicitante para complementar",
            description = "Motivo obrigatório: o que falta. Só SOLICITADA (422).",
            tags = "Regulação")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json",
                    schema = @Schema(implementation = SuccessResponseDto.class),
                    examples = @ExampleObject(name = "Success", summary = "SuccessResponse",
                            value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> devolver(@PathVariable UUID uuid, @Valid @RequestBody MotivoRegulacaoRequestDto dto) {
        return ResponseEntity.ok(sucesso("Solicitação devolvida ao solicitante!", service.devolver(uuid, dto)));
    }


    @RequerPermissao({"REGULACAO.REGULAR"})
    @PostMapping(value = "{uuid}/negativa", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Nega a solicitação",
            description = "Motivo obrigatório. Quem solicitou não nega a própria (422). Só SOLICITADA (422).",
            tags = "Regulação")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json",
                    schema = @Schema(implementation = SuccessResponseDto.class),
                    examples = @ExampleObject(name = "Success", summary = "SuccessResponse",
                            value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> negar(@PathVariable UUID uuid, @Valid @RequestBody MotivoRegulacaoRequestDto dto) {
        return ResponseEntity.ok(sucesso("Solicitação negada!", service.negar(uuid, dto)));
    }


    @RequerPermissao({"REGULACAO.SOLICITAR", "REGULACAO.REGULAR"})
    @PostMapping(value = "{uuid}/cancelamento", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cancela a solicitação",
            description = "Pelo solicitante ou pela regulação, enquanto SOLICITADA, DEVOLVIDA ou AUTORIZADA (422). Motivo obrigatório.",
            tags = "Regulação")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json",
                    schema = @Schema(implementation = SuccessResponseDto.class),
                    examples = @ExampleObject(name = "Success", summary = "SuccessResponse",
                            value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> cancelar(@PathVariable UUID uuid, @Valid @RequestBody MotivoRegulacaoRequestDto dto) {
        return ResponseEntity.ok(sucesso("Solicitação cancelada!", service.cancelar(uuid, dto)));
    }

    @RequerPermissao({"AGENDAMENTO.GERENCIAR", "REGULACAO.REGULAR"})
    @PostMapping(value = "{uuid}/agendamento", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Agenda a solicitação autorizada na unidade executante",
            description = "Cria o agendamento com o profissional que vai atender; sem data, vale a da vaga. A recepção da "
                    + "executante (AGENDAMENTO.GERENCIAR lá) ou a regulação. Só AUTORIZADA (422). Data no passado: 422.",
            tags = "Regulação")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json",
                    schema = @Schema(implementation = SuccessResponseDto.class),
                    examples = @ExampleObject(name = "Success", summary = "SuccessResponse",
                            value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> agendar(@PathVariable UUID uuid, @Valid @RequestBody AgendamentoRegulacaoRequestDto dto) {
        return ResponseEntity.ok(sucesso("Solicitação agendada na unidade executante!", service.agendar(uuid, dto)));
    }

    @RequerPermissao({"CONSULTA.REGISTRAR", "PROCEDIMENTO.REGISTRAR"})
    @PostMapping(value = "{uuid}/realizacao", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Registra o atendimento realizado, com a contrarreferência",
            description = "Na unidade executante. A contrarreferência volta para a unidade solicitante no detalhe da "
                    + "solicitação. Fecha o agendamento como REALIZADO. Só AGENDADA (422).",
            tags = "Regulação")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json",
                    schema = @Schema(implementation = SuccessResponseDto.class),
                    examples = @ExampleObject(name = "Success", summary = "SuccessResponse",
                            value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> realizar(@PathVariable UUID uuid, @Valid @RequestBody RealizacaoRegulacaoRequestDto dto) {
        return ResponseEntity.ok(sucesso("Atendimento realizado e contrarreferência registrada!", service.registrarRealizacao(uuid, dto)));
    }

    @RequerPermissao({"AGENDAMENTO.GERENCIAR", "ATENDIMENTO.GERENCIAR"})
    @PostMapping(value = "{uuid}/falta", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Registra que o paciente faltou ao agendamento",
            description = "Na unidade executante. Fecha o agendamento como CANCELADO, com a falta na observação. Só AGENDADA (422).",
            tags = "Regulação")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json",
                    schema = @Schema(implementation = SuccessResponseDto.class),
                    examples = @ExampleObject(name = "Success", summary = "SuccessResponse",
                            value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> faltar(@PathVariable UUID uuid, @Valid @RequestBody MotivoRegulacaoRequestDto dto) {
        return ResponseEntity.ok(sucesso("Falta registrada!", service.registrarFalta(uuid, dto)));
    }

    @RequerPermissao({"REGULACAO.REGULAR"})
    @GetMapping(value = "fila", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "A fila do regulador",
            description = "Solicitações SOLICITADA das unidades no escopo de quem regula, por prioridade (vermelho primeiro) "
                    + "e hora do pedido, com a posição na fila do procedimento. Filtros: procedimentoId e prioridade. Sem dado clínico.",
            tags = "Regulação")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = SolicitacaoRegulacaoResumoDto.class)))
    })
    @ApiErrorResponsesListagem
    public ResponseEntity<List<SolicitacaoRegulacaoResumoDto>> fila(@RequestParam(required = false) UUID procedimentoId,
                                                                    @RequestParam(required = false) PrioridadeRegulacao prioridade) {
        return ResponseEntity.ok(service.fila(procedimentoId, prioridade));
    }

    @RequerPermissao({"REGULACAO.CONSULTAR", "REGULACAO.SOLICITAR", "REGULACAO.REGULAR"})
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Lista as solicitações, da mais recente para a mais antiga",
            description = "Sem dado clínico: serve à recepção para informar o andamento. Filtros: status, pacienteId, "
                    + "unidadeSolicitanteId e procedimentoId.",
            tags = "Regulação")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = SolicitacaoRegulacaoResumoDto.class)))
    })
    @ApiErrorResponsesListagem
    public ResponseEntity<List<SolicitacaoRegulacaoResumoDto>> findAll(
            @RequestParam(required = false) StatusSolicitacaoRegulacao status,
            @RequestParam(required = false) UUID pacienteId,
            @RequestParam(required = false) UUID unidadeSolicitanteId,
            @RequestParam(required = false) UUID procedimentoId) {
        return ResponseEntity.ok(service.listar(status, pacienteId, unidadeSolicitanteId, procedimentoId));
    }

    @RequerPermissao({"REGULACAO.SOLICITAR", "REGULACAO.REGULAR"})
    @AuditarLeitura
    @GetMapping(value = "{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Detalhe da solicitação, com CID, justificativa e histórico",
            description = "Leitura auditada (dado de saúde). Fora do escopo das unidades solicitante e executante: 403.",
            tags = "Regulação")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = SolicitacaoRegulacaoResponseDto.class))
    })
    @ApiErrorResponsesBusca
    public ResponseEntity<SolicitacaoRegulacaoResponseDto> findById(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.buscarPorId(uuid));
    }

    private static SuccessResponseDto sucesso(String mensagem, SolicitacaoRegulacaoResponseDto s) {
        String detalhe = "Id: " + s.getUuid() + ", Procedimento: " + s.getProcedimentoNome() + ", Status: " + s.getStatus()
                + ", Prioridade: " + s.getPrioridade() + (s.getPosicaoNaFila() != null ? ", Posição na fila: " + s.getPosicaoNaFila() : "");
        return new SuccessResponseDto(mensagem, detalhe);
    }
}
