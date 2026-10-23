package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/** O tempo de espera no período (ADR-0144): da chegada ao primeiro contato e à consulta, só com a chegada registrada. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class EsperaAtendimentoDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private LocalDate de;
    private LocalDate ate;
    private UUID unidadeId;
    private String abrangencia;
    /** Atendimentos com chegada registrada no período. */
    private long comChegada;
    /** Tempos negativos ou acima de 24 horas, descartados como erro de registro. */
    private long descartados;
    private EstatisticaEsperaDto primeiroContato;
    private EstatisticaEsperaDto consulta;
}
