package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/** A desinterdição ou a destinação do produto apreendido (ADR-0200). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class EncerramentoMedidaRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private LocalDate data;
    @NotBlank(message = "Informe o motivo.")
    @Size(max = 1000, message = "O motivo vai até 1000 caracteres.")
    private String motivo;
}
