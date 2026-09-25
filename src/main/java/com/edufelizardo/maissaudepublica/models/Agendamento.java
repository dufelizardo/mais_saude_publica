package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.StatusAgendamento;
import com.edufelizardo.maissaudepublica.models.enuns.TipoAgendamento;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Terceira entidade da onda "Operação Assistencial" (ver ADR-0039, MAPA-DE-DOMINIOS.md #7). Existe
 * independente de {@link Atendimento} — um agendamento pode nunca virar atendimento (não
 * comparecimento), e um atendimento pode não ter agendamento (acolhimento espontâneo). Por isso o
 * vínculo entre as duas (ver ADR-0042) fica em {@code Atendimento.agendamento}, opcional, não aqui.
 * {@code profissional} referencia {@link Profissional} por FK direta (uuid interno), resolvida a
 * partir da matrícula na fronteira da API — mesmo padrão de {@code Atendimento.profissional} (ver
 * ADR-0034/ADR-0041).
 */
@Entity
@Table(name = "TB_AGENDAMENTO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Agendamento implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", referencedColumnName = "uuid", nullable = false)
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id", referencedColumnName = "uuid", nullable = false)
    private Profissional profissional;

    @NotNull
    private LocalDateTime dataHora;

    @Enumerated(EnumType.STRING)
    private StatusAgendamento status;

    @Enumerated(EnumType.STRING)
    private TipoAgendamento tipo;

    private String observacao;

    public Agendamento(Paciente paciente, Profissional profissional, LocalDateTime dataHora,
                        StatusAgendamento status, TipoAgendamento tipo, String observacao) {
        this.paciente = paciente;
        this.profissional = profissional;
        this.dataHora = dataHora;
        this.status = status;
        this.tipo = tipo;
        this.observacao = observacao;
    }
}
