package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** Copia ofertas e cotas de uma competência para outra (ADR-0179). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class CopiaCompetenciaRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe a competência de origem.")
    private java.time.YearMonth origem;
    @NotNull(message = "Informe a competência de destino.")
    private java.time.YearMonth destino;
}
