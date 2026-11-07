package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.TipoPontoOperacional;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/** Ponto operacional (ADR-0166). O tipo e o início só valem na criação. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class PontoOperacionalRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private TipoPontoOperacional tipo;
    @NotBlank(message = "Informe o nome do ponto.")
    @Size(max = 150, message = "O nome vai até 150 caracteres.")
    private String nome;
    @Size(max = 300, message = "A referência vai até 300 caracteres.")
    private String referencia;
    @DecimalMin(value = "-90", message = "A latitude fica entre -90 e 90.")
    @DecimalMax(value = "90", message = "A latitude fica entre -90 e 90.")
    private Double latitude;
    @DecimalMin(value = "-180", message = "A longitude fica entre -180 e 180.")
    @DecimalMax(value = "180", message = "A longitude fica entre -180 e 180.")
    private Double longitude;
    private LocalDate inicio;
}
