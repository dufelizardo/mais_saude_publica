package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/** Admissão no destino de um pedido de leito regulado (ADR-0184). Na vaga zero, o leito é escolhido aqui. */
@Data
public class AdmissaoRegulacaoLeitoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Informe a matrícula do médico responsável.")
    private String medicoMatricula;

    /** Só na vaga zero: o leito livre da unidade de destino. */
    private UUID leitoId;

    private LocalDate previsaoAlta;

    @Size(max = 500, message = "A justificativa vai até 500 caracteres.")
    private String justificativaFaixaEtaria;
}
