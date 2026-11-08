package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.RotaPublica;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ComunicadoPublicoDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ConteudoPublicoDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.CampanhaComunicacaoPublicaDto;
import com.edufelizardo.maissaudepublica.services.version1.CampanhaComunicacaoService;
import com.edufelizardo.maissaudepublica.services.version1.ComunicacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/publico/comunicacao")
@Tag(name = "Consulta pública")
public class ComunicacaoPublicaController {

    @Autowired
    private ComunicacaoService service;

    @Autowired
    private CampanhaComunicacaoService campanhaService;

    @RotaPublica
    @GetMapping(value = "/campanhas", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Campanhas de comunicação", description = "Para a landing page, sem login (ADR-0174): as campanhas em andamento com o portal, "
            + "com os conteúdos publicados e os materiais. Nenhum dado de pessoa.", tags = "Consulta pública")
    @ApiErrorResponsesListagem
    public ResponseEntity<List<CampanhaComunicacaoPublicaDto>> campanhas() {
        return ResponseEntity.ok(campanhaService.publicas());
    }

    @RotaPublica
    @GetMapping(value = "/avisos", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Avisos", description = "Para a landing page, sem login (ADR-0168): os comunicados vigentes à população, com o emergencial "
            + "primeiro. Nenhum dado de pessoa.", tags = "Consulta pública")
    @ApiErrorResponsesListagem
    public ResponseEntity<List<ComunicadoPublicoDto>> avisos() {
        return ResponseEntity.ok(service.comunicadosPublicos());
    }

    @RotaPublica
    @GetMapping(value = "/orientacoes", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Orientações de saúde", description = "Para a landing page, sem login (ADR-0168): os conteúdos publicados e dentro da validade.",
            tags = "Consulta pública")
    @ApiErrorResponsesListagem
    public ResponseEntity<List<ConteudoPublicoDto>> orientacoes() {
        return ResponseEntity.ok(service.conteudosPublicos());
    }
}
