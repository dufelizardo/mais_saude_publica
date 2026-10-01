package com.edufelizardo.maissaudepublica.models.enuns;

/** Cada passo da solicitação vira um evento imutável (ADR-0087). */
public enum TipoEventoRegulacao {
    SOLICITACAO,
    COMPLEMENTO,
    RECLASSIFICACAO,
    AUTORIZACAO,
    DEVOLUCAO,
    NEGATIVA,
    CANCELAMENTO
}
