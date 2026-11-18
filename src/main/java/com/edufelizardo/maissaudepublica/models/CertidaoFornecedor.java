package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Certidão de regularidade do fornecedor (ADR-0212): a mais nova de cada tipo vale; as antigas ficam como histórico. */
@Entity
@Table(name = "TB_CERTIDAO_FORNECEDOR", indexes = @Index(name = "ix_certidao_fornecedor", columnList = "fornecedor_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class CertidaoFornecedor implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fornecedor_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Fornecedor fornecedor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private com.edufelizardo.maissaudepublica.models.enuns.TipoCertidaoFornecedor tipo;

    @Column(length = 60)
    private String numero;

    @Column(nullable = false)
    private LocalDate emitidaEm;

    @Column(nullable = false)
    private LocalDate validaAte;

    @Column(nullable = false, updatable = false)
    private Instant registradoEm;

    @Column(updatable = false)
    private String registradoPorCpf;
}
