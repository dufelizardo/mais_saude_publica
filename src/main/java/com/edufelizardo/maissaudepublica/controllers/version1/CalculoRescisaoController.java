package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ExampleConstants;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.CalculoRescisaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.CalculoRescisaoResponseDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.SuccessResponseDto;
import com.edufelizardo.maissaudepublica.services.version1.CalculoRescisaoService;
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

@RestController
@RequestMapping(value = "/api/v1/calculo-rescisao/")
@Tag(name = "CalculoRescisao", description = "Endpoints para registrar verbas rescisórias — valores informados, não calculados pelo sistema (ver docs/rh/MODELO-RH.md).")
public class CalculoRescisaoController {

    @Autowired
    private CalculoRescisaoService service;

    @GetMapping(value = "profissional/{matricula}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Lista o histórico de cálculos de rescisão de um profissional", tags = "CalculoRescisao")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = CalculoRescisaoResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "CalculoRescisaoResponse",
                    value = ExampleConstants.CALCULO_RESCISAO_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesBusca
    public ResponseEntity<List<CalculoRescisaoResponseDto>> listarHistorico(@PathVariable String matricula) {
        return ResponseEntity.ok(service.listarHistorico(matricula));
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Registra um cálculo de rescisão",
            description = "Os valores são informados por quem já calculou fora do sistema (contador/DP) — não são calculados aqui.",
            tags = "CalculoRescisao")
    @ApiResponse(responseCode = "201", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = SuccessResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "SuccessResponse",
                    value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> create(@Valid @RequestBody CalculoRescisaoRequestDto dto) {
        CalculoRescisaoResponseDto responseDto = service.criar(dto);

        String successMessage = "Cálculo de rescisão registrado com sucesso!";
        String details = "Profissional: " + responseDto.getProfissionalNome() + ", Total: " + responseDto.getTotal();

        return ResponseEntity.status(HttpStatus.CREATED).body(new SuccessResponseDto(successMessage, details));
    }
}
