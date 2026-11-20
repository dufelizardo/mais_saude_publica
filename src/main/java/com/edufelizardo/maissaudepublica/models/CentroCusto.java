package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.*;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Centro de custo (ADR-0220): o geral da unidade (sem setor, um por unidade, criado sozinho) ou o de um setor. */
@Entity
@Table(name = "TB_CENTRO_CUSTO", uniqueConstraints = {@UniqueConstraint(name = "uk_centro_custo_codigo", columnNames = "codigo"),
        @UniqueConstraint(name = "uk_centro_custo_setor", columnNames = "setor_id")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class CentroCusto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private UnidadeDeSaude unidade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "setor_id", referencedColumnName = "uuid", updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Setor setor;

    @Column(nullable = false, length = 20, updatable = false)
    private String codigo;

    @Column(nullable = false, length = 150)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoCentroCusto tipo;

    /** O critério do rateio (ADR-0221); nulo, o padrão do tipo: pessoas no administrativo, custo direto no apoio. */
    @Enumerated(EnumType.STRING)
    private CriterioRateio criterioRateio;

    /** A área em m², base do critério AREA. */
    @Column(name = "area_m2", precision = 10, scale = 2)
    private BigDecimal areaM2;

    private boolean ativo;

    @Column(nullable = false, updatable = false)
    private Instant registradoEm;

    @Column(updatable = false)
    private String registradoPorCpf;
}
