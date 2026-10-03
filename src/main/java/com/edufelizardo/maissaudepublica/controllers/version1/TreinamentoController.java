package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ExampleConstants;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.TreinamentoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.SuccessResponseDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.TreinamentoResponseDto;
import com.edufelizardo.maissaudepublica.services.version1.TreinamentoService;
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
@RequestMapping(value = "/api/v1/treinamento/")
@Tag(name = "Treinamento", description = "Endpoints para gerenciar o catálogo de treinamentos (ver docs/rh/MODELO-RH.md).")
public class TreinamentoController {

    @Autowired
    private TreinamentoService service;

    @RequerPermissao({"RH.CONSULTAR", "RH.GERENCIAR"})
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Lista os treinamentos cadastrados", tags = "Treinamento")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = TreinamentoResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "TreinamentoResponse",
                    value = ExampleConstants.TREINAMENTO_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesListagem
    public ResponseEntity<List<TreinamentoResponseDto>> getAll() {
        List<TreinamentoResponseDto> responseDtos = service.listar();
        if (responseDtos.isEmpty()) {
            throw new ResourceNotFoundException("Nenhum item foi encontrado.");
        }
        return ResponseEntity.ok(responseDtos);
    }

    @RequerPermissao({"RH.CONSULTAR", "RH.GERENCIAR"})
    @GetMapping(value = "{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Busca um treinamento pelo id", tags = "Treinamento")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json",
                    schema = @Schema(implementation = TreinamentoResponseDto.class))
    })
    @ApiErrorResponsesBusca
    public ResponseEntity<TreinamentoResponseDto> findById(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.buscarPorId(uuid));
    }

    @RequerPermissao({"RH.GERENCIAR"})
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cria um treinamento", tags = "Treinamento")
    @ApiResponse(responseCode = "201", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = SuccessResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "SuccessResponse",
                    value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> create(@Valid @RequestBody TreinamentoRequestDto dto) {
        TreinamentoResponseDto responseDto = service.criar(dto);

        String successMessage = "Treinamento criado com sucesso!";
        String details = "Nome: " + responseDto.getNome();

        return ResponseEntity.status(HttpStatus.CREATED).body(new SuccessResponseDto(successMessage, details));
    }

    @RequerPermissao({"RH.GERENCIAR"})
    @PatchMapping(value = "{uuid}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Atualiza treinamento",
            description = "Mesmos campos e regras da criação (ADR-0083). Inexistente → 404; nome já usado por outro → 409.",
            tags = "Treinamento")
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> atualizar(@PathVariable UUID uuid, @Valid @RequestBody TreinamentoRequestDto dto) {
        TreinamentoResponseDto r = service.atualizar(uuid, dto);
        return ResponseEntity.ok(new SuccessResponseDto("Treinamento atualizado com sucesso!", "Nome: " + r.getNome()));
    }
}
