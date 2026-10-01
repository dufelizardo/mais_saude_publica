package com.edufelizardo.maissaudepublica.models.enuns;

/** Checagem de enfermagem de um item prescrito (ADR-0064). */
public enum SituacaoAdministracao {
    /** Dado ao paciente; baixa o lote pelo livro da Farmácia. */
    ADMINISTRADO,
    /** Não foi dado; exige motivo e não mexe no estoque. */
    NAO_ADMINISTRADO
}
