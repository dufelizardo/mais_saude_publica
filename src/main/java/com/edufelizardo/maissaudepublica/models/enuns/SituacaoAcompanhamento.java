package com.edufelizardo.maissaudepublica.models.enuns;

/**
 * Situação do acompanhamento programático (ADR-0119). Ativo e faltoso são vigentes; concluído (alta) e encerrado
 * (transferência, óbito, abandono…) fecham o acompanhamento.
 */
public enum SituacaoAcompanhamento {
    ATIVO,
    FALTOSO,
    CONCLUIDO,
    ENCERRADO;

    public boolean vigente() {
        return this == ATIVO || this == FALTOSO;
    }
}
