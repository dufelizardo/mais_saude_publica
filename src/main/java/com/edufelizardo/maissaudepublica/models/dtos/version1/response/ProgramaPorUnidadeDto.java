package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Um programa por unidade (BI3, ADR-0146): os indicadores da P6 em cada unidade que o oferece, com a meta da unidade ou da rede. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ProgramaPorUnidadeDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID programaId;
    private String nome;
    private LocalDate de;
    private LocalDate ate;
    private List<Linha> unidades;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Linha implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private UUID unidadeId;
        private String unidadeNome;
        private long inscritosAtivos;
        private int metasAbaixo;
        private int metasEmRisco;
        private List<IndicadorApuradoDto> indicadores;
    }
}
