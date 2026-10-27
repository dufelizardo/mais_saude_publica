package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.IndicadorProgramatico;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Uma meta do programa (ADR-0141). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class MetaProgramaticaRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe o indicador.")
    private IndicadorProgramatico indicador;

    /** Sem unidade, a rede inteira. */
    private UUID unidadeId;

    @NotNull(message = "Informe o alvo.")
    @PositiveOrZero(message = "O alvo não pode ser negativo.")
    private BigDecimal alvo;

    @NotNull(message = "Informe o início do período.")
    private LocalDate inicio;

    @NotNull(message = "Informe o fim do período.")
    private LocalDate fim;

    /** No indicador "exame no alvo": o exame e o limite (mínimo, máximo ou os dois). */
    private UUID exameId;

    private BigDecimal limiteMinimo;

    private BigDecimal limiteMaximo;
}
