package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ExampleConstants;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.SuccessResponseDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ProcedimentoReguladoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ProcedimentoReguladoResponseDto;
import com.edufelizardo.maissaudepublica.services.version1.ProcedimentoReguladoService;
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
@RequestMapping(value = "/api/v1/procedimento-regulado/")
@Tag(name = "Procedimento Regulado", description = "Catálogo do que passa pela Central de Regulação do Acesso: consultas especializadas, exames e procedimentos (ver docs/adr/0087-regulacao-do-acesso-backend.md).")
public class ProcedimentoReguladoController {

    @Autowired
    private ProcedimentoReguladoService service;

    @RequerPermissao({"REGULACAO.REGULAR"})
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cadastra um procedimento regulado",
            description = "Nome único (409 se repetido). Sem ativo, nasce ativo.", tags = "Procedimento Regulado")
    @ApiResponse(responseCode = "201", description = "Success:", content = {
            @Content(mediaType = "application/json",
                    schema = @Schema(implementation = SuccessResponseDto.class),
                    examples = @ExampleObject(name = "Success", summary = "SuccessResponse",
                            value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> create(@Valid @RequestBody ProcedimentoReguladoRequestDto dto) {
        ProcedimentoReguladoResponseDto criado = service.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(new SuccessResponseDto("Procedimento regulado cadastrado com sucesso!",
                "Id: " + criado.getUuid() + ", Nome: " + criado.getNome() + ", Tipo: " + criado.getTipo()));
    }

    @RequerPermissao({"REGULACAO.REGULAR"})
    @PatchMapping(value = "{uuid}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Edita um procedimento regulado",
            description = "Nome, tipo e ativo. Tirar de uso não afeta as solicitações já feitas.", tags = "Procedimento Regulado")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json",
                    schema = @Schema(implementation = SuccessResponseDto.class),
                    examples = @ExampleObject(name = "Success", summary = "SuccessResponse",
                            value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> update(@PathVariable UUID uuid, @Valid @RequestBody ProcedimentoReguladoRequestDto dto) {
        ProcedimentoReguladoResponseDto atualizado = service.atualizar(uuid, dto);
        return ResponseEntity.ok(new SuccessResponseDto("Procedimento regulado atualizado com sucesso!",
                "Id: " + atualizado.getUuid() + ", Nome: " + atualizado.getNome() + ", Ativo: " + atualizado.isAtivo()));
    }

    @RequerPermissao({"REGULACAO.CONSULTAR", "REGULACAO.SOLICITAR", "REGULACAO.REGULAR"})
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Lista o catálogo em ordem alfabética",
            description = "ativos=true traz só os que estão em uso (o seletor da nova solicitação).", tags = "Procedimento Regulado")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = ProcedimentoReguladoResponseDto.class)))
    })
    @ApiErrorResponsesListagem
    public ResponseEntity<List<ProcedimentoReguladoResponseDto>> findAll(@RequestParam(defaultValue = "false") boolean ativos) {
        return ResponseEntity.ok(service.listar(ativos));
    }

    @RequerPermissao({"REGULACAO.CONSULTAR", "REGULACAO.SOLICITAR", "REGULACAO.REGULAR"})
    @GetMapping(value = "{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Busca um procedimento regulado pelo id", tags = "Procedimento Regulado")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = ProcedimentoReguladoResponseDto.class))
    })
    @ApiErrorResponsesBusca
    public ResponseEntity<ProcedimentoReguladoResponseDto> findById(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.buscarPorId(uuid));
    }
}
