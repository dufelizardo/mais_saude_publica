package com.edufelizardo.maissaudepublica.models.enuns;

/** De onde veio a entrada (ADR-0177). */
public enum OrigemEntradaEstoque {
    /** Com o número do documento. */
    NOTA_FISCAL,
    DOACAO,
    /** A carga inicial da unidade. */
    SALDO_INICIAL,
    OUTRA
}
