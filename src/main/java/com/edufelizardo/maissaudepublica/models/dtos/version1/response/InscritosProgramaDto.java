package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/** Os pacientes acompanhados num programa, no escopo de quem lê, com os indicadores (ADR-0119). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class InscritosProgramaDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID programaId;
    private long inscritos;
    private long faltosos;
    private long atrasados;
    private List<AcompanhamentoDto> lista;
}
