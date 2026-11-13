package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.RotaPublica;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.EmissaoLaudoDtos;
import com.edufelizardo.maissaudepublica.services.version1.LaudoExameService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Verificação pública do laudo pelo código impresso (ADR-0183), sem login e sem o nome do paciente. */
@RestController
@RequestMapping(value = "/api/v1/publico/laudo")
@Tag(name = "Consulta pública")
public class LaudoPublicoController {

    @Autowired
    private LaudoExameService service;

    @RotaPublica
    @GetMapping(value = "{codigo}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Verifica um laudo pelo código", description = "Sem login. Diz se o laudo é válido ou foi substituído por versão mais nova, "
            + "com a data, a unidade, os exames, o hash do PDF e só as iniciais do paciente. Código desconhecido: 404 (ADR-0183).",
            tags = "Consulta pública")
    @ApiErrorResponsesBusca
    public ResponseEntity<EmissaoLaudoDtos.Verificacao> verificar(@PathVariable String codigo) {
        return ResponseEntity.ok(service.verificar(codigo));
    }
}
