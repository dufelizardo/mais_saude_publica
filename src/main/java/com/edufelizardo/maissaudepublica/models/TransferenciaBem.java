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

/** Transferência do bem entre unidades (ADR-0215): a origem envia, o destino aceita ou recusa; o bem só muda de unidade no aceite. */
@Entity
@Table(name = "TB_TRANSFERENCIA_BEM", indexes = {@Index(name = "ix_transferencia_bem", columnList = "bem_id"), @Index(name = "ix_transferencia_bem_situacao", columnList = "situacao")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class TransferenciaBem implements Serializable {
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origem_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private UnidadeDeSaude origem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destino_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private UnidadeDeSaude destino;

    @Column(nullable = false, length = 500, updatable = false)
    private String motivo;

    @Column(nullable = false, updatable = false)
    private LocalDate enviadaEm;

    @Column(updatable = false)
    private String enviadaPorCpf;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SituacaoTransferenciaBem situacao;

    private LocalDate resolvidaEm;

    private String resolvidaPorCpf;

    @Column(length = 500)
    private String motivoRecusa;

    @Column(nullable = false, updatable = false)
    private Instant registradoEm;
}
