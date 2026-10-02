package com.edufelizardo.maissaudepublica.models.enuns;

/** Cada passo do pedido de exame vira um evento imutável (ADR-0093). */
public enum TipoEventoExame {
    SOLICITACAO,
    COLETA,
    REJEICAO_AMOSTRA,
    RESULTADO,
    LIBERACAO,
    RETIFICACAO,
    CANCELAMENTO
}
