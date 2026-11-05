package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** A medida tomada diante da amostra fora do padrão (ADR-0164). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class MedidaAmostraAguaRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Informe a medida.")
    @Size(max = 1000, message = "A medida vai até 1000 caracteres.")
    private String medida;
}
