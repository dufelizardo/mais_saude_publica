package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.RotaPublica;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ProcessoSanitarioDtos;
import com.edufelizardo.maissaudepublica.services.version1.ProcessoSanitarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Verificação pública dos termos e da licença sanitária (ADR-0200), sem login e sem dado de pessoa. */
@RestController
@RequestMapping(value = "/api/v1/publico/visa")
@Tag(name = "Consulta pública")
public class VisaPublicoController {

    @Autowired
    private ProcessoSanitarioService service;

    @RotaPublica
    @GetMapping(value = "verificacao/{codigo}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Verifica um termo sanitário pelo código", description = "Sem login: tipo, número, data, estabelecimento e se o arquivo confere "
            + "com o hash. Código desconhecido: 404 (ADR-0200).", tags = "Consulta pública")
    @ApiErrorResponsesBusca
    public ResponseEntity<ProcessoSanitarioDtos.Verificacao> verificar(@PathVariable String codigo) {
        return ResponseEntity.ok(service.verificar(codigo));
    }

    @RotaPublica
    @GetMapping(value = "licenca", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Verifica a licença sanitária pelo número", description = "Sem login: estabelecimento, validade e situação. Número desconhecido: 404.",
            tags = "Consulta pública")
    @ApiErrorResponsesBusca
    public ResponseEntity<ProcessoSanitarioDtos.VerificacaoLicenca> verificarLicenca(@RequestParam String numero) {
        return ResponseEntity.ok(service.verificarLicenca(numero));
    }
}
