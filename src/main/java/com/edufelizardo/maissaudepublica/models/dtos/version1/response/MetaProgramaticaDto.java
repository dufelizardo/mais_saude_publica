package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.IndicadorProgramatico;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Uma meta do programa (ADR-0141). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class MetaProgramaticaDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private UUID programaId;
    private IndicadorProgramatico indicador;
    private String indicadorNome;
    private UUID unidadeId;
    private String unidadeNome;
    private BigDecimal alvo;
    private LocalDate inicio;
    private LocalDate fim;
    private UUID exameId;
    private String exameNome;
    private BigDecimal limiteMinimo;
    private BigDecimal limiteMaximo;
}
