package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.TipoAgendamento;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/** Bloco recorrente da agenda (ADR-0091). Sem {@code vigenteDesde}, vale a partir de hoje. */
@Data
@Getter
@Setter
public class BlocoAgendaRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank
    private String profissionalMatricula;

    @NotNull
    private UUID unidadeId;

    @NotNull
    private DayOfWeek diaSemana;

    @NotNull
    private LocalTime horaInicio;

    @NotNull
    private LocalTime horaFim;

    @NotNull
    @Min(5)
    @Max(240)
    private Integer duracaoMinutos;

    @NotNull
    private TipoAgendamento tipo;

    private LocalDate vigenteDesde;

    private LocalDate vigenteAte;
}
