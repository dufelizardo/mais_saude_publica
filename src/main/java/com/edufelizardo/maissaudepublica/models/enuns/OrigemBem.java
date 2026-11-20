package com.edufelizardo.maissaudepublica.models.enuns;

/** Como o bem entrou no patrimônio (ADR-0215). */
public enum OrigemBem {
    COMPRA,
    DOACAO,
    /** Cessão de uso ou comodato: o bem é de terceiro, sob a guarda da rede. */
    CESSAO_COMODATO,
    /** Transferido por outro ente (estado, União, outro município). */
    TRANSFERENCIA_ENTE
}
