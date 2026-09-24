package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ExampleConstants;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.NecessidadeDePessoalRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.NecessidadeDePessoalResponseDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.SuccessResponseDto;
import com.edufelizardo.maissaudepublica.services.version1.NecessidadeDePessoalService;
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
@RequestMapping(value = "/api/v1/necessidade-de-pessoal/")
@Tag(name = "NecessidadeDePessoal", description = "Endpoints para gerenciar necessidades de pessoal registradas pela Administração e encaminhadas ao RH (ver docs/adr/0036-necessidade-de-pessoal-encaminhada-ao-rh.md).")
public class NecessidadeDePessoalController {

    @Autowired
    private NecessidadeDePessoalService service;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Lista as necessidades de pessoal cadastradas", tags = "NecessidadeDePessoal")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = NecessidadeDePessoalResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "NecessidadeDePessoalResponse",
                    value = ExampleConstants.NECESSIDADE_DE_PESSOAL_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesListagem
    public ResponseEntity<List<NecessidadeDePessoalResponseDto>> getAll() {
        List<NecessidadeDePessoalResponseDto> responseDtos = service.listar();
        if (responseDtos.isEmpty()) {
            throw new ResourceNotFoundException("Nenhum item foi encontrado.");
        }
        return ResponseEntity.ok(responseDtos);
    }

    @GetMapping(value = "{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Busca uma necessidade de pessoal pelo id", tags = "NecessidadeDePessoal")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json",
                    schema = @Schema(implementation = NecessidadeDePessoalResponseDto.class))
    })
    @ApiErrorResponsesBusca
    public ResponseEntity<NecessidadeDePessoalResponseDto> findById(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.buscarPorId(uuid));
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cria uma necessidade de pessoal", tags = "NecessidadeDePessoal")
    @ApiResponse(responseCode = "201", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = SuccessResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "SuccessResponse",
                    value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> create(@Valid @RequestBody NecessidadeDePessoalRequestDto dto) {
        NecessidadeDePessoalResponseDto responseDto = service.criar(dto);

        String successMessage = "Necessidade de pessoal criada com sucesso!";
        String details = "Unidade: " + responseDto.getUnidadeNome() + ", Cargo: " + responseDto.getCargoNome();

        return ResponseEntity.status(HttpStatus.CREATED).body(new SuccessResponseDto(successMessage, details));
    }

    @PatchMapping(value = "{uuid}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Atualiza uma necessidade de pessoal",
            description = "Substitui os campos editáveis por inteiro (unidade, setor, cargo, quantidade, jornada, competências, justificativa). Não altera dataRegistro nem vagaAssociada.",
            tags = "NecessidadeDePessoal")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = SuccessResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "SuccessResponse",
                    value = ExampleConstants.SUCCESS_RESPONSE_UPDATE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> update(@PathVariable UUID uuid, @Valid @RequestBody NecessidadeDePessoalRequestDto dto) {
        NecessidadeDePessoalResponseDto responseDto = service.atualizar(uuid, dto);

        String successMessage = "Necessidade de pessoal atualizada com sucesso!";
        String details = "Unidade: " + responseDto.getUnidadeNome() + ", Cargo: " + responseDto.getCargoNome();

        return ResponseEntity.ok(new SuccessResponseDto(successMessage, details));
    }

    @PatchMapping(value = "{uuid}/vincular-vaga", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Vincula a vaga aberta pelo RH a partir desta necessidade",
            description = "Link informativo, nunca um gatilho automático (ver ADR-0036). Recusa vincular uma necessidade que já tem vaga associada (409).",
            tags = "NecessidadeDePessoal")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = SuccessResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "SuccessResponse",
                    value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> vincularVaga(@PathVariable UUID uuid, @RequestParam UUID vagaId) {
        NecessidadeDePessoalResponseDto responseDto = service.vincularVaga(uuid, vagaId);

        String successMessage = "Necessidade de pessoal vinculada à vaga com sucesso!";
        String details = "Unidade: " + responseDto.getUnidadeNome() + ", Vaga: " + responseDto.getVagaAssociadaUuid();

        return ResponseEntity.ok(new SuccessResponseDto(successMessage, details));
    }
}
