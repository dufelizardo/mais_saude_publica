package com.edufelizardo.maissaudepublica.models.enuns;

/** Resultado numérico frente à faixa de referência copiada na hora do registro (ADR-0093). */
public enum InterpretacaoResultado {
    NORMAL,
    ACIMA,
    ABAIXO,
    /** Acima do crítico máximo: valor de pânico (ADR-0180). */
    CRITICO_ALTO,
    /** Abaixo do crítico mínimo: valor de pânico (ADR-0180). */
    CRITICO_BAIXO
}
