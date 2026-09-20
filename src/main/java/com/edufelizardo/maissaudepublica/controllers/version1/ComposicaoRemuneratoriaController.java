package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ExampleConstants;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ComposicaoRemuneratoriaResponseDto;
import com.edufelizardo.maissaudepublica.services.version1.ComposicaoRemuneratoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/composicao-remuneratoria/")
@Tag(name = "ComposicaoRemuneratoria", description = "Leitura calculada (não persiste nada) da composição salarial vigente de um profissional — valor base + anuênio + ajustes individuais (ver docs/rh/MODELO-RH.md).")
public class ComposicaoRemuneratoriaController {

    @Autowired
    private ComposicaoRemuneratoriaService service;

    @GetMapping(value = "profissional/{matricula}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Calcula a composição remuneratória vigente de um profissional", tags = "ComposicaoRemuneratoria")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ComposicaoRemuneratoriaResponseDto.class),
                    examples = @ExampleObject(name = "Success", summary = "ComposicaoRemuneratoriaResponse",
                            value = ExampleConstants.COMPOSICAO_REMUNERATORIA_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesBusca
    public ResponseEntity<ComposicaoRemuneratoriaResponseDto> calcular(@PathVariable String matricula) {
        return ResponseEntity.ok(service.calcular(matricula));
    }
}
