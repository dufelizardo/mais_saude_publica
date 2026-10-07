package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.ModeloJornada;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Aplicação de um modelo de jornada à semana de um profissional (ADR-0107). Sem início ou intervalo, valem os do modelo; o
 * fim de cada dia sai das horas do modelo mais o intervalo.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AplicarModeloRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe a unidade.")
    private UUID unidadeId;

    @NotBlank(message = "Informe a matrícula do profissional.")
    private String profissionalMatricula;

    @NotNull(message = "Informe o modelo de jornada.")
    private ModeloJornada modelo;

    /** Qualquer dia vale pela semana (segunda a domingo). */
    @NotNull(message = "Informe a semana.")
    private LocalDate semana;

    private LocalTime inicio;

    @Min(value = 0, message = "O intervalo não pode ser negativo.")
    @Max(value = 120, message = "O intervalo vai até 2 horas (CLT, art. 71).")
    private Integer intervaloMinutos;

    private UUID equipeId;
}
