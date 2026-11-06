package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.SituacaoOperacional;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/** O que o sistema já sabe do impacto (ADR-0156), para a avaliação: só sugestão, editável. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class SugestaoImpactoDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /** Pessoas cadastradas nas microáreas afetadas; sem cadastro, a população de referência delas. */
    private Integer populacaoAfetada;
    private int microareas;
    private List<Unidade> unidades;
    private int leitosIndisponiveis;

    /** Unidade de saúde dentro do território afetado. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Unidade implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private UUID uuid;
        private String nome;
        private SituacaoOperacional situacaoOperacional;
        private int leitos;
        private int leitosIndisponiveis;
    }
}
