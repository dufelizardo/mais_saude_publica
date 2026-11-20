package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** O resultado de um item da contratação (ADR-0213). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ResultadoItemContratacaoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe o resultado.")
    private com.edufelizardo.maissaudepublica.models.enuns.ResultadoItemContratacao resultado;
    private UUID fornecedorId;
    @DecimalMin(value = "0.0001", message = "O preço é maior que zero.")
    @Digits(integer = 10, fraction = 4, message = "O preço vai até 10 dígitos inteiros e 4 decimais.")
    private BigDecimal precoUnitario;
    @Size(max = 1000, message = "A justificativa vai até 1000 caracteres.")
    private String justificativaPreco;
}
