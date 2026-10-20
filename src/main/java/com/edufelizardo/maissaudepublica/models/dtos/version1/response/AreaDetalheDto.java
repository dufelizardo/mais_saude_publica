package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.FinalidadeTerritorio;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Uma área com o histórico de equipes e de ACS e os ACS da equipe ainda sem microárea (ADR-0125). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AreaDetalheDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private ArvoreTerritorialDto.AreaNo area;
    private String territorioNome;
    private FinalidadeTerritorio finalidade;
    private String segmentoNome;
    private List<Responsabilidade> responsabilidades;
    private List<Atribuicao> atribuicoes;
    /** ACS vigentes da equipe responsável sem microárea. */
    private List<ArvoreTerritorialDto.AcsRef> acsSemMicroarea;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Responsabilidade implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        private UUID uuid;
        private UUID equipeId;
        private String equipeNome;
        private LocalDate inicio;
        private LocalDate fim;
        private String motivoFim;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Atribuicao implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        private UUID uuid;
        private UUID microareaId;
        private String microareaCodigo;
        private UUID membroId;
        private String matricula;
        private String nome;
        private LocalDate inicio;
        private LocalDate fim;
        private String motivoFim;
    }
}
