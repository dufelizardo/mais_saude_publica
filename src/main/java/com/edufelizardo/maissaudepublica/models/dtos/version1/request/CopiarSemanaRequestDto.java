package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/** Cópia dos turnos de uma semana da unidade para outra (ADR-0105). Qualquer dia vale pela semana (segunda a domingo). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class CopiarSemanaRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe a unidade.")
    private UUID unidadeId;

    @NotNull(message = "Informe a semana de origem.")
    private LocalDate origem;

    @NotNull(message = "Informe a semana de destino.")
    private LocalDate destino;
}
