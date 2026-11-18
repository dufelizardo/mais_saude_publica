package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Item da contratação (ADR-0213): os itens iguais das solicitações somados, com o preço de referência ponderado e o resultado. */
@Entity
@Table(name = "TB_ITEM_CONTRATACAO", indexes = @Index(name = "ix_item_contratacao", columnList = "contratacao_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ItemContratacao implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contratacao_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Contratacao contratacao;

    @Column(nullable = false)
    private int ordem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private com.edufelizardo.maissaudepublica.models.enuns.TipoItemCompra tipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", referencedColumnName = "uuid", updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private MaterialEstoque material;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicamento_id", referencedColumnName = "uuid", updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Medicamento medicamento;

    @Column(nullable = false, length = 500)
    private String descricao;

    @Column(nullable = false, length = 30)
    private String unidadeMedida;

    @Column(nullable = false)
    private int quantidade;

    @Column(precision = 14, scale = 4)
    private BigDecimal precoReferencia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private com.edufelizardo.maissaudepublica.models.enuns.ResultadoItemContratacao resultado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fornecedor_id", referencedColumnName = "uuid")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Fornecedor fornecedor;

    @Column(precision = 14, scale = 4)
    private BigDecimal precoUnitario;

    /** Obrigatória quando o preço passa da referência. */
    @Column(length = 1000)
    private String justificativaPreco;
}
