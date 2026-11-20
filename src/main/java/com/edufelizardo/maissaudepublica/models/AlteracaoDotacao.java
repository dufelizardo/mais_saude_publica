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

/** Suplementação ou anulação da dotação (ADR-0218), com o ato que a autorizou. */
@Entity
@Table(name = "TB_ALTERACAO_DOTACAO", indexes = @Index(name = "ix_alteracao_dotacao", columnList = "dotacao_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AlteracaoDotacao implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dotacao_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private DotacaoOrcamentaria dotacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private TipoAlteracaoDotacao tipo;

    @Column(nullable = false, precision = 15, scale = 2, updatable = false)
    private BigDecimal valor;

    /** O decreto ou a lei. */
    @Column(nullable = false, length = 60, updatable = false)
    private String ato;

    @Column(nullable = false, updatable = false)
    private LocalDate data;

    @Column(length = 500, updatable = false)
    private String motivo;

    @Column(nullable = false, updatable = false)
    private Instant registradoEm;

    @Column(updatable = false)
    private String registradoPorCpf;
}
