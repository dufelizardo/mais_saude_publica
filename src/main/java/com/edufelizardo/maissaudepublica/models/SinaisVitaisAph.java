package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Uma série de sinais vitais do atendimento pré-hospitalar (ADR-0188). */
@Entity
@Table(name = "TB_SINAIS_VITAIS_APH")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class SinaisVitaisAph implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ficha_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private FichaAph ficha;

    @NotNull
    @Column(nullable = false, updatable = false)
    private Instant medidoEm;

    @Column(updatable = false)
    private Integer paSistolica;

    @Column(updatable = false)
    private Integer paDiastolica;

    @Column(updatable = false)
    private Integer frequenciaCardiaca;

    @Column(updatable = false)
    private Integer frequenciaRespiratoria;

    @Column(updatable = false)
    private Integer saturacao;

    @Column(precision = 4, scale = 1, updatable = false)
    private BigDecimal temperatura;

    @Column(updatable = false)
    private Integer glicemia;

    @Column(updatable = false)
    private Integer dor;

    @Column(updatable = false)
    private Integer glasgow;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Profissional profissional;

    @NotNull
    @Column(nullable = false, updatable = false)
    private Instant registradoEm;

    @Column(updatable = false)
    private String registradoPorCpf;
}
