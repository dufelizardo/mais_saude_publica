package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ExampleConstants;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.FolhaPagamentoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.FolhaPagamentoResponseDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.SuccessResponseDto;
import com.edufelizardo.maissaudepublica.services.version1.FolhaPagamentoService;
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
@RequestMapping(value = "/api/v1/folha-pagamento/")
@Tag(name = "FolhaPagamento", description = "Endpoints para registrar folhas de pagamento mensais — valores informados, não calculados pelo sistema (ver docs/rh/MODELO-RH.md).")
public class FolhaPagamentoController {

    @Autowired
    private FolhaPagamentoService service;

    @GetMapping(value = "competencia", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Lista as folhas de pagamento de todos os profissionais numa competência",
            description = "Competência via query param (não path, o formato MM/AAAA tem barra) — ex.: ?valor=09/2026. Lista vazia (200) se ninguém foi processado ainda nessa competência, não é erro.",
            tags = "FolhaPagamento")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = FolhaPagamentoResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "FolhaPagamentoResponse",
                    value = ExampleConstants.FOLHA_PAGAMENTO_RESPONSE_EXAMPLE))
    })
    public ResponseEntity<List<FolhaPagamentoResponseDto>> listarPorCompetencia(@RequestParam String valor) {
        return ResponseEntity.ok(service.listarPorCompetencia(valor));
    }

    @GetMapping(value = "profissional/{matricula}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Lista o histórico de folhas de pagamento de um profissional", tags = "FolhaPagamento")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = FolhaPagamentoResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "FolhaPagamentoResponse",
                    value = ExampleConstants.FOLHA_PAGAMENTO_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesBusca
    public ResponseEntity<List<FolhaPagamentoResponseDto>> listarHistorico(@PathVariable String matricula) {
        return ResponseEntity.ok(service.listarHistorico(matricula));
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Registra uma folha de pagamento",
            description = "Os valores são informados — não calculados pelo sistema. No máximo uma folha por profissional por competência (409 na segunda tentativa).",
            tags = "FolhaPagamento")
    @ApiResponse(responseCode = "201", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = SuccessResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "SuccessResponse",
                    value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> create(@Valid @RequestBody FolhaPagamentoRequestDto dto) {
        FolhaPagamentoResponseDto responseDto = service.criar(dto);

        String successMessage = "Folha de pagamento registrada com sucesso!";
        String details = "Profissional: " + responseDto.getProfissionalNome() + ", Competência: " + responseDto.getCompetencia();

        return ResponseEntity.status(HttpStatus.CREATED).body(new SuccessResponseDto(successMessage, details));
    }
}
