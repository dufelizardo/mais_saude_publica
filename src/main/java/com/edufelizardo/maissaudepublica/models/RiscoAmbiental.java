package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.SituacaoRiscoAmbiental;
import com.edufelizardo.maissaudepublica.models.enuns.TipoRiscoAmbiental;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/** Risco ambiental permanente do território (ADR-0164): área contaminada, descarte irregular, fonte poluidora. Diferente da exposição do desastre (ADR-0161), que é de uma emergência. */
@Entity
@Table(name = "TB_RISCO_AMBIENTAL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class RiscoAmbiental implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoRiscoAmbiental tipo;

    @Column(nullable = false, length = 1000)
    private String descricao;

    @Column(nullable = false, length = 300)
    private String local;

    private Double latitude;

    private Double longitude;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "microarea_id", referencedColumnName = "uuid")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Microarea microarea;

    private Integer populacaoExposta;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SituacaoRiscoAmbiental situacao;

    private Instant controladoEm;

    @Column(length = 1000)
    private String motivoControle;

    @Column(nullable = false, updatable = false)
    private Instant registradoEm;

    @Column(updatable = false)
    private String registradoPorCpf;
}
