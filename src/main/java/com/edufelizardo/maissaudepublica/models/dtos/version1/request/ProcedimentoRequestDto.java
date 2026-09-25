package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.StatusProcedimento;
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
public class ProcedimentoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull
    private UUID consultaId;

    /**
     * FK direta ao {@link com.edufelizardo.maissaudepublica.models.Profissional} pela matrícula
     * (mesma convenção de {@code ConsultaRequestDto.profissionalMatricula}, ver ADR-0034).
     */
    @NotBlank
    private String profissionalMatricula;

    @NotBlank
    private String tipo;

    private String descricao;

    @NotNull
    private LocalDateTime dataRealizacao;

    @NotNull
    private StatusProcedimento status;
}
