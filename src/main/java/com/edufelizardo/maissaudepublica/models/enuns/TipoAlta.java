package com.edufelizardo.maissaudepublica.models.enuns;

/** Como a internação terminou (ADR-0098). */
public enum TipoAlta {
    MELHORADO,
    A_PEDIDO,
    /** Transferido para outra unidade. */
    TRANSFERENCIA,
    EVASAO,
    OBITO
}
