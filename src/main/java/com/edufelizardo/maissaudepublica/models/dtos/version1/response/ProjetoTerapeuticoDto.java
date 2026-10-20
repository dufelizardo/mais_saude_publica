package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.PeriodicidadeAcompanhamento;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoObjetivoTerapeutico;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Uma versão do PTS (ADR-0121), com os objetivos e as intervenções previstas. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ProjetoTerapeuticoDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private int versao;
    private boolean vigente;
    private String necessidades;
    private PeriodicidadeAcompanhamento periodicidadeRevisao;
    private LocalDate revisarEm;
    private boolean revisaoVencida;
    private boolean pactuadoComPessoa;
    private String pactuacaoObservacao;
    private String sinteseRevisao;
    private Instant elaboradoEm;
    private String profissionalNome;
    private List<Objetivo> objetivos;
    private List<Prevista> intervencoesPrevistas;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Objetivo implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private UUID uuid;
        private String descricao;
        private String meta;
        private LocalDate prazo;
        private String responsavelMatricula;
        private String responsavelNome;
        private SituacaoObjetivoTerapeutico situacao;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Prevista implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private UUID uuid;
        private UUID tipoId;
        private String tipoNome;
        private String frequencia;
        private String responsavelMatricula;
        private String responsavelNome;
        private String servico;
    }
}
