package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.CapacidadeAssistencialRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.CapacidadeAssistencialResponseDto;
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
@RequestMapping(value = "/api/v1/capacidade-assistencial/")
@Tag(name = "Modelo Operacional")
public class CapacidadeAssistencialController {

    @Autowired
    private ModeloOperacionalService service;

    @RequerPermissao({"ORGANIZACAO.GERENCIAR", "ADMINISTRATIVO.CONSULTAR"})
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Lista as capacidades assistenciais", description = "O catálogo, com o perfil (grupo) e em quantas unidades cada uma "
            + "está vigente; com perfilId, só as do perfil.", tags = "Modelo Operacional")
    @ApiErrorResponsesListagem
    public ResponseEntity<List<CapacidadeAssistencialResponseDto>> listar(@RequestParam(required = false) UUID perfilId) {
        return ResponseEntity.ok(service.listarCapacidades(perfilId));
    }

    @RequerPermissao({"ORGANIZACAO.GERENCIAR", "ADMINISTRATIVO.CONSULTAR"})
    @GetMapping(value = "{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Busca uma capacidade assistencial", tags = "Modelo Operacional")
    @ApiErrorResponsesBusca
    public ResponseEntity<CapacidadeAssistencialResponseDto> buscar(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.buscarCapacidade(uuid));
    }

    @RequerPermissao({"ORGANIZACAO.GERENCIAR"})
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cria uma capacidade assistencial", description = "Sem perfil, é transversal. Código repetido: 409; perfil inativo: 422.",
            tags = "Modelo Operacional")
    @ApiErrorResponsesMutacao
    public ResponseEntity<CapacidadeAssistencialResponseDto> criar(@Valid @RequestBody CapacidadeAssistencialRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criarCapacidade(dto));
    }

    @RequerPermissao({"ORGANIZACAO.GERENCIAR"})
    @PatchMapping(value = "{uuid}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Edita uma capacidade assistencial", description = "Corrige os dados ou desativa. O perfil não muda enquanto a "
            + "capacidade estiver vigente em alguma unidade (422).", tags = "Modelo Operacional")
    @ApiErrorResponsesMutacao
    public ResponseEntity<CapacidadeAssistencialResponseDto> atualizar(@PathVariable UUID uuid, @Valid @RequestBody CapacidadeAssistencialRequestDto dto) {
        return ResponseEntity.ok(service.atualizarCapacidade(uuid, dto));
    }
}
