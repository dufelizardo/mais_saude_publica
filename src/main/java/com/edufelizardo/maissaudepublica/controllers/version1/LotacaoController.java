package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ExampleConstants;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.LotacaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.LotacaoResponseDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.SuccessResponseDto;
import com.edufelizardo.maissaudepublica.services.version1.LotacaoService;
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
@RequestMapping(value = "/api/v1/lotacao/")
@Tag(name = "Lotacao", description = "Endpoints para gerenciar a lotação (vínculo profissional-unidade-cargo com histórico, ver docs/rh/MODELO-RH.md).")
public class LotacaoController {

    @Autowired
    private LotacaoService service;

    @GetMapping(value = "profissional/{matricula}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Lista o histórico de lotações de um profissional", tags = "Lotacao")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = LotacaoResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "LotacaoResponse",
                    value = ExampleConstants.LOTACAO_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesBusca
    public ResponseEntity<List<LotacaoResponseDto>> listarHistorico(@PathVariable String matricula) {
        return ResponseEntity.ok(service.listarHistorico(matricula));
    }

    @GetMapping(value = "profissional/{matricula}/atual", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Busca a lotação vigente de um profissional", tags = "Lotacao")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json",
                    schema = @Schema(implementation = LotacaoResponseDto.class))
    })
    @ApiErrorResponsesBusca
    public ResponseEntity<LotacaoResponseDto> buscarVigente(@PathVariable String matricula) {
        return ResponseEntity.ok(service.buscarVigente(matricula));
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cria uma lotação (admissão, transferência ou mudança de cargo)",
            description = "Se já existir uma lotação vigente para o profissional, ela é encerrada automaticamente (dataFim = dia anterior ao dataInicio da nova) antes da nova ser criada.",
            tags = "Lotacao")
    @ApiResponse(responseCode = "201", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = SuccessResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "SuccessResponse",
                    value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> create(@Valid @RequestBody LotacaoRequestDto dto) {
        LotacaoResponseDto responseDto = service.criar(dto);

        String successMessage = "Lotação criada com sucesso!";
        String details = "Profissional: " + responseDto.getProfissionalNome() + ", Unidade: " + responseDto.getUnidadeNome()
                + ", Cargo: " + responseDto.getCargoNome();

        return ResponseEntity.status(HttpStatus.CREATED).body(new SuccessResponseDto(successMessage, details));
    }
}
