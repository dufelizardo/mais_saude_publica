package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.SituacaoIntervencaoSaudeMental;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Intervenção realizada no caso (ADR-0121). Só inclusão. Da psicoterapia entra só o registro de que aconteceu, com
 * resumo: o conteúdo das sessões fica fora do prontuário compartilhado.
 */
@Entity
@Table(name = "TB_INTERVENCAO_SAUDE_MENTAL", indexes = @Index(name = "ix_intervencao_sm_acomp", columnList = "acompanhamento_id, realizada_em"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class IntervencaoSaudeMental implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acompanhamento_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private AcompanhamentoSaudeMental acompanhamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private TipoIntervencaoSaudeMental tipo;

    @Column(nullable = false, updatable = false)
    private Instant realizadaEm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id", referencedColumnName = "uuid", updatable = false)
    private Profissional profissional;

    /** Atendimento compartilhado: o outro profissional, de qualquer equipe. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compartilhado_com_id", referencedColumnName = "uuid", updatable = false)
    private Profissional compartilhadoCom;

    /** A intervenção prevista do PTS que esta cumpre, quando houver. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prevista_id", referencedColumnName = "uuid", updatable = false)
    private IntervencaoPrevista prevista;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private SituacaoIntervencaoSaudeMental situacao;

    @Column(nullable = false, length = 2000, updatable = false)
    private String resumo;

    @Column(updatable = false)
    private String registradoPorCpf;

    /** No matriciamento (ADR-0122): a equipe apoiada, ou que apoia, com quem o caso foi discutido. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipe_matriciada_id", referencedColumnName = "uuid", updatable = false)
    private Equipe equipeMatriciada;
}
