package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.AuditarLeitura;
import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.StatusProcedimentoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.RetificacaoProcedimentoRequestDto;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ExampleConstants;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ProcedimentoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ProcedimentoResponseDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.SuccessResponseDto;
import com.edufelizardo.maissaudepublica.services.version1.ProcedimentoService;
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
@RequestMapping(value = "/api/v1/procedimento/")
@Tag(name = "Procedimento", description = "Endpoints para gerenciar procedimentos realizados durante uma consulta (ver docs/adr/0044-procedimento-realizado-durante-a-consulta.md).")
public class ProcedimentoController {

    @Autowired
    private ProcedimentoService service;

    @RequerPermissao({"PRONTUARIO.CONSULTAR"})
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Lista os procedimentos cadastrados", tags = "Procedimento")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = ProcedimentoResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "ProcedimentoResponse",
                    value = ExampleConstants.PROCEDIMENTO_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesListagem
    public ResponseEntity<List<ProcedimentoResponseDto>> getAll() {
        List<ProcedimentoResponseDto> responseDtos = service.listar();
        if (responseDtos.isEmpty()) {
            throw new ResourceNotFoundException("Nenhum item foi encontrado.");
        }
        return ResponseEntity.ok(responseDtos);
    }

    @RequerPermissao({"PRONTUARIO.CONSULTAR"})
    @AuditarLeitura
    @GetMapping(value = "{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Busca um procedimento pelo id", tags = "Procedimento")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ProcedimentoResponseDto.class))
    })
    @ApiErrorResponsesBusca
    public ResponseEntity<ProcedimentoResponseDto> findById(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.buscarPorId(uuid));
    }

    @RequerPermissao({"PROCEDIMENTO.REGISTRAR"})
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cria um procedimento",
            description = "Registra um procedimento realizado durante uma consulta, com um profissional (por matrícula, ver ADR-0034).",
            tags = "Procedimento")
    @ApiResponse(responseCode = "201", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = SuccessResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "SuccessResponse",
                    value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> create(@Valid @RequestBody ProcedimentoRequestDto dto) {
        ProcedimentoResponseDto responseDto = service.criar(dto);

        String successMessage = "Procedimento criado com sucesso!";
        String details = "Tipo: " + responseDto.getTipo() + ", Profissional: " + responseDto.getProfissionalNome();

        return ResponseEntity.status(HttpStatus.CREATED).body(new SuccessResponseDto(successMessage, details));
    }

    @RequerPermissao({"PROCEDIMENTO.REGISTRAR"})
    @PostMapping(value = "{uuid}/status", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Registra o desfecho de um procedimento agendado",
            description = "Uma única vez, de AGENDADO para REALIZADO (dataRealizacao obrigatória) ou CANCELADO "
                    + "(justificativa obrigatória), com o profissional responsável (ADR-0062). A data prevista fica "
                    + "guardada. Procedimento que não está AGENDADO ou já foi retificado responde 422.",
            tags = "Procedimento")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json",
                    schema = @Schema(implementation = SuccessResponseDto.class),
                    examples = @ExampleObject(name = "Success", summary = "SuccessResponse",
                            value = ExampleConstants.SUCCESS_RESPONSE_UPDATE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> alterarStatus(@PathVariable UUID uuid,
                                                            @Valid @RequestBody StatusProcedimentoRequestDto dto) {
        ProcedimentoResponseDto responseDto = service.alterarStatus(uuid, dto);

        String details = "Status: " + responseDto.getStatus() + ", Profissional: " + responseDto.getProfissionalStatusNome();
        return ResponseEntity.ok(new SuccessResponseDto("Status do procedimento registrado com sucesso!", details));
    }

    @RequerPermissao({"PROCEDIMENTO.REGISTRAR"})
    @PostMapping(value = "{uuid}/retificacao", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Retifica um procedimento",
            description = "Registro clínico não é editado (ADR-0062): a retificação grava uma nova versão com os campos "
                    + "corrigidos e o motivo, ligada à anterior, que continua no prontuário. Só a versão vigente pode ser "
                    + "retificada (422); o consulta precisa ser o mesmo do registro original (400).",
            tags = "Procedimento")
    @ApiResponse(responseCode = "201", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = SuccessResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "SuccessResponse",
                    value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> retificar(@PathVariable UUID uuid,
                                                        @Valid @RequestBody RetificacaoProcedimentoRequestDto dto) {
        ProcedimentoResponseDto responseDto = service.retificar(uuid, dto);

        String details = "Nova versão: " + responseDto.getUuid() + ", Corrige: " + responseDto.getRetificacaoDeUuid();
        return ResponseEntity.status(HttpStatus.CREATED).body(new SuccessResponseDto("Procedimento retificado com sucesso!", details));
    }
}
