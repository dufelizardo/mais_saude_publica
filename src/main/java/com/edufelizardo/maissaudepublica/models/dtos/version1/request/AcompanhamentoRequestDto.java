package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/** Inscrição de um paciente num programa (ADR-0119). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AcompanhamentoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe o paciente.")
    private UUID pacienteId;

    @NotNull(message = "Informe a unidade responsável.")
    private UUID unidadeId;

    /** Opcional; precisa ser da unidade. */
    private UUID equipeId;

    /** Matrícula do profissional responsável; opcional. */
    private String profissionalMatricula;

    /** Sem data, hoje. */
    private LocalDate inicio;
}
