package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Equipe responsável por uma área, com início e fim (ADR-0125): uma vigente por área e uma área vigente por equipe.
 * Trocar a equipe fecha a anterior.
 */
@Entity
@Table(name = "TB_RESPONSABILIDADE_TERRITORIAL", indexes = {
        @Index(name = "ix_responsabilidade_area", columnList = "area_id, fim"),
        @Index(name = "ix_responsabilidade_equipe", columnList = "equipe_id, fim")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ResponsabilidadeTerritorial implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Area area;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipe_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Equipe equipe;

    @Column(nullable = false, updatable = false)
    private LocalDate inicio;

    private LocalDate fim;

    /** Por que terminou: troca, saída da equipe, redivisão. */
    @Column(length = 500)
    private String motivoFim;

    @Column(updatable = false)
    private String registradoPorCpf;

    public boolean vigente() {
        return fim == null;
    }
}
