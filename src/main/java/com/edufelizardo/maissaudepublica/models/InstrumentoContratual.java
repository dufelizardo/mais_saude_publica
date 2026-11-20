package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Ata de registro de preços ou contrato com um fornecedor (ADR-0213), com a vigência, o valor inicial, o gestor e o fiscal (art. 117). */
@Entity
@Table(name = "TB_INSTRUMENTO_CONTRATUAL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class InstrumentoContratual implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contratacao_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Contratacao contratacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private com.edufelizardo.maissaudepublica.models.enuns.TipoInstrumentoContratual tipo;

    @Column(nullable = false, unique = true, length = 60)
    private String numero;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fornecedor_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Fornecedor fornecedor;

    @Column(nullable = false)
    private LocalDate inicio;

    @Column(nullable = false)
    private LocalDate fim;

    @Column(nullable = false)
    private boolean servicoContinuo;

    /** A soma de quantidade inicial vezes preço; a base dos 25% do art. 125. */
    @Column(nullable = false, precision = 14, scale = 2, updatable = false)
    private BigDecimal valorInicial;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gestor_id", referencedColumnName = "uuid", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Profissional gestor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fiscal_id", referencedColumnName = "uuid", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Profissional fiscal;

    @Column(nullable = false, updatable = false)
    private Instant registradoEm;

    @Column(updatable = false)
    private String registradoPorCpf;
}
