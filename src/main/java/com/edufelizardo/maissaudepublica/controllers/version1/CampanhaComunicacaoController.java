package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.CampanhaComunicacaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EncerramentoCampanhaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.MaterialEducativoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.MotivoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.CampanhaComunicacaoDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.MaterialEducativoDto;
import com.edufelizardo.maissaudepublica.services.version1.CampanhaComunicacaoService;
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
@RequestMapping(value = "/api/v1/comunicacao/")
@Tag(name = "Comunicação", description = "Campanhas de comunicação e material educativo (ver docs/adr/0174-campanhas-de-comunicacao.md).")
public class CampanhaComunicacaoController {

    private static final String TAG = "Comunicação";

    @Autowired
    private CampanhaComunicacaoService service;

    @RequerPermissao({"COMUNICACAO.PUBLICAR", "CONTEUDO.EDITAR", "CONTEUDO.REVISAR"})
    @GetMapping(value = "campanha", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Campanhas de comunicação", description = "Com os conteúdos em vigor, os comunicados e o alcance por canal.", tags = TAG)
    @ApiErrorResponsesListagem
    public ResponseEntity<List<CampanhaComunicacaoDto>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @RequerPermissao({"COMUNICACAO.PUBLICAR"})
    @PostMapping(value = "campanha", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cria uma campanha", description = "Nasce planejada.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<CampanhaComunicacaoDto> criar(@Valid @RequestBody CampanhaComunicacaoRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(dto));
    }

    @RequerPermissao({"COMUNICACAO.PUBLICAR"})
    @PatchMapping(value = "campanha/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Edita a campanha", description = "Só a planejada (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<CampanhaComunicacaoDto> atualizar(@PathVariable UUID uuid, @Valid @RequestBody CampanhaComunicacaoRequestDto dto) {
        return ResponseEntity.ok(service.atualizar(uuid, dto));
    }

    @RequerPermissao({"COMUNICACAO.PUBLICAR"})
    @PostMapping(value = "campanha/{uuid}/inicio", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Inicia a campanha", description = "Ao menos um canal e o período aberto (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<CampanhaComunicacaoDto> iniciar(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.iniciar(uuid));
    }

    @RequerPermissao({"COMUNICACAO.PUBLICAR"})
    @PostMapping(value = "campanha/{uuid}/encerramento", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Encerra a campanha", description = "Com o balanço.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<CampanhaComunicacaoDto> encerrar(@PathVariable UUID uuid, @Valid @RequestBody EncerramentoCampanhaRequestDto dto) {
        return ResponseEntity.ok(service.encerrar(uuid, dto));
    }

    @RequerPermissao({"COMUNICACAO.PUBLICAR"})
    @PostMapping(value = "campanha/{uuid}/cancelamento", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cancela a campanha", description = "Com o motivo.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<CampanhaComunicacaoDto> cancelar(@PathVariable UUID uuid, @Valid @RequestBody MotivoRequestDto dto) {
        return ResponseEntity.ok(service.cancelar(uuid, dto));
    }

    @RequerPermissao({"COMUNICACAO.PUBLICAR", "CONTEUDO.EDITAR", "CONTEUDO.REVISAR"})
    @GetMapping(value = "material", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Materiais do conteúdo", description = "Pelo grupo de versões do conteúdo.", tags = TAG)
    @ApiErrorResponsesListagem
    public ResponseEntity<List<MaterialEducativoDto>> materiais(@RequestParam UUID conteudoGrupoId) {
        return ResponseEntity.ok(service.materiais(conteudoGrupoId));
    }

    @RequerPermissao({"CONTEUDO.EDITAR"})
    @PostMapping(value = "material", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cria um material", description = "Link só https (400).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<MaterialEducativoDto> criarMaterial(@Valid @RequestBody MaterialEducativoRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criarMaterial(dto));
    }

    @RequerPermissao({"CONTEUDO.EDITAR"})
    @PostMapping(value = "material/{uuid}/retirada", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Retira o material", description = "Com o motivo.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<MaterialEducativoDto> retirarMaterial(@PathVariable UUID uuid, @Valid @RequestBody MotivoRequestDto dto) {
        return ResponseEntity.ok(service.retirarMaterial(uuid, dto));
    }
}
