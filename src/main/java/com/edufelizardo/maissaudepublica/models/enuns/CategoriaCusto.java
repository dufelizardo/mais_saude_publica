package com.edufelizardo.maissaudepublica.models.enuns;

/** A categoria do custo na apuração mensal (ADR-0220), cada uma com a fonte de onde vem. */
public enum CategoriaCusto {
    /** A folha do RH na competência, pela lotação vigente. */
    PESSOAL,
    /** As saídas de consumo do Estoque pelo custo do lote. */
    MATERIAL_CONSUMO,
    /** As dispensações e administrações da Farmácia pelo preço do contrato do lote recebido. */
    MEDICAMENTOS,
    /** As liquidações de serviço de contrato: a parte de serviço da ordem de fornecimento e a ordem de serviço por contrato. */
    SERVICOS_TERCEIROS,
    /** A mão de obra e as peças compradas das ordens de serviço internas concluídas. */
    MANUTENCAO,
    /** A parcela do mês dos bens tombados (linear, MCASP). */
    DEPRECIACAO,
    /** As diárias do TFD liquidadas. */
    DIARIAS_TFD,
    /** As liquidações avulsas, pelo centro de custo informado. */
    OUTRAS
}
