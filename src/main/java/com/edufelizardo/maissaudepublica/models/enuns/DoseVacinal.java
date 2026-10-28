package com.edufelizardo.maissaudepublica.models.enuns;

/** Dose do esquema vacinal (ADR-0147). */
public enum DoseVacinal {
    PRIMEIRA("1ª dose"),
    SEGUNDA("2ª dose"),
    TERCEIRA("3ª dose"),
    QUARTA("4ª dose"),
    REFORCO("Reforço"),
    SEGUNDO_REFORCO("2º reforço"),
    UNICA("Dose única"),
    ANUAL("Dose anual"),
    ADICIONAL("Dose adicional");

    private final String rotulo;

    DoseVacinal(String rotulo) {
        this.rotulo = rotulo;
    }

    public String rotulo() {
        return rotulo;
    }
}
