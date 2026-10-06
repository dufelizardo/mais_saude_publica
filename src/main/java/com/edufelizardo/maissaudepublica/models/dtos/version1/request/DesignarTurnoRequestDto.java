package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** Designação de profissional para uma vaga, ou troca do profissional de um turno (ADR-0105). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class DesignarTurnoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Informe a matrícula do profissional.")
    private String profissionalMatricula;

    /** Motivo da troca, para a auditoria. */
    @Size(max = 300, message = "O motivo vai até 300 caracteres.")
    private String motivo;
}
