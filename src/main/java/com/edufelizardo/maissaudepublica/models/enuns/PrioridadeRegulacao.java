package com.edufelizardo.maissaudepublica.models.enuns;

/**
 * Classificação da solicitação na fila, nas quatro cores usadas pelas centrais de regulação (ADR-0087).
 * A ordem da declaração é a ordem da fila: vermelho primeiro.
 */
public enum PrioridadeRegulacao {
    /** Emergência: risco imediato, não deveria esperar a fila eletiva. */
    VERMELHO,
    /** Urgente. */
    AMARELO,
    /** Não urgente, com prioridade sobre o eletivo. */
    VERDE,
    /** Eletivo. */
    AZUL
}
