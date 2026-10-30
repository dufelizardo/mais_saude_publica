package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/** A desmobilização da emergência (ADR-0159): quem iniciou, por quê e o que foi liberado de uma vez. Uma por emergência; só inclusão. */
@Entity
@Table(name = "TB_DESMOBILIZACAO", uniqueConstraints = @UniqueConstraint(name = "uk_desmobilizacao_emergencia", columnNames = "emergencia_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Desmobilizacao implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emergencia_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Emergencia emergencia;

    @Column(nullable = false, updatable = false)
    private Instant iniciadaEm;

    @Column(nullable = false, length = 200, updatable = false)
    private String responsavel;

    @Column(nullable = false, length = 1000, updatable = false)
    private String justificativa;

    @Column(nullable = false, updatable = false)
    private int mobilizacoesConcluidas;

    @Column(nullable = false, updatable = false)
    private int profissionaisLiberados;

    @Column(nullable = false, updatable = false)
    private int capacidadesDesativadas;

    @Column(nullable = false, updatable = false)
    private int planosDesativados;

    @Column(updatable = false)
    private String registradoPorCpf;
}
