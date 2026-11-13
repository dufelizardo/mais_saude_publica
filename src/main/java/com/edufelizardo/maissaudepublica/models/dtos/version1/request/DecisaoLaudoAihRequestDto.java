package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/** Decisão sobre o laudo de AIH (ADR-0186): autorizar, devolver, negar ou cancelar. Devolver, negar e cancelar pedem motivo. */
@Data
public class DecisaoLaudoAihRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Informe a matrícula.")
    private String profissionalMatricula;

    @Size(max = 1000, message = "O motivo vai até 1000 caracteres.")
    private String motivo;
}
