package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Item de um recebimento (ADR-0214): o entregue, o lote e a validade (material com lote e medicamento) e, no definitivo, o aceito. */
@Entity
@Table(name = "TB_ITEM_RECEBIMENTO_COMPRA", indexes = @Index(name = "ix_item_recebimento_compra", columnList = "recebimento_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ItemRecebimentoCompra implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recebimento_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private RecebimentoCompra recebimento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_ordem_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ItemOrdemFornecimento itemOrdem;

    @Column(nullable = false, updatable = false)
    private int quantidadeEntregue;

    @Column(length = 60, updatable = false)
    private String numeroLote;

    @Column(updatable = false)
    private LocalDate validade;

    /** Preenchida no definitivo; o recusado é a diferença. */
    private Integer quantidadeAceita;

    @Column(length = 500)
    private String motivoRecusa;
}
