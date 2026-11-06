package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** A vacinação antirrábica animal do ano (ADR-0163): os totais, por área e os registros. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class VacinacaoAntirrabicaDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private int ano;
    private int caes;
    private int gatos;
    private List<Area> areas;
    private List<Registro> registros;

    /** Os totais de uma área. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Area implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private UUID areaId;
        private String areaNome;
        private int caes;
        private int gatos;
    }

    /** Um registro do posto. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Registro implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private UUID uuid;
        private LocalDate data;
        private String areaNome;
        private String microareaCodigo;
        private String posto;
        private int caes;
        private int gatos;
        private String responsavel;
    }
}
