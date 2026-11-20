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

/** Inventário patrimonial da unidade (ADR-0217): a comissão confere os bens fotografados na abertura; o fechamento trata as divergências. */
@Entity
@Table(name = "TB_INVENTARIO_PATRIMONIO", indexes = @Index(name = "ix_inventario_patrimonio_unidade", columnList = "unidade_id, situacao"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class InventarioPatrimonio implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Column(nullable = false, unique = true, length = 20, updatable = false)
    private String numero;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private UnidadeDeSaude unidade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SituacaoInventarioPatrimonio situacao;

    @Column(nullable = false, updatable = false)
    private LocalDate abertoEm;

    @Column(updatable = false)
    private String abertoPorCpf;

    private LocalDate fechadoEm;

    private String fechadoPorCpf;

    @Column(length = 500)
    private String observacao;

    @Column(nullable = false, updatable = false)
    private Instant registradoEm;
}
