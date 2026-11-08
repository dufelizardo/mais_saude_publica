package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.OrigemEntradaEstoque;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** Entrada de material na unidade (ADR-0177). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class EntradaEstoqueRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe a unidade.")
    private java.util.UUID unidadeId;
    @NotNull(message = "Informe o material.")
    private java.util.UUID materialId;
    @NotNull(message = "Informe a quantidade.")
    @Positive(message = "A quantidade precisa ser maior que zero.")
    private Integer quantidade;
    @NotNull(message = "Informe a origem.")
    private OrigemEntradaEstoque origem;
    @Size(max = 60, message = "O lote vai até 60 caracteres.")
    private String numeroLote;
    private java.time.LocalDate validade;
    @PositiveOrZero(message = "O custo não pode ser negativo.")
    private java.math.BigDecimal custoUnitario;
    @Size(max = 60, message = "O documento vai até 60 caracteres.")
    private String documento;
    @Size(max = 150, message = "O fornecedor vai até 150 caracteres.")
    private String fornecedor;
    private String profissionalMatricula;
}
