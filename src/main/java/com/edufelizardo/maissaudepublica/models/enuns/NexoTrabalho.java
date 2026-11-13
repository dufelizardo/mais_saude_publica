package com.edufelizardo.maissaudepublica.models.enuns;

/** O nexo do agravo com o trabalho, concluído pela investigação (ADR-0208). */
public enum NexoTrabalho {
    /** Ainda sem conclusão. */
    EM_ANALISE,
    /** Relacionado ao trabalho. */
    CONFIRMADO,
    /** Sem relação com o trabalho: sai dos indicadores do trabalho. */
    DESCARTADO,
    /** Sem elementos para concluir. */
    INCONCLUSIVO
}
