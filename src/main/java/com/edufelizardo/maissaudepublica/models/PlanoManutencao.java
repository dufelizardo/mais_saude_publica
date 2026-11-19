package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.*;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Plano de manutenção do bem (ADR-0216): a cada periodicidade, a próxima ordem nasce quando o plano vence. */
@Entity
@Table(name = "TB_PLANO_MANUTENCAO", indexes = @Index(name = "ix_plano_manutencao_bem", columnList = "bem_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class PlanoManutencao implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bem_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private BemPatrimonial bem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoManutencao tipo;

    @Column(nullable = false)
    private int periodicidadeDias;

    private LocalDate ultimaExecucao;

    @Column(nullable = false)
    private LocalDate proximaEm;

    @Column(length = 500)
    private String descricao;

    @Column(nullable = false)
    private boolean ativo;

    @Column(nullable = false, updatable = false)
    private Instant registradoEm;

    @Column(updatable = false)
    private String registradoPorCpf;
}
