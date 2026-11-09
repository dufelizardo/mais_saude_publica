package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** Oferta da executante na competência (ADR-0179). No ajuste, executante, procedimento e competência não mudam. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class OfertaAssistencialRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private java.util.UUID executanteId;
    private java.util.UUID procedimentoId;
    /** "2026-11". */
    private java.time.YearMonth competencia;
    @NotNull(message = "Informe a quantidade.")
    @PositiveOrZero(message = "A quantidade não pode ser negativa.")
    private Integer quantidade;
    @Size(max = 500, message = "A observação vai até 500 caracteres.")
    private String observacao;
}
