package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.TipoEventoExame;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/** Um passo do pedido de exame (ADR-0093): quem, quando, em que exame e o texto do passo. Gravado uma vez. */
@Entity
@Table(name = "TB_EVENTO_EXAME")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class EventoExame implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private PedidoExame pedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", referencedColumnName = "uuid", updatable = false)
    private ItemPedidoExame item;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private TipoEventoExame tipo;

    @Column(length = 1000, updatable = false)
    private String texto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Profissional profissional;

    @NotNull
    @Column(nullable = false, updatable = false)
    private Instant ocorridoEm;

    @Column(updatable = false)
    private String registradoPorCpf;
}
