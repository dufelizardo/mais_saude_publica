package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.PrioridadeRegulacao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** Nova gravidade, com o motivo (ADR-0176). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ReclassificacaoUrgenciaRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Informe a matrícula do médico regulador.")
    private String profissionalMatricula;
    @NotNull(message = "Informe a nova gravidade.")
    private PrioridadeRegulacao prioridade;
    @NotBlank(message = "Informe o motivo.")
    @Size(max = 500, message = "O motivo vai até 500 caracteres.")
    private String motivo;
}
