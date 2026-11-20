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

/** Uma cotação da pesquisa de preços (ADR-0212). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class CotacaoCompraRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe a fonte.")
    private com.edufelizardo.maissaudepublica.models.enuns.FonteCotacao fonte;
    private UUID fornecedorId;
    @Size(max = 300, message = "A referência vai até 300 caracteres.")
    private String referencia;
    @NotNull(message = "Informe o preço unitário.")
    @DecimalMin(value = "0.0001", message = "O preço é maior que zero.")
    @Digits(integer = 10, fraction = 4, message = "O preço vai até 10 dígitos inteiros e 4 decimais.")
    private BigDecimal precoUnitario;
    @NotNull(message = "Informe a data da cotação.")
    private LocalDate cotadoEm;
}
