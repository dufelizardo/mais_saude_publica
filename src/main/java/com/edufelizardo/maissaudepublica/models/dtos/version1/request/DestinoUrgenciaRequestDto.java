package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** O destino definido depois do envio do recurso, com a equipe no local (ADR-0176). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class DestinoUrgenciaRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Informe a matrícula do médico regulador.")
    private String profissionalMatricula;
    @NotNull(message = "Informe a unidade de destino.")
    private java.util.UUID destinoId;
    private Boolean vagaZero;
    @Size(max = 500, message = "A justificativa da vaga zero vai até 500 caracteres.")
    private String justificativaVagaZero;
}
