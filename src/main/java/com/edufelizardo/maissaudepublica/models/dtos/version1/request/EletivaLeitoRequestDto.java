package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Ações na internação eletiva agendada (ADR-0185): a reserva (leito), a admissão (leito sem reserva, previsão de alta, justificativa
 * de faixa etária), a remarcação (nova data e motivo), a falta e o cancelamento (motivo).
 */
@Data
public class EletivaLeitoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Informe a matrícula.")
    private String profissionalMatricula;

    private UUID leitoId;

    private LocalDate dataPrevista;

    private LocalDate previsaoAlta;

    @Size(max = 500, message = "A justificativa vai até 500 caracteres.")
    private String justificativaFaixaEtaria;

    @Size(max = 1000, message = "O motivo vai até 1000 caracteres.")
    private String motivo;
}
