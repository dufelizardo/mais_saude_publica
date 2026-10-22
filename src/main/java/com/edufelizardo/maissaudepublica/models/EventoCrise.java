package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.DesfechoCriseSaudeMental;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoCriseSaudeMental;
import com.edufelizardo.maissaudepublica.models.enuns.TipoCriseSaudeMental;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Evento de crise em saúde mental (ADR-0123): não é um atendimento. Do paciente na unidade, com ou sem caso (se há caso
 * vigente, liga-se a ele). As etapas ficam em {@link EtapaCrise}. Autoagressão e tentativa de suicídio sugerem a
 * notificação compulsória de violência autoprovocada, que o profissional registra que fez.
 */
@Entity
@Table(name = "TB_EVENTO_CRISE", indexes = {@Index(name = "ix_evento_crise_paciente", columnList = "paciente_id, identificada_em"), @Index(name = "ix_evento_crise_unidade", columnList = "unidade_id, situacao")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class EventoCrise implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private UnidadeDeSaude unidade;

    /** O caso vigente do paciente na hora da crise, se houver. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acompanhamento_id", referencedColumnName = "uuid", updatable = false)
    private AcompanhamentoSaudeMental acompanhamento;

    @Column(nullable = false, updatable = false)
    private Instant identificadaEm;

    @Column(length = 200, updatable = false)
    private String localOcorrencia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private TipoCriseSaudeMental tipo;

    @Column(nullable = false, length = 2000, updatable = false)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SituacaoCriseSaudeMental situacao;

    @Enumerated(EnumType.STRING)
    private DesfechoCriseSaudeMental desfecho;

    private Instant encerradaEm;

    @Column(nullable = false, updatable = false)
    private boolean notificacaoSugerida;

    /** Quando o profissional registrou que notificou (ficha de violência autoprovocada, fora do sistema até a V1). */
    private LocalDate notificadaEm;

    @Column(length = 40)
    private String notificacaoNumero;

    private String notificacaoRegistradaPorCpf;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id", referencedColumnName = "uuid", updatable = false)
    private Profissional profissional;

    @Column(updatable = false)
    private String registradoPorCpf;
}
