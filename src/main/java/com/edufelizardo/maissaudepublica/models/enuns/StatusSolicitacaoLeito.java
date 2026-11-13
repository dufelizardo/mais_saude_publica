package com.edufelizardo.maissaudepublica.models.enuns;

/** Ciclo do pedido de leito na central de regulação de leitos (ADR-0184). */
public enum StatusSolicitacaoLeito {
    SOLICITADA,
    /** Com leito reservado na unidade de destino, ou enviada em vaga zero. */
    RESERVADA,
    ADMITIDA,
    NEGADA,
    CANCELADA
}
