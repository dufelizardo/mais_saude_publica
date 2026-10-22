package com.edufelizardo.maissaudepublica.models.enuns;

/**
 * Para onde o acolhimento em saúde mental leva a pessoa (ADR-0120). Acompanhamento na unidade e cuidado compartilhado
 * abrem (ou reaproveitam) o acompanhamento em saúde mental; encaminhamento pede o destino; crise só registra, porque a
 * condução da crise é da fatia SM3.
 */
public enum DesfechoAcolhimentoSaudeMental {
    ACOMPANHAMENTO_NA_UNIDADE,
    CUIDADO_COMPARTILHADO,
    ENCAMINHAMENTO,
    CRISE,
    ORIENTACAO_SEM_SEGUIMENTO;

    public boolean abreAcompanhamento() {
        return this == ACOMPANHAMENTO_NA_UNIDADE || this == CUIDADO_COMPARTILHADO;
    }
}
