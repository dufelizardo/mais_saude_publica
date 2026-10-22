package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Avaliação em saúde mental de um caso (ADR-0120): contexto e história, aspectos psicossociais, fatores de risco e de
 * proteção, necessidades e, quando couber, a hipótese diagnóstica em CID-10 do modelo clínico geral (sem catálogo de
 * transtornos próprio). Várias por caso ao longo do tempo; só inclusão.
 */
@Entity
@Table(name = "TB_AVALIACAO_SAUDE_MENTAL", indexes = {
        @Index(name = "ix_avaliacao_saude_mental_acomp", columnList = "acompanhamento_id, realizada_em")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AvaliacaoSaudeMental implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acompanhamento_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private AcompanhamentoSaudeMental acompanhamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id", referencedColumnName = "uuid", updatable = false)
    private Profissional profissional;

    @Column(nullable = false, updatable = false)
    private Instant realizadaEm;

    @Column(nullable = false, length = 4000, updatable = false)
    private String contextoHistoria;

    @Column(length = 4000, updatable = false)
    private String aspectosPsicossociais;

    @Column(length = 2000, updatable = false)
    private String fatoresRisco;

    @Column(length = 2000, updatable = false)
    private String fatoresProtecao;

    @Column(nullable = false, length = 2000, updatable = false)
    private String necessidades;

    /** Hipótese diagnóstica, opcional, no formato do CID-10 (ex.: F32.1). */
    @Column(length = 10, updatable = false)
    private String cid;

    @Column(updatable = false)
    private String registradoPorCpf;
}
