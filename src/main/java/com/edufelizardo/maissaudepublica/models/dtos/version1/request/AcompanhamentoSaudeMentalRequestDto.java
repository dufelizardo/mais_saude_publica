package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/** Abre o caso em saúde mental sem acolhimento novo, para quem já é cuidado na unidade (ADR-0120). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AcompanhamentoSaudeMentalRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe o paciente.")
    private UUID pacienteId;

    @NotNull(message = "Informe a unidade responsável.")
    private UUID unidadeId;

    @NotNull(message = "Informe a equipe de referência.")
    private UUID equipeId;

    @NotBlank(message = "Informe a matrícula do técnico de referência.")
    private String tecnicoMatricula;

    /** Sem data, hoje. */
    private LocalDate inicio;
}
