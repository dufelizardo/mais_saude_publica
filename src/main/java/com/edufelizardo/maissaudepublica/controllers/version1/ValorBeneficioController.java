package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ExampleConstants;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ValorBeneficioRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.SuccessResponseDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ValorBeneficioResponseDto;
import com.edufelizardo.maissaudepublica.services.version1.ValorBeneficioService;
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
@RequestMapping(value = "/api/v1/valor-beneficio/")
@Tag(name = "ValorBeneficio", description = "Endpoints para gerenciar o histórico de valores por tipo de benefício (ver docs/rh/MODELO-RH.md).")
public class ValorBeneficioController {

    @Autowired
    private ValorBeneficioService service;

    @GetMapping(value = "tipo/{tipoBeneficioId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Lista o histórico de valores de um tipo de benefício", tags = "ValorBeneficio")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = ValorBeneficioResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "ValorBeneficioResponse",
                    value = ExampleConstants.VALOR_BENEFICIO_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesBusca
    public ResponseEntity<List<ValorBeneficioResponseDto>> listarPorTipo(@PathVariable UUID tipoBeneficioId) {
        return ResponseEntity.ok(service.listarPorTipo(tipoBeneficioId));
    }

    @GetMapping(value = "tipo/{tipoBeneficioId}/vigente", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Busca o valor vigente de um tipo de benefício numa data (padrão: hoje)", tags = "ValorBeneficio")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ValorBeneficioResponseDto.class))
    })
    @ApiErrorResponsesBusca
    public ResponseEntity<ValorBeneficioResponseDto> buscarVigente(
            @PathVariable UUID tipoBeneficioId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return ResponseEntity.ok(service.buscarVigente(tipoBeneficioId, data));
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cria um novo valor para um tipo de benefício", tags = "ValorBeneficio")
    @ApiResponse(responseCode = "201", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = SuccessResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "SuccessResponse",
                    value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> create(@Valid @RequestBody ValorBeneficioRequestDto dto) {
        ValorBeneficioResponseDto responseDto = service.criar(dto);

        String successMessage = "Valor de benefício criado com sucesso!";
        String details = "Tipo: " + responseDto.getTipoBeneficioNome() + ", Valor: " + responseDto.getValor();

        return ResponseEntity.status(HttpStatus.CREATED).body(new SuccessResponseDto(successMessage, details));
    }
}
