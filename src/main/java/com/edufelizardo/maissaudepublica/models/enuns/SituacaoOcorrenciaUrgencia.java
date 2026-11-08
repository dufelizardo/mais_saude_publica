package com.edufelizardo.maissaudepublica.models.enuns;

/** Ciclo da ocorrência de urgência (ADR-0176). CONCLUIDA e CANCELADA fecham. */
public enum SituacaoOcorrenciaUrgencia {
    /** Aberta pelo atendente, aguardando a regulação médica. */
    RECEBIDA,
    /** O médico regulador classificou e decidiu. */
    REGULADA,
    /** O recurso saiu para o local. */
    EM_ATENDIMENTO,
    /** Com desfecho. */
    CONCLUIDA,
    /** Duplicada ou aberta por engano, antes do envio do recurso. */
    CANCELADA
}
