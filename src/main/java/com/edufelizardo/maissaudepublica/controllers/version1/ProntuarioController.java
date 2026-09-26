package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ProntuarioResponseDto;
import com.edufelizardo.maissaudepublica.services.version1.ProntuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
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

import java.util.UUID;

/**
 * Prontuário — agregação de leitura sobre Atendimento/Triagem/Evolução de Enfermagem/Consulta/
 * Procedimento de um Paciente (ver ADR-0039 decisão 6, ADR-0045, estendida pelas ADR-0047/0048).
 * Sem POST/PATCH: não existe entidade própria para criar/atualizar.
 */
@RestController
@RequestMapping(value = "/api/v1/prontuario/")
@Tag(name = "Prontuário", description = "Endpoint de leitura do histórico clínico consolidado de um paciente (ver docs/adr/0045-prontuario-agregacao-de-leitura.md).")
public class ProntuarioController {

    @Autowired
    private ProntuarioService service;

    @GetMapping(value = "{pacienteId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Busca o prontuário consolidado de um paciente",
            description = "Agrega os Atendimentos do paciente, cada um com suas Triagens e Evoluções de enfermagem e suas Consultas, cada uma com seus Procedimentos.",
            tags = "Prontuário")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ProntuarioResponseDto.class))
    })
    @ApiErrorResponsesBusca
    public ResponseEntity<ProntuarioResponseDto> findByPacienteId(@PathVariable UUID pacienteId) {
        return ResponseEntity.ok(service.buscarPorPacienteId(pacienteId));
    }
}
