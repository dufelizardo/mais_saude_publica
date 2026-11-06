package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** A entrada no acolhimento noturno (ADR-0153); além da permanência máxima, com a justificativa. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class PermanenciaAcolhimentoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe a unidade.")
    private UUID unidadeId;

    @NotNull(message = "Informe o paciente.")
    private UUID pacienteId;

    private LocalDate entrada;

    @NotNull(message = "Informe a saída prevista.")
    private LocalDate saidaPrevista;

    @Size(max = 500)
    private String justificativa;
}
