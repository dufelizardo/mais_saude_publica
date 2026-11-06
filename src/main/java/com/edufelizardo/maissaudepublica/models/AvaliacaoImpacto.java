package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.NivelImpacto;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Avaliação de impacto da emergência (ADR-0156): datada e repetível, para acompanhar a evolução. Os números sugeridos
 * vêm dos donos (Território, Organização, Leitos); o que o sistema não tem é informado à mão. Só inclusão.
 */
@Entity
@Table(name = "TB_AVALIACAO_IMPACTO", indexes = @Index(name = "ix_avaliacao_impacto_emergencia", columnList = "emergencia_id, avaliada_em"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AvaliacaoImpacto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emergencia_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Emergencia emergencia;

    @Column(nullable = false, updatable = false)
    private Instant avaliadaEm;

    @Column(updatable = false)
    private Integer populacaoAfetada;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "TB_AVALIACAO_IMPACTO_UNIDADE", joinColumns = @JoinColumn(name = "avaliacao_id"), inverseJoinColumns = @JoinColumn(name = "unidade_id"))
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<UnidadeDeSaude> unidadesAfetadas = new HashSet<>();

    @Column(updatable = false)
    private Integer profissionaisIndisponiveis;

    @Column(updatable = false)
    private Integer leitosIndisponiveis;

    @Column(length = 1000, updatable = false)
    private String medicamentosCriticos;

    @Column(length = 1000, updatable = false)
    private String equipamentos;

    @Column(length = 1000, updatable = false)
    private String transporte;

    @Column(length = 1000, updatable = false)
    private String infraestrutura;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private NivelImpacto nivelImpacto;

    @Column(length = 2000, updatable = false)
    private String observacao;

    @Column(updatable = false)
    private String registradoPorCpf;
}
