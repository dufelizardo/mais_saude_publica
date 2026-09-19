package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ExampleConstants;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.CandidatoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.CandidatoResponseDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.SuccessResponseDto;
import com.edufelizardo.maissaudepublica.services.version1.CandidatoService;
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
@RequestMapping(value = "/api/v1/candidato/")
@Tag(name = "Candidato", description = "Endpoints para gerenciar candidatos inscritos em vagas (ver docs/rh/MODELO-RH.md).")
public class CandidatoController {

    @Autowired
    private CandidatoService service;

    @GetMapping(value = "vaga/{vagaId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Lista os candidatos inscritos numa vaga", tags = "Candidato")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = CandidatoResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "CandidatoResponse",
                    value = ExampleConstants.CANDIDATO_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesBusca
    public ResponseEntity<List<CandidatoResponseDto>> listarPorVaga(@PathVariable UUID vagaId) {
        return ResponseEntity.ok(service.listarPorVaga(vagaId));
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Inscreve um candidato numa vaga", tags = "Candidato")
    @ApiResponse(responseCode = "201", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = SuccessResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "SuccessResponse",
                    value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> create(@Valid @RequestBody CandidatoRequestDto dto) {
        CandidatoResponseDto responseDto = service.criar(dto);

        String successMessage = "Candidato inscrito com sucesso!";
        String details = "Nome: " + responseDto.getNome();

        return ResponseEntity.status(HttpStatus.CREATED).body(new SuccessResponseDto(successMessage, details));
    }
}
