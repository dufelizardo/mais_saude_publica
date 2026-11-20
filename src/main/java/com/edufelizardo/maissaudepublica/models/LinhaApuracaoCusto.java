package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.*;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** O custo de um centro numa categoria, na apuração da competência (ADR-0220). */
@Entity
@Table(name = "TB_LINHA_APURACAO_CUSTO", uniqueConstraints = @UniqueConstraint(name = "uk_linha_apuracao_custo", columnNames = {"apuracao_id", "centro_id", "categoria"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class LinhaApuracaoCusto implements Serializable {
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
    @JoinColumn(name = "centro_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private CentroCusto centro;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private CategoriaCusto categoria;

    @Column(nullable = false, precision = 15, scale = 2, updatable = false)
    private BigDecimal valor;
}
