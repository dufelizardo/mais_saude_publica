package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Uma notificação negativa (ADR-0124). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class NotificacaoNegativaDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private UUID unidadeId;
    private String unidadeNome;
    private int ano;
    private int semana;
    private LocalDate inicio;
    private LocalDate fim;
    private String responsavelNome;
    private Instant declaradaEm;
}
