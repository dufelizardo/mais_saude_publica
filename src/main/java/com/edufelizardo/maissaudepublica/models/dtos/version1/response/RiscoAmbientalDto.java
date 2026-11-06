package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.SituacaoRiscoAmbiental;
import com.edufelizardo.maissaudepublica.models.enuns.TipoRiscoAmbiental;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/** Um risco ambiental (ADR-0164). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class RiscoAmbientalDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private TipoRiscoAmbiental tipo;
    private String descricao;
    private String local;
    private Double latitude;
    private Double longitude;
    private String microareaCodigo;
    private String areaNome;
    private Integer populacaoExposta;
    private SituacaoRiscoAmbiental situacao;
    private Instant controladoEm;
    private String motivoControle;
    private Instant registradoEm;
}
