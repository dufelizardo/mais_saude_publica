package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.CoberturaRedeDto;
import com.edufelizardo.maissaudepublica.services.version1.CoberturaRedeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/rede/")
@Tag(name = "Gestão da Rede · Cobertura", description = "Cobertura da rede e indicadores de pactuação (ver docs/adr/0198-cobertura-da-rede.md).")
public class CoberturaRedeController {

    @Autowired
    private CoberturaRedeService service;

    @RequerPermissao({"REDE.CONSULTAR", "REDE.GERENCIAR"})
    @GetMapping(value = "cobertura", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cobertura da rede na competência",
            description = "Por procedimento com oferta: pactuação, uso, extracotas, fora da referência, fila e esperas; por origem: população adscrita, "
                    + "cota por 1.000, demanda, saldo, referência e alertas; e as lacunas das redes temáticas.",
            tags = "Gestão da Rede · Cobertura")
    @ApiErrorResponsesListagem
    public ResponseEntity<CoberturaRedeDto> cobertura(@RequestParam(required = false) YearMonth competencia, @RequestParam(required = false) UUID procedimentoId) {
        return ResponseEntity.ok(service.painel(competencia, procedimentoId));
    }
}
