package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.SituacaoPlanoContingencia;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Plano de contingência (ADR-0157, desenho na ADR-0113): a preparação, configurável e versionada. Cada linha é uma versão; o código liga as versões. Só o rascunho se edita. */
@Entity
@Table(name = "TB_PLANO_CONTINGENCIA", uniqueConstraints = @UniqueConstraint(name = "uk_plano_contingencia_versao", columnNames = {"codigo", "versao"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class PlanoContingencia implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Column(nullable = false, length = 40, updatable = false)
    private String codigo;

    @Column(nullable = false, updatable = false)
    private int versao;

    @Column(nullable = false, length = 200)
    private String nome;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_id", referencedColumnName = "uuid", nullable = false)
    private TipoEmergencia tipo;

    @Column(nullable = false, length = 2000)
    private String objetivo;

    @Column(nullable = false)
    private LocalDate vigenciaInicio;

    private LocalDate vigenciaFim;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SituacaoPlanoContingencia situacao;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "TB_PLANO_CONTINGENCIA_NIVEL", joinColumns = @JoinColumn(name = "plano_id"), inverseJoinColumns = @JoinColumn(name = "nivel_id"))
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<NivelEmergencia> niveis = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "TB_PLANO_CONTINGENCIA_UNIDADE", joinColumns = @JoinColumn(name = "plano_id"), inverseJoinColumns = @JoinColumn(name = "unidade_id"))
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<UnidadeDeSaude> unidades = new HashSet<>();

    @OneToMany(mappedBy = "plano", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordem")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<CriterioAtivacao> criterios = new ArrayList<>();

    @OneToMany(mappedBy = "plano", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordem")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<AcaoPlano> acoes = new ArrayList<>();

    @OneToMany(mappedBy = "plano", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordem")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<RecursoPlano> recursos = new ArrayList<>();

    @OneToMany(mappedBy = "plano", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordem")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<ContatoPlano> contatos = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private Instant criadoEm;

    private Instant publicadoEm;

    /** Quando deixou de valer: substituída pela versão seguinte ou revogada. */
    private Instant encerradoEm;

    @Column(length = 500)
    private String motivoRevogacao;

    @Column(updatable = false)
    private String registradoPorCpf;
}
