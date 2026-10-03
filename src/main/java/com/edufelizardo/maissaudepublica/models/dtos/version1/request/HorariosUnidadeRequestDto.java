package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** O horário estruturado da unidade, que substitui o anterior inteiro (ADR-0101). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class HorariosUnidadeRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /** 24 horas: o horário não fecha a agenda (os turnos podem ficar vazios). */
    private boolean funciona24h;

    @NotNull(message = "Informe os turnos (lista vazia: sem horário).")
    private List<@Valid TurnoHorarioDto> turnos;
}
