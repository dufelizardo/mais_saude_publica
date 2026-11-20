package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Suplementação ou anulação da dotação (ADR-0218). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AlteracaoDotacaoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe o tipo.")
    private TipoAlteracaoDotacao tipo;
    @NotNull(message = "Informe o valor.")
    @DecimalMin(value = "0.01", message = "O valor é positivo.")
    @Digits(integer = 13, fraction = 2, message = "O valor tem até 13 dígitos e 2 casas.")
    private BigDecimal valor;
    @NotBlank(message = "Informe o ato (decreto ou lei).")
    @Size(max = 60, message = "O ato vai até 60 caracteres.")
    private String ato;
    @NotNull(message = "Informe a data.")
    private LocalDate data;
    @Size(max = 500, message = "O motivo vai até 500 caracteres.")
    private String motivo;
}
