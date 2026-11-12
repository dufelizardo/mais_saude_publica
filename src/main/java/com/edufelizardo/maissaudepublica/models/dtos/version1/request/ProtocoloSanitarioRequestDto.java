package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/** A defesa ou o recurso protocolado pelo autuado (ADR-0200). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ProtocoloSanitarioRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private LocalDate data;
    @NotBlank(message = "Resuma o que foi apresentado.")
    @Size(max = 4000, message = "O texto vai até 4000 caracteres.")
    private String texto;
}
