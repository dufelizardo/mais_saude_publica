package com.edufelizardo.maissaudepublica.models.enuns;

import java.util.Set;

/**
 * Ciclo da solicitação de regulação (ADR-0087). AGENDADA, REALIZADA e FALTOU são do fechamento do
 * ciclo, com o agendamento na unidade executante e a contrarreferência (ADR-0089).
 */
public enum StatusSolicitacaoRegulacao {
    /** Na fila, aguardando o regulador. */
    SOLICITADA,
    /** Voltou ao solicitante para complementar; ao complementar, volta à fila na posição original. */
    DEVOLVIDA,
    AUTORIZADA,
    NEGADA,
    AGENDADA,
    REALIZADA,
    FALTOU,
    CANCELADA;

    /** Ainda em curso: impede uma segunda solicitação do mesmo procedimento para o mesmo paciente. */
    public static final Set<StatusSolicitacaoRegulacao> EM_ABERTO = Set.of(SOLICITADA, DEVOLVIDA, AUTORIZADA, AGENDADA);

    /** O que ainda pode ser cancelado. */
    public static final Set<StatusSolicitacaoRegulacao> CANCELAVEIS = Set.of(SOLICITADA, DEVOLVIDA, AUTORIZADA);
}
