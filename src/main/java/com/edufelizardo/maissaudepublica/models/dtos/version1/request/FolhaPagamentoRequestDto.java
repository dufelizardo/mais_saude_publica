package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Getter
@Setter
public class FolhaPagamentoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank
    private String matriculaProfissional;

    @NotBlank
    private String competencia;

    @NotNull
    private BigDecimal proventos;

    @NotNull
    private BigDecimal descontos;

    @NotNull
    private BigDecimal encargos;

    @NotNull
    private BigDecimal total;
}
