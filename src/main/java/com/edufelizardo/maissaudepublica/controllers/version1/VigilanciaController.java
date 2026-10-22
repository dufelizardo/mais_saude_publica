package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.AuditarLeitura;
import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AgravoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ClassificacaoCasoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ContatoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.LocalCasoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EncerramentoNotificacaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.FichaAgravoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.InvestigacaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.NotificacaoNegativaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.NotificacaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AgravoDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.BoletimVigilanciaDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.MapaVigilanciaDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.NotificacaoDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.NotificacaoNegativaDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.NotificacoesDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.SemanasEpidemiologicasDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.SugestaoNotificacaoDto;
import com.edufelizardo.maissaudepublica.models.enuns.ClassificacaoCasoEpidemiologico;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoNotificacao;
import com.edufelizardo.maissaudepublica.services.version1.VigilanciaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/vigilancia/")
@Tag(name = "Vigilância epidemiológica", description = "Agravos com ficha versionada, notificação, investigação, classificação, notificação "
        + "negativa, sugestões e boletim (ver docs/adr/0124-vigilancia-epidemiologica-fundacao.md).")
public class VigilanciaController {

    private static final String TAG = "Vigilância epidemiológica";

    @Autowired
    private VigilanciaService service;

    @RequerPermissao({"VIGILANCIA.NOTIFICAR", "VIGILANCIA.INVESTIGAR", "VIGILANCIA.GERENCIAR"})
    @GetMapping(value = "agravos", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Catálogo de agravos", description = "Com a ficha vigente de cada um.", tags = TAG)
    @ApiErrorResponsesListagem
    public ResponseEntity<List<AgravoDto>> agravos() {
        return ResponseEntity.ok(service.agravos());
    }

    @RequerPermissao({"VIGILANCIA.NOTIFICAR", "VIGILANCIA.INVESTIGAR", "VIGILANCIA.GERENCIAR"})
    @GetMapping(value = "agravo/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Um agravo", description = "Com a ficha vigente.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<AgravoDto> agravo(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.agravo(uuid));
    }

    @RequerPermissao({"VIGILANCIA.GERENCIAR"})
    @PostMapping(value = "agravo", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cadastra um agravo", description = "Agravo de interesse local. Código repetido: 409; CID inválido: 400.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<AgravoDto> criar(@Valid @RequestBody AgravoRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criarAgravo(dto));
    }

    @RequerPermissao({"VIGILANCIA.GERENCIAR"})
    @PatchMapping(value = "agravo/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Atualiza um agravo", description = "O código não muda (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<AgravoDto> atualizar(@PathVariable UUID uuid, @Valid @RequestBody AgravoRequestDto dto) {
        return ResponseEntity.ok(service.atualizarAgravo(uuid, dto));
    }

    @RequerPermissao({"VIGILANCIA.GERENCIAR"})
    @PostMapping(value = "agravo/{uuid}/ficha", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Nova versão da ficha", description = "As notificações antigas continuam na versão delas. Chave repetida ou opção "
            + "sem as opções: 400.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<AgravoDto> ficha(@PathVariable UUID uuid, @Valid @RequestBody FichaAgravoRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.novaFicha(uuid, dto));
    }

    @RequerPermissao({"VIGILANCIA.NOTIFICAR"})
    @PostMapping(value = "notificacao", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Notifica um agravo", description = "Respostas conferidas contra a ficha vigente (400). Outra notificação em aberto do "
            + "mesmo agravo só avisa. A crise da SM3 informada fica notificada.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<NotificacaoDto> notificar(@Valid @RequestBody NotificacaoRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.notificar(dto));
    }

    @AuditarLeitura
    @RequerPermissao({"VIGILANCIA.NOTIFICAR", "VIGILANCIA.INVESTIGAR"})
    @GetMapping(value = "notificacoes", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Notificações", description = "Do escopo; as de agravo sensível só para quem investiga ou notificou. Filtros: unidadeId, "
            + "agravoId, situacao, atrasadas. Leitura auditada.", tags = TAG)
    @ApiErrorResponsesListagem
    public ResponseEntity<NotificacoesDto> notificacoes(@RequestParam(required = false) UUID unidadeId,
                                                        @RequestParam(required = false) UUID agravoId,
                                                        @RequestParam(required = false) SituacaoNotificacao situacao,
                                                        @RequestParam(required = false, defaultValue = "false") boolean atrasadas) {
        return ResponseEntity.ok(service.notificacoes(unidadeId, agravoId, situacao, atrasadas));
    }

    @AuditarLeitura
    @RequerPermissao({"VIGILANCIA.NOTIFICAR", "VIGILANCIA.INVESTIGAR"})
    @GetMapping(value = "notificacao/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Uma notificação", description = "Com a ficha, a investigação e as classificações. Agravo sensível de quem não "
            + "investiga nem notificou: 403. Leitura auditada.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<NotificacaoDto> notificacao(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.notificacao(uuid));
    }

    @RequerPermissao({"VIGILANCIA.INVESTIGAR"})
    @PostMapping(value = "notificacao/{uuid}/local", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Marca o local do caso", description = "O ponto no mapa (local provável de infecção) e a microárea desenhada que o "
            + "contém (ADR-0130). Encerrada: 422.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<NotificacaoDto> local(@PathVariable UUID uuid, @Valid @RequestBody LocalCasoRequestDto dto) {
        return ResponseEntity.ok(service.marcarLocal(uuid, dto));
    }

    @RequerPermissao({"VIGILANCIA.INVESTIGAR"})
    @PostMapping(value = "notificacao/{uuid}/contato", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Registra um contato do caso", description = "O paciente, ou o nome (obrigatório sem cadastro, 400), telefone e "
            + "endereço; o monitoramento vai até a exposição mais o período do agravo. Agravo sensível: só a vigilância e quem notificou (403).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<NotificacaoDto> contato(@PathVariable UUID uuid, @Valid @RequestBody ContatoRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.adicionarContato(uuid, dto));
    }

    @RequerPermissao({"VIGILANCIA.INVESTIGAR"})
    @PatchMapping(value = "contato/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Atualiza o contato", description = "Dados, acompanhamento e o cadastro. Virar caso só notificando o contato (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<NotificacaoDto> atualizarContato(@PathVariable UUID uuid, @Valid @RequestBody ContatoRequestDto dto) {
        return ResponseEntity.ok(service.atualizarContato(uuid, dto));
    }

    @RequerPermissao({"VIGILANCIA.NOTIFICAR", "VIGILANCIA.INVESTIGAR"})
    @GetMapping(value = "mapa", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Mapa dos casos", description = "Casos por microárea, pontos (só para a investigação, nunca de agravo sensível) e a "
            + "curva por semana epidemiológica. Período padrão: as últimas 12 semanas (ADR-0130); com o surto, os casos dele e as "
            + "microáreas declaradas em destaque (ADR-0131).", tags = TAG)
    @ApiErrorResponsesListagem
    public ResponseEntity<MapaVigilanciaDto> mapa(@RequestParam(required = false) UUID agravoId, @RequestParam(required = false) UUID unidadeId,
                                                  @RequestParam(required = false) LocalDate de,
                                                  @RequestParam(required = false) LocalDate ate,
                                                  @RequestParam(required = false) ClassificacaoCasoEpidemiologico classificacao,
                                                  @RequestParam(required = false) UUID surtoId) {
        return ResponseEntity.ok(service.mapa(agravoId, unidadeId, de, ate, classificacao, surtoId));
    }

    @RequerPermissao({"VIGILANCIA.INVESTIGAR"})
    @PostMapping(value = "notificacao/{uuid}/investigacao", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Abre ou atualiza a investigação", description = "Exame de outro paciente: 422; encerrada: 422.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<NotificacaoDto> investigar(@PathVariable UUID uuid, @Valid @RequestBody InvestigacaoRequestDto dto) {
        return ResponseEntity.ok(service.investigar(uuid, dto));
    }

    @RequerPermissao({"VIGILANCIA.INVESTIGAR"})
    @PostMapping(value = "notificacao/{uuid}/classificacao", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Classifica o caso", description = "Histórico. Confirmado e descartado pedem o critério (400), que o agravo precisa "
            + "aceitar (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<NotificacaoDto> classificar(@PathVariable UUID uuid, @Valid @RequestBody ClassificacaoCasoRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.classificar(uuid, dto));
    }

    @RequerPermissao({"VIGILANCIA.INVESTIGAR"})
    @PostMapping(value = "notificacao/{uuid}/encerramento", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Encerra a notificação", description = "Pede a classificação final (confirmado, descartado ou inconclusivo; 422) e a "
            + "evolução. Depois, não muda.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<NotificacaoDto> encerrar(@PathVariable UUID uuid, @Valid @RequestBody EncerramentoNotificacaoRequestDto dto) {
        return ResponseEntity.ok(service.encerrar(uuid, dto));
    }

    @AuditarLeitura
    @RequerPermissao({"VIGILANCIA.NOTIFICAR"})
    @GetMapping(value = "sugestoes", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Sugestões de notificação", description = "Internações e pedidos de exame dos últimos 30 dias com CID de agravo "
            + "compulsório, e crises com notificação pendente, sem notificação do agravo. Notificar é ato profissional. Leitura auditada.", tags = TAG)
    @ApiErrorResponsesListagem
    public ResponseEntity<List<SugestaoNotificacaoDto>> sugestoes(@RequestParam(required = false) UUID unidadeId) {
        return ResponseEntity.ok(service.sugestoes(unidadeId));
    }

    @RequerPermissao({"VIGILANCIA.NOTIFICAR"})
    @PostMapping(value = "notificacao-negativa", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Notificação negativa", description = "A unidade declara que não houve caso na semana epidemiológica (sem semana, a "
            + "anterior). Repetida: 409; semana com notificação: 422.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<NotificacaoNegativaDto> negativa(@Valid @RequestBody NotificacaoNegativaRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.declararNegativa(dto));
    }

    @RequerPermissao({"VIGILANCIA.NOTIFICAR", "VIGILANCIA.INVESTIGAR"})
    @GetMapping(value = "semanas", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Semanas epidemiológicas", description = "As declarações e as semanas recentes sem notificação nem declaração.", tags = TAG)
    @ApiErrorResponsesListagem
    public ResponseEntity<SemanasEpidemiologicasDto> semanas(@RequestParam(required = false) UUID unidadeId) {
        return ResponseEntity.ok(service.semanas(unidadeId));
    }

    @RequerPermissao({"VIGILANCIA.NOTIFICAR", "VIGILANCIA.INVESTIGAR", "VIGILANCIA.GERENCIAR"})
    @GetMapping(value = "boletim", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Boletim", description = "Só agregados por agravo: notificadas, em investigação, encerradas, confirmadas e descartadas.",
            tags = TAG)
    @ApiErrorResponsesListagem
    public ResponseEntity<BoletimVigilanciaDto> boletim(@RequestParam(required = false) UUID unidadeId) {
        return ResponseEntity.ok(service.boletim(unidadeId));
    }
}
