package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/** Um evento da linha do tempo do caso ou do surto (ADR-0131). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class EventoLinhaDoTempoDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Instant em;
    private String titulo;
    private String detalhe;
    private String autor;
}
