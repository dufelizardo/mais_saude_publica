package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/** Troca da equipe ou do técnico de referência (ADR-0120): fecha a referência vigente e abre a nova. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ReferenciaSaudeMentalRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe a equipe de referência.")
    private UUID equipeId;

    @NotBlank(message = "Informe a matrícula do técnico de referência.")
    private String tecnicoMatricula;

    @NotBlank(message = "Informe o motivo da troca.")
    @Size(max = 500, message = "O motivo vai até 500 caracteres.")
    private String motivo;
}
