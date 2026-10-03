package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.TipoAgendamento;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Bloco recorrente da agenda de um profissional numa unidade (ADR-0091): toda {@code diaSemana}, de
 * {@code horaInicio} a {@code horaFim}, em vagas de {@code duracaoMinutos}. Vale de {@code vigenteDesde} até
 * {@code vigenteAte} (aberto se nulo). Mudar o bloco é encerrá-lo e criar outro: o histórico da agenda fica.
 */
@Entity
@Table(name = "TB_BLOCO_AGENDA")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class BlocoAgenda implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Profissional profissional;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private UnidadeDeSaude unidade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private DayOfWeek diaSemana;

    @NotNull
    @Column(nullable = false, updatable = false)
    private LocalTime horaInicio;

    @NotNull
    @Column(nullable = false, updatable = false)
    private LocalTime horaFim;

    @Column(nullable = false, updatable = false)
    private int duracaoMinutos;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private TipoAgendamento tipo;

    @NotNull
    @Column(nullable = false, updatable = false)
    private LocalDate vigenteDesde;

    /** Último dia em que o bloco vale; nulo enquanto não for encerrado. */
    private LocalDate vigenteAte;

    public BlocoAgenda(Profissional profissional, UnidadeDeSaude unidade, DayOfWeek diaSemana, LocalTime horaInicio,
                       LocalTime horaFim, int duracaoMinutos, TipoAgendamento tipo, LocalDate vigenteDesde, LocalDate vigenteAte) {
        this.profissional = profissional;
        this.unidade = unidade;
        this.diaSemana = diaSemana;
        this.horaInicio = horaInicio;
        this.horaFim = horaFim;
        this.duracaoMinutos = duracaoMinutos;
        this.tipo = tipo;
        this.vigenteDesde = vigenteDesde;
        this.vigenteAte = vigenteAte;
    }

    public boolean valeEm(LocalDate dia) {
        return dia.getDayOfWeek() == diaSemana && !dia.isBefore(vigenteDesde) && (vigenteAte == null || !dia.isAfter(vigenteAte));
    }
}
