package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.PapelAtualizacaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.PapelRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.PapelResponseDto;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ExampleConstants;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.SuccessResponseDto;
import com.edufelizardo.maissaudepublica.services.version1.AcessoService;
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
@RequestMapping(value = "/api/v1/papel/")
@Tag(name = "Acesso", description = "Permissões, papéis e atribuições de acesso por escopo (ver docs/adr/0066-papeis-permissoes-e-escopo-por-unidade.md).")
public class PapelController {

    @Autowired
    private AcessoService service;

    @RequerPermissao({"ACESSO.GERENCIAR"})
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Lista os papéis com suas permissões", tags = "Acesso")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = PapelResponseDto.class)))
    })
    @ApiErrorResponsesListagem
    public ResponseEntity<List<PapelResponseDto>> findAll() {
        return ResponseEntity.ok(service.listarPapeis());
    }

    @RequerPermissao({"ACESSO.GERENCIAR"})
    @GetMapping(value = "{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Busca um papel pelo id", tags = "Acesso")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = PapelResponseDto.class))
    })
    @ApiErrorResponsesBusca
    public ResponseEntity<PapelResponseDto> findById(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.buscarPapel(uuid));
    }

    @RequerPermissao({"ACESSO.GERENCIAR"})
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cria um papel",
            description = "Código em maiúsculas, único (409 se repetido); permissões do catálogo (400 se desconhecida).",
            tags = "Acesso")
    @ApiResponse(responseCode = "201", description = "Success:", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = SuccessResponseDto.class),
                    examples = @ExampleObject(name = "Success", summary = "SuccessResponse",
                            value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> create(@Valid @RequestBody PapelRequestDto dto) {
        PapelResponseDto r = service.criarPapel(dto);

        String details = "Papel: " + r.getCodigo() + ", Id: " + r.getUuid();
        return ResponseEntity.status(HttpStatus.CREATED).body(new SuccessResponseDto("Papel criado com sucesso!", details));
    }

    @RequerPermissao({"ACESSO.GERENCIAR"})
    @PatchMapping(value = "{uuid}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Atualiza nome, descrição, situação e permissões de um papel",
            description = "O código não muda. O administrador da plataforma não pode ser desativado nem perder ACESSO.GERENCIAR (422).",
            tags = "Acesso")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = SuccessResponseDto.class),
                    examples = @ExampleObject(name = "Success", summary = "SuccessResponse",
                            value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> update(@PathVariable UUID uuid, @Valid @RequestBody PapelAtualizacaoRequestDto dto) {
        PapelResponseDto r = service.atualizarPapel(uuid, dto);

        String details = "Papel: " + r.getCodigo() + ", Permissões: " + r.getPermissoes().size() + ", Ativo: " + r.isAtivo();
        return ResponseEntity.ok(new SuccessResponseDto("Papel atualizado com sucesso!", details));
    }
}
