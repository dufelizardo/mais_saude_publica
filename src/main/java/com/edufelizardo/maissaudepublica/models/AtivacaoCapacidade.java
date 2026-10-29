package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.SituacaoAtivacaoCapacidade;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/** Capacidade emergencial ativada numa emergência (ADR-0158): a quantidade, até o que resta do ativável da unidade. */
@Entity
@Table(name = "TB_ATIVACAO_CAPACIDADE", indexes = @Index(name = "ix_ativacao_capacidade_capacidade", columnList = "capacidade_id, situacao"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AtivacaoCapacidade implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emergencia_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Emergencia emergencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "capacidade_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private CapacidadeEmergencial capacidade;

    @Column(nullable = false, updatable = false)
    private int quantidade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SituacaoAtivacaoCapacidade situacao;

    @Column(nullable = false, updatable = false)
    private Instant inicio;

    private Instant fim;

    @Column(nullable = false, length = 1000, updatable = false)
    private String justificativa;

    @Column(length = 500)
    private String justificativaFim;

    @Column(updatable = false)
    private String registradoPorCpf;
}
