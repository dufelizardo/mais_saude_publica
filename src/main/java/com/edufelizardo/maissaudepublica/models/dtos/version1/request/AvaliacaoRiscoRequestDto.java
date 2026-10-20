package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.*;
import com.edufelizardo.maissaudepublica.models.enuns.NivelRiscoSaudeMental;
import com.edufelizardo.maissaudepublica.models.enuns.TipoRiscoSaudeMental;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/** Avaliação (ou reavaliação) de risco do caso (ADR-0123). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AvaliacaoRiscoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe o tipo de risco.")
    private TipoRiscoSaudeMental tipo;

    @NotNull(message = "Informe o nível do risco.")
    private NivelRiscoSaudeMental nivel;

    @Size(max = 2000, message = "Os fatores vão até 2000 caracteres.")
    private String fatores;

    /** Obrigatórias de moderado para cima. */
    @Size(max = 2000, message = "As medidas vão até 2000 caracteres.")
    private String medidas;

    /** Sem valor, sem plano de segurança. */
    private Boolean planoSeguranca;

    @Size(max = 100, message = "A frequência de contato vai até 100 caracteres.")
    private String frequenciaContato;

    /** Obrigatória no risco alto. */
    private LocalDate reavaliarEm;
}
