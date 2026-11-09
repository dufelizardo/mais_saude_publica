package com.edufelizardo.maissaudepublica.models.enuns;

/** Situação do veículo, com histórico (ADR-0178). */
public enum SituacaoVeiculo {
    DISPONIVEL,
    /** Empenhado numa ocorrência; vem do SAMU, não se marca à mão. */
    EM_SERVICO,
    MANUTENCAO,
    INATIVO
}
