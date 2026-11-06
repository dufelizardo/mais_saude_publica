package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Vacinação antirrábica animal (ADR-0163): o registro agregado do posto no dia, com os cães e os gatos vacinados. */
@Entity
@Table(name = "TB_VACINACAO_ANTIRRABICA_ANIMAL", indexes = @Index(name = "ix_vacinacao_antirrabica_data", columnList = "data"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class VacinacaoAntirrabicaAnimal implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Column(nullable = false, updatable = false)
    private LocalDate data;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "microarea_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Microarea microarea;

    @Column(nullable = false, length = 200, updatable = false)
    private String posto;

    @Column(nullable = false, updatable = false)
    private int caes;

    @Column(nullable = false, updatable = false)
    private int gatos;

    @Column(nullable = false, length = 200, updatable = false)
    private String responsavel;

    @Column(nullable = false, updatable = false)
    private Instant registradoEm;

    @Column(updatable = false)
    private String registradoPorCpf;
}
