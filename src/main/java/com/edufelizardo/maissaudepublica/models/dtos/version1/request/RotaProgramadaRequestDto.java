package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Rota programada da unidade móvel (ADR-0166). Os pontos vão na ordem da rota. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class RotaProgramadaRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Informe o nome da rota.")
    @Size(max = 150, message = "O nome vai até 150 caracteres.")
    private String nome;
    @NotEmpty(message = "Informe os dias da rota.")
    private Set<DayOfWeek> dias;
    private LocalTime horaInicio;
    private LocalTime horaFim;
    @NotEmpty(message = "Informe os pontos da rota.")
    private List<UUID> pontoIds;
}
