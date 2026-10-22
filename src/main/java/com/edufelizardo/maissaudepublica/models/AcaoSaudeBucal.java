package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.AtividadeSaudeBucalColetiva;
import com.edufelizardo.maissaudepublica.models.enuns.LocalSaudeBucalColetiva;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Detalhe odontológico da ação coletiva (ADR-0140, desenho na ADR-0111): as atividades (escovação supervisionada, flúor,
 * educação, avaliação coletiva, levantamento), o local, o público e os kits. A ação continua sendo a ação programática
 * de Programas (P4): a saúde bucal coletiva não ganha motor próprio.
 */
@Entity
@Table(name = "TB_ACAO_SAUDE_BUCAL", uniqueConstraints = @UniqueConstraint(columnNames = "acao_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AcaoSaudeBucal implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acao_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private AcaoProgramatica acao;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "TB_ACAO_SAUDE_BUCAL_ATIVIDADE", joinColumns = @JoinColumn(name = "detalhe_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "atividade", nullable = false)
    private Set<AtividadeSaudeBucalColetiva> atividades = new LinkedHashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LocalSaudeBucalColetiva localTipo;

    /** A escola, a creche, a empresa ou a instituição. */
    @Column(length = 200)
    private String instituicao;

    @Column(length = 300)
    private String publicoAlvo;

    private Integer kitsDistribuidos;

    @Column(nullable = false)
    private Instant atualizadoEm;

    private String atualizadoPorCpf;
}
