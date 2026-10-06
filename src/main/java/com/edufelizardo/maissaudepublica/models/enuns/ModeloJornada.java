package com.edufelizardo.maissaudepublica.models.enuns;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.time.DayOfWeek.FRIDAY;
import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;
import static java.time.DayOfWeek.THURSDAY;
import static java.time.DayOfWeek.TUESDAY;
import static java.time.DayOfWeek.WEDNESDAY;

/**
 * Modelos de jornada para gerar a semana de um profissional (ADR-0107): os dias, as horas trabalhadas em cada um, o início
 * e o intervalo sugeridos. O intervalo não conta na jornada (CLT, art. 71): 40h são cinco dias de 8h, das 08:00 às 17:00
 * com 1h de intervalo.
 */
public enum ModeloJornada {
    H40_8H(LocalTime.of(8, 0), 60),
    H44_6X1(LocalTime.of(8, 0), 60),
    H30_6H(LocalTime.of(7, 0), 15),
    H20_4H(LocalTime.of(8, 0), 0),
    /** Plantões de 12h em dias alternados; na semana, segunda, quarta, sexta e domingo. Só em unidade 24 horas. */
    H12X36(LocalTime.of(7, 0), 0);

    private final LocalTime inicio;
    private final int intervalo;

    ModeloJornada(LocalTime inicio, int intervalo) {
        this.inicio = inicio;
        this.intervalo = intervalo;
    }

    public LocalTime inicio() {
        return inicio;
    }

    public int intervalo() {
        return intervalo;
    }

    public boolean plantao() {
        return this == H12X36;
    }

    /** Minutos trabalhados em cada dia da semana; o dia que não aparece é folga. */
    public Map<DayOfWeek, Integer> dias() {
        Map<DayOfWeek, Integer> dias = new LinkedHashMap<>();
        switch (this) {
            case H40_8H -> diasUteis(dias, 8 * 60);
            case H44_6X1 -> {
                diasUteis(dias, 8 * 60);
                dias.put(SATURDAY, 4 * 60);
            }
            case H30_6H -> diasUteis(dias, 6 * 60);
            case H20_4H -> diasUteis(dias, 4 * 60);
            case H12X36 -> {
                for (DayOfWeek d : new DayOfWeek[]{MONDAY, WEDNESDAY, FRIDAY, SUNDAY}) {
                    dias.put(d, 12 * 60);
                }
            }
        }
        return dias;
    }

    private static void diasUteis(Map<DayOfWeek, Integer> dias, int minutos) {
        for (DayOfWeek d : new DayOfWeek[]{MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY}) {
            dias.put(d, minutos);
        }
    }
}
