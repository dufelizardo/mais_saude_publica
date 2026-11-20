package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.*;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Bem patrimonial (ADR-0215): tombado com número próprio, na unidade e no setor, sob a responsabilidade de um profissional. O bem comprado
 * nasce a tombar no recebimento definitivo da CP3. */
@Entity
@Table(name = "TB_BEM_PATRIMONIAL", indexes = @Index(name = "ix_bem_patrimonial_unidade", columnList = "unidade_id, situacao"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class BemPatrimonial implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    /** {@code AAAA/000001}, dado no tombamento; vazio enquanto o bem está a tombar. */
    @Column(unique = true, length = 20)
    private String numeroTombamento;

    @Column(nullable = false, length = 500)
    private String descricao;

    /** Obrigatória a partir do tombamento. */
    @Enumerated(EnumType.STRING)
    private ClasseBem classe;

    @Column(length = 80)
    private String marca;

    @Column(length = 80)
    private String modelo;

    @Column(length = 80)
    private String numeroSerie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_id", referencedColumnName = "uuid", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private UnidadeDeSaude unidade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "setor_id", referencedColumnName = "uuid")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Setor setor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SituacaoBem situacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private OrigemBem origem;

    @Column(precision = 15, scale = 2)
    private BigDecimal valorAquisicao;

    private LocalDate dataAquisicao;

    /** A nota fiscal, o termo de doação ou de cessão. */
    @Column(length = 60)
    private String documento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fornecedor_id", referencedColumnName = "uuid", updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Fornecedor fornecedor;

    /** O item do recebimento definitivo de onde o bem comprado veio (ADR-0214). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_recebimento_id", referencedColumnName = "uuid", updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ItemRecebimentoCompra itemRecebimento;

    /** O veículo da frota que é este bem (ADR-0178). */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "veiculo_id", referencedColumnName = "uuid", unique = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Veiculo veiculo;

    private LocalDate tombadoEm;

    @Column(length = 500)
    private String observacao;

    @Column(nullable = false, updatable = false)
    private Instant registradoEm;

    @Column(updatable = false)
    private String registradoPorCpf;
}
