package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.PrioridadeRegulacao;
import com.edufelizardo.maissaudepublica.models.enuns.StatusSolicitacaoRegulacao;
import com.edufelizardo.maissaudepublica.models.enuns.TipoEventoRegulacao;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Um passo da {@link SolicitacaoRegulacao} (ADR-0087): quem fez, quando, o status e a prioridade que
 * resultaram, e o texto do passo (motivo, complemento ou observação). Gravado uma vez; não há edição nem
 * exclusão pela API.
 */
@Entity
@Table(name = "TB_EVENTO_REGULACAO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class EventoRegulacao implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitacao_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private SolicitacaoRegulacao solicitacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private TipoEventoRegulacao tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private StatusSolicitacaoRegulacao statusResultante;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private PrioridadeRegulacao prioridade;

    @Column(length = 4000, updatable = false)
    private String texto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Profissional profissional;

    @NotNull
    @Column(nullable = false, updatable = false)
    private Instant ocorridoEm;

    @Column(updatable = false)
    private String registradoPorCpf;

    public EventoRegulacao(SolicitacaoRegulacao solicitacao, TipoEventoRegulacao tipo, String texto,
                           Profissional profissional, Instant ocorridoEm, String registradoPorCpf) {
        this.solicitacao = solicitacao;
        this.tipo = tipo;
        this.statusResultante = solicitacao.getStatus();
        this.prioridade = solicitacao.getPrioridade();
        this.texto = texto;
        this.profissional = profissional;
        this.ocorridoEm = ocorridoEm;
        this.registradoPorCpf = registradoPorCpf;
    }
}
