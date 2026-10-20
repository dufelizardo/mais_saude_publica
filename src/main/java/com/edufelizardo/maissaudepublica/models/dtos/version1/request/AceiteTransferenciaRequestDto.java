package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/** Aceite da transferência pelo destino, com a nova referência (ADR-0122). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AceiteTransferenciaRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe a equipe de referência.")
    private UUID equipeId;

    @NotBlank(message = "Informe a matrícula do técnico de referência.")
    private String tecnicoMatricula;
}
