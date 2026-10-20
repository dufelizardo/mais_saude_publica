package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/** Os casos em saúde mental do escopo (ADR-0120), com os números da tela: em acompanhamento, faltosos e sem avaliação. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class CasosSaudeMentalDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private long emAcompanhamento;
    private long faltosos;
    private long semAvaliacao;
    private List<AcompanhamentoSaudeMentalDto> lista;
}
