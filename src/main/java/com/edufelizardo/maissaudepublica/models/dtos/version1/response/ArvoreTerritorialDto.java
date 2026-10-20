package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.FinalidadeTerritorio;
import com.edufelizardo.maissaudepublica.models.enuns.TipoEquipe;
import com.edufelizardo.maissaudepublica.models.enuns.TipoSegmentoTerritorial;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * O território inteiro (ADR-0125): segmento → território → área (com a equipe) → microáreas (com o ACS), mais os
 * indicadores e os avisos.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ArvoreTerritorialDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Indicadores indicadores;
    private List<Segmento> segmentos;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Indicadores implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        private int areas;
        private int areasSemEquipe;
        private int microareas;
        private int microareasSemAcs;
        /** Áreas e microáreas ainda sem desenho. */
        private int semGeometria;
        private int acimaDaReferencia;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Segmento implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        private UUID uuid;
        private String codigo;
        private String nome;
        private TipoSegmentoTerritorial tipo;
        private UUID unidadeGestaoId;
        private String unidadeGestaoNome;
        private String geometria;
        private List<TerritorioNo> territorios;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class TerritorioNo implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        private UUID uuid;
        private UUID segmentoId;
        private String nome;
        private FinalidadeTerritorio finalidade;
        private boolean ativo;
        private List<AreaNo> areas;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class AreaNo implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        private UUID uuid;
        private UUID territorioId;
        private String codigo;
        private String codigoCnes;
        private String nome;
        private String geometria;
        /** A equipe responsável vigente; nula, área sem equipe. */
        private EquipeRef equipe;
        private List<MicroareaNo> microareas;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class MicroareaNo implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        private UUID uuid;
        private UUID areaId;
        private String codigo;
        private String nome;
        private String geometria;
        private Integer populacaoReferencia;
        /** O ACS vigente; nulo, microárea sem ACS. */
        private AcsRef acs;
        private List<String> avisos;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class EquipeRef implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        private UUID uuid;
        private String nome;
        private TipoEquipe tipo;
        private UUID unidadeId;
        private String unidadeNome;
        private LocalDate desde;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class AcsRef implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        private UUID membroId;
        private String matricula;
        private String nome;
        private LocalDate desde;
    }
}
