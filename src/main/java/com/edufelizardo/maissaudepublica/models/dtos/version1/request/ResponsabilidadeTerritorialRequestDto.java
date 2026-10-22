package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/** Equipe responsável pela área (ADR-0125). Trocando a equipe, o motivo é obrigatório. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ResponsabilidadeTerritorialRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe a equipe.")
    private UUID equipeId;

    /** Sem valor, hoje. */
    private LocalDate inicio;

    @Size(max = 500, message = "O motivo vai até 500 caracteres.")
    private String motivo;
}
