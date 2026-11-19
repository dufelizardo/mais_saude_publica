package com.edufelizardo.maissaudepublica.models.enuns;

/** A base do rateio de um centro administrativo ou de apoio para os destinos da unidade (ADR-0221, PNGC). */
public enum CriterioRateio {
    /** As horas semanais das lotações vigentes no centro. */
    PESSOAS,
    /** O custo direto do centro de destino. */
    CUSTO_DIRETO,
    /** Os m² informados no centro. */
    AREA,
    /** As saídas de consumo do Estoque no mês para o setor do centro. */
    REQUISICOES
}
