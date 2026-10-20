package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/**
 * Intervenção prevista numa versão do PTS (ADR-0121): tipo, frequência, responsável e serviço envolvido.
 */
@Entity
@Table(name = "TB_INTERVENCAO_PREVISTA", indexes = @Index(name = "ix_intervencao_prevista_pts", columnList = "pts_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class IntervencaoPrevista implements Serializable {
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private TipoIntervencaoSaudeMental tipo;

    @Column(length = 100, updatable = false)
    private String frequencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsavel_id", referencedColumnName = "uuid", updatable = false)
    private Profissional responsavel;

    /** Serviço envolvido, em texto até o catálogo de serviços e instituições (SM4, SM6). */
    @Column(length = 300, updatable = false)
    private String servico;
}
