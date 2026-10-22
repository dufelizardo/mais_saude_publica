package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.NivelRiscoSaudeMental;
import com.edufelizardo.maissaudepublica.models.enuns.TipoRiscoSaudeMental;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Avaliação de risco do caso em saúde mental (ADR-0123): separada da avaliação, com histórico. Reavaliar grava um
 * registro novo; o vigente de cada tipo é o mais recente, e "sem risco atual" fecha o tipo. Só inclusão.
 */
@Entity
@Table(name = "TB_AVALIACAO_RISCO", indexes = @Index(name = "ix_avaliacao_risco_acomp", columnList = "acompanhamento_id, avaliada_em"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AvaliacaoRisco implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acompanhamento_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private AcompanhamentoSaudeMental acompanhamento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private TipoRiscoSaudeMental tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private NivelRiscoSaudeMental nivel;

    @Column(length = 2000, updatable = false)
    private String fatores;

    /** Medidas adotadas; obrigatórias de moderado para cima. */
    @Column(length = 2000, updatable = false)
    private String medidas;

    @Column(nullable = false, updatable = false)
    private boolean planoSeguranca;

    /** Ex.: diário, semanal. */
    @Column(length = 100, updatable = false)
    private String frequenciaContato;

    @Column(updatable = false)
    private LocalDate reavaliarEm;

    @Column(nullable = false, updatable = false)
    private Instant avaliadaEm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id", referencedColumnName = "uuid", updatable = false)
    private Profissional profissional;

    @Column(updatable = false)
    private String registradoPorCpf;
}
