package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.DiariaTfdRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.LiquidacaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.PagamentoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.LiquidacaoDtos;
import com.edufelizardo.maissaudepublica.services.version1.LiquidacaoService;
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
@Tag(name = "Liquidação e pagamento", description = "Fila a liquidar, liquidação, pagamento, diárias do TFD, restos a pagar e execução (ver a ADR-0219).")
public class LiquidacaoController {

    private static final String TAG = "Liquidação e pagamento";
    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;

    @Autowired
    private LiquidacaoService service;

    @RequerPermissao({"FINANCEIRO.CONSULTAR", "FINANCEIRO.GERENCIAR"})
    @GetMapping(value = "a-liquidar", produces = JSON)
    @Operation(summary = "A liquidar", description = "Os atestos ainda sem liquidação: recebimentos definitivos, ordens de serviço por contrato e diárias do TFD.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<List<LiquidacaoDtos.Pendencia>> pendencias() {
        return ResponseEntity.ok(service.pendencias());
    }

    @RequerPermissao({"FINANCEIRO.CONSULTAR", "FINANCEIRO.GERENCIAR"})
    @GetMapping(value = "liquidacoes", produces = JSON)
    @Operation(summary = "Liquidações", description = "Na ordem do vencimento; aPagar deixa só as não pagas.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<List<LiquidacaoDtos.Liquidacao>> liquidacoes(@RequestParam(required = false) Boolean aPagar) {
        return ResponseEntity.ok(service.liquidacoes(aPagar));
    }

    @RequerPermissao({"FINANCEIRO.GERENCIAR"})
    @PostMapping(value = "liquidacao", produces = JSON, consumes = JSON)
    @Operation(summary = "Liquida", description = "O atesto (404/409) contra o empenho do credor (422), até o valor do atesto e o saldo a liquidar (422); retenções até o bruto (400).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<LiquidacaoDtos.Liquidacao> liquidar(@Valid @RequestBody LiquidacaoRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.liquidar(dto));
    }

    @RequerPermissao({"FINANCEIRO.GERENCIAR"})
    @PostMapping(value = "liquidacao/{uuid}/pagamento", produces = JSON, consumes = JSON)
    @Operation(summary = "Paga", description = "A ordem bancária e a data (400); fora da ordem cronológica pede justificativa (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<LiquidacaoDtos.Liquidacao> pagar(@PathVariable UUID uuid, @Valid @RequestBody PagamentoRequestDto dto) {
        return ResponseEntity.ok(service.pagar(uuid, dto));
    }

    @RequerPermissao({"TFD.AUTORIZAR", "FINANCEIRO.CONSULTAR", "FINANCEIRO.GERENCIAR"})
    @GetMapping(value = "diarias-tfd", produces = JSON)
    @Operation(summary = "Diárias do TFD", description = "Da solicitação pedida ou todas, com a situação da liquidação.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<List<LiquidacaoDtos.Diaria>> diarias(@RequestParam(required = false) UUID solicitacaoId) {
        return ResponseEntity.ok(service.diarias(solicitacaoId));
    }

    @RequerPermissao({"TFD.AUTORIZAR", "FINANCEIRO.GERENCIAR"})
    @PostMapping(value = "diaria-tfd", produces = JSON, consumes = JSON)
    @Operation(summary = "Concede a diária do TFD", description = "Da solicitação de TFD autorizada (422), ao credor pessoa física (422); uma por beneficiário (409).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<LiquidacaoDtos.Diaria> conceder(@Valid @RequestBody DiariaTfdRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.conceder(dto));
    }

    @RequerPermissao({"FINANCEIRO.CONSULTAR", "FINANCEIRO.GERENCIAR"})
    @GetMapping(value = "restos-a-pagar", produces = JSON)
    @Operation(summary = "Restos a pagar", description = "Os empenhos de exercícios anteriores: processados e não processados.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<List<LiquidacaoDtos.RestoAPagar>> restos() {
        return ResponseEntity.ok(service.restosAPagar());
    }

    @RequerPermissao({"FINANCEIRO.CONSULTAR", "FINANCEIRO.GERENCIAR"})
    @GetMapping(value = "execucao", produces = JSON)
    @Operation(summary = "Execução do exercício", description = "Por dotação e por fonte; a fila a liquidar, as vencidas e o prazo médio de pagamento.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<LiquidacaoDtos.Execucao> execucao(@RequestParam(required = false) Integer exercicio) {
        return ResponseEntity.ok(service.execucao(exercicio));
    }
}
