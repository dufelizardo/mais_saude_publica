package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ExampleConstants;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.PacienteRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.PacienteResponseDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.SuccessResponseDto;
import com.edufelizardo.maissaudepublica.services.version1.PacienteService;
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
@RequestMapping(value = "/api/v1/paciente/")
@Tag(name = "Paciente", description = "Endpoints para gerenciar pacientes/cidadãos atendidos pela rede pública de saúde (ver docs/adr/0039-mapa-de-dominios-e-prioridades-de-arquitetura.md).")
public class PacienteController {

    @Autowired
    private PacienteService service;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Lista os pacientes cadastrados", tags = "Paciente")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = PacienteResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "PacienteResponse",
                    value = ExampleConstants.PACIENTE_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesListagem
    public ResponseEntity<List<PacienteResponseDto>> getAll() {
        List<PacienteResponseDto> responseDtos = service.listar();
        if (responseDtos.isEmpty()) {
            throw new ResourceNotFoundException("Nenhum item foi encontrado.");
        }
        return ResponseEntity.ok(responseDtos);
    }

    @GetMapping(value = "{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Busca um paciente pelo id", tags = "Paciente")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json",
                    schema = @Schema(implementation = PacienteResponseDto.class))
    })
    @ApiErrorResponsesBusca
    public ResponseEntity<PacienteResponseDto> findById(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.buscarPorId(uuid));
    }

    @GetMapping(value = "cpf/{cpf}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Busca pacientes pelo CPF",
            description = "CPF não é único (ver ADR-0017, mesmo raciocínio aplicado ao Paciente) — pode haver mais de um registro.",
            tags = "Paciente")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = PacienteResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "PacienteResponse",
                    value = ExampleConstants.PACIENTE_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesBusca
    public ResponseEntity<List<PacienteResponseDto>> findByCpf(@PathVariable String cpf) {
        return ResponseEntity.ok(service.buscarPorCpf(cpf));
    }

    @GetMapping(value = "cartao-sus/{cartaoSus}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Busca pacientes pelo Cartão Nacional de Saúde (CNS)", tags = "Paciente")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = PacienteResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "PacienteResponse",
                    value = ExampleConstants.PACIENTE_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesBusca
    public ResponseEntity<List<PacienteResponseDto>> findByCartaoSus(@PathVariable String cartaoSus) {
        return ResponseEntity.ok(service.buscarPorCartaoSus(cartaoSus));
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cria um paciente", tags = "Paciente")
    @ApiResponse(responseCode = "201", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = SuccessResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "SuccessResponse",
                    value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> create(@Valid @RequestBody PacienteRequestDto dto) {
        PacienteResponseDto responseDto = service.criar(dto);

        String successMessage = "Paciente criado com sucesso!";
        String details = "Nome: " + responseDto.getNome() + ", CPF: " + responseDto.getCpf();

        return ResponseEntity.status(HttpStatus.CREATED).body(new SuccessResponseDto(successMessage, details));
    }

    @PatchMapping(value = "{uuid}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Atualiza um paciente",
            description = "Cobre tanto inativar o paciente (ativo=false) quanto corrigir dados cadastrais — substitui os campos editáveis por inteiro.",
            tags = "Paciente")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = SuccessResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "SuccessResponse",
                    value = ExampleConstants.SUCCESS_RESPONSE_UPDATE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> update(@PathVariable UUID uuid, @Valid @RequestBody PacienteRequestDto dto) {
        PacienteResponseDto responseDto = service.atualizar(uuid, dto);

        String successMessage = "Paciente atualizado com sucesso!";
        String details = "Nome: " + responseDto.getNome() + ", Ativo: " + responseDto.isAtivo();

        return ResponseEntity.ok(new SuccessResponseDto(successMessage, details));
    }
}
