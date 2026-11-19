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

/** Dotação orçamentária do Fundo Municipal de Saúde no exercício (ADR-0218): o programa de trabalho, a natureza da despesa e a
 * fonte, com o valor da lei; as alterações ficam em {@link AlteracaoDotacao}. */
@Entity
@Table(name = "TB_DOTACAO_ORCAMENTARIA", uniqueConstraints = @UniqueConstraint(name = "uk_dotacao_orcamentaria", columnNames = {"exercicio", "programa_trabalho", "natureza_despesa", "fonte"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class DotacaoOrcamentaria implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Column(nullable = false, updatable = false)
    private int exercicio;

    /** A classificação funcional-programática, por exemplo {@code 10.301.0010.2045}. */
    @Column(nullable = false, length = 40, updatable = false)
    private String programaTrabalho;

    @Column(nullable = false, length = 300)
    private String descricao;

    /** Por exemplo {@code 3.3.90.30} (material de consumo). */
    @Column(nullable = false, length = 20, updatable = false)
    private String naturezaDespesa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private FonteRecursoSaude fonte;

    /** O código da fonte no SIAFIC do município. */
    @Column(length = 20)
    private String fonteCodigo;

    @Column(nullable = false, precision = 15, scale = 2, updatable = false)
    private BigDecimal valorInicial;

    @Column(nullable = false, updatable = false)
    private Instant registradoEm;

    @Column(updatable = false)
    private String registradoPorCpf;
}
