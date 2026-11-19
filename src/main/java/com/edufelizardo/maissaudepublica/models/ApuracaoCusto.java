package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.*;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** A apuração de custos de uma competência (ADR-0220): refeita enquanto aberta; fechada, não muda mais. */
@Entity
@Table(name = "TB_APURACAO_CUSTO", uniqueConstraints = @UniqueConstraint(name = "uk_apuracao_custo_competencia", columnNames = "competencia"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ApuracaoCusto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    /** AAAA-MM. */
    @Column(nullable = false, length = 7, updatable = false)
    private String competencia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SituacaoApuracaoCusto situacao;

    /** O que não achou centro: a folha sem lotação na competência e a avulsa antiga sem centro. */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal naoAlocado;

    /** O custo dos centros administrativos e de apoio sem destino na unidade (ADR-0221); nulo na apuração feita antes do rateio. */
    @Column(precision = 15, scale = 2)
    private BigDecimal naoRateado;

    /** As saídas sem preço: o lote do Estoque sem custo e o medicamento que não veio de um recebimento de Compras. */
    @Column(nullable = false)
    private Integer saidasSemCusto;

    @Column(nullable = false)
    private Instant apuradaEm;

    private String apuradaPorCpf;

    private Instant fechadaEm;

    private String fechadaPorCpf;
}
