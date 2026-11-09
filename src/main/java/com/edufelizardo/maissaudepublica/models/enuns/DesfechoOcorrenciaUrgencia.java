package com.edufelizardo.maissaudepublica.models.enuns;

/** Como a ocorrência terminou (ADR-0176). */
public enum DesfechoOcorrenciaUrgencia {
    /** Orientação por telefone. */
    ORIENTADO,
    /** Atendido no local, sem remoção. */
    ATENDIDO_NO_LOCAL,
    /** Chegou à unidade de destino e foi recebido lá. */
    RECEBIDO_NO_DESTINO,
    OBITO_NO_LOCAL,
    /** O paciente recusou o atendimento ou a remoção. */
    RECUSA,
    /** Engano ou trote: não havia ocorrência. */
    SEM_OCORRENCIA,
    /** Quem chamou desistiu antes da chegada. */
    DESISTENCIA
}
