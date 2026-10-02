package com.edufelizardo.maissaudepublica.models.enuns;

/** Situação do leito no mapa (ADR-0098). */
public enum SituacaoLeito {
    LIVRE,
    OCUPADO,
    /** Depois da saída do paciente, até alguém registrar a limpeza. */
    HIGIENIZACAO,
    /** Fora de uso por um tempo (manutenção, isolamento…), com motivo. */
    BLOQUEADO
}
