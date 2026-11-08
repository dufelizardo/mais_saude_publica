package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Lote de um material numa unidade (ADR-0177). Sem controle de lote, é o saldo único do material na unidade (número e validade
 * nulos). A quantidade só muda por {@link MovimentacaoEstoque}.
 */
@Entity
@Table(name = "TB_LOTE_MATERIAL", indexes = @Index(name = "ix_lote_material_unidade", columnList = "unidade_id, material_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class LoteMaterial implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private MaterialEstoque material;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private UnidadeDeSaude unidade;

    @Column(length = 60, updatable = false)
    private String numeroLote;

    @Column(updatable = false)
    private LocalDate validade;

    @Column(nullable = false)
    private Integer quantidade;

    /** O da última entrada; dá o valor do estoque. */
    @Column(precision = 12, scale = 4)
    private BigDecimal custoUnitario;

    @Column(nullable = false, updatable = false)
    private Instant criadoEm;

    public boolean vencido(LocalDate hoje) {
        return validade != null && validade.isBefore(hoje);
    }
}
