package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.MeioExposicaoAmbiental;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Exposição ambiental no desastre (ADR-0161): o meio, o agente, a população exposta, as unidades na área e as medidas.
 * Datada e repetível, só inclusão, como a avaliação de impacto.
 */
@Entity
@Table(name = "TB_EXPOSICAO_AMBIENTAL", indexes = @Index(name = "ix_exposicao_ambiental_emergencia", columnList = "emergencia_id, avaliada_em"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ExposicaoAmbiental implements Serializable {
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private MeioExposicaoAmbiental meio;

    @Column(nullable = false, length = 200, updatable = false)
    private String agente;

    @Column(length = 300, updatable = false)
    private String fonte;

    @Column(updatable = false)
    private Integer populacaoExposta;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "TB_EXPOSICAO_AMBIENTAL_UNIDADE", joinColumns = @JoinColumn(name = "exposicao_id"), inverseJoinColumns = @JoinColumn(name = "unidade_id"))
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<UnidadeDeSaude> unidades = new HashSet<>();

    @Column(length = 1000, updatable = false)
    private String medidas;

    @Column(length = 1000, updatable = false)
    private String recomendacao;

    @Column(updatable = false)
    private String registradoPorCpf;
}
