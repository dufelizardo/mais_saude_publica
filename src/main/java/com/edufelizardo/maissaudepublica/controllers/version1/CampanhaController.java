package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.CampanhaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.LocalCampanhaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.CampanhaDto;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoCampanha;
import com.edufelizardo.maissaudepublica.services.version1.CampanhaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/")
@Tag(name = "Campanhas", description = "Campanhas de saúde: período, público, meta, locais, publicação e cancelamento "
        + "(ver docs/adr/0133-programas-campanhas.md).")
public class CampanhaController {

    private static final String TAG = "Campanhas";

    @Autowired
    private CampanhaService service;

    @RequerPermissao({"PROGRAMA.GERENCIAR", "PROGRAMA.ACOMPANHAR"})
    @GetMapping(value = "campanhas", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Campanhas", description = "Em andamento primeiro. Filtros: situacao (pelas datas, ou cancelada) e programaId.", tags = TAG)
    @ApiErrorResponsesListagem
    public ResponseEntity<List<CampanhaDto>> listar(@RequestParam(required = false) SituacaoCampanha situacao,
                                                    @RequestParam(required = false) UUID programaId) {
        return ResponseEntity.ok(service.listar(situacao, programaId));
    }

    @RequerPermissao({"PROGRAMA.GERENCIAR", "PROGRAMA.ACOMPANHAR"})
    @GetMapping(value = "campanha/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Detalhe da campanha", description = "Com os locais e o andamento da meta (o atendido nas ações realizadas).", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<CampanhaDto> buscar(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.buscar(uuid));
    }

    @RequerPermissao({"PROGRAMA.GERENCIAR"})
    @PostMapping(value = "campanha", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cadastra a campanha", description = "Fim antes do início, idade máxima menor que a mínima ou condição inválida: 400.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<CampanhaDto> criar(@Valid @RequestBody CampanhaRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(dto));
    }

    @RequerPermissao({"PROGRAMA.GERENCIAR"})
    @PutMapping(value = "campanha/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Atualiza a campanha", description = "Cancelada: 422; local fora do período novo: 422.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<CampanhaDto> atualizar(@PathVariable UUID uuid, @Valid @RequestBody CampanhaRequestDto dto) {
        return ResponseEntity.ok(service.atualizar(uuid, dto));
    }

    @RequerPermissao({"PROGRAMA.GERENCIAR"})
    @PostMapping(value = "campanha/{uuid}/publicacao", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Publica ou retira da consulta pública", description = "Corpo: {\"publicada\": true|false}. Cancelada: 422.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<CampanhaDto> publicar(@PathVariable UUID uuid, @RequestBody Map<String, Boolean> corpo) {
        return ResponseEntity.ok(service.publicar(uuid, Boolean.TRUE.equals(corpo.get("publicada"))));
    }

    @RequerPermissao({"PROGRAMA.GERENCIAR"})
    @PostMapping(value = "campanha/{uuid}/cancelamento", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cancela a campanha", description = "Corpo: {\"motivo\": \"...\"} (400 sem motivo). Sai da consulta pública e as ações "
            + "pendentes são canceladas.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<CampanhaDto> cancelar(@PathVariable UUID uuid, @RequestBody Map<String, String> corpo) {
        return ResponseEntity.ok(service.cancelar(uuid, corpo.get("motivo")));
    }

    @RequerPermissao({"PROGRAMA.GERENCIAR"})
    @PostMapping(value = "campanha/{uuid}/local", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Inclui um local", description = "Unidade da rede (a mesma duas vezes: 409) ou local temporário com nome (400) sob a "
            + "unidade responsável. Período dentro do da campanha (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<CampanhaDto> incluirLocal(@PathVariable UUID uuid, @Valid @RequestBody LocalCampanhaRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.incluirLocal(uuid, dto));
    }

    @RequerPermissao({"PROGRAMA.GERENCIAR"})
    @DeleteMapping(value = "campanha-local/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Retira um local", description = "Com ações registradas nele: 409.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<CampanhaDto> removerLocal(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.removerLocal(uuid));
    }
}
