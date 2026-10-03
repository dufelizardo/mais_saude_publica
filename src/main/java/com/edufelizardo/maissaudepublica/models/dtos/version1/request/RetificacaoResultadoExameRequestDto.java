package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/** Correção de resultado já liberado, com motivo; o novo já sai liberado (ADR-0093). */
@Data
@Getter
@Setter
public class RetificacaoResultadoExameRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank
    private String profissionalMatricula;

    private BigDecimal valorNumerico;

    @Size(max = 2000)
    private String valorTexto;

    @Size(max = 1000)
    private String observacao;

    @NotBlank
    @Size(max = 1000)
    private String motivo;
}
