package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/** Um compartilhamento do cuidado do caso (ADR-0122). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class CompartilhamentoCuidadoDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private UUID equipeId;
    private String equipeNome;
    private String unidadeNome;
    private String objetivo;
    private LocalDate inicio;
    private LocalDate fim;
    private String motivoFim;
}
