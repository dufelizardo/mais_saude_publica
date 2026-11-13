package com.edufelizardo.maissaudepublica.models.enuns;

/** Lançamento do livro de movimentação do lote de imunobiológico (ADR-0148), em doses. */
public enum TipoMovimentacaoImunobiologico {
    ENTRADA,
    APLICACAO,
    ESTORNO_APLICACAO,
    PERDA_FISICA,
    PERDA_TECNICA,
    AJUSTE_INVENTARIO,
    /** As doses saem para outra sala de vacina (ADR-0206). */
    REMANEJAMENTO_SAIDA,
    REMANEJAMENTO_ENTRADA,
    /** As doses voltam à origem: remanejamento recusado ou cancelado. */
    ESTORNO_REMANEJAMENTO
}
