package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/** Lote no inventário (ADR-0189): o saldo do sistema na abertura e a contagem. */
@Entity
@Table(name = "TB_ITEM_INVENTARIO_ESTOQUE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ItemInventarioEstoque implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventario_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private InventarioEstoque inventario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private LoteMaterial lote;

    @Column(nullable = false, updatable = false)
    private Integer saldoSistema;

    private Integer quantidadeContada;

    private Instant contadoEm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contado_por_id", referencedColumnName = "uuid")
    private Profissional contadoPor;
}
