package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AreaGestaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.FimTitularidadeRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.TitularidadeRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AreaGestaoDto;
import com.edufelizardo.maissaudepublica.services.version1.EstruturaDeGestaoService;
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
@RequestMapping(value = "/api/v1/gestao/")
@Tag(name = "Estrutura de gestão", description = "Gerências, coordenações e supervisões, com quem responde (ver docs/adr/0175-coordenacoes-e-gerencia.md).")
public class EstruturaDeGestaoController {

    private static final String TAG = "Estrutura de gestão";

    @Autowired
    private EstruturaDeGestaoService service;

    @RequerPermissao({"ADMINISTRATIVO.CONSULTAR", "ADMINISTRATIVO.GERENCIAR", "INDICADOR.CONSULTAR"})
    @GetMapping(value = "area", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Áreas de gestão", description = "Com quem responde hoje e quantas unidades a regional tem.", tags = TAG)
    @ApiErrorResponsesListagem
    public ResponseEntity<List<AreaGestaoDto>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @RequerPermissao({"ADMINISTRATIVO.CONSULTAR", "ADMINISTRATIVO.GERENCIAR", "INDICADOR.CONSULTAR"})
    @GetMapping(value = "area/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Detalhe da área", description = "Com o histórico de quem respondeu e as unidades da regional.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<AreaGestaoDto> detalhe(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.detalhe(uuid));
    }

    @RequerPermissao({"ADMINISTRATIVO.CONSULTAR", "ADMINISTRATIVO.GERENCIAR", "INDICADOR.CONSULTAR"})
    @GetMapping(value = "regional", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Regionais", description = "As regionais ativas, para a coordenação regional.", tags = TAG)
    @ApiErrorResponsesListagem
    public ResponseEntity<List<AreaGestaoDto.Unidade>> regionais() {
        return ResponseEntity.ok(service.regionais());
    }

    @RequerPermissao({"ADMINISTRATIVO.GERENCIAR"})
    @PostMapping(value = "area", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cria uma área", description = "Hierarquia inválida ou regional que não é regional: 422; regional com coordenação: 409.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<AreaGestaoDto> criar(@Valid @RequestBody AreaGestaoRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(dto));
    }

    @RequerPermissao({"ADMINISTRATIVO.GERENCIAR"})
    @PatchMapping(value = "area/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Edita a área", description = "O tipo não muda (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<AreaGestaoDto> atualizar(@PathVariable UUID uuid, @Valid @RequestBody AreaGestaoRequestDto dto) {
        return ResponseEntity.ok(service.atualizar(uuid, dto));
    }

    @RequerPermissao({"ADMINISTRATIVO.GERENCIAR"})
    @PostMapping(value = "area/{uuid}/titularidade", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Designa quem responde", description = "O anterior no mesmo papel termina no dia antes.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<AreaGestaoDto> designar(@PathVariable UUID uuid, @Valid @RequestBody TitularidadeRequestDto dto) {
        return ResponseEntity.ok(service.designar(uuid, dto));
    }

    @RequerPermissao({"ADMINISTRATIVO.GERENCIAR"})
    @PostMapping(value = "titularidade/{uuid}/encerramento", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Encerra a designação", description = "Sem substituto.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<AreaGestaoDto> encerrar(@PathVariable UUID uuid, @Valid @RequestBody FimTitularidadeRequestDto dto) {
        return ResponseEntity.ok(service.encerrar(uuid, dto));
    }
}
