package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ExampleConstants;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.PerfilPorTipoUnidadeRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.PerfilPorTipoUnidadeResponseDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.SuccessResponseDto;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.services.version1.PerfilPorTipoUnidadeService;
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
@RequestMapping(value = "/api/v1/perfil-por-tipo-unidade/")
@Tag(name = "PerfilPorTipoUnidade", description = "Endpoints para configurar qual Perfil Administrativo vale para cada tipo de unidade de saúde (ver docs/adr/0031-perfil-administrativo-por-tipo-de-unidade.md).")
public class PerfilPorTipoUnidadeController {

    @Autowired
    private PerfilPorTipoUnidadeService service;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Lista as associações perfil/tipo de unidade cadastradas", tags = "PerfilPorTipoUnidade")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = PerfilPorTipoUnidadeResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "PerfilPorTipoUnidadeResponse",
                    value = ExampleConstants.PERFIL_POR_TIPO_UNIDADE_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesListagem
    public ResponseEntity<List<PerfilPorTipoUnidadeResponseDto>> getAll() {
        List<PerfilPorTipoUnidadeResponseDto> responseDtos = service.listar();
        if (responseDtos.isEmpty()) {
            throw new ResourceNotFoundException("Nenhum item foi encontrado.");
        }
        return ResponseEntity.ok(responseDtos);
    }

    @GetMapping(value = "{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Busca uma associação perfil/tipo de unidade pelo id", tags = "PerfilPorTipoUnidade")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json",
                    schema = @Schema(implementation = PerfilPorTipoUnidadeResponseDto.class))
    })
    @ApiErrorResponsesBusca
    public ResponseEntity<PerfilPorTipoUnidadeResponseDto> findById(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.buscarPorId(uuid));
    }

    @GetMapping(value = "tipo/{tipo}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Resolve o perfil administrativo configurado para um tipo de unidade", tags = "PerfilPorTipoUnidade")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json",
                    schema = @Schema(implementation = PerfilPorTipoUnidadeResponseDto.class))
    })
    @ApiErrorResponsesBusca
    public ResponseEntity<PerfilPorTipoUnidadeResponseDto> findByTipo(@PathVariable TipoUnidadeDeSaude tipo) {
        return ResponseEntity.ok(service.buscarPorTipo(tipo));
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Associa um perfil administrativo a um tipo de unidade", tags = "PerfilPorTipoUnidade")
    @ApiResponse(responseCode = "201", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = SuccessResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "SuccessResponse",
                    value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> create(@Valid @RequestBody PerfilPorTipoUnidadeRequestDto dto) {
        PerfilPorTipoUnidadeResponseDto responseDto = service.criar(dto);

        String successMessage = "Associação perfil/tipo de unidade criada com sucesso!";
        String details = "Tipo: " + responseDto.getTipo() + ", Perfil: " + responseDto.getPerfilAdministrativoCodigo();

        return ResponseEntity.status(HttpStatus.CREATED).body(new SuccessResponseDto(successMessage, details));
    }

    @PatchMapping(value = "{uuid}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Atualiza uma associação perfil/tipo de unidade",
            description = "Substitui o perfil associado a um tipo (ex.: trocar qual perfil vale para UBS).",
            tags = "PerfilPorTipoUnidade")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = SuccessResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "SuccessResponse",
                    value = ExampleConstants.SUCCESS_RESPONSE_UPDATE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> update(@PathVariable UUID uuid, @Valid @RequestBody PerfilPorTipoUnidadeRequestDto dto) {
        PerfilPorTipoUnidadeResponseDto responseDto = service.atualizar(uuid, dto);

        String successMessage = "Associação perfil/tipo de unidade atualizada com sucesso!";
        String details = "Tipo: " + responseDto.getTipo() + ", Perfil: " + responseDto.getPerfilAdministrativoCodigo();

        return ResponseEntity.ok(new SuccessResponseDto(successMessage, details));
    }
}
