package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.NecessidadeTratamentoOdontologico;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/** Abertura do plano de tratamento no atendimento de odontologia (ADR-0137). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class PlanoTratamentoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe o atendimento.")
    private UUID atendimentoId;

    /** A avaliação de origem; sem diagnóstico ou prioridade, valem os dela. */
    private UUID avaliacaoId;

    @NotBlank(message = "Informe a matrícula do dentista responsável.")
    private String profissionalMatricula;

    @Size(max = 2000, message = "O diagnóstico vai até 2000 caracteres.")
    private String diagnostico;

    @Size(max = 1000, message = "O objetivo vai até 1000 caracteres.")
    private String objetivo;

    private NecessidadeTratamentoOdontologico prioridade;

    @Valid
    private List<ItemPlanoTratamentoRequestDto> itens;
}
