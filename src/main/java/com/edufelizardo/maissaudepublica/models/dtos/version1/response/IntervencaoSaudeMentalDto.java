package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.SituacaoIntervencaoSaudeMental;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/** Uma intervenção realizada no caso (ADR-0121). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class IntervencaoSaudeMentalDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private UUID tipoId;
    private String tipoNome;
    private Instant realizadaEm;
    private SituacaoIntervencaoSaudeMental situacao;
    private String profissionalNome;
    private String compartilhadoComNome;
    private UUID previstaId;
    private String resumo;
    private String equipeMatriciadaNome;
}
