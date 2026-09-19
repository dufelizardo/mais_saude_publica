package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ExampleConstants;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AdesaoBeneficioRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AdesaoBeneficioResponseDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.SuccessResponseDto;
import com.edufelizardo.maissaudepublica.services.version1.AdesaoBeneficioService;
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
@RequestMapping(value = "/api/v1/adesao-beneficio/")
@Tag(name = "AdesaoBeneficio", description = "Endpoints para gerenciar a adesão de profissionais a tipos de benefício (ver docs/rh/MODELO-RH.md).")
public class AdesaoBeneficioController {

    @Autowired
    private AdesaoBeneficioService service;

    @GetMapping(value = "profissional/{matricula}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Lista o histórico de adesões de benefício de um profissional", tags = "AdesaoBeneficio")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = AdesaoBeneficioResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "AdesaoBeneficioResponse",
                    value = ExampleConstants.ADESAO_BENEFICIO_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesBusca
    public ResponseEntity<List<AdesaoBeneficioResponseDto>> listarHistorico(@PathVariable String matricula) {
        return ResponseEntity.ok(service.listarHistorico(matricula));
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cria uma adesão de benefício",
            description = "Um profissional pode ter várias adesões vigentes ao mesmo tempo (ex.: VT + VR + plano de saúde) — diferente de Lotacao, não há limite de uma vigente por vez.",
            tags = "AdesaoBeneficio")
    @ApiResponse(responseCode = "201", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = SuccessResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "SuccessResponse",
                    value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> create(@Valid @RequestBody AdesaoBeneficioRequestDto dto) {
        AdesaoBeneficioResponseDto responseDto = service.criar(dto);

        String successMessage = "Adesão de benefício criada com sucesso!";
        String details = "Profissional: " + responseDto.getProfissionalNome() + ", Benefício: " + responseDto.getTipoBeneficioNome();

        return ResponseEntity.status(HttpStatus.CREATED).body(new SuccessResponseDto(successMessage, details));
    }
}
