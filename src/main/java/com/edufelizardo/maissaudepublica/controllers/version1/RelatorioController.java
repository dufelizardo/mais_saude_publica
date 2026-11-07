package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.RelatorioCatalogoDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.RelatorioDto;
import com.edufelizardo.maissaudepublica.services.version1.RelatorioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/")
@Tag(name = "Relatórios", description = "Catálogo sobre os números dos domínios, só agregados (ver docs/adr/0167-bi-relatorios.md).")
public class RelatorioController {

    private static final String TAG = "Relatórios";

    @Autowired
    private RelatorioService service;

    @RequerPermissao({"INDICADOR.CONSULTAR"})
    @GetMapping(value = "relatorios", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Catálogo de relatórios", description = "Os relatórios disponíveis, por categoria.", tags = TAG)
    @ApiErrorResponsesListagem
    public ResponseEntity<List<RelatorioCatalogoDto>> catalogo() {
        return ResponseEntity.ok(service.catalogo());
    }

    @RequerPermissao({"INDICADOR.CONSULTAR"})
    @GetMapping(value = "relatorios/{codigo}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Gera o relatório", description = "Resumo e tabelas do período, pelos números de cada domínio. Sem unidade, as unidades do "
            + "acesso. Padrão: o mês corrente; até um ano (400). Código desconhecido: 404.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<RelatorioDto> gerar(@PathVariable String codigo,
                                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
                                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate,
                                              @RequestParam(required = false) UUID unidadeId) {
        return ResponseEntity.ok(service.gerar(codigo, de, ate, unidadeId));
    }

    @RequerPermissao({"INDICADOR.CONSULTAR"})
    @GetMapping(value = "relatorios/{codigo}/exportacao", produces = "text/csv")
    @Operation(summary = "Relatório em CSV", description = "O mesmo relatório, separado por ponto e vírgula. A exportação entra na trilha.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<byte[]> exportacao(@PathVariable String codigo,
                                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
                                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate,
                                             @RequestParam(required = false) UUID unidadeId) {
        byte[] corpo = ("\uFEFF" + service.csv(codigo, de, ate, unidadeId)).getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"relatorio-" + codigo.toLowerCase(Locale.ROOT) + ".csv\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(corpo);
    }
}
