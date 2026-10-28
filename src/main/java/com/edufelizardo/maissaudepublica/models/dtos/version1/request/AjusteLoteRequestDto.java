package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** Ajuste de inventário do lote (ADR-0148): o saldo contado, com a justificativa. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AjusteLoteRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull
    @PositiveOrZero
    private Integer saldo;
    @NotBlank
    @Size(max = 500)
    private String justificativa;
}
