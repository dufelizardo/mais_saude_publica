package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * A agenda no período (ADR-0143): marcados, realizados, faltas, cancelados, encaixes e o absenteísmo (faltas sobre
 * realizados mais faltas, em %). Os ainda pendentes são os marcados que não são nenhum dos outros.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AgendaProducaoDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private long marcados;
    private long realizados;
    private long faltas;
    private long cancelados;
    private long encaixes;
    /** Nulo quando não houve realizado nem falta. */
    private BigDecimal absenteismo;
}
