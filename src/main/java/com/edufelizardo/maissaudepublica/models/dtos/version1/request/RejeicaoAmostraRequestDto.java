package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.MotivoRejeicaoAmostra;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/** Amostra que não serve: os exames dela voltam para recoleta (ADR-0093). */
@Data
@Getter
@Setter
public class RejeicaoAmostraRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank
    private String profissionalMatricula;

    @NotNull
    private MotivoRejeicaoAmostra motivo;

    @Size(max = 500)
    private String observacao;
}
