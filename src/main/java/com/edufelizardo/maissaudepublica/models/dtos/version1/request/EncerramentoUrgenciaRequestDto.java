package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.DesfechoOcorrenciaUrgencia;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** Encerramento com o desfecho, pelo médico regulador (ADR-0176). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class EncerramentoUrgenciaRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Informe a matrícula do médico regulador.")
    private String profissionalMatricula;
    @NotNull(message = "Informe o desfecho.")
    private DesfechoOcorrenciaUrgencia desfecho;
    @NotBlank(message = "Descreva o encerramento.")
    @Size(max = 1000, message = "A descrição vai até 1000 caracteres.")
    private String observacao;
}
