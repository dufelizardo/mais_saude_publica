package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Getter
@Setter
public class DispensacaoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull
    private UUID loteId;

    @NotNull
    private UUID pacienteId;

    /**
     * FK direta ao {@link com.edufelizardo.maissaudepublica.models.Profissional} pela matrícula
     * (mesma convenção de {@code TriagemRequestDto.profissionalMatricula}, ver ADR-0034).
     */
    @NotBlank
    private String profissionalMatricula;

    /**
     * Opcional — nem toda dispensação nasce de uma consulta registrada no sistema (ver ADR-0051).
     */
    private UUID consultaId;

    @NotNull
    @Positive
    private Integer quantidade;

    @NotNull
    private LocalDateTime dataHora;
}
