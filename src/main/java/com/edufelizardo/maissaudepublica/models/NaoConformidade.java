package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.GravidadeNaoConformidade;
import com.edufelizardo.maissaudepublica.models.enuns.OrigemNaoConformidade;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoNaoConformidade;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Não conformidade da qualidade do serviço (ADR-0204): a origem, a análise de causa, o plano de ação por ciclo e a verificação de eficácia (PDCA). */
@Entity
@Table(name = "TB_NAO_CONFORMIDADE", indexes = @Index(name = "ix_nao_conformidade_unidade", columnList = "unidade_id, situacao"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class NaoConformidade implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Column(nullable = false, unique = true, length = 20, updatable = false)
    private String numero;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private UnidadeDeSaude unidade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "setor_id", referencedColumnName = "uuid")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Setor setor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrigemNaoConformidade origem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incidente_id", referencedColumnName = "uuid", updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private IncidenteSeguranca incidente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "irregularidade_id", referencedColumnName = "uuid", updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private IrregularidadeSanitaria irregularidade;

    @Column(nullable = false, length = 4000)
    private String descricao;

    @Column(length = 2000)
    private String correcaoImediata;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GravidadeNaoConformidade gravidade;

    @Column(nullable = false)
    private LocalDate identificadaEm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsavel_id", referencedColumnName = "uuid", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Profissional responsavel;

    @Column(length = 4000)
    private String analiseCausa;

    /** As categorias de causa (6M), separadas por ponto e vírgula. */
    @Column(length = 200)
    private String categoriasCausa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SituacaoNaoConformidade situacao;

    @Column(nullable = false)
    private int ciclo;

    private LocalDate verificacaoPrevista;

    private LocalDate encerradaEm;

    @Column(length = 1000)
    private String motivoCancelamento;

    @Column(nullable = false, updatable = false)
    private Instant registradoEm;

    @Column(updatable = false)
    private String registradoPorCpf;
}
