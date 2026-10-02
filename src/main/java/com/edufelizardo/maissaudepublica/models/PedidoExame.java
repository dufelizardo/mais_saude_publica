package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.PrioridadeExame;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Pedido de exames de um paciente (ADR-0093). O que foi pedido não muda depois; cada exame do pedido é um
 * {@link ItemPedidoExame} com situação própria, porque cada um tem a sua coleta, o seu resultado e a sua liberação.
 */
@Entity
@Table(name = "TB_PEDIDO_EXAME")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class PedidoExame implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Paciente paciente;

    /** O atendimento em que o pedido foi feito, quando houver. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atendimento_id", referencedColumnName = "uuid", updatable = false)
    private Atendimento atendimento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_solicitante_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private UnidadeDeSaude unidadeSolicitante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_solicitante_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Profissional profissionalSolicitante;

    /** Dado de saúde. */
    @NotNull
    @Column(nullable = false, length = 2000, updatable = false)
    private String indicacaoClinica;

    @Column(length = 8, updatable = false)
    private String cid;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private PrioridadeExame prioridade;

    @NotNull
    @Column(nullable = false, updatable = false)
    private Instant solicitadoEm;

    @Column(updatable = false)
    private String solicitadoPorCpf;
}
