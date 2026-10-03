package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalTime;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** Um turno de funcionamento num dia da semana (ADR-0101). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class TurnoHorarioDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe o dia da semana do turno.")
    private DayOfWeek diaSemana;

    @NotNull(message = "Informe a hora em que o turno abre.")
    private LocalTime abre;

    @NotNull(message = "Informe a hora em que o turno fecha.")
    private LocalTime fecha;
}
