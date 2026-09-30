package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AtribuicaoAcessoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.RevogacaoAcessoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AtribuicaoAcessoResponseDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.EscopoAcessoResponseDto;
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
@RequestMapping(value = "/api/v1/atribuicao-acesso/")
@Tag(name = "Acesso", description = "Permissões, papéis e atribuições de acesso por escopo (ver docs/adr/0066-papeis-permissoes-e-escopo-por-unidade.md).")
public class AtribuicaoAcessoController {

    @Autowired
    private AcessoService service;

    @RequerPermissao({"ACESSO.GERENCIAR"})
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Lista as atribuições de acesso, da mais recente para a mais antiga",
            description = "Filtro opcional por usuarioId. Revogadas continuam na lista, marcadas (vigente = false).",
            tags = "Acesso")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = AtribuicaoAcessoResponseDto.class)))
    })
    @ApiErrorResponsesListagem
    public ResponseEntity<List<AtribuicaoAcessoResponseDto>> findAll(@RequestParam(required = false) UUID usuarioId) {
        return ResponseEntity.ok(service.listarAtribuicoes(usuarioId));
    }

    @RequerPermissao({"ACESSO.GERENCIAR"})
    @GetMapping(value = "escopos", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Lista as unidades em que um acesso pode ser concedido",
            description = "Todos os níveis da hierarquia (Federal a UBS), só os que estão no escopo de quem consulta.",
            tags = "Acesso")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = EscopoAcessoResponseDto.class)))
    })
    @ApiErrorResponsesListagem
    public ResponseEntity<List<EscopoAcessoResponseDto>> escopos() {
        return ResponseEntity.ok(service.listarEscopos());
    }

    @RequerPermissao({"ACESSO.GERENCIAR"})
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Concede um papel a um usuário num escopo",
            description = "Sem unidade = rede inteira; a unidade vale também para as que estão abaixo dela. Período opcional "
                    + "(fim antes do início → 400). Papel inativo → 422; mesmo papel no mesmo escopo já vigente → 409.",
            tags = "Acesso")
    @ApiResponse(responseCode = "201", description = "Success:", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = SuccessResponseDto.class),
                    examples = @ExampleObject(name = "Success", summary = "SuccessResponse",
                            value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> create(@Valid @RequestBody AtribuicaoAcessoRequestDto dto) {
        AtribuicaoAcessoResponseDto r = service.conceder(dto);

        String details = "Usuário: " + r.getUsuarioNome() + ", Papel: " + r.getPapelCodigo()
                + ", Escopo: " + (r.getUnidadeNome() != null ? r.getUnidadeNome() : "rede inteira") + ", Id: " + r.getUuid();
        return ResponseEntity.status(HttpStatus.CREATED).body(new SuccessResponseDto("Acesso concedido com sucesso!", details));
    }

    @RequerPermissao({"ACESSO.GERENCIAR"})
    @PostMapping(value = "{uuid}/revogacao", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Revoga um acesso",
            description = "Não apaga: guarda motivo, autor e hora. Já revogado → 422.", tags = "Acesso")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = SuccessResponseDto.class),
                    examples = @ExampleObject(name = "Success", summary = "SuccessResponse",
                            value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> revogar(@PathVariable UUID uuid, @Valid @RequestBody RevogacaoAcessoRequestDto dto) {
        AtribuicaoAcessoResponseDto r = service.revogar(uuid, dto);

        String details = "Usuário: " + r.getUsuarioNome() + ", Papel: " + r.getPapelCodigo() + ", Motivo: " + r.getMotivoRevogacao();
        return ResponseEntity.ok(new SuccessResponseDto("Acesso revogado com sucesso!", details));
    }
}
