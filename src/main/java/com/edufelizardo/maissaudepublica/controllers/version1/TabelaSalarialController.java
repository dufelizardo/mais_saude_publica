package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ExampleConstants;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.TabelaSalarialRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.SuccessResponseDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.TabelaSalarialResponseDto;
import com.edufelizardo.maissaudepublica.services.version1.TabelaSalarialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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

@RestController
@RequestMapping(value = "/api/v1/tabela-salarial/")
@Tag(name = "TabelaSalarial", description = "Endpoints para gerenciar o histórico de valores por cargo (ver docs/rh/MODELO-RH.md).")
public class TabelaSalarialController {

    @Autowired
    private TabelaSalarialService service;

    @GetMapping(value = "cargo/{cargoId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Lista o histórico de valores de um cargo", tags = "TabelaSalarial")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = TabelaSalarialResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "TabelaSalarialResponse",
                    value = ExampleConstants.TABELA_SALARIAL_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesBusca
    public ResponseEntity<List<TabelaSalarialResponseDto>> listarPorCargo(@PathVariable UUID cargoId) {
        return ResponseEntity.ok(service.listarPorCargo(cargoId));
    }

    @GetMapping(value = "cargo/{cargoId}/vigente", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Busca o valor vigente de um cargo numa data (padrão: hoje)", tags = "TabelaSalarial")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json",
                    schema = @Schema(implementation = TabelaSalarialResponseDto.class))
    })
    @ApiErrorResponsesBusca
    public ResponseEntity<TabelaSalarialResponseDto> buscarVigente(
            @PathVariable UUID cargoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return ResponseEntity.ok(service.buscarVigente(cargoId, data));
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cria um novo valor de tabela salarial para um cargo (dissídio ou revisão do plano)", tags = "TabelaSalarial")
    @ApiResponse(responseCode = "201", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = SuccessResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "SuccessResponse",
                    value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> create(@Valid @RequestBody TabelaSalarialRequestDto dto) {
        TabelaSalarialResponseDto responseDto = service.criar(dto);

        String successMessage = "Valor de tabela salarial criado com sucesso!";
        String details = "Cargo: " + responseDto.getCargoNome() + ", Valor base: " + responseDto.getValorBase();

        return ResponseEntity.status(HttpStatus.CREATED).body(new SuccessResponseDto(successMessage, details));
    }
}
