package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/** Inclusão de uma capacidade assistencial na unidade (ADR-0117). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class CapacidadeDaUnidadeRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe a capacidade.")
    private UUID capacidadeId;

    /** Sem data, hoje. */
    private LocalDate inicio;

    @Size(max = 300, message = "A observação vai até 300 caracteres.")
    private String observacao;
}
