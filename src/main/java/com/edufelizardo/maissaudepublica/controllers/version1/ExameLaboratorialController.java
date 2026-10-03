package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ExampleConstants;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.SuccessResponseDto;
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
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ExameLaboratorialRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ExameLaboratorialResponseDto;
import com.edufelizardo.maissaudepublica.services.version1.ExameLaboratorialService;

@RestController
@RequestMapping(value = "/api/v1/exame-laboratorial/")
@Tag(name = "Exame Laboratorial", description = "Catálogo de exames do laboratório assistencial: material, preparo, prazo e resultado numérico (com faixa de referência) ou em texto (ver docs/adr/0093-laboratorio-backend.md).")
public class ExameLaboratorialController {

    @Autowired
    private ExameLaboratorialService service;

    @RequerPermissao({"LABORATORIO.GERENCIAR"})
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cadastra um exame no catálogo", description = "Nome único (409). Mínima maior que a máxima: 400.", tags = "Exame Laboratorial")
    @ApiResponse(responseCode = "201", description = "Success:", content = {
            @Content(mediaType = "application/json",
                    schema = @Schema(implementation = SuccessResponseDto.class),
                    examples = @ExampleObject(name = "Success", summary = "SuccessResponse",
                            value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> create(@Valid @RequestBody ExameLaboratorialRequestDto dto) {
        ExameLaboratorialResponseDto e = service.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(new SuccessResponseDto("Exame cadastrado com sucesso!",
                "Id: " + e.getUuid() + ", Nome: " + e.getNome() + ", Material: " + e.getMaterial()));
    }

    @RequerPermissao({"LABORATORIO.GERENCIAR"})
    @PatchMapping(value = "{uuid}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Edita um exame do catálogo", description = "Resultados já registrados guardam a referência da época.", tags = "Exame Laboratorial")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json",
                    schema = @Schema(implementation = SuccessResponseDto.class),
                    examples = @ExampleObject(name = "Success", summary = "SuccessResponse",
                            value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> update(@PathVariable UUID uuid, @Valid @RequestBody ExameLaboratorialRequestDto dto) {
        ExameLaboratorialResponseDto e = service.atualizar(uuid, dto);
        return ResponseEntity.ok(new SuccessResponseDto("Exame atualizado com sucesso!", "Id: " + e.getUuid() + ", Ativo: " + e.isAtivo()));
    }

    @RequerPermissao({"EXAME.SOLICITAR", "LABORATORIO.COLETAR", "LABORATORIO.ANALISAR", "LABORATORIO.LIBERAR", "LABORATORIO.GERENCIAR"})
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Lista o catálogo em ordem alfabética", description = "ativos=true traz só os em uso.", tags = "Exame Laboratorial")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ExameLaboratorialResponseDto.class)))
    })
    @ApiErrorResponsesListagem
    public ResponseEntity<List<ExameLaboratorialResponseDto>> findAll(@RequestParam(defaultValue = "false") boolean ativos) {
        return ResponseEntity.ok(service.listar(ativos));
    }

    @RequerPermissao({"EXAME.SOLICITAR", "LABORATORIO.COLETAR", "LABORATORIO.ANALISAR", "LABORATORIO.LIBERAR", "LABORATORIO.GERENCIAR"})
    @GetMapping(value = "{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Busca um exame do catálogo", tags = "Exame Laboratorial")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = ExameLaboratorialResponseDto.class))
    })
    @ApiErrorResponsesBusca
    public ResponseEntity<ExameLaboratorialResponseDto> findById(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.buscarPorId(uuid));
    }
}
