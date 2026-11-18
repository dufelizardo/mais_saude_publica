package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Item da solicitação de compra (ADR-0212): material, medicamento ou serviço, com a quantidade e, depois da pesquisa, o preço de referência. */
@Entity
@Table(name = "TB_ITEM_SOLICITACAO_COMPRA", indexes = @Index(name = "ix_item_solicitacao_compra", columnList = "solicitacao_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ItemSolicitacaoCompra implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitacao_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private SolicitacaoCompra solicitacao;

    @Column(nullable = false)
    private int ordem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private com.edufelizardo.maissaudepublica.models.enuns.TipoItemCompra tipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", referencedColumnName = "uuid")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private MaterialEstoque material;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicamento_id", referencedColumnName = "uuid")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Medicamento medicamento;

    @Column(nullable = false, length = 500)
    private String descricao;

    @Column(nullable = false, length = 30)
    private String unidadeMedida;

    @Column(nullable = false)
    private int quantidade;

    /** A mediana das cotações, fixada ao concluir a pesquisa. */
    @Column(precision = 14, scale = 4)
    private BigDecimal precoReferencia;

    /** Obrigatória quando o item fica com menos de 3 cotações. */
    @Column(length = 1000)
    private String justificativaCotacao;

    /** O item da contratação que somou este (ADR-0213). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_contratacao_id", referencedColumnName = "uuid")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ItemContratacao itemContratacao;
}
