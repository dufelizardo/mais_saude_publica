package com.edufelizardo.maissaudepublica.models.enuns;

/** Com que frequência o programa prevê acompanhar cada pessoa (ADR-0118); a P2 usa para apontar os atrasados (ADR-0119). */
public enum PeriodicidadeAcompanhamento {
    MENSAL(30),
    BIMESTRAL(60),
    TRIMESTRAL(90),
    QUADRIMESTRAL(120),
    SEMESTRAL(180),
    ANUAL(365),
    SEM_PERIODICIDADE(null);

    private final Integer dias;

    PeriodicidadeAcompanhamento(Integer dias) {
        this.dias = dias;
    }

    /** Intervalo esperado entre atendimentos; nulo, sem periodicidade (ninguém fica atrasado). */
    public Integer dias() {
        return dias;
    }
}
