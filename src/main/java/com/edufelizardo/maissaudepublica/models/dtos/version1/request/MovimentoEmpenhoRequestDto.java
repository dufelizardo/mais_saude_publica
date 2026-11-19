package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Reforço ou anulação do empenho (ADR-0218). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class MovimentoEmpenhoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe o tipo.")
    private TipoMovimentoEmpenho tipo;
    @NotNull(message = "Informe o valor.")
    @DecimalMin(value = "0.01", message = "O valor é positivo.")
    @Digits(integer = 13, fraction = 2, message = "O valor tem até 13 dígitos e 2 casas.")
    private BigDecimal valor;
    @NotNull(message = "Informe a data.")
    private LocalDate data;
    @NotBlank(message = "Informe o motivo.")
    @Size(max = 500, message = "O motivo vai até 500 caracteres.")
    private String motivo;
}
