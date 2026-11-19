package com.edufelizardo.maissaudepublica.models.enuns;

/** Fonte do recurso da dotação do Fundo Municipal de Saúde (ADR-0218): a própria do município ou o bloco de financiamento do SUS
 * (Portaria GM/MS 3.992/2017). */
public enum FonteRecursoSaude {
    RECURSOS_PROPRIOS,
    /** Bloco de Manutenção das Ações e Serviços Públicos de Saúde. */
    BLOCO_MANUTENCAO,
    /** Bloco de Estruturação da Rede de Serviços Públicos de Saúde. */
    BLOCO_ESTRUTURACAO,
    TRANSFERENCIA_ESTADUAL,
    OUTRAS
}
