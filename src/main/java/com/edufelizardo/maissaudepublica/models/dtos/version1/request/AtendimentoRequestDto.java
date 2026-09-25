package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.StatusAtendimento;
import com.edufelizardo.maissaudepublica.models.enuns.TipoAtendimento;
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
public class AtendimentoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull
    private UUID pacienteId;

    /**
     * FK direta ao {@link com.edufelizardo.maissaudepublica.models.Profissional} pela matrícula
     * (mesma convenção de {@code SetorRequestDto.matriculaResponsavel}, ver ADR-0034).
     */
    @NotBlank
    private String profissionalMatricula;

    @NotNull
    private UUID unidadeId;

    /**
     * Opcional — nem todo atendimento passa por um setor específico.
     */
    private UUID setorId;

    @NotNull
    private TipoAtendimento tipo;

    @NotNull
    private StatusAtendimento status;

    @NotNull
    private LocalDateTime dataHora;
}
