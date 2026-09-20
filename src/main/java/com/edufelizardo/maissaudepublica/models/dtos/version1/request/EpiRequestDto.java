package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

@Data
@Getter
@Setter
public class EpiRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank
    private String matriculaProfissional;

    @NotBlank
    private String tipo;

    private String numeroCA;

    @NotNull
    private LocalDate dataEntrega;

    private LocalDate dataDevolucao;
}
