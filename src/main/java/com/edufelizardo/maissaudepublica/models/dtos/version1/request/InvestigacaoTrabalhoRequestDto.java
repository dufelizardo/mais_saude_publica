package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/** A visita ao local de trabalho e os achados (ADR-0208). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class InvestigacaoTrabalhoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /** A data da visita; sem ela, a investigação começa sem visita. */
    private LocalDate visitaEm;

    @Size(max = 500, message = "O local visitado vai até 500 caracteres.")
    private String localVisitado;

    @Size(max = 2000, message = "Os achados vão até 2000 caracteres.")
    private String achados;
}
