package com.edufelizardo.maissaudepublica.models.enuns;

/** De onde vem a liquidação (ADR-0219): o atesto registrado na plataforma ou, sem ele, a avulsa com a justificativa. */
public enum OrigemLiquidacao {
    /** O recebimento definitivo de Compras (ADR-0214). */
    RECEBIMENTO,
    /** A ordem de serviço de manutenção por contrato, concluída (ADR-0216). */
    ORDEM_SERVICO,
    /** A diária ou ajuda de custo do TFD concedida. */
    DIARIA_TFD,
    AVULSA
}
