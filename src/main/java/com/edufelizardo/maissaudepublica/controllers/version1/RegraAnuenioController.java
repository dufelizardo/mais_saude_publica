package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ExampleConstants;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.RegraAnuenioRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.RegraAnuenioResponseDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.SuccessResponseDto;
import com.edufelizardo.maissaudepublica.services.version1.RegraAnuenioService;
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

import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/regra-anuenio/")
@Tag(name = "RegraAnuenio", description = "Endpoints para gerenciar a regra de progressão automática por tempo de serviço, uma por categoria salarial (ver docs/rh/MODELO-RH.md).")
public class RegraAnuenioController {

    @Autowired
    private RegraAnuenioService service;

    @GetMapping(value = "categoria/{categoriaId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Busca a regra de anuênio de uma categoria salarial", tags = "RegraAnuenio")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json",
                    schema = @Schema(implementation = RegraAnuenioResponseDto.class),
                    examples = @ExampleObject(name = "Success",
                            summary = "RegraAnuenioResponse",
                            value = ExampleConstants.REGRA_ANUENIO_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesBusca
    public ResponseEntity<RegraAnuenioResponseDto> buscarPorCategoria(@PathVariable UUID categoriaId) {
        return ResponseEntity.ok(service.buscarPorCategoria(categoriaId));
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cria a regra de anuênio de uma categoria salarial",
            description = "Só uma regra por categoria — tentar criar uma segunda para a mesma categoria retorna 409.",
            tags = "RegraAnuenio")
    @ApiResponse(responseCode = "201", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = SuccessResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "SuccessResponse",
                    value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> create(@Valid @RequestBody RegraAnuenioRequestDto dto) {
        RegraAnuenioResponseDto responseDto = service.criar(dto);

        String successMessage = "Regra de anuênio criada com sucesso!";
        String details = "Categoria: " + responseDto.getCategoriaNome() + ", Percentual por ano: " + responseDto.getPercentualPorAno();

        return ResponseEntity.status(HttpStatus.CREATED).body(new SuccessResponseDto(successMessage, details));
    }

    @PatchMapping(value = "{uuid}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Atualiza o percentual/teto de uma regra de anuênio",
            description = "A categoria não muda numa edição — é a chave que garante uma regra por categoria.",
            tags = "RegraAnuenio")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = SuccessResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "SuccessResponse",
                    value = ExampleConstants.SUCCESS_RESPONSE_UPDATE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> update(@PathVariable UUID uuid, @Valid @RequestBody RegraAnuenioRequestDto dto) {
        RegraAnuenioResponseDto responseDto = service.atualizar(uuid, dto);

        String successMessage = "Regra de anuênio atualizada com sucesso!";
        String details = "Categoria: " + responseDto.getCategoriaNome() + ", Percentual por ano: " + responseDto.getPercentualPorAno();

        return ResponseEntity.ok(new SuccessResponseDto(successMessage, details));
    }
}
