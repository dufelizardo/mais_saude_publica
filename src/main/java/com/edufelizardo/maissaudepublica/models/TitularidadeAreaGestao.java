package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.PapelTitularidade;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/** Quem responde pela área, como histórico (ADR-0175): designar o seguinte encerra o anterior, sem apagar. */
@Entity
@Table(name = "TB_TITULARIDADE_AREA_GESTAO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class TitularidadeAreaGestao implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private AreaGestao area;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Profissional profissional;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, updatable = false)
    private PapelTitularidade papel;

    @Column(nullable = false, updatable = false)
    private LocalDate inicio;

    private LocalDate fim;

    /** O ato de designação, como o número da portaria. */
    @Column(length = 120, updatable = false)
    private String ato;

    @Column(nullable = false, updatable = false)
    private LocalDateTime registradaEm;

    @Column(updatable = false)
    private String registradaPorCpf;

    public boolean vigenteEm(LocalDate dia) {
        return !inicio.isAfter(dia) && (fim == null || !fim.isBefore(dia));
    }
}
