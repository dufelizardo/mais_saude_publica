package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.OperacaoEstoqueRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.CentralEstoqueDtos;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoRequisicaoEstoque;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoTransferenciaEstoque;
import com.edufelizardo.maissaudepublica.services.version1.CentralEstoqueService;
import com.edufelizardo.maissaudepublica.services.version1.InventarioEstoqueService;
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

/** Almoxarifado central, requisição, transferência e inventário (ADR-0189). */
@RestController
@RequestMapping(value = "/api/v1/estoque/")
@Tag(name = "Estoque: central", description = "Central de abastecimento, requisição, transferência e inventário (ver docs/adr/0189-estoque-central-requisicao-inventario.md).")
public class CentralEstoqueController {

    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;
    private static final String TAG = "Estoque: central";
    private static final String C = "ESTOQUE.CONSULTAR";
    private static final String M = "ESTOQUE.MOVIMENTAR";
    private static final String G = "ESTOQUE.GERENCIAR";
    private static final String R = "ESTOQUE.REQUISITAR";
    private static final String A = "ESTOQUE.ATENDER";

    @Autowired
    private CentralEstoqueService central;

    @Autowired
    private InventarioEstoqueService inventario;

    @RequerPermissao({C, M, G, R, A})
    @GetMapping(value = "centrais", produces = JSON)
    @Operation(summary = "Centrais de abastecimento", description = "As unidades com a capacidade CENTRAL_ABASTECIMENTO.", tags = TAG)
    @ApiErrorResponsesListagem
    public ResponseEntity<List<CentralEstoqueDtos.Central>> centrais() {
        return ResponseEntity.ok(central.centrais());
    }

    // ── Requisição ──

    @RequerPermissao({R})
    @PostMapping(value = "requisicao", produces = JSON, consumes = JSON)
    @Operation(summary = "Cria o rascunho da requisição", description = "Sem itens ou material repetido: 400. Não é central ou é a própria: 422.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<CentralEstoqueDtos.Requisicao> criar(@Valid @RequestBody OperacaoEstoqueRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(central.criarRequisicao(dto));
    }

    @RequerPermissao({R})
    @PutMapping(value = "requisicao/{uuid}", produces = JSON, consumes = JSON)
    @Operation(summary = "Muda o rascunho", description = "Só no rascunho (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<CentralEstoqueDtos.Requisicao> atualizar(@PathVariable UUID uuid, @Valid @RequestBody OperacaoEstoqueRequestDto dto) {
        return ResponseEntity.ok(central.atualizarRequisicao(uuid, dto));
    }

    @RequerPermissao({R})
    @PostMapping(value = "requisicao/{uuid}/envio", produces = JSON, consumes = JSON)
    @Operation(summary = "Envia a requisição à central", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<CentralEstoqueDtos.Requisicao> enviar(@PathVariable UUID uuid, @Valid @RequestBody OperacaoEstoqueRequestDto dto) {
        return ResponseEntity.ok(central.enviarRequisicao(uuid, dto));
    }

    @RequerPermissao({R})
    @PostMapping(value = "requisicao/{uuid}/cancelamento", produces = JSON, consumes = JSON)
    @Operation(summary = "Cancela a requisição", description = "Rascunho ou enviada, com motivo.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<CentralEstoqueDtos.Requisicao> cancelar(@PathVariable UUID uuid, @Valid @RequestBody OperacaoEstoqueRequestDto dto) {
        return ResponseEntity.ok(central.cancelarRequisicao(uuid, dto));
    }

    @RequerPermissao({A})
    @PostMapping(value = "requisicao/{uuid}/negacao", produces = JSON, consumes = JSON)
    @Operation(summary = "A central nega", description = "Enviada, com motivo.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<CentralEstoqueDtos.Requisicao> negar(@PathVariable UUID uuid, @Valid @RequestBody OperacaoEstoqueRequestDto dto) {
        return ResponseEntity.ok(central.negarRequisicao(uuid, dto));
    }

    @RequerPermissao({A})
    @PostMapping(value = "requisicao/{uuid}/atendimento", produces = JSON, consumes = JSON)
    @Operation(summary = "A central atende", description = "Até o pedido (400) e o saldo da central (422); gera a transferência.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<CentralEstoqueDtos.Requisicao> atender(@PathVariable UUID uuid, @Valid @RequestBody OperacaoEstoqueRequestDto dto) {
        return ResponseEntity.ok(central.atenderRequisicao(uuid, dto));
    }

    @RequerPermissao({C, M, G, R, A})
    @GetMapping(value = "requisicao", produces = JSON)
    @Operation(summary = "Requisições", description = "Da unidade e as recebidas pela central (sem os rascunhos).", tags = TAG)
    @ApiErrorResponsesListagem
    public ResponseEntity<List<CentralEstoqueDtos.Requisicao>> requisicoes(@RequestParam(required = false) UUID unidadeId,
            @RequestParam(required = false) UUID centralId, @RequestParam(required = false) SituacaoRequisicaoEstoque situacao) {
        return ResponseEntity.ok(central.requisicoes(unidadeId, centralId, situacao));
    }

    @RequerPermissao({C, M, G, R, A})
    @GetMapping(value = "requisicao/{uuid}", produces = JSON)
    @Operation(summary = "Detalhe da requisição", description = "Com o saldo e o consumo de 30 dias da unidade e o saldo da central.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<CentralEstoqueDtos.Requisicao> requisicao(@PathVariable UUID uuid) {
        return ResponseEntity.ok(central.buscarRequisicao(uuid));
    }

    // ── Transferência ──

    @RequerPermissao({M})
    @PostMapping(value = "transferencia", produces = JSON, consumes = JSON)
    @Operation(summary = "Transferência avulsa", description = "Por lote, da origem (unidadeId) ao destino.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<CentralEstoqueDtos.Transferencia> transferir(@Valid @RequestBody OperacaoEstoqueRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(central.transferir(dto));
    }

    @RequerPermissao({M})
    @PostMapping(value = "transferencia/{uuid}/recebimento", produces = JSON, consumes = JSON)
    @Operation(summary = "Recebe a transferência", description = "Todos os itens conferidos; diferença com motivo (400).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<CentralEstoqueDtos.Transferencia> receber(@PathVariable UUID uuid, @Valid @RequestBody OperacaoEstoqueRequestDto dto) {
        return ResponseEntity.ok(central.receber(uuid, dto));
    }

    @RequerPermissao({C, M, G, R, A})
    @GetMapping(value = "transferencia", produces = JSON)
    @Operation(summary = "Transferências", description = "Que saem ou chegam na unidade.", tags = TAG)
    @ApiErrorResponsesListagem
    public ResponseEntity<List<CentralEstoqueDtos.Transferencia>> transferencias(@RequestParam(required = false) UUID unidadeId,
            @RequestParam(required = false) SituacaoTransferenciaEstoque situacao) {
        return ResponseEntity.ok(central.transferencias(unidadeId, situacao));
    }

    @RequerPermissao({C, M, G, R, A})
    @GetMapping(value = "transferencia/{uuid}", produces = JSON)
    @Operation(summary = "Detalhe da transferência", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<CentralEstoqueDtos.Transferencia> transferencia(@PathVariable UUID uuid) {
        return ResponseEntity.ok(central.buscarTransferencia(uuid));
    }

    // ── Inventário ──

    @RequerPermissao({M})
    @PostMapping(value = "inventario", produces = JSON, consumes = JSON)
    @Operation(summary = "Abre o inventário", description = "Um aberto por unidade (409); bloqueia a movimentação até fechar.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<CentralEstoqueDtos.Inventario> abrir(@Valid @RequestBody OperacaoEstoqueRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventario.abrir(dto));
    }

    @RequerPermissao({M})
    @PostMapping(value = "inventario/{uuid}/contagem", produces = JSON, consumes = JSON)
    @Operation(summary = "Registra a contagem", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<CentralEstoqueDtos.Inventario> contar(@PathVariable UUID uuid, @Valid @RequestBody OperacaoEstoqueRequestDto dto) {
        return ResponseEntity.ok(inventario.contar(uuid, dto));
    }

    @RequerPermissao({M})
    @PostMapping(value = "inventario/{uuid}/fechamento", produces = JSON, consumes = JSON)
    @Operation(summary = "Fecha o inventário", description = "Todos contados (422); lança os ajustes.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<CentralEstoqueDtos.Inventario> fechar(@PathVariable UUID uuid, @Valid @RequestBody OperacaoEstoqueRequestDto dto) {
        return ResponseEntity.ok(inventario.fechar(uuid, dto));
    }

    @RequerPermissao({M})
    @PostMapping(value = "inventario/{uuid}/cancelamento", produces = JSON, consumes = JSON)
    @Operation(summary = "Cancela o inventário", description = "Com motivo; nada é ajustado.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<CentralEstoqueDtos.Inventario> cancelarInventario(@PathVariable UUID uuid, @Valid @RequestBody OperacaoEstoqueRequestDto dto) {
        return ResponseEntity.ok(inventario.cancelar(uuid, dto));
    }

    @RequerPermissao({C, M, G})
    @GetMapping(value = "inventario", produces = JSON)
    @Operation(summary = "Inventários da unidade", tags = TAG)
    @ApiErrorResponsesListagem
    public ResponseEntity<List<CentralEstoqueDtos.Inventario>> inventarios(@RequestParam UUID unidadeId) {
        return ResponseEntity.ok(inventario.listar(unidadeId));
    }

    @RequerPermissao({C, M, G})
    @GetMapping(value = "inventario/{uuid}", produces = JSON)
    @Operation(summary = "Detalhe do inventário", description = "Com cada lote, o saldo do sistema, a contagem e a diferença.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<CentralEstoqueDtos.Inventario> inventario(@PathVariable UUID uuid) {
        return ResponseEntity.ok(inventario.buscar(uuid));
    }
}
