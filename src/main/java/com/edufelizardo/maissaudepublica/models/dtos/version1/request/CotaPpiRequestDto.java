package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** Cota da PPI numa oferta (ADR-0179). No ajuste, só a quantidade. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class CotaPpiRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private java.util.UUID beneficiariaId;
    @NotNull(message = "Informe a quantidade.")
    @Positive(message = "A cota precisa ser maior que zero.")
    private Integer quantidade;
}
