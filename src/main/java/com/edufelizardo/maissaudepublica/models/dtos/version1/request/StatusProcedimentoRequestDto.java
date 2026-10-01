package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.StatusProcedimento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Desfecho de um procedimento agendado (ADR-0062): {@code REALIZADO} com a data em que foi feito, ou
 * {@code CANCELADO} com a justificativa. Acontece uma única vez; depois disso, correções são retificação.
 */
@Data
@Getter
@Setter
public class StatusProcedimentoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull
    private StatusProcedimento status;

    @NotBlank
    private String profissionalMatricula;

    /** Obrigatória quando {@code REALIZADO}. */
    private LocalDateTime dataRealizacao;

    /** Obrigatória quando {@code CANCELADO}. */
    @Size(max = 1000)
    private String justificativa;
}
