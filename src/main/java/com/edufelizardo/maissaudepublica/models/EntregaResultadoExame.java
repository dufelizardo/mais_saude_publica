package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.FormaEntregaResultado;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Entrega do resultado ao paciente (ADR-0181): quem retirou (o próprio paciente ou um terceiro identificado), como, onde, por
 * quem e quando. Cada segunda via é outra entrega. Gravada uma vez.
 */
@Entity
@Table(name = "TB_ENTREGA_RESULTADO_EXAME")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class EntregaResultadoExame implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private PedidoExame pedido;

    @Column(nullable = false, updatable = false)
    private boolean retiradoPeloPaciente;

    @Column(length = 150, updatable = false)
    private String terceiroNome;

    @Column(length = 30, updatable = false)
    private String terceiroDocumento;

    @Column(length = 50, updatable = false)
    private String terceiroParentesco;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private FormaEntregaResultado forma;

    /** Onde o resultado foi entregue: a unidade que responde pela entrega. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private UnidadeDeSaude unidade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Profissional profissional;

    @NotNull
    @Column(nullable = false, updatable = false)
    private Instant entregueEm;

    @Column(updatable = false)
    private String registradoPorCpf;
}
