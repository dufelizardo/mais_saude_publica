package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ItemComponenteRedeRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ReferenciaRedeRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.RedeTematicaDtos;
import com.edufelizardo.maissaudepublica.models.enuns.ComponenteRedeTematica;
import com.edufelizardo.maissaudepublica.models.enuns.RedeTematica;
import com.edufelizardo.maissaudepublica.services.version1.RedeTematicaService;
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
@RequestMapping(value = "/api/v1/rede/")
@Tag(name = "Gestão da Rede · Redes temáticas", description = "Redes temáticas e referência estrutural (ver docs/adr/0197-redes-tematicas-e-referencia.md).")
public class RedeTematicaController {

    private static final String TAG = "Gestão da Rede · Redes temáticas";
    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;

    @Autowired
    private RedeTematicaService service;

    @RequerPermissao({"REDE.CONSULTAR", "REDE.GERENCIAR"})
    @GetMapping(value = "tematica", produces = JSON)
    @Operation(summary = "Catálogo das redes temáticas", description = "Cada rede com os componentes e os perfis e capacidades ligados.", tags = TAG)
    @ApiErrorResponsesListagem
    public ResponseEntity<List<RedeTematicaDtos.RedeDoCatalogo>> catalogo() {
        return ResponseEntity.ok(service.catalogo());
    }

    @RequerPermissao({"REDE.CONSULTAR", "REDE.GERENCIAR"})
    @GetMapping(value = "tematica/{rede}", produces = JSON)
    @Operation(summary = "Painel da rede temática", description = "Por componente, as unidades do acesso; e as lacunas.", tags = TAG)
    @ApiErrorResponsesListagem
    public ResponseEntity<RedeTematicaDtos.Painel> painel(@PathVariable RedeTematica rede) {
        return ResponseEntity.ok(service.painel(rede));
    }

    @RequerPermissao({"REDE.GERENCIAR"})
    @PostMapping(value = "tematica/componente/{componente}/item", produces = JSON, consumes = JSON)
    @Operation(summary = "Liga um perfil ou uma capacidade ao componente", description = "Só quem gerencia a rede inteira (403); repetido, 409.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<RedeTematicaDtos.Item> ligar(@PathVariable ComponenteRedeTematica componente, @RequestBody ItemComponenteRedeRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.ligar(componente, dto));
    }

    @RequerPermissao({"REDE.GERENCIAR"})
    @DeleteMapping(value = "tematica/item/{uuid}")
    @Operation(summary = "Desliga o item do componente", description = "Só quem gerencia a rede inteira (403).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<Void> desligar(@PathVariable UUID uuid) {
        service.desligar(uuid);
        return ResponseEntity.noContent().build();
    }

    @RequerPermissao({"REDE.CONSULTAR", "REDE.GERENCIAR"})
    @GetMapping(value = "referencia", produces = JSON)
    @Operation(summary = "Referências da rede", description = "As do acesso, com o histórico, e as autorizações fora da referência nos últimos 90 dias.", tags = TAG)
    @ApiErrorResponsesListagem
    public ResponseEntity<RedeTematicaDtos.PainelReferencias> referencias(@RequestParam(required = false) UUID procedimentoId) {
        return ResponseEntity.ok(service.referencias(procedimentoId));
    }

    @RequerPermissao({"REDE.CONSULTAR", "REDE.GERENCIAR", "REGULACAO.REGULAR"})
    @GetMapping(value = "referencia/sugestao", produces = JSON)
    @Operation(summary = "Referência para a autorização", description = "A vigente hoje que cobre a solicitante; campos nulos sem referência.", tags = TAG)
    @ApiErrorResponsesListagem
    public ResponseEntity<RedeTematicaDtos.Sugestao> sugestao(@RequestParam UUID procedimentoId, @RequestParam UUID solicitanteId) {
        return ResponseEntity.ok(service.sugestao(procedimentoId, solicitanteId));
    }

    @RequerPermissao({"REDE.GERENCIAR"})
    @PostMapping(value = "referencia", produces = JSON, consumes = JSON)
    @Operation(summary = "Registra a referência", description = "No escopo da origem; troca encerra a vigente na véspera.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<RedeTematicaDtos.Referencia> registrar(@Valid @RequestBody ReferenciaRedeRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrar(dto));
    }

    @RequerPermissao({"REDE.GERENCIAR"})
    @PostMapping(value = "referencia/{uuid}/encerramento", produces = JSON)
    @Operation(summary = "Encerra a referência", description = "Hoje, sem substituir.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<RedeTematicaDtos.Referencia> encerrar(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.encerrar(uuid));
    }
}
