package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/** Os surtos do escopo e os indicadores (ADR-0131). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class SurtosDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private long abertos;
    private long confirmados;
    /** Medidas abertas com prazo vencido, de surtos e de casos. */
    private long medidasVencidas;
    private List<SurtoDto> lista;
}
