package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AjusteEstoqueRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EntradaEstoqueRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EstoqueMinimoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.MaterialEstoqueRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.PerdaEstoqueRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.SaidaEstoqueRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.EstoqueDtos;
import com.edufelizardo.maissaudepublica.models.enuns.CategoriaMaterial;
import com.edufelizardo.maissaudepublica.models.enuns.TipoMovimentacaoEstoque;
import com.edufelizardo.maissaudepublica.services.version1.EstoqueService;
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
@RequestMapping(value = "/api/v1/estoque/")
@Tag(name = "Estoque e almoxarifado", description = "Catálogo de materiais, estoque por unidade em lotes, livro de movimentação e alertas "
        + "(ver docs/adr/0177-estoque-e-almoxarifado.md).")
public class EstoqueController {

    private static final String TAG = "Estoque e almoxarifado";
    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;

    @Autowired
    private EstoqueService service;

    @RequerPermissao({"ESTOQUE.CONSULTAR", "ESTOQUE.MOVIMENTAR", "ESTOQUE.GERENCIAR"})
    @GetMapping(value = "material", produces = JSON)
    @Operation(summary = "Catálogo de materiais", description = "Por nome; ativos=true só os ativos.", tags = TAG)
    @ApiErrorResponsesListagem
    public ResponseEntity<List<EstoqueDtos.Material>> materiais(@RequestParam(required = false) Boolean ativos) {
        return ResponseEntity.ok(service.materiais(ativos));
    }

    @RequerPermissao({"ESTOQUE.GERENCIAR"})
    @PostMapping(value = "material", produces = JSON, consumes = JSON)
    @Operation(summary = "Cadastra um material", description = "Código e nome únicos (409).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<EstoqueDtos.Material> criarMaterial(@Valid @RequestBody MaterialEstoqueRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criarMaterial(dto));
    }

    @RequerPermissao({"ESTOQUE.GERENCIAR"})
    @PutMapping(value = "material/{uuid}", produces = JSON, consumes = JSON)
    @Operation(summary = "Atualiza um material", description = "O controle de lote não muda depois da primeira entrada (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<EstoqueDtos.Material> atualizarMaterial(@PathVariable UUID uuid, @Valid @RequestBody MaterialEstoqueRequestDto dto) {
        return ResponseEntity.ok(service.atualizarMaterial(uuid, dto));
    }

    @RequerPermissao({"ESTOQUE.GERENCIAR"})
    @PutMapping(value = "minimo", produces = JSON, consumes = JSON)
    @Operation(summary = "Define o estoque mínimo", description = "O ponto de reposição do material na unidade.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<EstoqueDtos.ItemDeSaldo> minimo(@Valid @RequestBody EstoqueMinimoRequestDto dto) {
        return ResponseEntity.ok(service.definirMinimo(dto));
    }

    @RequerPermissao({"ESTOQUE.MOVIMENTAR"})
    @PostMapping(value = "entrada", produces = JSON, consumes = JSON)
    @Operation(summary = "Entrada de material", description = "Nota fiscal (com o número), doação, saldo inicial ou outra. Material com lote pede lote e "
            + "validade (400); lote vencido ou com outra validade: 422.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<EstoqueDtos.Movimentacao> entrada(@Valid @RequestBody EntradaEstoqueRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.entrada(dto));
    }

    @RequerPermissao({"ESTOQUE.MOVIMENTAR"})
    @PostMapping(value = "saida", produces = JSON, consumes = JSON)
    @Operation(summary = "Saída por consumo", description = "Sem lote, dos que vencem primeiro (um lançamento por lote). Lote vencido, saldo "
            + "insuficiente ou setor de outra unidade: 422.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<List<EstoqueDtos.Movimentacao>> saida(@Valid @RequestBody SaidaEstoqueRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.saida(dto));
    }

    @RequerPermissao({"ESTOQUE.MOVIMENTAR"})
    @PostMapping(value = "perda", produces = JSON, consumes = JSON)
    @Operation(summary = "Perda", description = "Vencimento, avaria, extravio ou outro; mais que o saldo: 422.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<EstoqueDtos.Movimentacao> perda(@Valid @RequestBody PerdaEstoqueRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.perda(dto));
    }

    @RequerPermissao({"ESTOQUE.MOVIMENTAR"})
    @PostMapping(value = "ajuste", produces = JSON, consumes = JSON)
    @Operation(summary = "Ajuste de inventário", description = "Pela contagem, com justificativa; contagem igual ao saldo: 422.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<EstoqueDtos.Movimentacao> ajuste(@Valid @RequestBody AjusteEstoqueRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.ajuste(dto));
    }

    @RequerPermissao({"ESTOQUE.MOVIMENTAR"})
    @PostMapping(value = "consumo-sem-saldo/{uuid}/conferencia", produces = JSON)
    @Operation(summary = "Confere um consumo sem saldo", description = "O almoxarife acertou a entrada ou o inventário; o consumo sai do alerta. "
            + "Já conferido: 422 (ADR-0183).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<Void> conferirConsumoSemSaldo(@PathVariable UUID uuid) {
        service.conferirConsumoSemSaldo(uuid);
        return ResponseEntity.noContent().build();
    }

    @RequerPermissao({"ESTOQUE.CONSULTAR", "ESTOQUE.MOVIMENTAR", "ESTOQUE.GERENCIAR"})
    @GetMapping(value = "saldo", produces = JSON)
    @Operation(summary = "Saldo da unidade", description = "Por material, com os lotes, o mínimo e os alertas (abaixo do mínimo, vencendo em até 90 dias, "
            + "vencidos com saldo) e o valor do estoque. Filtro: categoria.", tags = TAG)
    @ApiErrorResponsesListagem
    public ResponseEntity<EstoqueDtos.Saldo> saldo(@RequestParam UUID unidadeId, @RequestParam(required = false) CategoriaMaterial categoria) {
        return ResponseEntity.ok(service.saldo(unidadeId, categoria));
    }

    @RequerPermissao({"ESTOQUE.CONSULTAR", "ESTOQUE.MOVIMENTAR", "ESTOQUE.GERENCIAR"})
    @GetMapping(value = "movimentacao", produces = JSON)
    @Operation(summary = "Livro de movimentação", description = "Mais recente primeiro. Filtros: unidadeId, materialId, tipo, dias (30, até 365).", tags = TAG)
    @ApiErrorResponsesListagem
    public ResponseEntity<List<EstoqueDtos.Movimentacao>> movimentacoes(@RequestParam(required = false) UUID unidadeId,
                                                                        @RequestParam(required = false) UUID materialId,
                                                                        @RequestParam(required = false) TipoMovimentacaoEstoque tipo,
                                                                        @RequestParam(required = false) Integer dias) {
        return ResponseEntity.ok(service.movimentacoes(unidadeId, materialId, tipo, dias));
    }
}
