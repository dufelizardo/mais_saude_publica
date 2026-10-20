package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.FinalidadeTerritorio;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/** Território de um segmento com uma finalidade (ADR-0125): o recorte da APS não é o da vigilância nem o de referência. */
@Entity
@Table(name = "TB_TERRITORIO", uniqueConstraints = @UniqueConstraint(name = "uk_territorio_nome_por_segmento", columnNames = {"segmento_id", "nome"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Territorio implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "segmento_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private SegmentoTerritorial segmento;

    @Column(nullable = false, length = 120)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FinalidadeTerritorio finalidade;

    @Column(nullable = false)
    private boolean ativo;
}
