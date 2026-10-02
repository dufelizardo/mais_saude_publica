package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import java.time.LocalDate;
import java.util.List;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** Um dia da agenda (ADR-0091). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class DiaAgendaDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private LocalDate data;
    private List<ItemAgendaDto> itens;
}
