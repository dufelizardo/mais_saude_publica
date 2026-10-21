package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.FaceDente;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Um registro do odontograma (ADR-0135, desenho na ADR-0111): a condição de um dente (FDI) ou de uma face dele numa
 * data, com a origem. O odontograma de uma data é a projeção dos registros até ela. Registro clínico não se edita: a
 * marcação errada é anulada, com motivo, e continua no histórico.
 */
@Entity
@Table(name = "TB_REGISTRO_ODONTOGRAMA", indexes = @Index(name = "ix_registro_odontograma_paciente", columnList = "paciente_id, dente"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class RegistroOdontograma implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Paciente paciente;

    /** Notação FDI (ISO 3950): permanentes 11 a 48, decíduos 51 a 85. */
    @Column(nullable = false, updatable = false)
    private int dente;

    /** Nula: a condição é do dente inteiro. */
    @Enumerated(EnumType.STRING)
    @Column(updatable = false)
    private FaceDente face;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "condicao_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private CondicaoOdontologica condicao;

    @Column(nullable = false, updatable = false)
    private LocalDate data;

    /** A origem: a avaliação odontológica (o procedimento entra na SB3). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "avaliacao_id", referencedColumnName = "uuid", updatable = false)
    private AvaliacaoOdontologica avaliacao;

    @Column(length = 500, updatable = false)
    private String observacao;

    private Instant anuladoEm;

    @Column(length = 500)
    private String motivoAnulacao;

    private String anuladoPorCpf;

    @Column(nullable = false, updatable = false)
    private Instant registradoEm;

    @Column(updatable = false)
    private String registradoPorCpf;
}
