package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.PerfilOperacionalRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.PerfilOperacionalResponseDto;
import com.edufelizardo.maissaudepublica.services.version1.ModeloOperacionalService;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping(value = "/api/v1/perfil-operacional/")
@Tag(name = "Modelo Operacional", description = "Catálogos de perfil operacional e de capacidade assistencial, e o que cada unidade opera e consegue fazer (ver docs/adr/0117-modelo-operacional-perfis-e-capacidades.md).")
public class PerfilOperacionalController {

    @Autowired
    private ModeloOperacionalService service;

    @RequerPermissao({"ORGANIZACAO.GERENCIAR", "ADMINISTRATIVO.CONSULTAR"})
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Lista os perfis operacionais", description = "O catálogo, com quantas capacidades cada perfil agrupa e em quantas "
            + "unidades está vigente.", tags = "Modelo Operacional")
    @ApiErrorResponsesListagem
    public ResponseEntity<List<PerfilOperacionalResponseDto>> listar() {
        return ResponseEntity.ok(service.listarPerfis());
    }

    @RequerPermissao({"ORGANIZACAO.GERENCIAR", "ADMINISTRATIVO.CONSULTAR"})
    @GetMapping(value = "{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Busca um perfil operacional", tags = "Modelo Operacional")
    @ApiErrorResponsesBusca
    public ResponseEntity<PerfilOperacionalResponseDto> buscar(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.buscarPerfil(uuid));
    }

    @RequerPermissao({"ORGANIZACAO.GERENCIAR"})
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cria um perfil operacional", description = "Código repetido: 409. O código é guardado em maiúsculas.", tags = "Modelo Operacional")
    @ApiErrorResponsesMutacao
    public ResponseEntity<PerfilOperacionalResponseDto> criar(@Valid @RequestBody PerfilOperacionalRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criarPerfil(dto));
    }

    @RequerPermissao({"ORGANIZACAO.GERENCIAR"})
    @PatchMapping(value = "{uuid}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Edita um perfil operacional", description = "Corrige código, nome e descrição, ou desativa (ativo=false): um perfil "
            + "inativo não entra em unidade nova, mas os vigentes ficam. Código repetido: 409.", tags = "Modelo Operacional")
    @ApiErrorResponsesMutacao
    public ResponseEntity<PerfilOperacionalResponseDto> atualizar(@PathVariable UUID uuid, @Valid @RequestBody PerfilOperacionalRequestDto dto) {
        return ResponseEntity.ok(service.atualizarPerfil(uuid, dto));
    }
}
