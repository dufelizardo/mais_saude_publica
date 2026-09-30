package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ExampleConstants;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.UsuarioAtualizacaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.UsuarioRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.SuccessResponseDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.UsuarioResponseDto;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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
@RequestMapping(value = "/api/v1/usuario/")
@Tag(name = "Acesso", description = "Permissões, papéis e atribuições de acesso por escopo (ver docs/adr/0066-papeis-permissoes-e-escopo-por-unidade.md).")
public class UsuarioController {

    @Autowired
    private AcessoService service;

    @RequerPermissao({"ACESSO.GERENCIAR", "USUARIO.GERENCIAR"})
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Lista os usuários (sem dado sensível)", tags = "Acesso")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = UsuarioResponseDto.class)))
    })
    @ApiErrorResponsesListagem
    public ResponseEntity<List<UsuarioResponseDto>> findAll() {
        return ResponseEntity.ok(service.listarUsuarios());
    }

    @RequerPermissao({"ACESSO.GERENCIAR", "USUARIO.GERENCIAR"})
    @GetMapping(value = "{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Busca um usuário pelo id", tags = "Acesso")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = UsuarioResponseDto.class))
    })
    @ApiErrorResponsesBusca
    public ResponseEntity<UsuarioResponseDto> findById(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.buscarUsuario(uuid));
    }

    @RequerPermissao({"USUARIO.GERENCIAR"})
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cadastra um usuário",
            description = "CPF válido e único (400/409) e senha inicial de 8 a 72 caracteres. O acesso vem depois, por atribuição.",
            tags = "Acesso")
    @ApiResponse(responseCode = "201", description = "Success:", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = SuccessResponseDto.class),
                    examples = @ExampleObject(name = "Success", summary = "SuccessResponse",
                            value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> create(@Valid @RequestBody UsuarioRequestDto dto) {
        UsuarioResponseDto r = service.criarUsuario(dto);

        String details = "Usuário: " + r.getNome() + ", Id: " + r.getUuid();
        return ResponseEntity.status(HttpStatus.CREATED).body(new SuccessResponseDto("Usuário cadastrado com sucesso!", details));
    }

    @RequerPermissao({"USUARIO.GERENCIAR"})
    @PatchMapping(value = "{uuid}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Atualiza nome e situação de um usuário",
            description = "Desativado, o usuário não entra mais. Ninguém desativa o próprio usuário (422).", tags = "Acesso")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = SuccessResponseDto.class),
                    examples = @ExampleObject(name = "Success", summary = "SuccessResponse",
                            value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> update(@PathVariable UUID uuid, @Valid @RequestBody UsuarioAtualizacaoRequestDto dto) {
        UsuarioResponseDto r = service.atualizarUsuario(uuid, dto);

        String details = "Usuário: " + r.getNome() + ", Ativo: " + r.isAtivo();
        return ResponseEntity.ok(new SuccessResponseDto("Usuário atualizado com sucesso!", details));
    }

    @RequerPermissao({"USUARIO.GERENCIAR"})
    @PostMapping(value = "{uuid}/desbloqueio", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Desbloqueia um usuário bloqueado por tentativas de senha",
            description = "Usuário não bloqueado → 422.", tags = "Acesso")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = SuccessResponseDto.class),
                    examples = @ExampleObject(name = "Success", summary = "SuccessResponse",
                            value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> desbloquear(@PathVariable UUID uuid) {
        UsuarioResponseDto r = service.desbloquearUsuario(uuid);

        return ResponseEntity.ok(new SuccessResponseDto("Usuário desbloqueado com sucesso!", "Usuário: " + r.getNome()));
    }
}
