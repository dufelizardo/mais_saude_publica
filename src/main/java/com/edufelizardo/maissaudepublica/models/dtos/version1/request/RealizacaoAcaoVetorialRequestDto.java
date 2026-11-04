package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/** Registrar a ação de controle como realizada (ADR-0162). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class RealizacaoAcaoVetorialRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private LocalDate data;
    @Min(value = 0, message = "Os imóveis trabalhados não ficam abaixo de zero.")
    private Integer imoveisTrabalhados;
    @Size(max = 1000, message = "O resultado vai até 1000 caracteres.")
    private String resultado;
}
