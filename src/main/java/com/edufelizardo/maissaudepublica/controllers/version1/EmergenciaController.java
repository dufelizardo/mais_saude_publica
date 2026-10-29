package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AtivacaoPlanoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AvaliacaoImpactoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.DesativacaoPlanoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.PlanoContingenciaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.RevogacaoPlanoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ConversaoEmergenciaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EncerramentoEmergenciaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EventoEmergencialRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.MudancaNivelEmergenciaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.NivelEmergenciaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ReferenciasLegaisEmergenciaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.SituacaoEventoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.TerritorioEmergenciaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.TipoEmergenciaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.EmergenciaDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.EventoEmergencialDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.NivelEmergenciaDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.PlanoContingenciaDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.SugestaoImpactoDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.TipoEmergenciaDto;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoEmergencia;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoEventoEmergencial;
import com.edufelizardo.maissaudepublica.services.version1.EmergenciaService;
import com.edufelizardo.maissaudepublica.services.version1.PlanoContingenciaService;
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
@RequestMapping(value = "/api/v1/")
@Tag(name = "Emergências e Desastres", description = "Evento emergencial, emergência com nível e território, avaliação de impacto "
        + "e plano de contingência (ver docs/adr/0156-emergencias-nucleo.md e docs/adr/0157-emergencias-contingencia.md).")
public class EmergenciaController {

    private static final String TAG = "Emergências e Desastres";

    @Autowired
    private EmergenciaService service;

    @Autowired
    private PlanoContingenciaService planoService;

    // ── Catálogos ──

    @RequerPermissao({"EMERGENCIA.REGISTRAR", "EMERGENCIA.ATIVAR", "CONTINGENCIA.GERENCIAR", "INDICADOR.CONSULTAR"})
    @GetMapping(value = "emergencias/tipos", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Tipos de emergência", description = "O catálogo, com o código COBRADE dos desastres.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<List<TipoEmergenciaDto>> tipos() {
        return ResponseEntity.ok(service.tipos());
    }

    @RequerPermissao({"CONTINGENCIA.GERENCIAR"})
    @PostMapping(value = "emergencias/tipo", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cadastra o tipo de emergência", description = "Código repetido, 409.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<TipoEmergenciaDto> criarTipo(@Valid @RequestBody TipoEmergenciaRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criarTipo(dto));
    }

    @RequerPermissao({"CONTINGENCIA.GERENCIAR"})
    @PutMapping(value = "emergencias/tipo/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Ajusta o tipo de emergência", description = "O código não muda (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<TipoEmergenciaDto> atualizarTipo(@PathVariable UUID uuid, @Valid @RequestBody TipoEmergenciaRequestDto dto) {
        return ResponseEntity.ok(service.atualizarTipo(uuid, dto));
    }

    @RequerPermissao({"EMERGENCIA.REGISTRAR", "EMERGENCIA.ATIVAR", "CONTINGENCIA.GERENCIAR", "INDICADOR.CONSULTAR"})
    @GetMapping(value = "emergencias/niveis", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Níveis de resposta", description = "Em ordem, de 0 a 3 na carga.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<List<NivelEmergenciaDto>> niveis() {
        return ResponseEntity.ok(service.niveis());
    }

    @RequerPermissao({"CONTINGENCIA.GERENCIAR"})
    @PostMapping(value = "emergencias/nivel", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cadastra o nível de resposta", description = "Ordem repetida, 409.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<NivelEmergenciaDto> criarNivel(@Valid @RequestBody NivelEmergenciaRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criarNivel(dto));
    }

    @RequerPermissao({"CONTINGENCIA.GERENCIAR"})
    @PutMapping(value = "emergencias/nivel/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Ajusta o nível de resposta", description = "A ordem não muda (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<NivelEmergenciaDto> atualizarNivel(@PathVariable UUID uuid, @Valid @RequestBody NivelEmergenciaRequestDto dto) {
        return ResponseEntity.ok(service.atualizarNivel(uuid, dto));
    }

    // ── Evento ──

    @RequerPermissao({"EMERGENCIA.REGISTRAR", "EMERGENCIA.ATIVAR", "INDICADOR.CONSULTAR"})
    @GetMapping(value = "eventos-emergenciais", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Eventos emergenciais", description = "Os mais recentes primeiro, pela situação.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<List<EventoEmergencialDto>> eventos(@RequestParam(required = false) SituacaoEventoEmergencial situacao) {
        return ResponseEntity.ok(service.eventos(situacao));
    }

    @RequerPermissao({"EMERGENCIA.REGISTRAR", "EMERGENCIA.ATIVAR", "INDICADOR.CONSULTAR"})
    @GetMapping(value = "evento-emergencial/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Evento emergencial", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<EventoEmergencialDto> evento(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.evento(uuid));
    }

    @RequerPermissao({"EMERGENCIA.REGISTRAR"})
    @PostMapping(value = "evento-emergencial", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Registra o evento emergencial", description = "Tipo inativo, 422; no futuro ou área inválida, 400.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<EventoEmergencialDto> registrarEvento(@Valid @RequestBody EventoEmergencialRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrarEvento(dto));
    }

    @RequerPermissao({"EMERGENCIA.REGISTRAR"})
    @PostMapping(value = "evento-emergencial/{uuid}/situacao", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Monitora ou descarta o evento", description = "Descartar pede o motivo (400); convertido ou descartado, 422.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<EventoEmergencialDto> situacaoDoEvento(@PathVariable UUID uuid, @Valid @RequestBody SituacaoEventoRequestDto dto) {
        return ResponseEntity.ok(service.mudarSituacaoDoEvento(uuid, dto));
    }

    @RequerPermissao({"EMERGENCIA.ATIVAR"})
    @PostMapping(value = "evento-emergencial/{uuid}/emergencia", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Converte o evento em emergência", description = "No nível inicial, com a justificativa. Já convertido, 409; descartado, 422.",
            tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<EmergenciaDto> converter(@PathVariable UUID uuid, @Valid @RequestBody ConversaoEmergenciaRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.converter(uuid, dto));
    }

    // ── Emergência ──

    @RequerPermissao({"EMERGENCIA.REGISTRAR", "EMERGENCIA.ATIVAR", "INDICADOR.CONSULTAR"})
    @GetMapping(value = "emergencias", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Emergências", description = "As mais recentes primeiro, pela situação, com o nível e o último impacto.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<List<EmergenciaDto>> emergencias(@RequestParam(required = false) SituacaoEmergencia situacao) {
        return ResponseEntity.ok(service.emergencias(situacao));
    }

    @RequerPermissao({"EMERGENCIA.REGISTRAR", "EMERGENCIA.ATIVAR", "INDICADOR.CONSULTAR"})
    @GetMapping(value = "emergencia/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Emergência", description = "Com o território, o histórico de níveis e as avaliações de impacto.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<EmergenciaDto> emergencia(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.emergencia(uuid));
    }

    @RequerPermissao({"EMERGENCIA.ATIVAR"})
    @PostMapping(value = "emergencia/{uuid}/nivel", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Muda o nível", description = "Com a justificativa; o mesmo nível ou encerrada, 422.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<EmergenciaDto> mudarNivel(@PathVariable UUID uuid, @Valid @RequestBody MudancaNivelEmergenciaRequestDto dto) {
        return ResponseEntity.ok(service.mudarNivel(uuid, dto));
    }

    @RequerPermissao({"EMERGENCIA.REGISTRAR", "EMERGENCIA.ATIVAR"})
    @PutMapping(value = "emergencia/{uuid}/territorio", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Território afetado", description = "O polígono e as microáreas; sem a lista, as que o polígono atinge.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<EmergenciaDto> territorio(@PathVariable UUID uuid, @Valid @RequestBody TerritorioEmergenciaRequestDto dto) {
        return ResponseEntity.ok(service.definirTerritorio(uuid, dto));
    }

    @RequerPermissao({"EMERGENCIA.ATIVAR"})
    @PutMapping(value = "emergencia/{uuid}/referencias-legais", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "ESPIN e decreto", description = "Registro do ato do poder público; cada um completo ou vazio (400).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<EmergenciaDto> referencias(@PathVariable UUID uuid, @Valid @RequestBody ReferenciasLegaisEmergenciaRequestDto dto) {
        return ResponseEntity.ok(service.registrarReferencias(uuid, dto));
    }

    @RequerPermissao({"EMERGENCIA.ATIVAR"})
    @PostMapping(value = "emergencia/{uuid}/encerramento", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Encerra a emergência", description = "Com o motivo; já encerrada, 422.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<EmergenciaDto> encerrar(@PathVariable UUID uuid, @Valid @RequestBody EncerramentoEmergenciaRequestDto dto) {
        return ResponseEntity.ok(service.encerrar(uuid, dto));
    }

    @RequerPermissao({"EMERGENCIA.REGISTRAR", "EMERGENCIA.ATIVAR", "INDICADOR.CONSULTAR"})
    @GetMapping(value = "emergencia/{uuid}/sugestao-impacto", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Sugestão para a avaliação de impacto", description = "População das microáreas, unidades dentro da área e leitos fora de uso.",
            tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<SugestaoImpactoDto> sugestao(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.sugestao(uuid));
    }

    @RequerPermissao({"EMERGENCIA.REGISTRAR"})
    @PostMapping(value = "emergencia/{uuid}/avaliacao-impacto", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Avaliação de impacto", description = "Uma nova, datada; encerrada, 422.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<EmergenciaDto> avaliar(@PathVariable UUID uuid, @Valid @RequestBody AvaliacaoImpactoRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.avaliar(uuid, dto));
    }

    // ── Planos de contingência (E2, ADR-0157) ──

    @RequerPermissao({"EMERGENCIA.REGISTRAR", "EMERGENCIA.ATIVAR", "CONTINGENCIA.GERENCIAR", "INDICADOR.CONSULTAR"})
    @GetMapping(value = "emergencias/planos", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Planos de contingência", description = "As versões, pelo nome; com vigentes=true só as vigentes; pelo tipo.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<List<PlanoContingenciaDto>> planos(@RequestParam(required = false) Boolean vigentes, @RequestParam(required = false) UUID tipoId) {
        return ResponseEntity.ok(planoService.listar(vigentes, tipoId));
    }

    @RequerPermissao({"EMERGENCIA.REGISTRAR", "EMERGENCIA.ATIVAR", "CONTINGENCIA.GERENCIAR", "INDICADOR.CONSULTAR"})
    @GetMapping(value = "emergencias/plano/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Plano de contingência", description = "A versão com os critérios, as ações, os recursos, os contatos e as demais versões.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<PlanoContingenciaDto> plano(@PathVariable UUID uuid) {
        return ResponseEntity.ok(planoService.buscar(uuid));
    }

    @RequerPermissao({"CONTINGENCIA.GERENCIAR"})
    @PostMapping(value = "emergencias/plano", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cadastra o plano (rascunho)", description = "Código obrigatório (400) e único (409); critério ou ação fora dos níveis, 422.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<PlanoContingenciaDto> criarPlano(@Valid @RequestBody PlanoContingenciaRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(planoService.criar(dto));
    }

    @RequerPermissao({"CONTINGENCIA.GERENCIAR"})
    @PutMapping(value = "emergencias/plano/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Ajusta o rascunho", description = "Fora do rascunho, 422; o código não muda.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<PlanoContingenciaDto> atualizarPlano(@PathVariable UUID uuid, @Valid @RequestBody PlanoContingenciaRequestDto dto) {
        return ResponseEntity.ok(planoService.atualizar(uuid, dto));
    }

    @RequerPermissao({"CONTINGENCIA.GERENCIAR"})
    @PostMapping(value = "emergencias/plano/{uuid}/revisao", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Revisa a versão vigente", description = "Copia para um rascunho novo; não vigente, 422; rascunho já aberto, 409.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<PlanoContingenciaDto> revisarPlano(@PathVariable UUID uuid) {
        return ResponseEntity.status(HttpStatus.CREATED).body(planoService.revisar(uuid));
    }

    @RequerPermissao({"CONTINGENCIA.GERENCIAR"})
    @PostMapping(value = "emergencias/plano/{uuid}/publicacao", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Publica o rascunho", description = "Sem ação prevista ou com a vigência terminada, 422; a vigente anterior passa a substituída.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<PlanoContingenciaDto> publicarPlano(@PathVariable UUID uuid) {
        return ResponseEntity.ok(planoService.publicar(uuid));
    }

    @RequerPermissao({"CONTINGENCIA.GERENCIAR"})
    @PostMapping(value = "emergencias/plano/{uuid}/revogacao", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Revoga a versão", description = "A vigente ou o rascunho, com o motivo; fora de uso, 422.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<PlanoContingenciaDto> revogarPlano(@PathVariable UUID uuid, @Valid @RequestBody RevogacaoPlanoRequestDto dto) {
        return ResponseEntity.ok(planoService.revogar(uuid, dto));
    }

    @RequerPermissao({"EMERGENCIA.ATIVAR"})
    @PostMapping(value = "emergencia/{uuid}/plano-ativo", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Ativa o plano na emergência", description = "Vigente, em vigência, do mesmo tipo e cobrindo o nível atual (422); já ativo, 409.",
            tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<EmergenciaDto> ativarPlano(@PathVariable UUID uuid, @Valid @RequestBody AtivacaoPlanoRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.ativarPlano(uuid, dto));
    }

    @RequerPermissao({"EMERGENCIA.ATIVAR"})
    @PostMapping(value = "emergencia/{uuid}/plano-ativo/{ativacaoId}/desativacao", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Desativa o plano na emergência", description = "Com a justificativa; já desativado, 422.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<EmergenciaDto> desativarPlano(@PathVariable UUID uuid, @PathVariable UUID ativacaoId,
                                                        @Valid @RequestBody DesativacaoPlanoRequestDto dto) {
        return ResponseEntity.ok(service.desativarPlano(uuid, ativacaoId, dto));
    }
}
