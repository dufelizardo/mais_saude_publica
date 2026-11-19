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

/** Baixa do bem (ADR-0217): com o motivo e o processo; acima do valor de alçada, espera a confirmação da gestão. */
@Entity
@Table(name = "TB_BAIXA_BEM", indexes = @Index(name = "ix_baixa_bem", columnList = "bem_id, situacao"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class BaixaBem implements Serializable {
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
    private MotivoBaixaBem motivo;

    @Column(nullable = false, length = 60, updatable = false)
    private String processo;

    @Column(length = 60, updatable = false)
    private String boletimOcorrencia;

    @Column(nullable = false, updatable = false)
    private LocalDate dataBaixa;

    @Column(length = 500, updatable = false)
    private String observacao;

    /** O valor contábil estimado na data do pedido (ADR-0217, depreciação para leitura). */
    @Column(precision = 15, scale = 2, updatable = false)
    private BigDecimal valorContabil;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SituacaoBaixaBem situacao;

    private LocalDate resolvidaEm;

    private String resolvidaPorCpf;

    @Column(length = 500)
    private String motivoRecusa;

    @Column(nullable = false, updatable = false)
    private Instant registradoEm;

    @Column(updatable = false)
    private String registradoPorCpf;
}
