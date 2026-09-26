package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ExampleConstants;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.MedicamentoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.MedicamentoResponseDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.SuccessResponseDto;
import com.edufelizardo.maissaudepublica.services.version1.MedicamentoService;
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
@RequestMapping(value = "/api/v1/medicamento/")
@Tag(name = "Medicamento", description = "Endpoints para gerenciar o catálogo de medicamentos (ver docs/adr/0049-medicamento-primeira-entidade-da-farmacia.md).")
public class MedicamentoController {

    @Autowired
    private MedicamentoService service;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Lista os medicamentos cadastrados", tags = "Medicamento")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = MedicamentoResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "MedicamentoResponse",
                    value = ExampleConstants.MEDICAMENTO_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesListagem
    public ResponseEntity<List<MedicamentoResponseDto>> getAll() {
        List<MedicamentoResponseDto> responseDtos = service.listar();
        if (responseDtos.isEmpty()) {
            throw new ResourceNotFoundException("Nenhum item foi encontrado.");
        }
        return ResponseEntity.ok(responseDtos);
    }

    @GetMapping(value = "{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Busca um medicamento pelo id", tags = "Medicamento")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json",
                    schema = @Schema(implementation = MedicamentoResponseDto.class))
    })
    @ApiErrorResponsesBusca
    public ResponseEntity<MedicamentoResponseDto> findById(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.buscarPorId(uuid));
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cria um medicamento",
            description = "Cadastra um medicamento no catálogo.",
            tags = "Medicamento")
    @ApiResponse(responseCode = "201", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = SuccessResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "SuccessResponse",
                    value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> create(@Valid @RequestBody MedicamentoRequestDto dto) {
        MedicamentoResponseDto responseDto = service.criar(dto);

        String successMessage = "Medicamento criado com sucesso!";
        String details = "Nome: " + responseDto.getNome();

        return ResponseEntity.status(HttpStatus.CREATED).body(new SuccessResponseDto(successMessage, details));
    }

    @PatchMapping(value = "{uuid}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Atualiza um medicamento",
            description = "Substitui os campos editáveis por inteiro.",
            tags = "Medicamento")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = SuccessResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "SuccessResponse",
                    value = ExampleConstants.SUCCESS_RESPONSE_UPDATE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> update(@PathVariable UUID uuid, @Valid @RequestBody MedicamentoRequestDto dto) {
        MedicamentoResponseDto responseDto = service.atualizar(uuid, dto);

        String successMessage = "Medicamento atualizado com sucesso!";
        String details = "Nome: " + responseDto.getNome() + ", Ativo: " + responseDto.isAtivo();

        return ResponseEntity.ok(new SuccessResponseDto(successMessage, details));
    }
}
