package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.MeioComunicacaoCritico;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/** Registro da comunicação de valor crítico (ADR-0181). Sem data e hora, vale o agora. */
@Data
public class ComunicacaoCriticoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank
    private String profissionalMatricula;

    @NotBlank
    @Size(max = 150)
    private String receptorNome;

    @NotNull
    private MeioComunicacaoCritico meio;

    private LocalDateTime comunicadoEm;
}
