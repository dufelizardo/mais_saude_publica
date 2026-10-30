package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/** Boletim da estrutura temporária (ADR-0160): os números de um dia ou turno. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class BoletimEstruturaRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe a data do boletim.")
    private LocalDate data;
    @NotNull(message = "Informe os atendimentos.")
    @Min(value = 0, message = "Os números não ficam abaixo de zero.")
    private Integer atendimentos;
    @Min(value = 0, message = "Os números não ficam abaixo de zero.")
    private Integer remocoes;
    @Min(value = 0, message = "Os números não ficam abaixo de zero.")
    private Integer casosGraves;
    @Min(value = 0, message = "Os números não ficam abaixo de zero.")
    private Integer obitos;
    @Size(max = 1000, message = "A observação vai até 1000 caracteres.")
    private String observacao;
}
