package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/** Um alerta da situação operacional (ADR-0144), calculado na hora: fila, espera, agenda ou escala. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AlertaSituacaoDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /** FILA, ESPERA, AGENDA ou ESCALA. */
    private String tipo;
    /** ALTA ou MEDIA. */
    private String gravidade;
    private String titulo;
    private String detalhe;
    private UUID unidadeId;
    private String unidadeNome;
    /** A tela onde se age (Atendimentos, Agenda, Escalas). */
    private String rota;
}
