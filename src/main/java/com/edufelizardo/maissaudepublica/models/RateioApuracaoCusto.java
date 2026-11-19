package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.*;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Uma linha do rateio em degraus (ADR-0221): o valor que a origem mandou ao destino, pelo critério aplicado, na ordem do degrau. */
@Entity
@Table(name = "TB_RATEIO_APURACAO_CUSTO", uniqueConstraints = @UniqueConstraint(name = "uk_rateio_apuracao_custo", columnNames = {"apuracao_id", "origem_id", "destino_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class RateioApuracaoCusto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apuracao_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ApuracaoCusto apuracao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origem_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private CentroCusto origem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destino_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private CentroCusto destino;

    /** O degrau: a posição da origem na sequência da unidade. */
    @Column(nullable = false, updatable = false)
    private int ordem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private CriterioRateio criterio;

    @Column(nullable = false, precision = 15, scale = 2, updatable = false)
    private BigDecimal valor;
}
