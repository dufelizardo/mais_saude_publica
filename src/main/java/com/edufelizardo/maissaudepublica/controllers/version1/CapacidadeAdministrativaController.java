package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ExampleConstants;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.CapacidadeAdministrativaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.CapacidadeAdministrativaResponseDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.SuccessResponseDto;
import com.edufelizardo.maissaudepublica.services.version1.CapacidadeAdministrativaService;
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
@RequestMapping(value = "/api/v1/capacidade-administrativa/")
@Tag(name = "CapacidadeAdministrativa", description = "Endpoints para gerenciar o catálogo de capacidades administrativas (ver docs/adr/0032-catalogo-de-capacidades-administrativas.md).")
public class CapacidadeAdministrativaController {

    @Autowired
    private CapacidadeAdministrativaService service;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Lista as capacidades administrativas cadastradas", tags = "CapacidadeAdministrativa")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = CapacidadeAdministrativaResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "CapacidadeAdministrativaResponse",
                    value = ExampleConstants.CAPACIDADE_ADMINISTRATIVA_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesListagem
    public ResponseEntity<List<CapacidadeAdministrativaResponseDto>> getAll() {
        List<CapacidadeAdministrativaResponseDto> responseDtos = service.listar();
        if (responseDtos.isEmpty()) {
            throw new ResourceNotFoundException("Nenhum item foi encontrado.");
        }
        return ResponseEntity.ok(responseDtos);
    }

    @GetMapping(value = "{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Busca uma capacidade administrativa pelo id", tags = "CapacidadeAdministrativa")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json",
                    schema = @Schema(implementation = CapacidadeAdministrativaResponseDto.class))
    })
    @ApiErrorResponsesBusca
    public ResponseEntity<CapacidadeAdministrativaResponseDto> findById(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.buscarPorId(uuid));
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cria uma capacidade administrativa no catálogo", tags = "CapacidadeAdministrativa")
    @ApiResponse(responseCode = "201", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = SuccessResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "SuccessResponse",
                    value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> create(@Valid @RequestBody CapacidadeAdministrativaRequestDto dto) {
        CapacidadeAdministrativaResponseDto responseDto = service.criar(dto);

        String successMessage = "Capacidade administrativa criada com sucesso!";
        String details = "Código: " + responseDto.getCodigo() + ", Nome: " + responseDto.getNome();

        return ResponseEntity.status(HttpStatus.CREATED).body(new SuccessResponseDto(successMessage, details));
    }

    @PatchMapping(value = "{uuid}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Atualiza uma capacidade administrativa",
            description = "Cobre tanto desativar a capacidade (ativo=false) quanto corrigir código/nome/descrição — substitui os campos editáveis por inteiro.",
            tags = "CapacidadeAdministrativa")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = SuccessResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "SuccessResponse",
                    value = ExampleConstants.SUCCESS_RESPONSE_UPDATE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> update(@PathVariable UUID uuid, @Valid @RequestBody CapacidadeAdministrativaRequestDto dto) {
        CapacidadeAdministrativaResponseDto responseDto = service.atualizar(uuid, dto);

        String successMessage = "Capacidade administrativa atualizada com sucesso!";
        String details = "Código: " + responseDto.getCodigo() + ", Ativo: " + responseDto.isAtivo();

        return ResponseEntity.ok(new SuccessResponseDto(successMessage, details));
    }
}
