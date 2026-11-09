package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** O paciente chegou e foi recebido na unidade de destino (ADR-0176). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class RecebimentoUrgenciaRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String profissionalMatricula;
    @Size(max = 500, message = "A observação vai até 500 caracteres.")
    private String observacao;
}
