package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/** Adscrição manual do paciente a uma equipe, com motivo (ADR-0128). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AdscricaoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe a equipe.")
    private UUID equipeId;

    @NotBlank(message = "Informe o motivo da adscrição manual.")
    @Size(max = 400, message = "O motivo vai até 400 caracteres.")
    private String motivo;

    /** Sem valor, hoje. */
    private LocalDate inicio;
}
