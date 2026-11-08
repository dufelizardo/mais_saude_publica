package com.edufelizardo.maissaudepublica.models;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/** Plantão de uma viatura (ADR-0178): o período e quem tripula; a composição mínima depende do tipo. */
@Entity
@Table(name = "TB_PLANTAO_VIATURA", indexes = @Index(name = "ix_plantao_viatura", columnList = "veiculo_id, inicio"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class PlantaoViatura implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "veiculo_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Veiculo veiculo;

    @Column(nullable = false)
    private Instant inicio;

    @Column(nullable = false)
    private Instant fim;

    @Column(length = 500)
    private String observacao;

    private String registradoPorCpf;

    @OneToMany(mappedBy = "plantao", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<TripulantePlantao> tripulacao = new ArrayList<>();

    public boolean vigenteEm(Instant t) {
        return !inicio.isAfter(t) && fim.isAfter(t);
    }
}
