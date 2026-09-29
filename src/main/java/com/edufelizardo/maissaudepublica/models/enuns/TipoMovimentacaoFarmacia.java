package com.edufelizardo.maissaudepublica.models.enuns;

public enum TipoMovimentacaoFarmacia {
    /** Saldo que o lote já tinha quando o livro de movimentação passou a existir (ADR-0057). */
    SALDO_INICIAL,
    ENTRADA,
    DISPENSACAO,
    PERDA,
    AJUSTE_INVENTARIO,
    /** Saída do lote de origem numa transferência entre unidades (ADR-0059). */
    TRANSFERENCIA_SAIDA,
    /** Entrada no lote da unidade de destino, par da {@link #TRANSFERENCIA_SAIDA} (ADR-0059). */
    TRANSFERENCIA_ENTRADA,
    /** Saldo de um lote duplicado da mesma remessa, levado ao lote que o incorpora (ADR-0060). */
    INCORPORACAO_SAIDA,
    /** Par da {@link #INCORPORACAO_SAIDA}, no lote que incorpora (ADR-0060). */
    INCORPORACAO_ENTRADA
}
