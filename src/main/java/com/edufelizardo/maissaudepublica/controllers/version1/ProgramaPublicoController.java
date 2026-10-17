package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.RotaPublica;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ProgramaPublicoDto;
import com.edufelizardo.maissaudepublica.services.version1.ProgramaService;
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
@RequestMapping(value = "/api/v1/publico/programa")
@Tag(name = "Consulta pública")
public class ProgramaPublicoController {

    @Autowired
    private ProgramaService service;

    @RotaPublica
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Programas de saúde ativos", description = "Para a landing page, sem login: nome, categoria, descrição, público e as unidades "
            + "onde cada programa é oferecido. Nenhum dado de paciente.", tags = "Consulta pública")
    @ApiErrorResponsesListagem
    public ResponseEntity<List<ProgramaPublicoDto>> listar() {
        return ResponseEntity.ok(service.publico());
    }
}
