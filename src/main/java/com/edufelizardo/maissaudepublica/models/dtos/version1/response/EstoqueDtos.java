package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.CategoriaMaterial;
import com.edufelizardo.maissaudepublica.models.enuns.MotivoPerdaEstoque;
import com.edufelizardo.maissaudepublica.models.enuns.OrigemEntradaEstoque;
import com.edufelizardo.maissaudepublica.models.enuns.TipoMovimentacaoEstoque;
import com.edufelizardo.maissaudepublica.models.enuns.UnidadeMedidaMaterial;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Respostas do Estoque e Almoxarifado (ADR-0177). */
public final class EstoqueDtos {

    private EstoqueDtos() {
    }

    /** Material do catálogo; {@code comMovimento} trava o controle de lote. */
    public record Material(UUID uuid, String codigo, String nome, String catmat, CategoriaMaterial categoria, UnidadeMedidaMaterial unidadeMedida,
                           boolean controlaLote, String descricao, boolean ativo, boolean comMovimento) {
    }

    public record Lote(UUID uuid, String numeroLote, LocalDate validade, int quantidade, BigDecimal custoUnitario, boolean vencido,
                       Long diasParaVencer) {
    }

    /** O material na unidade: o total, o mínimo, a próxima validade e os lotes com saldo. */
    public record ItemDeSaldo(UUID materialId, String codigo, String nome, CategoriaMaterial categoria, UnidadeMedidaMaterial unidadeMedida,
                              boolean controlaLote, int total, Integer minimo, boolean abaixoDoMinimo, LocalDate proximaValidade,
                              BigDecimal valor, List<Lote> lotes, int reservado, int disponivel) {
    }

    /** Lote que vence em até 90 dias ou já venceu, com saldo. */
    public record Vencimento(UUID loteId, UUID materialId, String materialNome, String numeroLote, LocalDate validade, int quantidade,
                             long diasParaVencer) {
    }

    /** O estoque da unidade e os alertas: abaixo do mínimo, vencendo e vencidos com saldo. */
    public record Saldo(UUID unidadeId, String unidadeNome, List<ItemDeSaldo> itens, int abaixoDoMinimo, int vencendo, int vencidosComSaldo,
                        BigDecimal valorTotal, List<Vencimento> vencimentos, List<ConsumoSemSaldo> consumosSemSaldo) {
    }

    /** Consumo lançado pelo sistema sem saldo, aguardando a conferência do almoxarife (ADR-0183). */
    public record ConsumoSemSaldo(UUID uuid, UUID materialId, String materialCodigo, String materialNome, int quantidadeFaltante, String origem,
                                  String profissionalNome, Instant ocorridoEm) {
    }

    public record Movimentacao(UUID uuid, TipoMovimentacaoEstoque tipo, UUID materialId, String materialCodigo, String materialNome,
                               UUID unidadeId, String unidadeNome, UUID loteId, String numeroLote, LocalDate validade, int quantidade,
                               int saldoApos, OrigemEntradaEstoque origem, String documento, String fornecedor, String setorNome,
                               MotivoPerdaEstoque motivoPerda, String justificativa, String profissionalNome, Instant registradoEm,
                               UUID emergenciaId) {
    }
}
