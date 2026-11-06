package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** Desativar a capacidade emergencial (ADR-0158). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class DesativacaoCapacidadeRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Justifique a desativação.")
    @Size(max = 500, message = "A justificativa vai até 500 caracteres.")
    private String justificativa;
}
