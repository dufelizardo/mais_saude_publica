package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/** Devolução, negativa ou cancelamento de uma solicitação: o motivo é obrigatório (ADR-0087). */
@Data
@Getter
@Setter
public class MotivoRegulacaoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank
    private String profissionalMatricula;

    @NotBlank
    @Size(max = 1000)
    private String motivo;
}
