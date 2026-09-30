package com.edufelizardo.maissaudepublica.models.enuns;

/** Se a ação foi feita ou recusada (ADR-0070). */
public enum ResultadoAuditoria {
    PERMITIDO,
    /** 403 (sem permissão ou fora do escopo) ou login recusado. */
    NEGADO
}
