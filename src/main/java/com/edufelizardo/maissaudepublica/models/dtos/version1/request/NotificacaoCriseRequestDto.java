package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/** Registro de que a notificação compulsória foi feita (ADR-0123). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class NotificacaoCriseRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /** Sem data, hoje. */
    private LocalDate notificadaEm;

    @Size(max = 40, message = "O número vai até 40 caracteres.")
    private String numero;
}
