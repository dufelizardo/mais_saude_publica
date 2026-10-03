package com.edufelizardo.maissaudepublica.models.enuns;

/** Situação operacional da unidade (ADR-0101). Em obra ou inoperante, a agenda não oferece vaga nem aceita marcação. */
public enum SituacaoOperacional {
    EM_OPERACAO,
    /** Funciona com restrição: a agenda só avisa. */
    EM_MANUTENCAO,
    EM_OBRA,
    INOPERANTE;

    /** Fechada para atendimento: em obra ou inoperante. */
    public boolean fechada() {
        return this == EM_OBRA || this == INOPERANTE;
    }
}
