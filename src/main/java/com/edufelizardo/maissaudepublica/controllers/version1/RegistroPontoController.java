package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ExampleConstants;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.RegistroPontoCorrecaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.RegistroPontoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.RegistroPontoResponseDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.SuccessResponseDto;
import com.edufelizardo.maissaudepublica.services.version1.RegistroPontoService;
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
@RequestMapping(value = "/api/v1/registro-ponto/")
@Tag(name = "RegistroPonto", description = "Endpoints para gerenciar registros de jornada — entrada/saída/intervalo (ver docs/rh/MODELO-RH.md).")
public class RegistroPontoController {

    @Autowired
    private RegistroPontoService service;

    @GetMapping(value = "profissional/{matricula}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Lista o histórico de registros de ponto de um profissional",
            description = "Sem dataInicio/dataFim, lista tudo. Com os dois, filtra o período (inclusive).",
            tags = "RegistroPonto")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = RegistroPontoResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "RegistroPontoResponse",
                    value = ExampleConstants.REGISTRO_PONTO_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesBusca
    public ResponseEntity<List<RegistroPontoResponseDto>> listarHistorico(
            @PathVariable String matricula,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {
        return ResponseEntity.ok(service.listarHistorico(matricula, dataInicio, dataFim));
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cria um registro de ponto", tags = "RegistroPonto")
    @ApiResponse(responseCode = "201", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = SuccessResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "SuccessResponse",
                    value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> create(@Valid @RequestBody RegistroPontoRequestDto dto) {
        RegistroPontoResponseDto responseDto = service.criar(dto);

        String successMessage = "Registro de ponto criado com sucesso!";
        String details = "Profissional: " + responseDto.getProfissionalNome() + ", Tipo: " + responseDto.getTipo();

        return ResponseEntity.status(HttpStatus.CREATED).body(new SuccessResponseDto(successMessage, details));
    }

    @PatchMapping(value = "{uuid}/solicitar-correcao", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Solicita a correção de um registro de ponto",
            description = "Fica pendente de aprovação do gestor -- não altera dataHora/tipo até ser aprovada. Recusa (409) se já houver uma correção pendente.",
            tags = "RegistroPonto")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = SuccessResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "SuccessResponse",
                    value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> solicitarCorrecao(@PathVariable UUID uuid, @Valid @RequestBody RegistroPontoCorrecaoRequestDto dto) {
        RegistroPontoResponseDto responseDto = service.solicitarCorrecao(uuid, dto);

        String successMessage = "Correção de ponto solicitada com sucesso!";
        String details = "Profissional: " + responseDto.getProfissionalNome() + ", pendente de aprovação do gestor.";

        return ResponseEntity.ok(new SuccessResponseDto(successMessage, details));
    }

    @PatchMapping(value = "{uuid}/aprovar-correcao", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Aprova a correção pendente de um registro de ponto",
            description = "Aplica a data/hora e o tipo propostos e limpa a pendência. Recusa (409) se não houver correção pendente.",
            tags = "RegistroPonto")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = SuccessResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "SuccessResponse",
                    value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> aprovarCorrecao(@PathVariable UUID uuid) {
        RegistroPontoResponseDto responseDto = service.aprovarCorrecao(uuid);

        String successMessage = "Correção de ponto aprovada com sucesso!";
        String details = "Profissional: " + responseDto.getProfissionalNome();

        return ResponseEntity.ok(new SuccessResponseDto(successMessage, details));
    }

    @PatchMapping(value = "{uuid}/rejeitar-correcao", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Rejeita a correção pendente de um registro de ponto",
            description = "Descarta a proposta e mantém o registro original. Recusa (409) se não houver correção pendente.",
            tags = "RegistroPonto")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = SuccessResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "SuccessResponse",
                    value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> rejeitarCorrecao(@PathVariable UUID uuid) {
        RegistroPontoResponseDto responseDto = service.rejeitarCorrecao(uuid);

        String successMessage = "Correção de ponto rejeitada com sucesso!";
        String details = "Profissional: " + responseDto.getProfissionalNome();

        return ResponseEntity.ok(new SuccessResponseDto(successMessage, details));
    }
}
