package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** Saída por consumo (ADR-0177). Sem lote, sai dos que vencem primeiro (FEFO). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class SaidaEstoqueRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe a unidade.")
    private java.util.UUID unidadeId;
    @NotNull(message = "Informe o material.")
    private java.util.UUID materialId;
    private java.util.UUID loteId;
    @NotNull(message = "Informe a quantidade.")
    @Positive(message = "A quantidade precisa ser maior que zero.")
    private Integer quantidade;
    /** O setor que consumiu. */
    private java.util.UUID setorId;
    @Size(max = 500, message = "A observação vai até 500 caracteres.")
    private String justificativa;
    private String profissionalMatricula;
    /** O consumo extraordinário de uma emergência ativa ou em desmobilização (ADR-0191): pode usar a reserva dela. */
    private java.util.UUID emergenciaId;
}
