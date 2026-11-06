package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Rota programada da unidade móvel (ADR-0166): dias, horário e pontos em ordem. */
@Entity
@Table(name = "TB_ROTA_PROGRAMADA", indexes = @Index(name = "ix_rota_programada_unidade", columnList = "unidade_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class RotaProgramada implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private UnidadeDeSaude unidade;

    @Column(nullable = false, length = 150)
    private String nome;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "TB_ROTA_DIA", joinColumns = @JoinColumn(name = "rota_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "dia", nullable = false, length = 10)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<DayOfWeek> dias = new HashSet<>();

    private LocalTime horaInicio;

    private LocalTime horaFim;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "TB_ROTA_PONTO", joinColumns = @JoinColumn(name = "rota_id"), inverseJoinColumns = @JoinColumn(name = "ponto_id"))
    @OrderColumn(name = "ordem")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<PontoOperacional> pontos = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDate inicio;

    private LocalDate fim;

    @Column(length = 500)
    private String motivoFim;

    @Column(updatable = false)
    private String registradoPorCpf;

    public boolean vigente() {
        return fim == null;
    }
}
