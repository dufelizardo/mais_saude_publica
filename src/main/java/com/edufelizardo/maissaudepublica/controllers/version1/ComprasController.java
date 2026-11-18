package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AnaliseSolicitacaoCompraRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.CertidaoFornecedorRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ConclusaoPesquisaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.CotacaoCompraRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.FornecedorRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.MotivoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.SancaoFornecedorRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.SolicitacaoCompraRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ComprasDtos;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoSolicitacaoCompra;
import com.edufelizardo.maissaudepublica.services.version1.ComprasService;
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
@RequestMapping(value = "/api/v1/compras/")
@Tag(name = "Compras", description = "Fornecedores, solicitações de compra e pesquisa de preços (ver a ADR-0212).")
public class ComprasController {

    private static final String TAG = "Compras";
    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;

    @Autowired
    private ComprasService service;

    // ── Fornecedores ──

    @RequerPermissao({"COMPRAS.SOLICITAR", "COMPRAS.GERENCIAR"})
    @GetMapping(value = "fornecedores", produces = JSON)
    @Operation(summary = "Fornecedores", description = "Com a situação derivada das certidões e das sanções; busca por nome ou documento.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<List<ComprasDtos.Fornecedor>> fornecedores(@RequestParam(required = false) String busca,
                                                                     @RequestParam(required = false) Boolean ativos) {
        return ResponseEntity.ok(service.fornecedores(busca, ativos));
    }

    @RequerPermissao({"COMPRAS.SOLICITAR", "COMPRAS.GERENCIAR"})
    @GetMapping(value = "fornecedor/{uuid}", produces = JSON)
    @Operation(summary = "Fornecedor", description = "Com as certidões e as sanções.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<ComprasDtos.Fornecedor> fornecedor(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.fornecedor(uuid));
    }

    @RequerPermissao({"COMPRAS.GERENCIAR"})
    @PostMapping(value = "fornecedor", produces = JSON, consumes = JSON)
    @Operation(summary = "Cadastra o fornecedor", description = "CPF ou CNPJ válido (400) e único (409).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<ComprasDtos.Fornecedor> cadastrarFornecedor(@Valid @RequestBody FornecedorRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.cadastrarFornecedor(dto));
    }

    @RequerPermissao({"COMPRAS.GERENCIAR"})
    @PutMapping(value = "fornecedor/{uuid}", produces = JSON, consumes = JSON)
    @Operation(summary = "Atualiza o fornecedor", description = "O documento não muda (422); ativo desativa ou reativa.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<ComprasDtos.Fornecedor> atualizarFornecedor(@PathVariable UUID uuid, @Valid @RequestBody FornecedorRequestDto dto) {
        return ResponseEntity.ok(service.atualizarFornecedor(uuid, dto));
    }

    @RequerPermissao({"COMPRAS.GERENCIAR"})
    @PostMapping(value = "fornecedor/{uuid}/certidao", produces = JSON, consumes = JSON)
    @Operation(summary = "Registra uma certidão", description = "Emissão não futura e validade a partir dela (400); FGTS só da pessoa jurídica (400).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<ComprasDtos.Fornecedor> registrarCertidao(@PathVariable UUID uuid, @Valid @RequestBody CertidaoFornecedorRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrarCertidao(uuid, dto));
    }

    @RequerPermissao({"COMPRAS.GERENCIAR"})
    @PostMapping(value = "fornecedor/{uuid}/sancao", produces = JSON, consumes = JSON)
    @Operation(summary = "Registra uma sanção", description = "Art. 156: impedimento até 3 anos, inidoneidade de 3 a 6, multa com o valor (400).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<ComprasDtos.Fornecedor> registrarSancao(@PathVariable UUID uuid, @Valid @RequestBody SancaoFornecedorRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrarSancao(uuid, dto));
    }

    @RequerPermissao({"COMPRAS.GERENCIAR"})
    @PostMapping(value = "sancao/{uuid}/revogacao", produces = JSON, consumes = JSON)
    @Operation(summary = "Revoga a sanção", description = "Com o motivo; já revogada, 422.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<ComprasDtos.Fornecedor> revogarSancao(@PathVariable UUID uuid, @Valid @RequestBody MotivoRequestDto dto) {
        return ResponseEntity.ok(service.revogarSancao(uuid, dto.getMotivo()));
    }

    // ── Solicitações ──

    @RequerPermissao({"COMPRAS.SOLICITAR", "COMPRAS.GERENCIAR"})
    @GetMapping(value = "solicitacoes", produces = JSON)
    @Operation(summary = "Solicitações de compra", description = "As das unidades em que se pede e, para quem compra, as da rede fora do rascunho.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<List<ComprasDtos.Solicitacao>> solicitacoes(@RequestParam(required = false) UUID unidadeId,
                                                                      @RequestParam(required = false) SituacaoSolicitacaoCompra situacao) {
        return ResponseEntity.ok(service.solicitacoes(unidadeId, situacao));
    }

    @RequerPermissao({"COMPRAS.SOLICITAR", "COMPRAS.GERENCIAR"})
    @GetMapping(value = "solicitacao/{uuid}", produces = JSON)
    @Operation(summary = "Solicitação de compra", description = "Com os itens e as cotações.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<ComprasDtos.Solicitacao> solicitacao(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.solicitacao(uuid));
    }

    @RequerPermissao({"COMPRAS.SOLICITAR"})
    @PostMapping(value = "solicitacao", produces = JSON, consumes = JSON)
    @Operation(summary = "Cria a solicitação", description = "Em rascunho, na unidade de quem pede (403); itens conferidos (400/404/422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<ComprasDtos.Solicitacao> criar(@Valid @RequestBody SolicitacaoCompraRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(dto));
    }

    @RequerPermissao({"COMPRAS.SOLICITAR"})
    @PutMapping(value = "solicitacao/{uuid}", produces = JSON, consumes = JSON)
    @Operation(summary = "Edita a solicitação", description = "Rascunho ou devolvida (422); troca os itens.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<ComprasDtos.Solicitacao> atualizar(@PathVariable UUID uuid, @Valid @RequestBody SolicitacaoCompraRequestDto dto) {
        return ResponseEntity.ok(service.atualizar(uuid, dto));
    }

    @RequerPermissao({"COMPRAS.SOLICITAR"})
    @PostMapping(value = "solicitacao/{uuid}/envio", produces = JSON)
    @Operation(summary = "Envia para a análise", description = "Rascunho ou devolvida (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<ComprasDtos.Solicitacao> enviar(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.enviar(uuid));
    }

    @RequerPermissao({"COMPRAS.GERENCIAR"})
    @PostMapping(value = "solicitacao/{uuid}/analise", produces = JSON, consumes = JSON)
    @Operation(summary = "Analisa a solicitação", description = "Enviada (422): aprovar, devolver ou rejeitar (os dois com o motivo, 400).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<ComprasDtos.Solicitacao> analisar(@PathVariable UUID uuid, @Valid @RequestBody AnaliseSolicitacaoCompraRequestDto dto) {
        return ResponseEntity.ok(service.analisar(uuid, dto));
    }

    @RequerPermissao({"COMPRAS.SOLICITAR", "COMPRAS.GERENCIAR"})
    @PostMapping(value = "solicitacao/{uuid}/cancelamento", produces = JSON, consumes = JSON)
    @Operation(summary = "Cancela a solicitação", description = "Quem pede, antes da aprovação; quem compra, até concluir a pesquisa (422 depois).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<ComprasDtos.Solicitacao> cancelar(@PathVariable UUID uuid, @Valid @RequestBody MotivoRequestDto dto) {
        return ResponseEntity.ok(service.cancelar(uuid, dto.getMotivo()));
    }

    @RequerPermissao({"COMPRAS.SOLICITAR"})
    @GetMapping(value = "sugestao", produces = JSON)
    @Operation(summary = "Sugestão pelo estoque", description = "Materiais abaixo do mínimo na unidade, fora de solicitação em andamento.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<List<ComprasDtos.Sugestao>> sugestao(@RequestParam UUID unidadeId) {
        return ResponseEntity.ok(service.sugestao(unidadeId));
    }

    // ── Pesquisa de preços ──

    @RequerPermissao({"COMPRAS.GERENCIAR"})
    @PostMapping(value = "item/{uuid}/cotacao", produces = JSON, consumes = JSON)
    @Operation(summary = "Registra uma cotação", description = "Solicitação aprovada (422); fornecedor ativo e sem impedimento (422); até 1 ano (400).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<ComprasDtos.Solicitacao> cotar(@PathVariable UUID uuid, @Valid @RequestBody CotacaoCompraRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.cotar(uuid, dto));
    }

    @RequerPermissao({"COMPRAS.GERENCIAR"})
    @DeleteMapping(value = "cotacao/{uuid}", produces = JSON)
    @Operation(summary = "Remove a cotação", description = "Antes de concluir a pesquisa (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<ComprasDtos.Solicitacao> removerCotacao(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.removerCotacao(uuid));
    }

    @RequerPermissao({"COMPRAS.GERENCIAR"})
    @PostMapping(value = "solicitacao/{uuid}/pesquisa", produces = JSON, consumes = JSON)
    @Operation(summary = "Conclui a pesquisa de preços", description = "Todo item cotado; menos de 3 cotações pede a justificativa (422). Fixa a mediana.",
            tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<ComprasDtos.Solicitacao> concluirPesquisa(@PathVariable UUID uuid, @Valid @RequestBody ConclusaoPesquisaRequestDto dto) {
        return ResponseEntity.ok(service.concluirPesquisa(uuid, dto));
    }
}
