package com.edufelizardo.maissaudepublica.models.enuns;

/** Tipo da manutenção (ADR-0216): a corretiva nasce do chamado; as outras, do plano (RDC 509/2021). */
public enum TipoManutencao {
    CORRETIVA,
    PREVENTIVA,
    CALIBRACAO,
    /** Teste de segurança elétrica do equipamento médico (NBR IEC 62353). */
    SEGURANCA_ELETRICA
}
