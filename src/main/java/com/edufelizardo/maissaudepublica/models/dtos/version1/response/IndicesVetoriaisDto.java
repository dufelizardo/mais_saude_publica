package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Os índices do ciclo bimestral (ADR-0162): IIP e Breteau por área e no município, com a classificação do Ministério da Saúde. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class IndicesVetoriaisDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private int ano;
    private int ciclo;
    private LocalDate inicio;
    private LocalDate fim;
    private Linha municipio;
    private List<Linha> areas;

    /** Os números de uma área (ou do município, sem área). */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Linha implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private UUID areaId;
        private String areaNome;
        private int imoveisVisitados;
        private int imoveisInspecionados;
        private int imoveisPendentes;
        private int imoveisPositivos;
        private int depositosPositivos;
        private Double iip;
        private Double ib;
        private Double percentualPendencia;
        /** SATISFATORIO (IIP abaixo de 1), ALERTA (1 a 3,9), RISCO (4 ou mais); sem inspeção, nulo. */
        private String classificacao;
    }
}
