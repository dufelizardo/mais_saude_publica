package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Getter
@Setter
public class LoteRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull
    private UUID medicamentoId;

    @NotNull
    private UUID unidadeId;

    @NotBlank
    private String numeroLote;

    @NotNull
    private LocalDate validade;

    /** Vira o lançamento de ENTRADA do livro de movimentação (ADR-0057). */
    @NotNull
    @PositiveOrZero
    private Integer quantidade;

    /** Responsável pelo recebimento, registrado na entrada. Opcional para não quebrar quem já cadastra lotes. */
    private String profissionalMatricula;
}
