package com.edufelizardo.maissaudepublica.models.enuns;

public enum TipoMovimentacaoFarmacia {
    /** Saldo que o lote já tinha quando o livro de movimentação passou a existir (ADR-0057). */
    SALDO_INICIAL,
    ENTRADA,
    DISPENSACAO,
    PERDA,
    AJUSTE_INVENTARIO
}
