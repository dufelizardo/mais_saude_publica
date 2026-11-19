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

/** Bem a conferir no inventário (ADR-0217): o resultado, a conservação, onde foi encontrado e a ação do fechamento. */
@Entity
@Table(name = "TB_ITEM_INVENTARIO", indexes = @Index(name = "ix_item_inventario", columnList = "inventario_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ItemInventario implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventario_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private InventarioPatrimonio inventario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bem_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private BemPatrimonial bem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "setor_esperado_id", referencedColumnName = "uuid", updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Setor setorEsperado;

    @Enumerated(EnumType.STRING)
    private ResultadoConferencia resultado;

    @Enumerated(EnumType.STRING)
    private ConservacaoBem conservacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "setor_encontrado_id", referencedColumnName = "uuid")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Setor setorEncontrado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_encontrada_id", referencedColumnName = "uuid")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private UnidadeDeSaude unidadeEncontrada;

    @Column(length = 500)
    private String observacao;

    private LocalDate conferidoEm;

    private String conferidoPorCpf;

    @Enumerated(EnumType.STRING)
    private AcaoInventario acao;
}
