package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.SituacaoObjetivoTerapeutico;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Objetivo de uma versão do PTS (ADR-0121), com meta, prazo, responsável da equipe de referência e situação.
 */
@Entity
@Table(name = "TB_OBJETIVO_TERAPEUTICO", indexes = @Index(name = "ix_objetivo_terapeutico_pts", columnList = "pts_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ObjetivoTerapeutico implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pts_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private ProjetoTerapeuticoSingular pts;

    @Column(nullable = false, updatable = false)
    private int ordem;

    @Column(nullable = false, length = 1000, updatable = false)
    private String descricao;

    @Column(length = 1000, updatable = false)
    private String meta;

    @Column(updatable = false)
    private LocalDate prazo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsavel_id", referencedColumnName = "uuid", updatable = false)
    private Profissional responsavel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private SituacaoObjetivoTerapeutico situacao;
}
