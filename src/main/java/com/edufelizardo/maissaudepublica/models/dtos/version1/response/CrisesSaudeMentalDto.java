package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/** As crises do escopo (ADR-0123), com os números: em curso e notificações pendentes. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class CrisesSaudeMentalDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private long emCurso;
    private long notificacoesPendentes;
    private List<EventoCriseDto> lista;
}
