package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.IndicadorProgramatico;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/** A evolução mensal de um indicador programático (ADR-0141). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class SerieIndicadorDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private IndicadorProgramatico indicador;
    private String nome;
    private boolean aplicavel;
    private String motivo;
    private List<Ponto> pontos;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Ponto implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        /** O mês, como 2026-10. */
        private String mes;
        private long numerador;
        private Long denominador;
        private BigDecimal valor;
    }
}
