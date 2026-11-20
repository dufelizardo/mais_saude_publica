package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.*;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * O resumo de um centro na apuração (ADR-0221): o custo direto, o recebido no rateio e o enviado; o final é direto + recebido - enviado.
 * {@code internacao}: o centro produtivo cujo setor tinha leito ativo, que vai ao custo por paciente-dia.
 */
@Entity
@Table(name = "TB_CUSTO_CENTRO_APURACAO", uniqueConstraints = @UniqueConstraint(name = "uk_custo_centro_apuracao", columnNames = {"apuracao_id", "centro_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class CustoCentroApuracao implements Serializable {
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

    @Column(nullable = false, precision = 15, scale = 2, updatable = false)
    private BigDecimal direto;

    @Column(nullable = false, precision = 15, scale = 2, updatable = false)
    private BigDecimal recebido;

    @Column(nullable = false, precision = 15, scale = 2, updatable = false)
    private BigDecimal enviado;

    @Column(nullable = false, updatable = false)
    private boolean internacao;
}
