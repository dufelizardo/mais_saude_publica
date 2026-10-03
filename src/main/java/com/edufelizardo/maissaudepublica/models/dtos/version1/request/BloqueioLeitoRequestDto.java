package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
/** Bloqueio de leito, com motivo (ADR-0098). */
public class BloqueioLeitoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Informe o motivo do bloqueio.")
    @Size(max = 500, message = "O motivo vai até 500 caracteres.")
    private String motivo;
}
