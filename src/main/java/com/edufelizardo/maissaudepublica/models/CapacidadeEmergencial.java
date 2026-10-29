package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.RecursoCapacidade;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/** Capacidade emergencial da unidade (ADR-0158), cadastrada antes da emergência: a normal e quanto se pode ativar a mais. Uma por unidade e recurso. */
@Entity
@Table(name = "TB_CAPACIDADE_EMERGENCIAL", uniqueConstraints = @UniqueConstraint(name = "uk_capacidade_emergencial_recurso", columnNames = {"unidade_id", "recurso"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class CapacidadeEmergencial implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private UnidadeDeSaude unidade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private RecursoCapacidade recurso;

    @Column(nullable = false)
    private int normal;

    /** Quanto se pode ativar a mais em emergência (ex.: 30 leitos extras). */
    @Column(nullable = false)
    private int emergencial;

    @Column(length = 500)
    private String observacao;

    @Column(nullable = false)
    private boolean ativa;

    @Column(nullable = false)
    private Instant atualizadaEm;

    private String registradoPorCpf;
}
