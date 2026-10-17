package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.LiberadoParaAutenticados;
import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.CategoriaProgramaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.CategoriaProgramaDto;
import com.edufelizardo.maissaudepublica.services.version1.ProgramaService;
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
@RequestMapping(value = "/api/v1/categoria-programa/")
@Tag(name = "Programas de Saúde", description = "Catálogo de programas de saúde: categorias, programas com o público-alvo e as unidades que os oferecem (ver docs/adr/0118-programas-catalogo.md).")
public class CategoriaProgramaController {

    @Autowired
    private ProgramaService service;

    @LiberadoParaAutenticados
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Lista as categorias de programas", description = "Por ordem, com quantos programas cada uma tem. Aberto a quem está logado.",
            tags = "Programas de Saúde")
    @ApiErrorResponsesListagem
    public ResponseEntity<List<CategoriaProgramaDto>> listar() {
        return ResponseEntity.ok(service.categorias());
    }

    @RequerPermissao({"PROGRAMA.GERENCIAR"})
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cria uma categoria", description = "Nome repetido: 409. Sem ordem, vai para o fim.", tags = "Programas de Saúde")
    @ApiErrorResponsesMutacao
    public ResponseEntity<CategoriaProgramaDto> criar(@Valid @RequestBody CategoriaProgramaRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criarCategoria(dto));
    }

    @RequerPermissao({"PROGRAMA.GERENCIAR"})
    @PatchMapping(value = "{uuid}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Edita uma categoria", description = "Renomeia, reordena ou desativa (não recebe programa novo). Nome repetido: 409.",
            tags = "Programas de Saúde")
    @ApiErrorResponsesMutacao
    public ResponseEntity<CategoriaProgramaDto> atualizar(@PathVariable UUID uuid, @Valid @RequestBody CategoriaProgramaRequestDto dto) {
        return ResponseEntity.ok(service.atualizarCategoria(uuid, dto));
    }
}
