package com.edufelizardo.maissaudepublica.services.version1;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/** Semana epidemiológica (ADR-0124): domingo a sábado; a semana 1 é a que contém 4 de janeiro. */
class SemanaEpidemiologicaTest {

    @Test
    void semanaUmContemQuatroDeJaneiro() {
        // 4/1/2026 é domingo: abre a semana 1 de 2026.
        assertThat(SemanaEpidemiologica.de(LocalDate.of(2026, 1, 4))).isEqualTo(new SemanaEpidemiologica(2026, 1));
        // 1/1/2026 ainda é da última semana de 2025, que teve 53 semanas (começou em 29/12/2024).
        assertThat(SemanaEpidemiologica.de(LocalDate.of(2026, 1, 1))).isEqualTo(new SemanaEpidemiologica(2025, 53));
        assertThat(SemanaEpidemiologica.de(LocalDate.of(2024, 12, 29))).isEqualTo(new SemanaEpidemiologica(2025, 1));
        assertThat(SemanaEpidemiologica.de(LocalDate.of(2026, 10, 20))).isEqualTo(new SemanaEpidemiologica(2026, 42));
    }

    @Test
    void inicioFimEAnterior() {
        SemanaEpidemiologica se = new SemanaEpidemiologica(2026, 42);
        assertThat(se.inicio()).isEqualTo(LocalDate.of(2026, 10, 18));
        assertThat(se.fim()).isEqualTo(LocalDate.of(2026, 10, 24));
        assertThat(new SemanaEpidemiologica(2026, 1).anterior()).isEqualTo(new SemanaEpidemiologica(2025, 53));
        assertThat(new SemanaEpidemiologica(2025, 53).valida()).isTrue();
        assertThat(new SemanaEpidemiologica(2026, 53).valida()).isFalse();
        assertThat(new SemanaEpidemiologica(2026, 0).valida()).isFalse();
    }
}
