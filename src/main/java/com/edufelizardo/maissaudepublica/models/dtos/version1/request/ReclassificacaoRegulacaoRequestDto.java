package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.PrioridadeRegulacao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/** Nova prioridade dada pelo regulador a uma solicitação na fila (ADR-0087). */
@Data
@Getter
@Setter
public class ReclassificacaoRegulacaoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank
    private String profissionalMatricula;

    @NotNull
    private PrioridadeRegulacao prioridade;

    @NotBlank
    @Size(max = 1000)
    private String motivo;
}
