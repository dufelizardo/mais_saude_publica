package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.NivelRiscoSaudeMental;
import com.edufelizardo.maissaudepublica.models.enuns.TipoRiscoSaudeMental;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Uma avaliação de risco (ADR-0123). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AvaliacaoRiscoDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private TipoRiscoSaudeMental tipo;
    private NivelRiscoSaudeMental nivel;
    private String fatores;
    private String medidas;
    private boolean planoSeguranca;
    private String frequenciaContato;
    private LocalDate reavaliarEm;
    private boolean reavaliacaoVencida;
    private Instant avaliadaEm;
    private String profissionalNome;
    /** É a avaliação mais recente do tipo. */
    private boolean vigente;
}
