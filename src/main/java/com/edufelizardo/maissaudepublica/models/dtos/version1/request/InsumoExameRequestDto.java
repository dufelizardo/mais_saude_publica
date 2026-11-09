package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/** Um insumo do exame (ADR-0183): o material do Estoque e a quantidade por coleta ou realização. */
@Data
public class InsumoExameRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull
    private UUID materialId;

    @NotNull
    @Min(1)
    @Max(100)
    private Integer quantidade;

    /** Só no exame de imagem: baixa apenas quando a realização usou contraste. */
    private Boolean soComContraste;
}
