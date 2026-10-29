package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/** Mudança de nível da emergência (ADR-0156), com a justificativa. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class MudancaNivelEmergenciaRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe o nível.")
    private UUID nivelId;
    @NotBlank(message = "Justifique a mudança.")
    @Size(max = 1000, message = "A justificativa vai até 1000 caracteres.")
    private String justificativa;
}
