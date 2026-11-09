package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Cota da PPI (ADR-0179): a parte da oferta que cabe a uma unidade, ou a uma esfera (município, regional) e às unidades abaixo dela.
 * A Regulação consome a cota na autorização.
 */
@Entity
@Table(name = "TB_COTA_PPI", uniqueConstraints = @UniqueConstraint(name = "uk_cota_ppi", columnNames = {"oferta_id", "beneficiaria_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class CotaPpi implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oferta_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private OfertaAssistencial oferta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "beneficiaria_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private UnidadeDeSaude beneficiaria;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(nullable = false)
    private Instant atualizadaEm;

    private String atualizadaPorCpf;
}
