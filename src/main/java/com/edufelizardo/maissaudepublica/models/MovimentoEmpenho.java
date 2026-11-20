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

/** Reforço ou anulação do empenho (ADR-0218). */
@Entity
@Table(name = "TB_MOVIMENTO_EMPENHO", indexes = @Index(name = "ix_movimento_empenho", columnList = "empenho_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class MovimentoEmpenho implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empenho_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Empenho empenho;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private TipoMovimentoEmpenho tipo;

    @Column(nullable = false, precision = 15, scale = 2, updatable = false)
    private BigDecimal valor;

    @Column(nullable = false, updatable = false)
    private LocalDate data;

    @Column(nullable = false, length = 500, updatable = false)
    private String motivo;

    @Column(nullable = false, updatable = false)
    private Instant registradoEm;

    @Column(updatable = false)
    private String registradoPorCpf;
}
