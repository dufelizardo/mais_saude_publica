package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Contraindicação vacinal (ADR-0150): o paciente não recebe mais o imunobiológico sem a justificativa (a decisão do CRIE).
 * Nasce da conduta "contraindicar" no encerramento do ESAVI; uma por paciente e imunobiológico.
 */
@Entity
@Table(name = "TB_CONTRAINDICACAO_VACINAL", uniqueConstraints = @UniqueConstraint(name = "uk_contraindicacao_paciente_imunobiologico",
        columnNames = {"paciente_id", "imunobiologico_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ContraindicacaoVacinal implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "imunobiologico_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Imunobiologico imunobiologico;

    /** O ESAVI que a gerou. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notificacao_id", referencedColumnName = "uuid", updatable = false)
    private Notificacao notificacao;

    @Column(nullable = false, updatable = false)
    private LocalDate desde;

    @Column(nullable = false, length = 500, updatable = false)
    private String motivo;

    @Column(nullable = false, updatable = false)
    private Instant registradaEm;

    @Column(updatable = false)
    private String registradoPorCpf;
}
