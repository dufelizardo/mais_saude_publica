package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** Iniciar a desmobilização (ADR-0159): quem responde e por quê. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class DesmobilizacaoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Informe o responsável pela desmobilização.")
    @Size(max = 200, message = "O responsável vai até 200 caracteres.")
    private String responsavel;
    @NotBlank(message = "Justifique a desmobilização.")
    @Size(max = 1000, message = "A justificativa vai até 1000 caracteres.")
    private String justificativa;
}
