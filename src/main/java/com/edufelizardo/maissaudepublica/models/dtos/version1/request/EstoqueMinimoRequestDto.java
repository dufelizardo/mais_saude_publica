package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** Ponto de reposição do material na unidade (ADR-0177). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class EstoqueMinimoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe a unidade.")
    private java.util.UUID unidadeId;
    @NotNull(message = "Informe o material.")
    private java.util.UUID materialId;
    @NotNull(message = "Informe o mínimo.")
    @PositiveOrZero(message = "O mínimo não pode ser negativo.")
    private Integer quantidadeMinima;
}
