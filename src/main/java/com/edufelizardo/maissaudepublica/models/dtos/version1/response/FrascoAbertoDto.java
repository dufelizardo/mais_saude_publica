package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/** Frasco multidose aberto (ADR-0148), com o que resta e até quando vale. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class FrascoAbertoDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private UUID loteId;
    private String sigla;
    private String numeroLote;
    private LocalDateTime abertoEm;
    private LocalDateTime validoAte;
    private int dosesNoFrasco;
    private int dosesUsadas;
    private int dosesRestantes;
    private long minutosRestantes;
}
