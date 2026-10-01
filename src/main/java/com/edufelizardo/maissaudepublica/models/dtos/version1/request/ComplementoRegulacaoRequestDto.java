package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/** Complemento do solicitante a uma solicitação devolvida; ela volta à fila (ADR-0087). */
@Data
@Getter
@Setter
public class ComplementoRegulacaoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank
    private String profissionalMatricula;

    @NotBlank
    @Size(max = 4000)
    private String complemento;
}
