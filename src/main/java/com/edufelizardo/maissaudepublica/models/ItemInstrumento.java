package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Item da ata ou do contrato (ADR-0213): a quantidade (com os aditivos), o preço e o saldo, que a ordem de fornecimento (CP3) consome. */
@Entity
@Table(name = "TB_ITEM_INSTRUMENTO", indexes = @Index(name = "ix_item_instrumento", columnList = "instrumento_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ItemInstrumento implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instrumento_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private InstrumentoContratual instrumento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_contratacao_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ItemContratacao itemContratacao;

    @Column(nullable = false, updatable = false)
    private int quantidadeInicial;

    @Column(nullable = false)
    private int quantidade;

    @Column(nullable = false)
    private int saldo;

    @Column(nullable = false, precision = 14, scale = 4)
    private BigDecimal precoUnitario;
}
