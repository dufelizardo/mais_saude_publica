package com.edufelizardo.maissaudepublica.models.enuns;

/** De onde veio a notificação (ADR-0124). */
public enum OrigemNotificacao {
    ATENDIMENTO,
    INTERNACAO,
    EXAME,
    SAUDE_MENTAL,
    /** Da dose de vacina: ESAVI e erro de imunização (ADR-0150). */
    VACINACAO,
    BUSCA_ATIVA,
    COMUNIDADE,
    OUTRA
}
