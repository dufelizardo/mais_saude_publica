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

/** Diária ou ajuda de custo do TFD (ADR-0219): por solicitação e beneficiário, com o credor pessoa física, os dias e o valor da diária. */
@Entity
@Table(name = "TB_DIARIA_TFD", uniqueConstraints = @UniqueConstraint(name = "uk_diaria_tfd", columnNames = {"solicitacao_id", "beneficiario"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class DiariaTfd implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitacao_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private SolicitacaoTransporte solicitacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private BeneficiarioDiariaTfd beneficiario;

    /** O beneficiário como credor (o cadastro de fornecedores aceita o CPF). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credor_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Fornecedor credor;

    @Column(nullable = false, updatable = false)
    private int dias;

    @Column(nullable = false, precision = 15, scale = 2, updatable = false)
    private BigDecimal valorDiaria;

    @Column(nullable = false, precision = 15, scale = 2, updatable = false)
    private BigDecimal valorTotal;

    @Column(length = 500, updatable = false)
    private String observacao;

    @Column(nullable = false, updatable = false)
    private LocalDate concedidaEm;

    @Column(updatable = false)
    private String concedidaPorCpf;

    @Column(nullable = false, updatable = false)
    private Instant registradoEm;
}
