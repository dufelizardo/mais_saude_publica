package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.LiberadoParaAutenticados;
import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ProcedimentoCatalogoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ProcedimentoCatalogoDto;
import com.edufelizardo.maissaudepublica.services.version1.ProcedimentoCatalogoService;
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
@RequestMapping(value = "/api/v1/")
@Tag(name = "Catálogo de procedimentos", description = "Catálogo geral de procedimentos, de todas as especialidades, com o SIGTAP opcional "
        + "e o complemento odontológico (ver docs/adr/0136-saude-bucal-procedimento-odontologico.md).")
public class ProcedimentoCatalogoController {

    private static final String TAG = "Catálogo de procedimentos";

    @Autowired
    private ProcedimentoCatalogoService service;

    @LiberadoParaAutenticados
    @GetMapping(value = "procedimentos-catalogo", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Catálogo de procedimentos", description = "Ativos e inativos, por nome. Com especialidadeId, os itens dela e os de "
            + "uso geral.", tags = TAG)
    @ApiErrorResponsesListagem
    public ResponseEntity<List<ProcedimentoCatalogoDto>> listar(@RequestParam(required = false) UUID especialidadeId) {
        return ResponseEntity.ok(service.listar(especialidadeId));
    }

    @RequerPermissao({"ORGANIZACAO.GERENCIAR"})
    @PostMapping(value = "procedimento-catalogo", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cadastra um procedimento no catálogo", description = "Código repetido: 409. Dente, face, condição do odontograma e "
            + "exigência de dentista só em especialidade odontológica (422); a condição precisa caber no alcance (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<ProcedimentoCatalogoDto> criar(@Valid @RequestBody ProcedimentoCatalogoRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(dto));
    }

    @RequerPermissao({"ORGANIZACAO.GERENCIAR"})
    @PutMapping(value = "procedimento-catalogo/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Atualiza um procedimento do catálogo", description = "O código não muda (422); desativar o tira de uso sem apagar.",
            tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<ProcedimentoCatalogoDto> atualizar(@PathVariable UUID uuid, @Valid @RequestBody ProcedimentoCatalogoRequestDto dto) {
        return ResponseEntity.ok(service.atualizar(uuid, dto));
    }
}
