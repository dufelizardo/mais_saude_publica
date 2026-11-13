package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

/** O recebimento do remanejamento no destino, com a temperatura da caixa na chegada (ADR-0206). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class RecebimentoRemanejamentoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID camaraId;
    @NotNull(message = "Informe a temperatura da caixa na chegada.")
    @DecimalMin(value = "-30.0", message = "Confira a temperatura informada.")
    @DecimalMax(value = "40.0", message = "Confira a temperatura informada.")
    private BigDecimal temperaturaChegada;
}
