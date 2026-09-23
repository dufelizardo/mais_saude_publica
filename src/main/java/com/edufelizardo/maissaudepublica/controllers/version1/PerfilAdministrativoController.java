package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ExampleConstants;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.PerfilAdministrativoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.PerfilAdministrativoResponseDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.SuccessResponseDto;
import com.edufelizardo.maissaudepublica.services.version1.PerfilAdministrativoService;
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
@RequestMapping(value = "/api/v1/perfil-administrativo/")
@Tag(name = "PerfilAdministrativo", description = "Endpoints para gerenciar o catálogo de perfis administrativos (ver docs/adr/0031-perfil-administrativo-por-tipo-de-unidade.md).")
public class PerfilAdministrativoController {

    @Autowired
    private PerfilAdministrativoService service;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Lista os perfis administrativos cadastrados", tags = "PerfilAdministrativo")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = PerfilAdministrativoResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "PerfilAdministrativoResponse",
                    value = ExampleConstants.PERFIL_ADMINISTRATIVO_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesListagem
    public ResponseEntity<List<PerfilAdministrativoResponseDto>> getAll() {
        List<PerfilAdministrativoResponseDto> responseDtos = service.listar();
        if (responseDtos.isEmpty()) {
            throw new ResourceNotFoundException("Nenhum item foi encontrado.");
        }
        return ResponseEntity.ok(responseDtos);
    }

    @GetMapping(value = "{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Busca um perfil administrativo pelo id", tags = "PerfilAdministrativo")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json",
                    schema = @Schema(implementation = PerfilAdministrativoResponseDto.class))
    })
    @ApiErrorResponsesBusca
    public ResponseEntity<PerfilAdministrativoResponseDto> findById(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.buscarPorId(uuid));
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cria um perfil administrativo no catálogo", tags = "PerfilAdministrativo")
    @ApiResponse(responseCode = "201", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = SuccessResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "SuccessResponse",
                    value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> create(@Valid @RequestBody PerfilAdministrativoRequestDto dto) {
        PerfilAdministrativoResponseDto responseDto = service.criar(dto);

        String successMessage = "Perfil administrativo criado com sucesso!";
        String details = "Código: " + responseDto.getCodigo() + ", Nome: " + responseDto.getNome();

        return ResponseEntity.status(HttpStatus.CREATED).body(new SuccessResponseDto(successMessage, details));
    }

    @PatchMapping(value = "{uuid}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Atualiza um perfil administrativo",
            description = "Cobre tanto desativar o perfil (ativo=false) quanto corrigir código/nome/descrição — substitui os campos editáveis por inteiro.",
            tags = "PerfilAdministrativo")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = SuccessResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "SuccessResponse",
                    value = ExampleConstants.SUCCESS_RESPONSE_UPDATE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> update(@PathVariable UUID uuid, @Valid @RequestBody PerfilAdministrativoRequestDto dto) {
        PerfilAdministrativoResponseDto responseDto = service.atualizar(uuid, dto);

        String successMessage = "Perfil administrativo atualizado com sucesso!";
        String details = "Código: " + responseDto.getCodigo() + ", Ativo: " + responseDto.isAtivo();

        return ResponseEntity.ok(new SuccessResponseDto(successMessage, details));
    }
}
