package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/** Cada mudança de nível da emergência (ADR-0156), com a justificativa; a primeira, sem nível anterior, é a ativação. */
@Entity
@Table(name = "TB_MUDANCA_NIVEL_EMERGENCIA", indexes = @Index(name = "ix_mudanca_nivel_emergencia", columnList = "emergencia_id, mudado_em"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class MudancaNivelEmergencia implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emergencia_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Emergencia emergencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nivel_anterior_id", referencedColumnName = "uuid", updatable = false)
    private NivelEmergencia nivelAnterior;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nivel_novo_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private NivelEmergencia nivelNovo;

    @Column(nullable = false, length = 1000, updatable = false)
    private String justificativa;

    @Column(nullable = false, updatable = false)
    private Instant mudadoEm;

    @Column(updatable = false)
    private String mudadoPorCpf;
}
