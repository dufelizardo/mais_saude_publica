package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.StatusInternacaoAgendada;
import com.edufelizardo.maissaudepublica.models.enuns.TipoLeito;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Internação eletiva agendada (ADR-0185): a cirurgia ou o procedimento programado, com a data, o leito pedido e a reserva do
 * leito a partir de dois dias antes. Um agendamento ativo por paciente.
 */
@Entity
@Table(name = "TB_INTERNACAO_AGENDADA")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class InternacaoAgendada implements Serializable {
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

    @NotNull
    @Column(nullable = false)
    private LocalDate dataPrevista;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private TipoLeito tipoLeito;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medico_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Profissional medico;

    @Column(nullable = false, length = 10, updatable = false)
    private String cid;

    /** O procedimento ou a cirurgia prevista, em texto (o código SIGTAP é integração do domínio 20). */
    @Column(nullable = false, length = 500, updatable = false)
    private String procedimento;

    private Integer previsaoDias;

    /** A solicitação da Regulação que autorizou o procedimento, quando houver. */
    @Column(updatable = false)
    private UUID solicitacaoRegulacaoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusInternacaoAgendada status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leito_reservado_id", referencedColumnName = "uuid")
    private Leito leitoReservado;

    private Instant reservadoAte;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "internacao_id", referencedColumnName = "uuid")
    private Internacao internacao;

    @Column(length = 1000)
    private String motivoEncerramento;

    @NotNull
    @Column(nullable = false, updatable = false)
    private Instant agendadaEm;

    @Column(updatable = false)
    private String registradoPorCpf;

    /** Reserva que passou do dia previsto sem admissão: o leito conta como livre. */
    public boolean reservaVencida(Instant agora) {
        return status == StatusInternacaoAgendada.RESERVADA && reservadoAte != null && reservadoAte.isBefore(agora);
    }
}
