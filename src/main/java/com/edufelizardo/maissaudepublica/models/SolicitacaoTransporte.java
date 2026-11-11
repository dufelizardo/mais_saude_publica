package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.MotivoTransporte;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoSolicitacaoTransporte;
import com.edufelizardo.maissaudepublica.models.enuns.TrechoTransporte;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Solicitação de transporte sanitário eletivo (ADR-0190): o paciente, para quê, quando, de onde e para onde, o que precisa e o
 * acompanhante. Ligada ao agendamento, à solicitação regulada ou à internação (alta) quando há; o TFD passa pela comissão.
 */
@Entity
@Table(name = "TB_SOLICITACAO_TRANSPORTE", indexes = {
        @Index(name = "ix_solicitacao_transporte_data", columnList = "data_hora"),
        @Index(name = "ix_solicitacao_transporte_paciente", columnList = "paciente_id"),
        @Index(name = "ix_solicitacao_transporte_recorrencia", columnList = "recorrencia_id")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class SolicitacaoTransporte implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Paciente paciente;

    /** A unidade que pede (a de origem do paciente). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private UnidadeDeSaude unidade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30, updatable = false)
    private MotivoTransporte motivo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agendamento_id", referencedColumnName = "uuid", updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Agendamento agendamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitacao_regulacao_id", referencedColumnName = "uuid", updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private SolicitacaoRegulacao solicitacaoRegulacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "internacao_id", referencedColumnName = "uuid", updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Internacao internacao;

    /** O compromisso: a hora da consulta, do exame ou da sessão; na alta, a hora de sair. */
    @Column(nullable = false)
    private LocalDateTime dataHora;

    @Column(nullable = false, length = 500)
    private String enderecoEmbarque;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destino_unidade_id", referencedColumnName = "uuid")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private UnidadeDeSaude destinoUnidade;

    @Column(nullable = false, length = 500)
    private String destino;

    @Column(length = 100)
    private String destinoMunicipio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TrechoTransporte trecho;

    private boolean maca;
    private boolean cadeiraDeRodas;
    private boolean oxigenio;

    @Column(length = 150)
    private String acompanhanteNome;

    @Column(length = 30)
    private String acompanhanteDocumento;

    /** Laudo do TFD: CID, justificativa e quem pede. */
    @Column(length = 10)
    private String cid;

    @Column(length = 2000)
    private String justificativa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id", referencedColumnName = "uuid")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Profissional profissional;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SituacaoSolicitacaoTransporte situacao;

    /** O motivo do cancelamento, da negação ou da falta. */
    @Column(length = 500)
    private String motivoSituacao;

    /** As sessões geradas juntas (hemodiálise, terapia seriada). */
    private UUID recorrenciaId;

    @Column(length = 500)
    private String observacao;

    @Column(nullable = false, updatable = false)
    private Instant solicitadoEm;

    private String solicitadoPorCpf;
    private Instant decididoEm;
    private String decididoPorCpf;

    /** Quantos lugares ocupa: o paciente e o acompanhante. */
    public int lugares() {
        return acompanhanteNome != null ? 2 : 1;
    }
}
