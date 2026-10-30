package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Boletim da estrutura temporária (ADR-0160): os números de um dia ou turno, datado e repetível; só inclusão. */
@Entity
@Table(name = "TB_BOLETIM_ESTRUTURA", indexes = @Index(name = "ix_boletim_estrutura_estrutura", columnList = "estrutura_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class BoletimEstrutura implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estrutura_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private EstruturaTemporaria estrutura;

    @Column(nullable = false, updatable = false)
    private LocalDate data;

    @Column(nullable = false, updatable = false)
    private int atendimentos;

    @Column(nullable = false, updatable = false)
    private int remocoes;

    @Column(nullable = false, updatable = false)
    private int casosGraves;

    @Column(nullable = false, updatable = false)
    private int obitos;

    @Column(length = 1000, updatable = false)
    private String observacao;

    @Column(nullable = false, updatable = false)
    private Instant registradoEm;

    @Column(updatable = false)
    private String registradoPorCpf;
}
