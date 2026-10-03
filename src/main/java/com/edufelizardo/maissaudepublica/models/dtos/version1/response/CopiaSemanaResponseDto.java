package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/** Resultado da cópia de semana (ADR-0105): quantos turnos entraram e quais ficaram de fora, com o motivo. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class CopiaSemanaResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private int copiados;
    private List<Ignorado> ignorados;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Ignorado implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private LocalDate data;
        private String profissionalNome;
        private String motivo;
    }
}
