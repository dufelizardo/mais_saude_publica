package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/**
 * Área de um território (ADR-0125): o espaço sob responsabilidade de uma equipe ({@link ResponsabilidadeTerritorial}),
 * dividido em microáreas. A UBS chega à área pela equipe, não o contrário.
 */
@Entity
@Table(name = "TB_AREA", uniqueConstraints = @UniqueConstraint(name = "uk_area_codigo_por_territorio", columnNames = {"territorio_id", "codigo"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Area implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "territorio_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Territorio territorio;

    @Column(nullable = false, length = 20)
    private String codigo;

    /** Código da área no CNES, quando há. */
    @Column(length = 10)
    private String codigoCnes;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(columnDefinition = "text")
    private String geometria;
}
