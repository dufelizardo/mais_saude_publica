package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/** As ações programáticas do escopo no período, com os indicadores (ADR-0132). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AcoesProgramaticasDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private LocalDate de;
    private LocalDate ate;
    private long pendentes;
    /** Pendentes com a data passada, de qualquer período. */
    private long vencidas;
    private long realizadasNoMes;
    private long prontasParaConfirmar;
    private List<AcaoProgramaticaDto> lista;
}
