package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.PrioridadeRegulacao;
import com.edufelizardo.maissaudepublica.models.enuns.StatusSolicitacaoLeito;
import com.edufelizardo.maissaudepublica.models.enuns.TipoLeito;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Pedido de leito de outra unidade à central de regulação de leitos (ADR-0184): quem precisa, de onde, qual leito e com que
 * prioridade; a reserva do leito no destino (ou a vaga zero) e a admissão. Um pedido em andamento por paciente.
 */
@Entity
@Table(name = "TB_SOLICITACAO_LEITO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class SolicitacaoLeito implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_origem_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private UnidadeDeSaude unidadeOrigem;

    /** A internação na origem, quando o paciente já está internado e precisa de outro nível (enfermaria para UTI, por exemplo). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "internacao_origem_id", referencedColumnName = "uuid", updatable = false)
    private Internacao internacaoOrigem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atendimento_id", referencedColumnName = "uuid", updatable = false)
    private Atendimento atendimento;

    /** A ocorrência de urgência (ADR-0176) que pediu a transferência, quando houver. */
    @Column(updatable = false)
    private UUID ocorrenciaUrgenciaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitado_por_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Profissional solicitadoPor;

    @Column(nullable = false, length = 10, updatable = false)
    private String cid;

    @Column(nullable = false, length = 2000, updatable = false)
    private String quadroClinico;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private TipoLeito tipoLeito;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PrioridadeRegulacao prioridade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusSolicitacaoLeito status;

    @NotNull
    @Column(nullable = false, updatable = false)
    private Instant solicitadaEm;

    /** O leito reservado no destino; nulo na vaga zero e antes da reserva. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leito_reservado_id", referencedColumnName = "uuid")
    private Leito leitoReservado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_destino_id", referencedColumnName = "uuid")
    private UnidadeDeSaude unidadeDestino;

    private Instant reservadoAte;

    private boolean vagaZero;

    @Column(length = 1000)
    private String justificativaVagaZero;

    /** Como o paciente vai: viatura, transporte sanitário ou meios próprios (texto até a ligação com o Transporte). */
    @Column(length = 300)
    private String transporte;

    /** Motivo da negação ou do cancelamento. */
    @Column(length = 1000)
    private String motivoEncerramento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "internacao_destino_id", referencedColumnName = "uuid")
    private Internacao internacaoDestino;

    @Column(updatable = false)
    private String registradoPorCpf;

    /** A reserva venceu sem admissão: o pedido volta para a fila. */
    public boolean reservaVencida(Instant agora) {
        return status == StatusSolicitacaoLeito.RESERVADA && !vagaZero && reservadoAte != null && reservadoAte.isBefore(agora);
    }
}
