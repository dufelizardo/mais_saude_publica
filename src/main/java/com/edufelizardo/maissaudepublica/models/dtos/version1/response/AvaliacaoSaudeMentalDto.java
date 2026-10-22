package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/** Uma avaliação em saúde mental (ADR-0120); só vai para quem tem o conteúdo do caso liberado. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AvaliacaoSaudeMentalDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private Instant realizadaEm;
    private String profissionalNome;
    private String contextoHistoria;
    private String aspectosPsicossociais;
    private String fatoresRisco;
    private String fatoresProtecao;
    private String necessidades;
    private String cid;
}
