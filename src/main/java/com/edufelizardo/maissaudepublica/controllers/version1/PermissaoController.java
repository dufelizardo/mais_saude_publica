package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.dtos.version1.response.PermissaoResponseDto;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.services.version1.AcessoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
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
@RequestMapping(value = "/api/v1/permissao/")
@Tag(name = "Acesso", description = "Permissões, papéis e atribuições de acesso por escopo (ver docs/adr/0066-papeis-permissoes-e-escopo-por-unidade.md).")
public class PermissaoController {

    @Autowired
    private AcessoService service;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Lista o catálogo de permissões (RECURSO.ACAO)", tags = "Acesso")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = PermissaoResponseDto.class)))
    })
    @ApiErrorResponsesListagem
    public ResponseEntity<List<PermissaoResponseDto>> findAll() {
        return ResponseEntity.ok(service.listarPermissoes());
    }
}
