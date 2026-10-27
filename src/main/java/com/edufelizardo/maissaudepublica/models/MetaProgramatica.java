package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.IndicadorProgramatico;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Meta programática (ADR-0141, desenho na ADR-0109): o alvo de um indicador do programa num período, na rede ou numa
 * unidade. No indicador "exame no alvo", a meta diz o exame e o limite.
 */
@Entity
@Table(name = "TB_META_PROGRAMATICA", indexes = @Index(name = "ix_meta_programatica_programa", columnList = "programa_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class MetaProgramatica implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "programa_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Programa programa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private IndicadorProgramatico indicador;

    /** Nula: a rede inteira. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_id", referencedColumnName = "uuid", updatable = false)
    private UnidadeDeSaude unidade;

    /** O percentual (0 a 100) ou a quantidade, conforme o indicador. */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal alvo;

    @Column(nullable = false)
    private LocalDate inicio;

    @Column(nullable = false)
    private LocalDate fim;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exame_id", referencedColumnName = "uuid")
    private ExameLaboratorial exame;

    @Column(precision = 12, scale = 4)
    private BigDecimal limiteMinimo;

    @Column(precision = 12, scale = 4)
    private BigDecimal limiteMaximo;

    @Column(nullable = false, updatable = false)
    private Instant criadaEm;

    @Column(updatable = false)
    private String registradoPorCpf;
}
