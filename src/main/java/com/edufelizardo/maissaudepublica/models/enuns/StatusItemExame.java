package com.edufelizardo.maissaudepublica.models.enuns;

/** Situação de cada exame do pedido (ADR-0093). */
public enum StatusItemExame {
    SOLICITADO,
    COLETADO,
    /** Exame de imagem realizado, aguardando o laudo (ADR-0182). */
    REALIZADO,
    RESULTADO_REGISTRADO,
    LIBERADO,
    CANCELADO
}
