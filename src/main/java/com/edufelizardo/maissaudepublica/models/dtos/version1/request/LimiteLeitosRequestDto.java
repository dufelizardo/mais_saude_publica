package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/** Novo limite de leitos de uma capacidade (ADR-0171). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class LimiteLeitosRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe a capacidade.")
    private UUID capacidadeId;
    @NotNull(message = "Informe a quantidade.")
    @PositiveOrZero(message = "A quantidade não pode ser negativa.")
    private Integer quantidade;
    @NotBlank(message = "Informe o motivo.")
    @Size(max = 500, message = "O motivo vai até 500 caracteres.")
    private String motivo;
}
