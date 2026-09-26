package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ExampleConstants;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.DispensacaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.DispensacaoResponseDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.SuccessResponseDto;
import com.edufelizardo.maissaudepublica.services.version1.DispensacaoService;
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

/**
 * Dispensação de medicamentos — só criação e leitura, sem PATCH (ver ADR-0051): é um registro
 * histórico de uma transação já ocorrida, não um cadastro editável.
 */
@RestController
@RequestMapping(value = "/api/v1/dispensacao/")
@Tag(name = "Dispensação", description = "Endpoints para registrar e consultar dispensações de medicamentos (ver docs/adr/0051-dispensacao-terceira-entidade-da-farmacia.md).")
public class DispensacaoController {

    @Autowired
    private DispensacaoService service;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Lista as dispensações registradas", tags = "Dispensação")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = DispensacaoResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "DispensacaoResponse",
                    value = ExampleConstants.DISPENSACAO_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesListagem
    public ResponseEntity<List<DispensacaoResponseDto>> getAll() {
        List<DispensacaoResponseDto> responseDtos = service.listar();
        if (responseDtos.isEmpty()) {
            throw new ResourceNotFoundException("Nenhum item foi encontrado.");
        }
        return ResponseEntity.ok(responseDtos);
    }

    @GetMapping(value = "{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Busca uma dispensação pelo id", tags = "Dispensação")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json",
                    schema = @Schema(implementation = DispensacaoResponseDto.class))
    })
    @ApiErrorResponsesBusca
    public ResponseEntity<DispensacaoResponseDto> findById(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.buscarPorId(uuid));
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Registra uma dispensação",
            description = "Registra a dispensação de um lote de medicamento a um paciente, debitando a quantidade do lote. Retorna 422 se o estoque do lote for insuficiente.",
            tags = "Dispensação")
    @ApiResponse(responseCode = "201", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = SuccessResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "SuccessResponse",
                    value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> create(@Valid @RequestBody DispensacaoRequestDto dto) {
        DispensacaoResponseDto responseDto = service.criar(dto);

        String successMessage = "Dispensação registrada com sucesso!";
        String details = "Medicamento: " + responseDto.getMedicamentoNome() + ", Quantidade: " + responseDto.getQuantidade();

        return ResponseEntity.status(HttpStatus.CREATED).body(new SuccessResponseDto(successMessage, details));
    }
}
