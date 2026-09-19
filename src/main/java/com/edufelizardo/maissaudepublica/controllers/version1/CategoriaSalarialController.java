package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ExampleConstants;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.CategoriaSalarialRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.CategoriaSalarialResponseDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.SuccessResponseDto;
import com.edufelizardo.maissaudepublica.services.version1.CategoriaSalarialService;
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
@RequestMapping(value = "/api/v1/categoria-salarial/")
@Tag(name = "CategoriaSalarial", description = "Endpoints para gerenciar categorias salariais (nível em que um dissídio se aplica, ver docs/rh/MODELO-RH.md).")
public class CategoriaSalarialController {

    @Autowired
    private CategoriaSalarialService service;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Lista as categorias salariais cadastradas", tags = "CategoriaSalarial")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = CategoriaSalarialResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "CategoriaSalarialResponse",
                    value = ExampleConstants.CATEGORIA_SALARIAL_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesListagem
    public ResponseEntity<List<CategoriaSalarialResponseDto>> getAll() {
        List<CategoriaSalarialResponseDto> responseDtos = service.listar();
        if (responseDtos.isEmpty()) {
            throw new ResourceNotFoundException("Nenhum item foi encontrado.");
        }
        return ResponseEntity.ok(responseDtos);
    }

    @GetMapping(value = "{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Busca uma categoria salarial pelo id", tags = "CategoriaSalarial")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json",
                    schema = @Schema(implementation = CategoriaSalarialResponseDto.class))
    })
    @ApiErrorResponsesBusca
    public ResponseEntity<CategoriaSalarialResponseDto> findById(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.buscarPorId(uuid));
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cria uma categoria salarial", tags = "CategoriaSalarial")
    @ApiResponse(responseCode = "201", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = SuccessResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "SuccessResponse",
                    value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> create(@Valid @RequestBody CategoriaSalarialRequestDto dto) {
        CategoriaSalarialResponseDto responseDto = service.criar(dto);

        String successMessage = "Categoria salarial criada com sucesso!";
        String details = "Nome: " + responseDto.getNome();

        return ResponseEntity.status(HttpStatus.CREATED).body(new SuccessResponseDto(successMessage, details));
    }

    @PatchMapping(value = "{uuid}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Atualiza uma categoria salarial", tags = "CategoriaSalarial")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = SuccessResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "SuccessResponse",
                    value = ExampleConstants.SUCCESS_RESPONSE_UPDATE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> update(@PathVariable UUID uuid, @Valid @RequestBody CategoriaSalarialRequestDto dto) {
        CategoriaSalarialResponseDto responseDto = service.atualizar(uuid, dto);

        String successMessage = "Categoria salarial atualizada com sucesso!";
        String details = "Nome: " + responseDto.getNome();

        return ResponseEntity.ok(new SuccessResponseDto(successMessage, details));
    }
}
