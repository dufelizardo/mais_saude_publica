package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Abertura do percurso do paciente na linha de cuidado (ADR-0152). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class PercursoLinhaRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe o paciente.")
    private UUID pacienteId;

    @NotNull(message = "Informe a linha de cuidado.")
    private UUID linhaId;

    /** A data de referência: a última menstruação, o nascimento, a suspeita. */
    @NotNull(message = "Informe a data de início do percurso.")
    private LocalDate inicio;

    @NotNull(message = "Informe a unidade responsável.")
    private UUID unidadeId;

    private UUID equipeId;

    private UUID acompanhamentoId;
}
