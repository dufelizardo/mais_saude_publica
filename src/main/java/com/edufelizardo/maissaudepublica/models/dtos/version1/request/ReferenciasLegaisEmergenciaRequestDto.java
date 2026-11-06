package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.TipoDecretoEmergencia;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/** A ESPIN e o decreto, registrados (ADR-0156); o sistema não declara. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ReferenciasLegaisEmergenciaRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Size(max = 60, message = "O número vai até 60 caracteres.")
    private String espinNumero;
    private LocalDate espinData;
    private TipoDecretoEmergencia decretoTipo;
    @Size(max = 60, message = "O número vai até 60 caracteres.")
    private String decretoNumero;
    private LocalDate decretoData;
}
