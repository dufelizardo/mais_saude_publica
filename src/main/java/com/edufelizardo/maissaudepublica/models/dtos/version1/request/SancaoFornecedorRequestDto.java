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

/** Sanção ao fornecedor (Lei 14.133/2021, art. 156; ADR-0212). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class SancaoFornecedorRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe o tipo da sanção.")
    private com.edufelizardo.maissaudepublica.models.enuns.TipoSancaoFornecedor tipo;
    @NotNull(message = "Informe o início.")
    private LocalDate inicio;
    /** Impedimento: até 3 anos; inidoneidade: de 3 a 6 anos. */
    private LocalDate fim;
    @DecimalMin(value = "0.01", message = "O valor da multa é maior que zero.")
    private BigDecimal valor;
    @NotBlank(message = "Informe o motivo.")
    @Size(max = 1000, message = "O motivo vai até 1000 caracteres.")
    private String motivo;
    @Size(max = 60, message = "O processo vai até 60 caracteres.")
    private String processo;
}
