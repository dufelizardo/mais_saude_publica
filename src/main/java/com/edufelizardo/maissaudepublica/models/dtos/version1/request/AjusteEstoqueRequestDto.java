package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** Ajuste do lote pela contagem do inventário (ADR-0177). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AjusteEstoqueRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe o lote.")
    private java.util.UUID loteId;
    @NotNull(message = "Informe a quantidade contada.")
    @PositiveOrZero(message = "A contagem não pode ser negativa.")
    private Integer quantidadeContada;
    @NotBlank(message = "Justifique o ajuste.")
    @Size(max = 500, message = "A justificativa vai até 500 caracteres.")
    private String justificativa;
    private String profissionalMatricula;
}
