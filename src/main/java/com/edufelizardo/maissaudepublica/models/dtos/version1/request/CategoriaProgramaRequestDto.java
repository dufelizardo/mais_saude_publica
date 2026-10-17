package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** Uma categoria de programas de saúde (ADR-0118). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class CategoriaProgramaRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Informe o nome.")
    @Size(max = 80, message = "O nome vai até 80 caracteres.")
    private String nome;

    /** Sem valor, vai para o fim. */
    @Min(value = 0, message = "A ordem não pode ser negativa.")
    private Integer ordem;

    /** Sem valor, ativa. */
    private Boolean ativa;
}
