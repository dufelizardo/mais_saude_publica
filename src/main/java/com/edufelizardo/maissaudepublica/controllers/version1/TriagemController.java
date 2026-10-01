package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.AuditarLeitura;
import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.RetificacaoTriagemRequestDto;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ExampleConstants;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.TriagemRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.TriagemResponseDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.SuccessResponseDto;
import com.edufelizardo.maissaudepublica.services.version1.TriagemService;
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
@RequestMapping(value = "/api/v1/triagem/")
@Tag(name = "Triagem", description = "Endpoints para gerenciar triagens de enfermagem realizadas durante um atendimento (ver docs/adr/0047-triagem-primeira-entidade-da-enfermagem.md).")
public class TriagemController {

    @Autowired
    private TriagemService service;

    @RequerPermissao({"PRONTUARIO.CONSULTAR"})
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Lista as triagens cadastradas", tags = "Triagem")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = TriagemResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "TriagemResponse",
                    value = ExampleConstants.TRIAGEM_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesListagem
    public ResponseEntity<List<TriagemResponseDto>> getAll() {
        List<TriagemResponseDto> responseDtos = service.listar();
        if (responseDtos.isEmpty()) {
            throw new ResourceNotFoundException("Nenhum item foi encontrado.");
        }
        return ResponseEntity.ok(responseDtos);
    }

    @RequerPermissao({"PRONTUARIO.CONSULTAR"})
    @AuditarLeitura
    @GetMapping(value = "{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Busca uma triagem pelo id", tags = "Triagem")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json",
                    schema = @Schema(implementation = TriagemResponseDto.class))
    })
    @ApiErrorResponsesBusca
    public ResponseEntity<TriagemResponseDto> findById(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.buscarPorId(uuid));
    }

    @RequerPermissao({"TRIAGEM.REGISTRAR"})
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cria uma triagem",
            description = "Registra uma triagem de enfermagem realizada durante um atendimento, com um profissional (por matrícula, ver ADR-0034).",
            tags = "Triagem")
    @ApiResponse(responseCode = "201", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = SuccessResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "SuccessResponse",
                    value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> create(@Valid @RequestBody TriagemRequestDto dto) {
        TriagemResponseDto responseDto = service.criar(dto);

        String successMessage = "Triagem criada com sucesso!";
        String details = "Profissional: " + responseDto.getProfissionalNome() + ", Classificação: " + responseDto.getClassificacaoRisco();

        return ResponseEntity.status(HttpStatus.CREATED).body(new SuccessResponseDto(successMessage, details));
    }

    @RequerPermissao({"TRIAGEM.REGISTRAR"})
    @PostMapping(value = "{uuid}/retificacao", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Retifica uma triagem",
            description = "Registro clínico não é editado (ADR-0062): a retificação grava uma nova versão com os campos "
                    + "corrigidos e o motivo, ligada à anterior, que continua no prontuário. Só a versão vigente pode ser "
                    + "retificada (422); o atendimento precisa ser o mesmo do registro original (400).",
            tags = "Triagem")
    @ApiResponse(responseCode = "201", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = SuccessResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "SuccessResponse",
                    value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> retificar(@PathVariable UUID uuid,
                                                        @Valid @RequestBody RetificacaoTriagemRequestDto dto) {
        TriagemResponseDto responseDto = service.retificar(uuid, dto);

        String details = "Nova versão: " + responseDto.getUuid() + ", Corrige: " + responseDto.getRetificacaoDeUuid();
        return ResponseEntity.status(HttpStatus.CREATED).body(new SuccessResponseDto("Triagem retificada com sucesso!", details));
    }
}
