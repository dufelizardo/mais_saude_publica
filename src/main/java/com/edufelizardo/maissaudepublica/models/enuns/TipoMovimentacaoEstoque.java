package com.edufelizardo.maissaudepublica.models.enuns;

/** Cada lançamento do livro do estoque (ADR-0177); nada se edita nem se apaga. */
public enum TipoMovimentacaoEstoque {
    /** A carga do que já havia na unidade. */
    SALDO_INICIAL,
    /** Nota fiscal, doação ou outra entrada. */
    ENTRADA,
    /** Consumo por um setor da unidade. */
    SAIDA_CONSUMO,
    /** Vencimento, avaria ou extravio. */
    PERDA,
    /** Diferença da contagem, com justificativa. */
    AJUSTE_INVENTARIO,
    /** Saída para outra unidade, pela transferência (ADR-0189). */
    TRANSFERENCIA_SAIDA,
    /** Entrada vinda de outra unidade, no recebimento da transferência. */
    TRANSFERENCIA_ENTRADA,
    /** O que não chegou volta ao lote de origem. */
    TRANSFERENCIA_ESTORNO
}
