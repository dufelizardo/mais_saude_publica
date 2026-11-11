package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/** Item da requisição ao almoxarifado central (ADR-0189): o pedido e o atendido. */
@Entity
@Table(name = "TB_ITEM_REQUISICAO_ESTOQUE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ItemRequisicaoEstoque implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requisicao_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private RequisicaoEstoque requisicao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private MaterialEstoque material;

    @Column(nullable = false)
    private Integer quantidadePedida;

    private Integer quantidadeAtendida;
}
