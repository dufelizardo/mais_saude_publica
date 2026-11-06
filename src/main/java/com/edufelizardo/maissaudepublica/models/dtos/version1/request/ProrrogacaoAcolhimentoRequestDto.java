package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** A nova saída prevista do acolhimento noturno (ADR-0153); além da permanência máxima, com a justificativa. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ProrrogacaoAcolhimentoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe a nova saída prevista.")
    private LocalDate saidaPrevista;

    @Size(max = 500)
    private String justificativa;
}
