package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.TipoServicoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.TipoServicoDto;
import com.edufelizardo.maissaudepublica.services.version1.ServicoUnidadeService;
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
@RequestMapping(value = "/api/v1/tipo-servico/")
@Tag(name = "Modelo Operacional")
public class TipoServicoController {

    @Autowired
    private ServicoUnidadeService service;

    @RequerPermissao({"ORGANIZACAO.GERENCIAR", "ADMINISTRATIVO.CONSULTAR"})
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Lista os tipos de serviço", description = "O catálogo, com a capacidade exigida e quantos serviços vigentes usam cada tipo.",
            tags = "Modelo Operacional")
    @ApiErrorResponsesListagem
    public ResponseEntity<List<TipoServicoDto>> listar() {
        return ResponseEntity.ok(service.tipos());
    }

    @RequerPermissao({"ORGANIZACAO.GERENCIAR"})
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cria um tipo de serviço", description = "Código repetido: 409; capacidade inativa: 422.", tags = "Modelo Operacional")
    @ApiErrorResponsesMutacao
    public ResponseEntity<TipoServicoDto> criar(@Valid @RequestBody TipoServicoRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criarTipo(dto));
    }

    @RequerPermissao({"ORGANIZACAO.GERENCIAR"})
    @PatchMapping(value = "{uuid}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Edita um tipo de serviço", description = "A capacidade não muda com serviço vigente do tipo (422).", tags = "Modelo Operacional")
    @ApiErrorResponsesMutacao
    public ResponseEntity<TipoServicoDto> atualizar(@PathVariable UUID uuid, @Valid @RequestBody TipoServicoRequestDto dto) {
        return ResponseEntity.ok(service.atualizarTipo(uuid, dto));
    }
}
