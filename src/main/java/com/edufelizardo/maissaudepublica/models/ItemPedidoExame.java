package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.StatusItemExame;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/** Um exame do pedido, com a sua situação, a amostra atual e o resultado atual (ADR-0093). */
@Entity
@Table(name = "TB_ITEM_PEDIDO_EXAME")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ItemPedidoExame implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private PedidoExame pedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exame_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private ExameLaboratorial exame;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusItemExame status;

    /** A amostra em que o exame está; volta a nulo se a amostra for rejeitada. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "amostra_id", referencedColumnName = "uuid")
    private AmostraExame amostra;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resultado_atual_id", referencedColumnName = "uuid")
    private ResultadoExame resultadoAtual;

    @Column(length = 1000)
    private String motivoCancelamento;

    public ItemPedidoExame(PedidoExame pedido, ExameLaboratorial exame) {
        this.pedido = pedido;
        this.exame = exame;
        this.status = StatusItemExame.SOLICITADO;
    }
}
