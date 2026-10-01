package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.PrioridadeRegulacao;
import com.edufelizardo.maissaudepublica.models.enuns.StatusSolicitacaoRegulacao;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Pedido de acesso a um {@link ProcedimentoRegulado} que a unidade de origem não oferece (ADR-0087). O que
 * foi pedido (paciente, procedimento, CID, justificativa, quem e quando) não muda depois; o status, a
 * prioridade e a vaga mudam, e cada mudança fica num {@link EventoRegulacao}.
 *
 * <p>A fila não é uma tabela: é a ordenação das solicitações {@code SOLICITADA} por prioridade e, dentro
 * dela, pela hora do pedido.
 */
@Entity
@Table(name = "TB_SOLICITACAO_REGULACAO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class SolicitacaoRegulacao implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    // ── Pedido (gravado uma vez) ───────────────────────────────────────────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "procedimento_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private ProcedimentoRegulado procedimento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_solicitante_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private UnidadeDeSaude unidadeSolicitante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_solicitante_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Profissional profissionalSolicitante;

    /** CID-10 que motiva o pedido. Dado de saúde. */
    @NotNull
    @Column(nullable = false, length = 8, updatable = false)
    private String cid;

    /** Justificativa clínica. Dado de saúde; o complemento pedido pelo regulador fica no evento. */
    @NotNull
    @Column(nullable = false, length = 4000, updatable = false)
    private String justificativa;

    @NotNull
    @Column(nullable = false, updatable = false)
    private Instant solicitadoEm;

    @Column(updatable = false)
    private String solicitadoPorCpf;

    // ── Andamento ──────────────────────────────────────────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusSolicitacaoRegulacao status;

    /** Proposta pelo solicitante; o regulador pode reclassificar enquanto está na fila. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PrioridadeRegulacao prioridade;

    /** Onde o paciente vai ser atendido; preenchida na autorização. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_executante_id", referencedColumnName = "uuid")
    private UnidadeDeSaude unidadeExecutante;

    /** Data e hora da vaga; preenchida na autorização. */
    private LocalDateTime dataHoraPrevista;

    public SolicitacaoRegulacao(Paciente paciente, ProcedimentoRegulado procedimento, UnidadeDeSaude unidadeSolicitante,
                                Profissional profissionalSolicitante, String cid, String justificativa,
                                PrioridadeRegulacao prioridade, Instant solicitadoEm, String solicitadoPorCpf) {
        this.paciente = paciente;
        this.procedimento = procedimento;
        this.unidadeSolicitante = unidadeSolicitante;
        this.profissionalSolicitante = profissionalSolicitante;
        this.cid = cid;
        this.justificativa = justificativa;
        this.prioridade = prioridade;
        this.solicitadoEm = solicitadoEm;
        this.solicitadoPorCpf = solicitadoPorCpf;
        this.status = StatusSolicitacaoRegulacao.SOLICITADA;
    }
}
