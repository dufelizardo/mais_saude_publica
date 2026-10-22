package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/** Cuidado compartilhado com outra equipe (ADR-0122). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class CompartilhamentoCuidadoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe a equipe parceira.")
    private UUID equipeId;

    @NotBlank(message = "Descreva o objetivo do compartilhamento.")
    @Size(max = 500, message = "O objetivo vai até 500 caracteres.")
    private String objetivo;
}
