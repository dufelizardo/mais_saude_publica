package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/** Fechamento ou reabertura do censo diário (ADR-0185). */
@Data
public class CensoDiarioRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe a unidade.")
    private UUID unidadeId;

    @NotNull(message = "Informe o dia.")
    private LocalDate data;

    /** Só na reabertura, obrigatório. */
    @Size(max = 1000, message = "O motivo vai até 1000 caracteres.")
    private String motivo;
}
