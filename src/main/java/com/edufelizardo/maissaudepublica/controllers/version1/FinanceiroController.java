package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.*;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.FinanceiroDtos;
import com.edufelizardo.maissaudepublica.services.version1.FinanceiroService;
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
@RequestMapping(value = "/api/v1/financeiro/")
@Tag(name = "Financeiro", description = "Dotações e empenhos do Fundo Municipal de Saúde (ver a ADR-0218).")
public class FinanceiroController {

    private static final String TAG = "Financeiro";
    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;

    @Autowired
    private FinanceiroService service;

    @RequerPermissao({"FINANCEIRO.CONSULTAR", "FINANCEIRO.GERENCIAR"})
    @GetMapping(value = "dotacoes", produces = JSON)
    @Operation(summary = "Dotações", description = "Com o autorizado, o empenhado e o disponível; filtra pelo exercício.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<List<FinanceiroDtos.Dotacao>> dotacoes(@RequestParam(required = false) Integer exercicio) {
        return ResponseEntity.ok(service.dotacoes(exercicio));
    }

    @RequerPermissao({"FINANCEIRO.GERENCIAR"})
    @PostMapping(value = "dotacao", produces = JSON, consumes = JSON)
    @Operation(summary = "Registra a dotação", description = "Exercício anterior, atual ou seguinte (400); única por programa, natureza e fonte (409).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<FinanceiroDtos.Dotacao> criarDotacao(@Valid @RequestBody DotacaoOrcamentariaRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criarDotacao(dto));
    }

    @RequerPermissao({"FINANCEIRO.GERENCIAR"})
    @PostMapping(value = "dotacao/{uuid}/alteracao", produces = JSON, consumes = JSON)
    @Operation(summary = "Suplementa ou anula a dotação", description = "Data do exercício e não futura (400); a anulação até o disponível (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<FinanceiroDtos.Dotacao> alterarDotacao(@PathVariable UUID uuid, @Valid @RequestBody AlteracaoDotacaoRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.alterarDotacao(uuid, dto));
    }

    @RequerPermissao({"FINANCEIRO.CONSULTAR", "FINANCEIRO.GERENCIAR"})
    @GetMapping(value = "empenhos", produces = JSON)
    @Operation(summary = "Empenhos", description = "Com o valor atual, o comprometido em ordens e o saldo; filtra por exercício, dotação e fornecedor.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<List<FinanceiroDtos.Empenho>> empenhos(@RequestParam(required = false) Integer exercicio,
                                                                  @RequestParam(required = false) UUID dotacaoId,
                                                                  @RequestParam(required = false) UUID fornecedorId) {
        return ResponseEntity.ok(service.empenhos(exercicio, dotacaoId, fornecedorId));
    }

    @RequerPermissao({"FINANCEIRO.CONSULTAR", "FINANCEIRO.GERENCIAR"})
    @GetMapping(value = "empenho/{uuid}", produces = JSON)
    @Operation(summary = "Empenho", description = "Com os movimentos e as ordens de fornecimento.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<FinanceiroDtos.Empenho> empenho(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.empenho(uuid));
    }

    @RequerPermissao({"COMPRAS.GERENCIAR", "FINANCEIRO.CONSULTAR", "FINANCEIRO.GERENCIAR"})
    @GetMapping(value = "empenhos-disponiveis", produces = JSON)
    @Operation(summary = "Empenhos para a ordem", description = "Os empenhos com saldo do fornecedor da ata ou do contrato, para a ordem de fornecimento.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<List<FinanceiroDtos.Empenho>> disponiveis(@RequestParam UUID instrumentoId) {
        return ResponseEntity.ok(service.disponiveisPara(instrumentoId));
    }

    @RequerPermissao({"FINANCEIRO.GERENCIAR"})
    @PostMapping(value = "empenho", produces = JSON, consumes = JSON)
    @Operation(summary = "Registra o empenho", description = "Número único no exercício (409); data (400); fornecedor regular e instrumento dele (422); dentro do disponível (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<FinanceiroDtos.Empenho> empenhar(@Valid @RequestBody EmpenhoRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.empenhar(dto));
    }

    @RequerPermissao({"FINANCEIRO.GERENCIAR"})
    @PostMapping(value = "empenho/{uuid}/movimento", produces = JSON, consumes = JSON)
    @Operation(summary = "Reforça ou anula o empenho", description = "O reforço dentro do disponível da dotação; a anulação até o saldo (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<FinanceiroDtos.Empenho> movimentar(@PathVariable UUID uuid, @Valid @RequestBody MovimentoEmpenhoRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.movimentar(uuid, dto));
    }
}
