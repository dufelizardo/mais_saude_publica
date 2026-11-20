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

/** Peça usada na ordem de serviço (ADR-0216): do Estoque da unidade (com o material) ou trazida pelo contrato (descrita). */
@Entity
@Table(name = "TB_PECA_ORDEM_SERVICO", indexes = @Index(name = "ix_peca_ordem_servico", columnList = "ordem_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class PecaOrdemServico implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordem_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private OrdemServicoManutencao ordem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", referencedColumnName = "uuid", updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private MaterialEstoque material;

    @Column(nullable = false, length = 300, updatable = false)
    private String descricao;

    @Column(nullable = false, updatable = false)
    private int quantidade;

    @Column(precision = 15, scale = 2, updatable = false)
    private BigDecimal custoUnitario;
}
