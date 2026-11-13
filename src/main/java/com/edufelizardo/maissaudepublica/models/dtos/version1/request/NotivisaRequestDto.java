package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/** O registro da notificação do evento adverso ao SNVS pelo Notivisa (ADR-0203). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class NotivisaRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Informe o número da notificação no Notivisa.")
    @Size(max = 40, message = "O número vai até 40 caracteres.")
    private String numero;
    private Instant notificadoEm;
}
