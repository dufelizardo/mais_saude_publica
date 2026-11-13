package com.edufelizardo.maissaudepublica.models.enuns;

/**
 * Resultado numérico frente à faixa de referência copiada na hora do registro (ADR-0093); no laudo de imagem, a classificação
 * do radiologista: sem alterações (NORMAL), com alterações ou achado crítico (ADR-0182).
 */
public enum InterpretacaoResultado {
    NORMAL,
    ACIMA,
    ABAIXO,
    /** Acima do crítico máximo: valor de pânico (ADR-0180). */
    CRITICO_ALTO,
    /** Abaixo do crítico mínimo: valor de pânico (ADR-0180). */
    CRITICO_BAIXO,
    /** Laudo de imagem com alterações (ADR-0182). */
    ALTERADO,
    /** Laudo de imagem com achado crítico: segue a comunicação de crítico da ADR-0181 (ADR-0182). */
    ACHADO_CRITICO
}
