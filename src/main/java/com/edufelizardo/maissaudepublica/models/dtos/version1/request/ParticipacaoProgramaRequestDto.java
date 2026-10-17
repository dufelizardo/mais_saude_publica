package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/** Inclusão de uma unidade (e, se for o caso, equipe) que oferece o programa (ADR-0118). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ParticipacaoProgramaRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe a unidade.")
    private UUID unidadeId;

    /** Opcional; precisa ser da unidade. */
    private UUID equipeId;

    /** Sem data, hoje. */
    private LocalDate inicio;
}
