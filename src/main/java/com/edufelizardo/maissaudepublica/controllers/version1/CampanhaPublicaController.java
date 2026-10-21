package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.RotaPublica;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.CampanhaPublicaDto;
import com.edufelizardo.maissaudepublica.services.version1.CampanhaService;
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
@RequestMapping(value = "/api/v1/publico/campanha")
@Tag(name = "Consulta pública")
public class CampanhaPublicaController {

    @Autowired
    private CampanhaService service;

    @RotaPublica
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Campanhas de saúde", description = "Para a landing page, sem login (ADR-0133): as publicadas, em andamento e as "
            + "próximas, com público, período, locais e o andamento da meta. Nenhum dado de paciente.", tags = "Consulta pública")
    @ApiErrorResponsesListagem
    public ResponseEntity<List<CampanhaPublicaDto>> listar() {
        return ResponseEntity.ok(service.publico());
    }
}
