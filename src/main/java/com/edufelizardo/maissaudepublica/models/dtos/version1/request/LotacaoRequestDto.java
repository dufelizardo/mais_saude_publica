package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class LotacaoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank
    private String matriculaProfissional;

    @NotNull
    private UUID unidadeId;

    @NotNull
    private UUID cargoId;

    /** O setor da unidade, opcional (ADR-0221). */
    private UUID setorId;

    private Integer jornadaSemanalHoras;

    @NotNull
    private LocalDate dataInicio;

    private String motivo;
}
