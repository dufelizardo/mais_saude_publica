package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** O encerramento da emergência (ADR-0156), com o motivo. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class EncerramentoEmergenciaRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Informe o motivo do encerramento.")
    @Size(max = 1000, message = "O motivo vai até 1000 caracteres.")
    private String motivo;
}
