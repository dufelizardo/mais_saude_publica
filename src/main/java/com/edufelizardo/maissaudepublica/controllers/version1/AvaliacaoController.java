package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ExampleConstants;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AvaliacaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AvaliacaoResponseDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.SuccessResponseDto;
import com.edufelizardo.maissaudepublica.services.version1.AvaliacaoService;
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
@RequestMapping(value = "/api/v1/avaliacao/")
@Tag(name = "Avaliacao", description = "Endpoints para registrar avaliações de desempenho (ver docs/rh/MODELO-RH.md).")
public class AvaliacaoController {

    @Autowired
    private AvaliacaoService service;

    @GetMapping(value = "profissional/{matricula}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Lista o histórico de avaliações de um profissional", tags = "Avaliacao")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = AvaliacaoResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "AvaliacaoResponse",
                    value = ExampleConstants.AVALIACAO_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesBusca
    public ResponseEntity<List<AvaliacaoResponseDto>> listarHistorico(@PathVariable String matricula) {
        return ResponseEntity.ok(service.listarHistorico(matricula));
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Registra uma avaliação de desempenho", tags = "Avaliacao")
    @ApiResponse(responseCode = "201", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = SuccessResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "SuccessResponse",
                    value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> create(@Valid @RequestBody AvaliacaoRequestDto dto) {
        AvaliacaoResponseDto responseDto = service.criar(dto);

        String successMessage = "Avaliação registrada com sucesso!";
        String details = "Profissional: " + responseDto.getProfissionalNome() + ", Ciclo: " + responseDto.getCicloNome();

        return ResponseEntity.status(HttpStatus.CREATED).body(new SuccessResponseDto(successMessage, details));
    }
}
