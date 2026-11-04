package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AcaoEmergencialRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AgravoEmergenciaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ExposicaoAmbientalRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.PainelEpidemiaDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.BoletimEstruturaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EstruturaTemporariaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EventoMassaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.InicioOperacaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.SituacaoEstruturaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.SituacaoAcaoEmergencialRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.MobilizacaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.SituacaoMobilizacaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.TransferenciaMobilizacaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.CapacidadeEmergencialRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AtivacaoCapacidadeRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.DesativacaoCapacidadeRequestDto;
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
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.CapacidadeEmergencialDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.EmergenciaDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.RelatorioPosEventoDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.CancelamentoAcoesRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.DesmobilizacaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.RelatorioPosEventoRequestDto;
import org.springframework.http.HttpHeaders;
import java.nio.charset.StandardCharsets;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.EventoEmergencialDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.NivelEmergenciaDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.PlanoContingenciaDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.SugestaoImpactoDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.TipoEmergenciaDto;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoEmergencia;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoEventoEmergencial;
import com.edufelizardo.maissaudepublica.services.version1.CapacidadeEmergencialService;
import com.edufelizardo.maissaudepublica.services.version1.EmergenciaService;
import com.edufelizardo.maissaudepublica.services.version1.RelatorioPosEventoService;
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
        + "plano de contingência, coordenação, desmobilização e relatório pós-evento (ver as ADRs 0156 a 0159).")
public class EmergenciaController {

    private static final String TAG = "Emergências e Desastres";

    @Autowired
    private EmergenciaService service;

    @Autowired
    private PlanoContingenciaService planoService;

    @Autowired
    private CapacidadeEmergencialService capacidadeService;

    @Autowired
    private RelatorioPosEventoService relatorioService;

    // ── Catálogos ──

    @RequerPermissao({"EMERGENCIA.REGISTRAR", "EMERGENCIA.ATIVAR", "EMERGENCIA.COORDENAR", "CONTINGENCIA.GERENCIAR", "INDICADOR.CONSULTAR"})
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

    @RequerPermissao({"EMERGENCIA.REGISTRAR", "EMERGENCIA.ATIVAR", "EMERGENCIA.COORDENAR", "CONTINGENCIA.GERENCIAR", "INDICADOR.CONSULTAR"})
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

    @RequerPermissao({"EMERGENCIA.REGISTRAR", "EMERGENCIA.ATIVAR", "EMERGENCIA.COORDENAR", "CONTINGENCIA.GERENCIAR", "INDICADOR.CONSULTAR"})
    @GetMapping(value = "eventos-emergenciais", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Eventos emergenciais", description = "Os mais recentes primeiro, pela situação.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<List<EventoEmergencialDto>> eventos(@RequestParam(required = false) SituacaoEventoEmergencial situacao) {
        return ResponseEntity.ok(service.eventos(situacao));
    }

    @RequerPermissao({"EMERGENCIA.REGISTRAR", "EMERGENCIA.ATIVAR", "EMERGENCIA.COORDENAR", "CONTINGENCIA.GERENCIAR", "INDICADOR.CONSULTAR"})
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

    @RequerPermissao({"EMERGENCIA.REGISTRAR", "EMERGENCIA.ATIVAR", "EMERGENCIA.COORDENAR", "CONTINGENCIA.GERENCIAR", "INDICADOR.CONSULTAR"})
    @GetMapping(value = "emergencias", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Emergências", description = "As mais recentes primeiro, pela situação, com o nível e o último impacto.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<List<EmergenciaDto>> emergencias(@RequestParam(required = false) SituacaoEmergencia situacao) {
        return ResponseEntity.ok(service.emergencias(situacao));
    }

    @RequerPermissao({"EMERGENCIA.REGISTRAR", "EMERGENCIA.ATIVAR", "EMERGENCIA.COORDENAR", "CONTINGENCIA.GERENCIAR", "INDICADOR.CONSULTAR"})
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

    @RequerPermissao({"EMERGENCIA.REGISTRAR", "EMERGENCIA.ATIVAR", "EMERGENCIA.COORDENAR", "CONTINGENCIA.GERENCIAR", "INDICADOR.CONSULTAR"})
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

    @RequerPermissao({"EMERGENCIA.REGISTRAR", "EMERGENCIA.ATIVAR", "EMERGENCIA.COORDENAR", "CONTINGENCIA.GERENCIAR", "INDICADOR.CONSULTAR"})
    @GetMapping(value = "emergencias/planos", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Planos de contingência", description = "As versões, pelo nome; com vigentes=true só as vigentes; pelo tipo.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<List<PlanoContingenciaDto>> planos(@RequestParam(required = false) Boolean vigentes, @RequestParam(required = false) UUID tipoId) {
        return ResponseEntity.ok(planoService.listar(vigentes, tipoId));
    }

    @RequerPermissao({"EMERGENCIA.REGISTRAR", "EMERGENCIA.ATIVAR", "EMERGENCIA.COORDENAR", "CONTINGENCIA.GERENCIAR", "INDICADOR.CONSULTAR"})
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

    // ── Coordenação: ações, mobilização e capacidade (E3, ADR-0158) ──

    @RequerPermissao({"EMERGENCIA.COORDENAR"})
    @PostMapping(value = "emergencia/{uuid}/acao", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Nova ação emergencial", description = "Avulsa, com prioridade e prazo; emergência encerrada, 422.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<EmergenciaDto> novaAcao(@PathVariable UUID uuid, @Valid @RequestBody AcaoEmergencialRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.novaAcao(uuid, dto));
    }

    @RequerPermissao({"EMERGENCIA.COORDENAR"})
    @PostMapping(value = "emergencia/{uuid}/acoes-do-plano", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Gera as ações do plano", description = "Uma por ação prevista do nível atual nos planos ativos, sem repetir; nada a gerar, 422.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<EmergenciaDto> gerarAcoes(@PathVariable UUID uuid) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.gerarAcoesDoPlano(uuid));
    }

    @RequerPermissao({"EMERGENCIA.COORDENAR"})
    @PostMapping(value = "emergencia/{uuid}/acao/{acaoId}/situacao", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Situação da ação", description = "Em execução, concluída ou cancelada (com o motivo, 400); concluída ou cancelada não muda (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<EmergenciaDto> situacaoDaAcao(@PathVariable UUID uuid, @PathVariable UUID acaoId, @Valid @RequestBody SituacaoAcaoEmergencialRequestDto dto) {
        return ResponseEntity.ok(service.situacaoDaAcao(uuid, acaoId, dto));
    }

    @RequerPermissao({"EMERGENCIA.COORDENAR"})
    @PostMapping(value = "emergencia/{uuid}/mobilizacao", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Mobiliza um recurso", description = "O profissional pela matrícula (autoriza turno sem lotação na unidade de destino); os demais pelo recurso.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<EmergenciaDto> mobilizar(@PathVariable UUID uuid, @Valid @RequestBody MobilizacaoRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.mobilizar(uuid, dto));
    }

    @RequerPermissao({"EMERGENCIA.COORDENAR"})
    @PostMapping(value = "emergencia/{uuid}/mobilizacao/{mobilizacaoId}/situacao", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Situação da mobilização", description = "Em andamento, concluída ou cancelada (com o motivo); a ligada a transferência segue a Farmácia (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<EmergenciaDto> situacaoDaMobilizacao(@PathVariable UUID uuid, @PathVariable UUID mobilizacaoId, @Valid @RequestBody SituacaoMobilizacaoRequestDto dto) {
        return ResponseEntity.ok(service.situacaoDaMobilizacao(uuid, mobilizacaoId, dto));
    }

    @RequerPermissao({"EMERGENCIA.COORDENAR"})
    @PutMapping(value = "emergencia/{uuid}/mobilizacao/{mobilizacaoId}/transferencia", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Liga a transferência da Farmácia", description = "Só medicamento, para a mesma unidade de destino (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<EmergenciaDto> ligarTransferencia(@PathVariable UUID uuid, @PathVariable UUID mobilizacaoId, @Valid @RequestBody TransferenciaMobilizacaoRequestDto dto) {
        return ResponseEntity.ok(service.ligarTransferencia(uuid, mobilizacaoId, dto));
    }

    @RequerPermissao({"EMERGENCIA.COORDENAR"})
    @PostMapping(value = "emergencia/{uuid}/capacidade-ativa", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Ativa capacidade emergencial", description = "Até o que resta do ativável da unidade (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<EmergenciaDto> ativarCapacidade(@PathVariable UUID uuid, @Valid @RequestBody AtivacaoCapacidadeRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.ativarCapacidade(uuid, dto));
    }

    @RequerPermissao({"EMERGENCIA.COORDENAR"})
    @PostMapping(value = "emergencia/{uuid}/capacidade-ativa/{ativacaoId}/desativacao", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Desativa a capacidade", description = "Com a justificativa; já desativada, 422.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<EmergenciaDto> desativarCapacidade(@PathVariable UUID uuid, @PathVariable UUID ativacaoId, @Valid @RequestBody DesativacaoCapacidadeRequestDto dto) {
        return ResponseEntity.ok(service.desativarCapacidade(uuid, ativacaoId, dto));
    }

    @RequerPermissao({"EMERGENCIA.REGISTRAR", "EMERGENCIA.ATIVAR", "EMERGENCIA.COORDENAR", "CONTINGENCIA.GERENCIAR", "INDICADOR.CONSULTAR"})
    @GetMapping(value = "emergencias/capacidades", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Capacidade emergencial das unidades", description = "Normal, ativável e o ativado agora; nos leitos, os leitos ativos.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<List<CapacidadeEmergencialDto>> capacidades(@RequestParam(required = false) UUID unidadeId) {
        return ResponseEntity.ok(capacidadeService.listar(unidadeId));
    }

    @RequerPermissao({"CONTINGENCIA.GERENCIAR"})
    @PostMapping(value = "emergencias/capacidade", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cadastra a capacidade emergencial", description = "Uma por unidade e recurso (409).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<CapacidadeEmergencialDto> criarCapacidade(@Valid @RequestBody CapacidadeEmergencialRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(capacidadeService.criar(dto));
    }

    @RequerPermissao({"CONTINGENCIA.GERENCIAR"})
    @PutMapping(value = "emergencias/capacidade/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Ajusta a capacidade emergencial", description = "Unidade e recurso não mudam; o ativável não fica abaixo do ativado (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<CapacidadeEmergencialDto> atualizarCapacidade(@PathVariable UUID uuid, @Valid @RequestBody CapacidadeEmergencialRequestDto dto) {
        return ResponseEntity.ok(capacidadeService.atualizar(uuid, dto));
    }

    // ── Desmobilização e relatório pós-evento (E5, ADR-0159) ──

    @RequerPermissao({"EMERGENCIA.ATIVAR"})
    @PostMapping(value = "emergencia/{uuid}/desmobilizacao", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Inicia a desmobilização", description = "Da ativa (422): conclui as mobilizações, desativa capacidade e planos e registra o que "
            + "foi liberado; daí em diante não entra ação, mobilização nem capacidade nova.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<EmergenciaDto> desmobilizar(@PathVariable UUID uuid, @Valid @RequestBody DesmobilizacaoRequestDto dto) {
        return ResponseEntity.ok(service.iniciarDesmobilizacao(uuid, dto));
    }

    @RequerPermissao({"EMERGENCIA.COORDENAR", "EMERGENCIA.ATIVAR"})
    @PostMapping(value = "emergencia/{uuid}/acoes-restantes/cancelamento", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cancela as ações restantes", description = "Na desmobilização, com o motivo; nenhuma aberta ou fora dela, 422.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<EmergenciaDto> cancelarRestantes(@PathVariable UUID uuid, @Valid @RequestBody CancelamentoAcoesRequestDto dto) {
        return ResponseEntity.ok(service.cancelarAcoesRestantes(uuid, dto));
    }

    @RequerPermissao({"EMERGENCIA.REGISTRAR", "EMERGENCIA.ATIVAR", "EMERGENCIA.COORDENAR", "CONTINGENCIA.GERENCIAR", "INDICADOR.CONSULTAR"})
    @GetMapping(value = "emergencia/{uuid}/relatorio", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Relatório pós-evento", description = "O texto (sem rascunho, vazio) e os números calculados do evento.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<RelatorioPosEventoDto> relatorio(@PathVariable UUID uuid) {
        return ResponseEntity.ok(relatorioService.buscar(uuid));
    }

    @RequerPermissao({"EMERGENCIA.COORDENAR", "EMERGENCIA.ATIVAR"})
    @PutMapping(value = "emergencia/{uuid}/relatorio", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Rascunho do relatório", description = "Na desmobilização ou encerrada (422); finalizado, 422; plano não usado, 422.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<RelatorioPosEventoDto> salvarRelatorio(@PathVariable UUID uuid, @Valid @RequestBody RelatorioPosEventoRequestDto dto) {
        return ResponseEntity.ok(relatorioService.salvar(uuid, dto));
    }

    @RequerPermissao({"EMERGENCIA.ATIVAR"})
    @PostMapping(value = "emergencia/{uuid}/relatorio/finalizacao", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Finaliza o relatório", description = "Com a emergência encerrada e o resumo (422); finalizado não muda.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<RelatorioPosEventoDto> finalizarRelatorio(@PathVariable UUID uuid) {
        return ResponseEntity.ok(relatorioService.finalizar(uuid));
    }

    @RequerPermissao({"CONTINGENCIA.GERENCIAR"})
    @PostMapping(value = "emergencia/{uuid}/relatorio/recomendacao/{recomendacaoId}/revisao-plano", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Revisa o plano da recomendação", description = "A versão vigente vira rascunho novo, ligado à recomendação; já gerada, 409.",
            tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<RelatorioPosEventoDto> revisarPlanoDaRecomendacao(@PathVariable UUID uuid, @PathVariable UUID recomendacaoId) {
        return ResponseEntity.ok(relatorioService.revisarPlano(uuid, recomendacaoId));
    }

    @RequerPermissao({"EMERGENCIA.REGISTRAR", "EMERGENCIA.ATIVAR", "EMERGENCIA.COORDENAR", "CONTINGENCIA.GERENCIAR", "INDICADOR.CONSULTAR"})
    @GetMapping(value = "emergencia/{uuid}/relatorio.csv", produces = "text/csv")
    @Operation(summary = "Números do relatório em CSV", description = "Separado por ponto e vírgula, para a planilha.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<byte[]> relatorioCsv(@PathVariable UUID uuid) {
        byte[] corpo = ("\uFEFF" + relatorioService.csv(uuid)).getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"relatorio-pos-evento.csv\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(corpo);
    }

    // ── Eventos especializados (E6a, ADR-0160) ──

    @RequerPermissao({"EMERGENCIA.COORDENAR", "EMERGENCIA.ATIVAR"})
    @PutMapping(value = "emergencia/{uuid}/evento-massa", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Dados do evento de massa", description = "Natureza, local, período previsto, público, organizador e risco; só no perfil evento "
            + "de massa e na emergência planejada ou ativa (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<EmergenciaDto> salvarEventoMassa(@PathVariable UUID uuid, @Valid @RequestBody EventoMassaRequestDto dto) {
        return ResponseEntity.ok(service.salvarEventoMassa(uuid, dto));
    }

    @RequerPermissao({"EMERGENCIA.ATIVAR"})
    @PostMapping(value = "emergencia/{uuid}/operacao", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Inicia a operação da emergência planejada", description = "Da planejada, com os dados do evento (422): passa a ativa.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<EmergenciaDto> iniciarOperacao(@PathVariable UUID uuid, @Valid @RequestBody InicioOperacaoRequestDto dto) {
        return ResponseEntity.ok(service.iniciarOperacao(uuid, dto));
    }

    @RequerPermissao({"EMERGENCIA.COORDENAR", "EMERGENCIA.ATIVAR"})
    @PostMapping(value = "emergencia/{uuid}/estrutura", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Nova estrutura temporária", description = "Posto médico, tenda de hidratação ou hospital de campanha, com a unidade de "
            + "referência; nasce planejada. Na emergência planejada ou ativa (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<EmergenciaDto> novaEstrutura(@PathVariable UUID uuid, @Valid @RequestBody EstruturaTemporariaRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.novaEstrutura(uuid, dto));
    }

    @RequerPermissao({"EMERGENCIA.COORDENAR", "EMERGENCIA.ATIVAR"})
    @PutMapping(value = "emergencia/{uuid}/estrutura/{estruturaId}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Ajusta a estrutura temporária", description = "Desativada não muda (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<EmergenciaDto> atualizarEstrutura(@PathVariable UUID uuid, @PathVariable UUID estruturaId,
                                                            @Valid @RequestBody EstruturaTemporariaRequestDto dto) {
        return ResponseEntity.ok(service.atualizarEstrutura(uuid, estruturaId, dto));
    }

    @RequerPermissao({"EMERGENCIA.COORDENAR", "EMERGENCIA.ATIVAR"})
    @PostMapping(value = "emergencia/{uuid}/estrutura/{estruturaId}/situacao", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Abre ou desativa a estrutura", description = "Em operação só da planejada e com a emergência ativa (422); desativar pede o "
            + "motivo (400).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<EmergenciaDto> situacaoDaEstrutura(@PathVariable UUID uuid, @PathVariable UUID estruturaId,
                                                             @Valid @RequestBody SituacaoEstruturaRequestDto dto) {
        return ResponseEntity.ok(service.situacaoDaEstrutura(uuid, estruturaId, dto));
    }

    @RequerPermissao({"EMERGENCIA.REGISTRAR"})
    @PostMapping(value = "emergencia/{uuid}/estrutura/{estruturaId}/boletim", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Boletim da estrutura", description = "Atendimentos, remoções, casos graves e óbitos de um dia ou turno; só da estrutura que "
            + "já operou (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<EmergenciaDto> boletim(@PathVariable UUID uuid, @PathVariable UUID estruturaId, @Valid @RequestBody BoletimEstruturaRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.boletim(uuid, estruturaId, dto));
    }

    // ── Epidemia e desastre ambiental (E6b, ADR-0161) ──

    @RequerPermissao({"EMERGENCIA.REGISTRAR", "EMERGENCIA.ATIVAR"})
    @PutMapping(value = "emergencia/{uuid}/agravo", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Agravo da epidemia", description = "Na emergência ativa de perfil epidemia (422); agravo sensível, 422. Sem ele, vale o do "
            + "surto de origem.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<EmergenciaDto> definirAgravo(@PathVariable UUID uuid, @Valid @RequestBody AgravoEmergenciaRequestDto dto) {
        return ResponseEntity.ok(service.definirAgravo(uuid, dto));
    }

    @RequerPermissao({"EMERGENCIA.REGISTRAR", "EMERGENCIA.ATIVAR", "EMERGENCIA.COORDENAR", "CONTINGENCIA.GERENCIAR", "INDICADOR.CONSULTAR"})
    @GetMapping(value = "emergencia/{uuid}/painel-epidemia", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Painel da epidemia", description = "As notificações do agravo por semana epidemiológica, desde 4 semanas antes do início, na "
            + "área afetada; confirmados, descartados e óbitos. Só no perfil epidemia e com agravo (422).", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<PainelEpidemiaDto> painelEpidemia(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.painelEpidemia(uuid));
    }

    @RequerPermissao({"EMERGENCIA.REGISTRAR"})
    @PostMapping(value = "emergencia/{uuid}/exposicao-ambiental", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Exposição ambiental", description = "Meio, agente, população exposta, unidades e medidas; datada e repetível. Na emergência "
            + "ativa de perfil desastre ambiental (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<EmergenciaDto> registrarExposicao(@PathVariable UUID uuid, @Valid @RequestBody ExposicaoAmbientalRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrarExposicao(uuid, dto));
    }
}
