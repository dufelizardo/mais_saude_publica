package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/** Resultado da aplicação de um modelo de jornada (ADR-0107): quantos turnos foram criados e quais ficaram de fora. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AplicacaoModeloResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private int criados;
    private double horas;
    private List<CopiaSemanaResponseDto.Ignorado> ignorados;
}
