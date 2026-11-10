package com.edufelizardo.maissaudepublica.models.enuns;

/** Cada passo do pedido de leito vira um evento imutável (ADR-0184). */
public enum TipoEventoSolicitacaoLeito {
    SOLICITACAO,
    RESERVA,
    VAGA_ZERO,
    EXPIRACAO,
    RECUSA_DESTINO,
    NEGACAO,
    CANCELAMENTO,
    ADMISSAO
}
