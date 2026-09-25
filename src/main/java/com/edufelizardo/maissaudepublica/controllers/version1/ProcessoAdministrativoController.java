package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ExampleConstants;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ProcessoAdministrativoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ProcessoAdministrativoResponseDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.SuccessResponseDto;
import com.edufelizardo.maissaudepublica.services.version1.ProcessoAdministrativoService;
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
@RequestMapping(value = "/api/v1/processo-administrativo/")
@Tag(name = "ProcessoAdministrativo", description = "Endpoints para gerenciar processos administrativos vinculados a uma capacidade (ver docs/adr/0033-processos-administrativos-por-capacidade.md).")
public class ProcessoAdministrativoController {

    @Autowired
    private ProcessoAdministrativoService service;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Lista os processos administrativos cadastrados", tags = "ProcessoAdministrativo")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = ProcessoAdministrativoResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "ProcessoAdministrativoResponse",
                    value = ExampleConstants.PROCESSO_ADMINISTRATIVO_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesListagem
    public ResponseEntity<List<ProcessoAdministrativoResponseDto>> getAll() {
        List<ProcessoAdministrativoResponseDto> responseDtos = service.listar();
        if (responseDtos.isEmpty()) {
            throw new ResourceNotFoundException("Nenhum item foi encontrado.");
        }
        return ResponseEntity.ok(responseDtos);
    }

    @GetMapping(value = "{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Busca um processo administrativo pelo id", tags = "ProcessoAdministrativo")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ProcessoAdministrativoResponseDto.class))
    })
    @ApiErrorResponsesBusca
    public ResponseEntity<ProcessoAdministrativoResponseDto> findById(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.buscarPorId(uuid));
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cria um processo administrativo vinculado a uma capacidade", tags = "ProcessoAdministrativo")
    @ApiResponse(responseCode = "201", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = SuccessResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "SuccessResponse",
                    value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> create(@Valid @RequestBody ProcessoAdministrativoRequestDto dto) {
        ProcessoAdministrativoResponseDto responseDto = service.criar(dto);

        String successMessage = "Processo administrativo criado com sucesso!";
        String details = "Código: " + responseDto.getCodigo() + ", Capacidade: " + responseDto.getCapacidadeCodigo();

        return ResponseEntity.status(HttpStatus.CREATED).body(new SuccessResponseDto(successMessage, details));
    }

    @PatchMapping(value = "{uuid}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Atualiza um processo administrativo",
            description = "Cobre tanto desativar o processo (ativo=false) quanto corrigir código/nome/descrição/capacidade — substitui os campos editáveis por inteiro.",
            tags = "ProcessoAdministrativo")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = SuccessResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "SuccessResponse",
                    value = ExampleConstants.SUCCESS_RESPONSE_UPDATE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> update(@PathVariable UUID uuid, @Valid @RequestBody ProcessoAdministrativoRequestDto dto) {
        ProcessoAdministrativoResponseDto responseDto = service.atualizar(uuid, dto);

        String successMessage = "Processo administrativo atualizado com sucesso!";
        String details = "Código: " + responseDto.getCodigo() + ", Ativo: " + responseDto.isAtivo();

        return ResponseEntity.ok(new SuccessResponseDto(successMessage, details));
    }
}
