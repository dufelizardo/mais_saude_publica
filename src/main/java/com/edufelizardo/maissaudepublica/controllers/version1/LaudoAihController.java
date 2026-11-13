package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.AuditarLeitura;
import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.DecisaoLaudoAihRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.FaixaAihRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.LaudoAihRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.LaudoAihDtos;
import com.edufelizardo.maissaudepublica.services.version1.LaudoAihService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** Laudo para solicitação de AIH, autorização interna e faixas de numeração (ADR-0186). */
@RestController
@RequestMapping(value = "/api/v1/laudo-aih/")
@Tag(name = "Laudo de AIH", description = "Solicitação de AIH, autorização interna com o número e faixas de numeração (ver docs/adr/0186-laudo-aih.md).")
public class LaudoAihController {

    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;
    private static final String TAG = "Laudo de AIH";

    @Autowired
    private LaudoAihService service;

    @RequerPermissao({"AIH.SOLICITAR"})
    @PostMapping(produces = JSON, consumes = JSON)
    @Operation(summary = "Solicita a AIH", description = "Da internação ou da eletiva agendada (uma das duas, 400). Já há laudo em andamento ou autorizado: 409.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<LaudoAihDtos.Laudo> solicitar(@Valid @RequestBody LaudoAihRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.solicitar(dto));
    }

    @RequerPermissao({"INTERNACAO.CONSULTAR", "INTERNACAO.GERENCIAR", "INTERNACAO.ALTA", "AIH.SOLICITAR", "AIH.AUTORIZAR"})
    @GetMapping(produces = JSON)
    @Operation(summary = "Laudos de uma internação ou de uma eletiva", description = "Informe internacaoId ou agendamentoId; do mais novo para o mais velho.", tags = TAG)
    @ApiErrorResponsesListagem
    public ResponseEntity<List<LaudoAihDtos.Laudo>> listar(@RequestParam(required = false) UUID internacaoId,
                                                           @RequestParam(required = false) UUID agendamentoId) {
        return ResponseEntity.ok(service.listar(internacaoId, agendamentoId));
    }

    @RequerPermissao({"AIH.AUTORIZAR"})
    @GetMapping(value = "fila", produces = JSON)
    @Operation(summary = "Fila do autorizador", description = "Laudos solicitados, urgência primeiro.", tags = TAG)
    @ApiErrorResponsesListagem
    public ResponseEntity<List<LaudoAihDtos.Laudo>> fila() {
        return ResponseEntity.ok(service.fila());
    }

    @RequerPermissao({"AIH.SOLICITAR", "AIH.AUTORIZAR", "INTERNACAO.GERENCIAR"})
    @GetMapping(value = "pendencias", produces = JSON)
    @Operation(summary = "Internações de urgência sem laudo", description = "Internadas há mais de 48 horas sem laudo de AIH (fora os cancelados).", tags = TAG)
    @ApiErrorResponsesListagem
    public ResponseEntity<List<LaudoAihDtos.Pendencia>> pendencias() {
        return ResponseEntity.ok(service.pendencias());
    }

    @RequerPermissao({"AIH.NUMERAR", "AIH.AUTORIZAR"})
    @GetMapping(value = "faixas", produces = JSON)
    @Operation(summary = "Faixas de numeração de AIH", description = "Com o primeiro e o último número já com o dígito verificador.", tags = TAG)
    @ApiErrorResponsesListagem
    public ResponseEntity<List<LaudoAihDtos.Faixa>> faixas() {
        return ResponseEntity.ok(service.faixas());
    }

    @RequerPermissao({"AIH.NUMERAR"})
    @PostMapping(value = "faixas", produces = JSON, consumes = JSON)
    @Operation(summary = "Cadastra a faixa recebida do gestor", description = "12 dígitos, sem o verificador. Início maior que o fim: 400. Sobreposta: 409.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<LaudoAihDtos.Faixa> cadastrarFaixa(@Valid @RequestBody FaixaAihRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.cadastrarFaixa(dto));
    }

    @RequerPermissao({"AIH.NUMERAR"})
    @PostMapping(value = "faixas/{uuid}/ativacao", produces = JSON)
    @Operation(summary = "Ativa ou desativa a faixa", description = "A faixa desativada não numera.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<LaudoAihDtos.Faixa> ativarFaixa(@PathVariable UUID uuid, @RequestParam boolean ativa) {
        return ResponseEntity.ok(service.ativarFaixa(uuid, ativa));
    }

    @RequerPermissao({"INTERNACAO.CONSULTAR", "INTERNACAO.GERENCIAR", "INTERNACAO.ALTA", "AIH.SOLICITAR", "AIH.AUTORIZAR"})
    @AuditarLeitura
    @GetMapping(value = "{uuid}", produces = JSON)
    @Operation(summary = "Detalhe do laudo", description = "Com os eventos. Leitura auditada.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<LaudoAihDtos.Laudo> buscar(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.buscarPorId(uuid));
    }

    @RequerPermissao({"INTERNACAO.CONSULTAR", "INTERNACAO.GERENCIAR", "INTERNACAO.ALTA", "AIH.SOLICITAR", "AIH.AUTORIZAR"})
    @AuditarLeitura
    @GetMapping(value = "{uuid}/laudo.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Laudo e autorização em PDF", description = "Com a situação e o número da AIH. Leitura auditada.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<byte[]> pdf(@PathVariable UUID uuid) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"laudo-aih-" + uuid + ".pdf\"")
                .body(service.pdf(uuid));
    }

    @RequerPermissao({"AIH.SOLICITAR"})
    @PutMapping(value = "{uuid}", produces = JSON, consumes = JSON)
    @Operation(summary = "Corrige o laudo", description = "Só solicitado ou devolvido (422); o devolvido volta para a fila.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<LaudoAihDtos.Laudo> corrigir(@PathVariable UUID uuid, @Valid @RequestBody LaudoAihRequestDto dto) {
        return ResponseEntity.ok(service.corrigir(uuid, dto));
    }

    @RequerPermissao({"AIH.AUTORIZAR"})
    @PostMapping(value = "{uuid}/autorizacao", produces = JSON, consumes = JSON)
    @Operation(summary = "Autoriza a AIH", description = "Dá o próximo número da faixa vigente do ano. Sem faixa ou esgotada: 422. Quem solicitou não autoriza: 422.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<LaudoAihDtos.Laudo> autorizar(@PathVariable UUID uuid, @Valid @RequestBody DecisaoLaudoAihRequestDto dto) {
        return ResponseEntity.ok(service.autorizar(uuid, dto));
    }

    @RequerPermissao({"AIH.AUTORIZAR"})
    @PostMapping(value = "{uuid}/devolucao", produces = JSON, consumes = JSON)
    @Operation(summary = "Devolve para correção", description = "Com motivo (400 sem ele).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<LaudoAihDtos.Laudo> devolver(@PathVariable UUID uuid, @Valid @RequestBody DecisaoLaudoAihRequestDto dto) {
        return ResponseEntity.ok(service.devolver(uuid, dto));
    }

    @RequerPermissao({"AIH.AUTORIZAR"})
    @PostMapping(value = "{uuid}/negacao", produces = JSON, consumes = JSON)
    @Operation(summary = "Nega a AIH", description = "Com motivo (400 sem ele).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<LaudoAihDtos.Laudo> negar(@PathVariable UUID uuid, @Valid @RequestBody DecisaoLaudoAihRequestDto dto) {
        return ResponseEntity.ok(service.negar(uuid, dto));
    }

    @RequerPermissao({"AIH.SOLICITAR"})
    @PostMapping(value = "{uuid}/cancelamento", produces = JSON, consumes = JSON)
    @Operation(summary = "Cancela o laudo", description = "Só solicitado ou devolvido, com motivo.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<LaudoAihDtos.Laudo> cancelar(@PathVariable UUID uuid, @Valid @RequestBody DecisaoLaudoAihRequestDto dto) {
        return ResponseEntity.ok(service.cancelar(uuid, dto));
    }
}
