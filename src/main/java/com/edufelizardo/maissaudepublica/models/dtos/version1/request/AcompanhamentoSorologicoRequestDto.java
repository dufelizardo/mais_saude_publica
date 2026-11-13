package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.EtapaAcompanhamentoSorologico;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/** Etapa do acompanhamento sorológico realizada (ADR-0207). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AcompanhamentoSorologicoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe a etapa.")
    private EtapaAcompanhamentoSorologico etapa;

    @NotNull(message = "Informe a data em que foi realizado.")
    private LocalDate realizadoEm;
}
