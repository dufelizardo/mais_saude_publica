package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/** As transferências pendentes do escopo: as que chegam e as que saem (ADR-0122). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class TransferenciasCuidadoDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private List<TransferenciaCuidadoDto> recebidas;
    private List<TransferenciaCuidadoDto> enviadas;
}
