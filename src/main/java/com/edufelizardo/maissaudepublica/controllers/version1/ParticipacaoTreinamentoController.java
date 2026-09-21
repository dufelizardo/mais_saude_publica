package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ExampleConstants;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ParticipacaoTreinamentoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ParticipacaoTreinamentoResponseDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.SuccessResponseDto;
import com.edufelizardo.maissaudepublica.services.version1.ParticipacaoTreinamentoService;
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
@RequestMapping(value = "/api/v1/participacao-treinamento/")
@Tag(name = "ParticipacaoTreinamento", description = "Endpoints para registrar participação de profissionais em treinamentos (ver docs/rh/MODELO-RH.md).")
public class ParticipacaoTreinamentoController {

    @Autowired
    private ParticipacaoTreinamentoService service;

    @GetMapping(value = "profissional/{matricula}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Lista o histórico de participações em treinamento de um profissional", tags = "ParticipacaoTreinamento")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = ParticipacaoTreinamentoResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "ParticipacaoTreinamentoResponse",
                    value = ExampleConstants.PARTICIPACAO_TREINAMENTO_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesBusca
    public ResponseEntity<List<ParticipacaoTreinamentoResponseDto>> listarHistorico(@PathVariable String matricula) {
        return ResponseEntity.ok(service.listarHistorico(matricula));
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Registra a participação de um profissional num treinamento",
            description = "A data de validade é calculada automaticamente a partir da validadeMeses do treinamento — null se o treinamento não tiver validade.",
            tags = "ParticipacaoTreinamento")
    @ApiResponse(responseCode = "201", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = SuccessResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "SuccessResponse",
                    value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> create(@Valid @RequestBody ParticipacaoTreinamentoRequestDto dto) {
        ParticipacaoTreinamentoResponseDto responseDto = service.criar(dto);

        String successMessage = "Participação em treinamento registrada com sucesso!";
        String details = "Profissional: " + responseDto.getProfissionalNome() + ", Treinamento: " + responseDto.getTreinamentoNome();

        return ResponseEntity.status(HttpStatus.CREATED).body(new SuccessResponseDto(successMessage, details));
    }
}
