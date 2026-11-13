package com.edufelizardo.maissaudepublica.models.enuns;

/** De onde sai e para onde vai o paciente deslocado na emergência (ADR-0193). O domicílio é só origem (voltar é o retorno). */
public enum LocalDeslocamento {
    DOMICILIO,
    UNIDADE_SAUDE,
    ABRIGO,
    CASA_PARENTE,
    OUTRO
}
