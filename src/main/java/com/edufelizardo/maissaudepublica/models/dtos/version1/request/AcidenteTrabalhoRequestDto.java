package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Getter
@Setter
public class AcidenteTrabalhoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank
    private String matriculaProfissional;

    @NotNull
    private LocalDateTime dataHora;

    private String descricao;

    @NotNull
    private Boolean catEmitida;

    private String catUrl;

    private Integer diasAfastamento;
}
