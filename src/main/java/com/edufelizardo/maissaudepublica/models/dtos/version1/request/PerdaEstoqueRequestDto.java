package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.MotivoPerdaEstoque;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** Perda de material de um lote (ADR-0177). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class PerdaEstoqueRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe o lote.")
    private java.util.UUID loteId;
    @NotNull(message = "Informe a quantidade.")
    @Positive(message = "A quantidade precisa ser maior que zero.")
    private Integer quantidade;
    @NotNull(message = "Informe o motivo.")
    private MotivoPerdaEstoque motivo;
    @Size(max = 500, message = "A justificativa vai até 500 caracteres.")
    private String justificativa;
    private String profissionalMatricula;
}
