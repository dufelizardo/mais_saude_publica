package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Item da ordem (ADR-0214): a quantidade pedida e a aceita nos recebimentos definitivos. */
@Entity
@Table(name = "TB_ITEM_ORDEM_FORNECIMENTO", indexes = @Index(name = "ix_item_ordem_fornecimento", columnList = "ordem_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ItemOrdemFornecimento implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordem_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private OrdemFornecimento ordem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_instrumento_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ItemInstrumento itemInstrumento;

    @Column(nullable = false, updatable = false)
    private int quantidade;

    @Column(nullable = false)
    private int quantidadeAceita;
}
