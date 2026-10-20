package com.edufelizardo.maissaudepublica.services.version1;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;

/**
 * Semana epidemiológica (ADR-0124): de domingo a sábado; a semana 1 do ano é a que contém 4 de janeiro, a que tem a
 * maior parte dos dias em janeiro.
 */
public record SemanaEpidemiologica(int ano, int semana) {

    public static SemanaEpidemiologica de(LocalDate data) {
        int ano = data.getYear();
        LocalDate inicio = inicioDoAno(ano);
        if (data.isBefore(inicio)) {
            ano--;
            inicio = inicioDoAno(ano);
        } else if (!data.isBefore(inicioDoAno(ano + 1))) {
            ano++;
            inicio = inicioDoAno(ano);
        }
        return new SemanaEpidemiologica(ano, (int) (ChronoUnit.DAYS.between(inicio, data) / 7) + 1);
    }

    /** O domingo que abre a semana 1 do ano. */
    static LocalDate inicioDoAno(int ano) {
        return LocalDate.of(ano, 1, 4).with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
    }

    public LocalDate inicio() {
        return inicioDoAno(ano).plusWeeks(semana - 1L);
    }

    public LocalDate fim() {
        return inicio().plusDays(6);
    }

    public SemanaEpidemiologica anterior() {
        return de(inicio().minusDays(1));
    }

    /** A semana existe no ano (52 ou 53 semanas). */
    public boolean valida() {
        return semana >= 1 && !inicio().isAfter(inicioDoAno(ano + 1).minusDays(1));
    }
}
