package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ExampleConstants;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.LicencaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.LicencaResponseDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.SuccessResponseDto;
import com.edufelizardo.maissaudepublica.services.version1.LicencaService;
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
@RequestMapping(value = "/api/v1/licenca/")
@Tag(name = "Licenca", description = "Endpoints para gerenciar licenças legais (subtipo de Afastamento — ver docs/rh/MODELO-RH.md).")
public class LicencaController {

    @Autowired
    private LicencaService service;

    @GetMapping(value = "afastamento/{afastamentoId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Busca a licença de um afastamento", tags = "Licenca")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json",
                    schema = @Schema(implementation = LicencaResponseDto.class),
                    examples = @ExampleObject(name = "Success",
                            summary = "LicencaResponse",
                            value = ExampleConstants.LICENCA_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesBusca
    public ResponseEntity<LicencaResponseDto> buscarPorAfastamento(@PathVariable UUID afastamentoId) {
        return ResponseEntity.ok(service.buscarPorAfastamento(afastamentoId));
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cria a licença de um afastamento",
            description = "Só uma licença por afastamento — tentar criar uma segunda pro mesmo afastamento retorna 409.",
            tags = "Licenca")
    @ApiResponse(responseCode = "201", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = SuccessResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "SuccessResponse",
                    value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> create(@Valid @RequestBody LicencaRequestDto dto) {
        LicencaResponseDto responseDto = service.criar(dto);

        String successMessage = "Licença criada com sucesso!";
        String details = "Profissional: " + responseDto.getProfissionalNome() + ", Tipo: " + responseDto.getTipoLegal();

        return ResponseEntity.status(HttpStatus.CREATED).body(new SuccessResponseDto(successMessage, details));
    }
}
