package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ExampleConstants;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EvolucaoEnfermagemRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.EvolucaoEnfermagemResponseDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.SuccessResponseDto;
import com.edufelizardo.maissaudepublica.services.version1.EvolucaoEnfermagemService;
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
@RequestMapping(value = "/api/v1/evolucao-enfermagem/")
@Tag(name = "Evolução de Enfermagem", description = "Endpoints para gerenciar notas de evolução de enfermagem registradas ao longo de um atendimento (ver docs/adr/0048-evolucao-de-enfermagem-segunda-entidade-da-enfermagem.md).")
public class EvolucaoEnfermagemController {

    @Autowired
    private EvolucaoEnfermagemService service;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Lista as evoluções de enfermagem cadastradas", tags = "Evolução de Enfermagem")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = EvolucaoEnfermagemResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "EvolucaoEnfermagemResponse",
                    value = ExampleConstants.EVOLUCAO_ENFERMAGEM_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesListagem
    public ResponseEntity<List<EvolucaoEnfermagemResponseDto>> getAll() {
        List<EvolucaoEnfermagemResponseDto> responseDtos = service.listar();
        if (responseDtos.isEmpty()) {
            throw new ResourceNotFoundException("Nenhum item foi encontrado.");
        }
        return ResponseEntity.ok(responseDtos);
    }

    @GetMapping(value = "{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Busca uma evolução de enfermagem pelo id", tags = "Evolução de Enfermagem")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json",
                    schema = @Schema(implementation = EvolucaoEnfermagemResponseDto.class))
    })
    @ApiErrorResponsesBusca
    public ResponseEntity<EvolucaoEnfermagemResponseDto> findById(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.buscarPorId(uuid));
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cria uma evolução de enfermagem",
            description = "Registra uma nota de evolução de enfermagem ao longo de um atendimento, com um profissional (por matrícula, ver ADR-0034).",
            tags = "Evolução de Enfermagem")
    @ApiResponse(responseCode = "201", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = SuccessResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "SuccessResponse",
                    value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> create(@Valid @RequestBody EvolucaoEnfermagemRequestDto dto) {
        EvolucaoEnfermagemResponseDto responseDto = service.criar(dto);

        String successMessage = "Evolução de enfermagem criada com sucesso!";
        String details = "Profissional: " + responseDto.getProfissionalNome();

        return ResponseEntity.status(HttpStatus.CREATED).body(new SuccessResponseDto(successMessage, details));
    }

    @PatchMapping(value = "{uuid}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Atualiza uma evolução de enfermagem",
            description = "Substitui os campos editáveis por inteiro — cobre corrigir a descrição da evolução.",
            tags = "Evolução de Enfermagem")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = SuccessResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "SuccessResponse",
                    value = ExampleConstants.SUCCESS_RESPONSE_UPDATE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> update(@PathVariable UUID uuid, @Valid @RequestBody EvolucaoEnfermagemRequestDto dto) {
        EvolucaoEnfermagemResponseDto responseDto = service.atualizar(uuid, dto);

        String successMessage = "Evolução de enfermagem atualizada com sucesso!";
        String details = "Profissional: " + responseDto.getProfissionalNome();

        return ResponseEntity.ok(new SuccessResponseDto(successMessage, details));
    }
}
