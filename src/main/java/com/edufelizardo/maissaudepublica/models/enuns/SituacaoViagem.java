package com.edufelizardo.maissaudepublica.models.enuns;

/** Situação da viagem do transporte eletivo (ADR-0190). */
public enum SituacaoViagem {
    PROGRAMADA,
    EM_ANDAMENTO,
    CONCLUIDA,
    CANCELADA;

    public boolean aberta() {
        return this == PROGRAMADA || this == EM_ANDAMENTO;
    }
}
