package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.LimiteLeitosRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.LimiteLeitosDto;
import com.edufelizardo.maissaudepublica.services.version1.LimiteLeitosService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/unidade-saude/")
@Tag(name = "Modelo Operacional")
public class LimiteLeitosController {

    @Autowired
    private LimiteLeitosService service;

    @RequerPermissao({"LEITO.AUTORIZAR", "LEITO.GERENCIAR", "ORGANIZACAO.GERENCIAR", "ADMINISTRATIVO.CONSULTAR", "INTERNACAO.CONSULTAR"})
    @GetMapping(value = "id/{uuid}/limites-de-leitos", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Leitos autorizados da unidade", description = "Por capacidade de leitos vigente: ativos, autorizados e o histórico. Sem número, não há limite.",
            tags = "Modelo Operacional")
    @ApiErrorResponsesBusca
    public ResponseEntity<List<LimiteLeitosDto>> listar(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.daUnidade(uuid));
    }

    @RequerPermissao({"LEITO.AUTORIZAR"})
    @PostMapping(value = "id/{uuid}/limites-de-leitos", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Altera o limite de leitos", description = "Com o motivo. Não fica abaixo dos leitos ativos (422).", tags = "Modelo Operacional")
    @ApiErrorResponsesMutacao
    public ResponseEntity<List<LimiteLeitosDto>> alterar(@PathVariable UUID uuid, @Valid @RequestBody LimiteLeitosRequestDto dto) {
        return ResponseEntity.ok(service.alterar(uuid, dto));
    }
}
