package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** Desativar o plano na emergência (ADR-0157), com a justificativa. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class DesativacaoPlanoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Size(max = 200, message = "A autoridade vai até 200 caracteres.")
    private String autoridade;
    @NotBlank(message = "Justifique a desativação.")
    @Size(max = 1000, message = "A justificativa vai até 1000 caracteres.")
    private String justificativa;
}
