package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/** Atendimento realizado na executante, com a contrarreferência para a unidade de origem (ADR-0089). */
@Data
@Getter
@Setter
public class RealizacaoRegulacaoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank
    private String profissionalMatricula;

    /** O que foi feito, o achado e a conduta, para quem continua o cuidado na origem. */
    @NotBlank
    @Size(min = 10, max = 4000)
    private String contrarreferencia;
}
