package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** Local do caso marcado no mapa (ADR-0130), em WGS84. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class LocalCasoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe a latitude.")
    @DecimalMin(value = "-90", message = "A latitude fica entre -90 e 90.")
    @DecimalMax(value = "90", message = "A latitude fica entre -90 e 90.")
    private Double latitude;

    @NotNull(message = "Informe a longitude.")
    @DecimalMin(value = "-180", message = "A longitude fica entre -180 e 180.")
    @DecimalMax(value = "180", message = "A longitude fica entre -180 e 180.")
    private Double longitude;
}
