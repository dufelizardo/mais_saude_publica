package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ExampleConstants;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ResponsabilidadeAdministrativaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ResponsabilidadeAdministrativaResponseDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.SuccessResponseDto;
import com.edufelizardo.maissaudepublica.services.version1.ResponsabilidadeAdministrativaService;
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
@RequestMapping(value = "/api/v1/responsabilidade-administrativa/")
@Tag(name = "ResponsabilidadeAdministrativa", description = "Endpoints para gerenciar responsabilidades administrativas de profissionais sobre setores (ver docs/adr/0035-responsabilidade-administrativa-separada-da-lotacao.md).")
public class ResponsabilidadeAdministrativaController {

    @Autowired
    private ResponsabilidadeAdministrativaService service;

    @GetMapping(value = "profissional/{matricula}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Lista o histórico de responsabilidades administrativas de um profissional", tags = "ResponsabilidadeAdministrativa")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = ResponsabilidadeAdministrativaResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "ResponsabilidadeAdministrativaResponse",
                    value = ExampleConstants.RESPONSABILIDADE_ADMINISTRATIVA_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesBusca
    public ResponseEntity<List<ResponsabilidadeAdministrativaResponseDto>> listarHistorico(@PathVariable String matricula) {
        return ResponseEntity.ok(service.listarHistorico(matricula));
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cria uma responsabilidade administrativa",
            description = "Um profissional pode ter várias responsabilidades vigentes ao mesmo tempo — diferente de Lotacao, não há limite de uma vigente por vez.",
            tags = "ResponsabilidadeAdministrativa")
    @ApiResponse(responseCode = "201", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = SuccessResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "SuccessResponse",
                    value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> create(@Valid @RequestBody ResponsabilidadeAdministrativaRequestDto dto) {
        ResponsabilidadeAdministrativaResponseDto responseDto = service.criar(dto);

        String successMessage = "Responsabilidade administrativa criada com sucesso!";
        String details = "Profissional: " + responseDto.getProfissionalNome() + ", Setor: " + responseDto.getSetorNome();

        return ResponseEntity.status(HttpStatus.CREATED).body(new SuccessResponseDto(successMessage, details));
    }

    @PatchMapping(value = "{uuid}/encerrar", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Encerra uma responsabilidade administrativa",
            description = "Operação de negócio explícita: a responsabilidade não é apagada nem alterada destrutivamente, ela ganha uma data de fim. Recusa encerrar uma responsabilidade que já foi encerrada (409).",
            tags = "ResponsabilidadeAdministrativa")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = SuccessResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "SuccessResponse",
                    value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> encerrar(
            @PathVariable UUID uuid,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {
        ResponsabilidadeAdministrativaResponseDto responseDto = service.encerrar(uuid, dataFim);

        String successMessage = "Responsabilidade administrativa encerrada com sucesso!";
        String details = "Profissional: " + responseDto.getProfissionalNome() + ", Setor: " + responseDto.getSetorNome();

        return ResponseEntity.ok(new SuccessResponseDto(successMessage, details));
    }
}
