package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.MotivoAjusteIndividual;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Getter
@Setter
public class AjusteIndividualRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank
    private String matriculaProfissional;

    @NotNull
    private BigDecimal valor;

    @NotNull
    private LocalDate dataInicio;

    private LocalDate dataFim;

    @NotNull
    private MotivoAjusteIndividual motivo;

    private String referencia;
}
