package com.edufelizardo.maissaudepublica.models.enuns;

/** Motivo da baixa do bem (ADR-0217). */
public enum MotivoBaixaBem {
    INSERVIVEL,
    EXTRAVIO,
    /** Exige o número do boletim de ocorrência. */
    FURTO_ROUBO,
    DOACAO,
    ALIENACAO,
    /** O bem cedido ou em comodato volta ao dono. */
    DEVOLUCAO_CESSAO
}
