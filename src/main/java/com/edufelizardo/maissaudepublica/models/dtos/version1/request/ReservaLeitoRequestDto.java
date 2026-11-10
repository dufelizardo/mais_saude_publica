package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/**
 * Decisão do regulador de leitos (ADR-0184): reserva um leito livre compatível ou manda em vaga zero para uma unidade, com
 * justificativa.
 */
@Data
public class ReservaLeitoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Informe a matrícula do regulador.")
    private String profissionalMatricula;

    /** O leito reservado; sem ele, vaga zero. */
    private UUID leitoId;

    /** Na vaga zero, a unidade que recebe. */
    private UUID unidadeDestinoId;

    @Size(max = 1000, message = "A justificativa vai até 1000 caracteres.")
    private String justificativaVagaZero;

    /** Prazo da reserva, em horas: padrão 6, de 1 a 24. */
    @Min(1)
    @Max(24)
    private Integer horas;

    @Size(max = 300, message = "O transporte vai até 300 caracteres.")
    private String transporte;
}
