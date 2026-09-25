package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@Data
@Getter
@Setter
public class NecessidadeDePessoalRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull
    private UUID unidadeId;

    private UUID setorId;

    @NotNull
    private UUID cargoId;

    @NotNull
    private Integer quantidade;

    @NotNull
    private Integer jornadaSemanalHoras;

    private String competenciasNecessarias;

    private String justificativa;
}
