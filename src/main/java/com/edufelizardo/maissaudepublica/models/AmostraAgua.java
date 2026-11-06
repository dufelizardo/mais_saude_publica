package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Amostra de água do ponto (ADR-0164): os parâmetros e a conformidade pela Portaria GM/MS 888/2021, calculada no registro. */
@Entity
@Table(name = "TB_AMOSTRA_AGUA", indexes = @Index(name = "ix_amostra_agua_data", columnList = "data_coleta"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AmostraAgua implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ponto_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private PontoColetaAgua ponto;

    @Column(nullable = false, updatable = false)
    private LocalDate dataColeta;

    /** Presença de coliformes totais. */
    @Column(updatable = false)
    private Boolean coliformesTotais;

    /** Presença de Escherichia coli. */
    @Column(updatable = false)
    private Boolean ecoli;

    /** Cloro residual livre, em mg/L. */
    @Column(updatable = false)
    private Double cloroResidual;

    /** Turbidez, em uT. */
    @Column(updatable = false)
    private Double turbidez;

    /** Fluoreto, em mg/L. */
    @Column(updatable = false)
    private Double fluor;

    @Column(nullable = false, updatable = false)
    private boolean conforme;

    @Column(length = 500, updatable = false)
    private String naoConformidades;

    @Column(length = 1000)
    private String medida;

    private Instant medidaEm;

    @Column(nullable = false, updatable = false)
    private Instant registradoEm;

    @Column(updatable = false)
    private String registradoPorCpf;
}
