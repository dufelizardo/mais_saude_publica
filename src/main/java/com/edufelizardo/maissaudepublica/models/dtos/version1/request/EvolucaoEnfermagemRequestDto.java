package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class EvolucaoEnfermagemRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull
    private UUID atendimentoId;

    /**
     * FK direta ao {@link com.edufelizardo.maissaudepublica.models.Profissional} pela matrícula
     * (mesma convenção de {@code TriagemRequestDto.profissionalMatricula}, ver ADR-0034).
     */
    @NotBlank
    private String profissionalMatricula;

    @NotNull
    private LocalDateTime dataHora;

    @NotBlank
    private String descricao;
}
