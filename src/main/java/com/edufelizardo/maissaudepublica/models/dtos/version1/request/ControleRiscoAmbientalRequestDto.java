package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** O risco ambiental controlado, com o motivo (ADR-0164). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ControleRiscoAmbientalRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Informe como o risco foi controlado.")
    @Size(max = 1000, message = "O motivo vai até 1000 caracteres.")
    private String motivo;
}
