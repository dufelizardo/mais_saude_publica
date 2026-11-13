package com.edufelizardo.maissaudepublica.models.enuns;

/** Ciclo da internação eletiva agendada (ADR-0185). */
public enum StatusInternacaoAgendada {
    AGENDADA,
    /** Com leito reservado para o dia previsto. */
    RESERVADA,
    ADMITIDA,
    /** O paciente não compareceu. */
    FALTA,
    CANCELADA
}
