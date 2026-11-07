package com.edufelizardo.maissaudepublica.models.enuns;

/** Tipo do canal (ADR-0168). Só Portal e E-mail têm envio; os demais entram com a C5. */
public enum TipoCanalComunicacao {
    PORTAL,
    APLICATIVO,
    EMAIL,
    SMS,
    NOTIFICACAO,
    PAINEL,
    IMPRESSO,
    EVENTO,
    REDE_SOCIAL
}
