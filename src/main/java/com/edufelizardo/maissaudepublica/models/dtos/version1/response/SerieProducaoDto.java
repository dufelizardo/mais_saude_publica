package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.GranularidadeSerie;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/** Série da produção por dia ou por mês, terminando hoje (ADR-0143). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class SerieProducaoDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private GranularidadeSerie granularidade;
    private UUID unidadeId;
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

        /** yyyy-MM-dd no dia, yyyy-MM no mês. */
        private String periodo;
        private long atendimentos;
        private long faltas;
    }
}
