package com.edufelizardo.maissaudepublica.models.enuns;

import java.time.Period;

/** Unidade da idade nas faixas de referência (ADR-0180): recém-nascido em dias, criança em meses, adulto em anos. */
public enum UnidadeIdade {
    DIAS,
    MESES,
    ANOS;

    public Period periodo(int valor) {
        return switch (this) {
            case DIAS -> Period.ofDays(valor);
            case MESES -> Period.ofMonths(valor);
            case ANOS -> Period.ofYears(valor);
        };
    }

    /** Dias aproximados, só para comparar faixas entre si (a escolha da faixa usa o calendário). */
    public double diasAproximados(int valor) {
        return switch (this) {
            case DIAS -> valor;
            case MESES -> valor * 30.4375;
            case ANOS -> valor * 365.25;
        };
    }
}
