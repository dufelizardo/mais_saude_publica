package com.edufelizardo.maissaudepublica.models.enuns;

/** A decisão do médico regulador (ADR-0176). */
public enum DecisaoRegulacaoUrgencia {
    /** Orientação médica por telefone, sem envio: a ocorrência fecha como orientada. */
    ORIENTACAO,
    /** Envia uma viatura ou equipe ao local; o destino pode ser definido depois. */
    ENVIO_DE_RECURSO,
    /** O paciente vai por meios próprios a uma unidade indicada. */
    ENCAMINHAMENTO,
    /** De unidade para unidade, com ou sem recurso da central. */
    TRANSFERENCIA
}
