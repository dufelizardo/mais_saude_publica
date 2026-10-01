package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/** Cancelamento de uma transferência ainda em trânsito; o saldo volta ao lote de origem (ADR-0061). */
@Data
@Getter
@Setter
public class CancelamentoTransferenciaRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank
    private String profissionalMatricula;

    @NotBlank
    @Size(max = 1000)
    private String motivo;
}
