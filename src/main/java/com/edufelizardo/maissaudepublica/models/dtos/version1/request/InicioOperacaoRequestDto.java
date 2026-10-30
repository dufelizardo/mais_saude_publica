package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** Iniciar a operação da emergência planejada (ADR-0160): a justificativa. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class InicioOperacaoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Justifique o início da operação.")
    @Size(max = 1000, message = "A justificativa vai até 1000 caracteres.")
    private String justificativa;
}
