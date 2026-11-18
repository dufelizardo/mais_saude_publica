package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Termo aditivo (ADR-0213), numerado por instrumento; guarda o antes e o depois, sem apagar. */
@Entity
@Table(name = "TB_ADITIVO_CONTRATUAL", indexes = @Index(name = "ix_aditivo_contratual", columnList = "instrumento_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AditivoContratual implements Serializable {
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

    @Column(nullable = false, updatable = false)
    private int numero;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private com.edufelizardo.maissaudepublica.models.enuns.TipoAditivoContratual tipo;

    @Column(updatable = false)
    private LocalDate fimAnterior;

    @Column(updatable = false)
    private LocalDate novoFim;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_instrumento_id", referencedColumnName = "uuid", updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ItemInstrumento itemInstrumento;

    /** No acréscimo e na supressão, sempre positiva. */
    @Column(updatable = false)
    private Integer quantidade;

    @Column(precision = 14, scale = 4, updatable = false)
    private BigDecimal precoAnterior;

    @Column(precision = 14, scale = 4, updatable = false)
    private BigDecimal novoPreco;

    @Column(nullable = false, length = 1000, updatable = false)
    private String justificativa;

    @Column(nullable = false, updatable = false)
    private LocalDate assinadoEm;

    @Column(nullable = false, updatable = false)
    private Instant registradoEm;

    @Column(updatable = false)
    private String registradoPorCpf;
}
