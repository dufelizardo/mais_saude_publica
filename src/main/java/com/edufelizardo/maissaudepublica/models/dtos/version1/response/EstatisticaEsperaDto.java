package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/** Média, mediana e percentil 90 de uma espera, em minutos (ADR-0144); nulos sem amostra. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class EstatisticaEsperaDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private long amostra;
    private BigDecimal mediaMinutos;
    private BigDecimal medianaMinutos;
    private BigDecimal p90Minutos;
}
