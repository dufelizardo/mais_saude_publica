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

/** Liquidação da despesa (Lei 4.320/1964, art. 63; ADR-0219): o empenho, o atesto de origem, a nota, o bruto, as retenções e o
 * líquido; o pagamento (art. 64) com a ordem bancária do SIAFIC. */
@Entity
@Table(name = "TB_LIQUIDACAO", indexes = @Index(name = "ix_liquidacao_empenho", columnList = "empenho_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Liquidacao implements Serializable {
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
    private OrigemLiquidacao origem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recebimento_id", referencedColumnName = "uuid", updatable = false, unique = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private RecebimentoCompra recebimento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordem_servico_id", referencedColumnName = "uuid", updatable = false, unique = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private OrdemServicoManutencao ordemServico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diaria_id", referencedColumnName = "uuid", updatable = false, unique = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private DiariaTfd diaria;

    @Column(length = 60, updatable = false)
    private String notaFiscal;

    @Column(updatable = false)
    private LocalDate notaEmitidaEm;

    @Column(nullable = false, precision = 15, scale = 2, updatable = false)
    private BigDecimal valorBruto;

    @Column(nullable = false, precision = 15, scale = 2, updatable = false)
    private BigDecimal iss;

    @Column(nullable = false, precision = 15, scale = 2, updatable = false)
    private BigDecimal irrf;

    @Column(nullable = false, precision = 15, scale = 2, updatable = false)
    private BigDecimal inss;

    @Column(nullable = false, precision = 15, scale = 2, updatable = false)
    private BigDecimal outrasRetencoes;

    @Column(nullable = false, precision = 15, scale = 2, updatable = false)
    private BigDecimal valorLiquido;

    @Column(nullable = false, updatable = false)
    private LocalDate liquidadaEm;

    @Column(nullable = false, updatable = false)
    private LocalDate vencimento;

    /** Na liquidação avulsa: por que não há atesto na plataforma. */
    @Column(length = 500, updatable = false)
    private String justificativa;

    @Column(updatable = false)
    private String atestadoPorCpf;

    /** Na avulsa: o centro de custo que recebe a despesa na apuração (ADR-0220). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "centro_custo_id", referencedColumnName = "uuid", updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private CentroCusto centroCusto;

    private LocalDate pagaEm;

    @Column(length = 60)
    private String ordemBancaria;

    /** O motivo de pagar antes de outra liquidação vencida da mesma fonte (Lei 14.133/2021, art. 141, § 1º). */
    @Column(length = 500)
    private String justificativaForaDeOrdem;

    private String pagoPorCpf;

    @Column(nullable = false, updatable = false)
    private Instant registradoEm;

    @Column(updatable = false)
    private String registradoPorCpf;
}
