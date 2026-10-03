package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** Indicadores do mapa de leitos (ADR-0098). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class IndicadoresLeitosDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /** Leitos em uso (ativos), contando os bloqueados. */
    private long leitos;
    private long livres;
    private long ocupados;
    private long higienizacao;
    private long bloqueados;
    /** Ocupados sobre os leitos operacionais (ativos e não bloqueados), em %. Nulo sem leito operacional. */
    private Double taxaOcupacao;
    /** Média de dias das internações com alta nos últimos 30 dias. Nula sem alta no período. */
    private Double mediaPermanenciaDias;
    private long altas30Dias;

}
