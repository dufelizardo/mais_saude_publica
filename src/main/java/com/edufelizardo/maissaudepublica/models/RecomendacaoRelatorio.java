package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/** Recomendação do relatório pós-evento (ADR-0159); a que aponta um plano pode gerar a revisão dele (a versão nova fica ligada). */
@Entity
@Table(name = "TB_RECOMENDACAO_RELATORIO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class RecomendacaoRelatorio implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "relatorio_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private RelatorioPosEvento relatorio;

    @Column(nullable = false)
    private int ordem;

    @Column(nullable = false, length = 1000)
    private String descricao;

    /** O código do plano de contingência que a recomendação manda revisar. */
    @Column(length = 40)
    private String planoCodigo;

    /** A versão do plano aberta para revisão a partir desta recomendação. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plano_revisao_id", referencedColumnName = "uuid")
    private PlanoContingencia planoRevisao;
}
