package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/** Amostra de água (ADR-0164): ao menos um parâmetro (400). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AmostraAguaRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe o ponto de coleta.")
    private UUID pontoId;
    private LocalDate dataColeta;
    private Boolean coliformesTotais;
    private Boolean ecoli;
    @DecimalMin(value = "0", message = "O cloro residual não fica abaixo de zero.")
    private Double cloroResidual;
    @DecimalMin(value = "0", message = "A turbidez não fica abaixo de zero.")
    private Double turbidez;
    @DecimalMin(value = "0", message = "O flúor não fica abaixo de zero.")
    private Double fluor;
}
