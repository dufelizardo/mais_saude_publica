package com.edufelizardo.maissaudepublica.models.enuns;

/** O que o fechamento do inventário fez com a divergência (ADR-0217). */
public enum AcaoInventario {
    /** O bem estava em outro setor da unidade: o setor foi corrigido. */
    SETOR_CORRIGIDO,
    /** O bem estava em outra unidade: a transferência ficou pendente. */
    TRANSFERENCIA,
    /** Não encontrado ou inservível: a baixa fica sugerida. */
    BAIXA_SUGERIDA
}
