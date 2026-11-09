package com.edufelizardo.maissaudepublica.models.enuns;

/** Cada passo da ocorrência vira um evento imutável (ADR-0176); os tempos de resposta saem deles. */
public enum TipoEventoUrgencia {
    RECEBIMENTO,
    REGULACAO,
    RECLASSIFICACAO,
    DESTINO,
    ENVIO_DE_RECURSO,
    CHEGADA_AO_LOCAL,
    SAIDA_DO_LOCAL,
    CHEGADA_AO_DESTINO,
    RECEBIMENTO_NO_DESTINO,
    VINCULO_DO_PACIENTE,
    ENCERRAMENTO,
    CANCELAMENTO
}
