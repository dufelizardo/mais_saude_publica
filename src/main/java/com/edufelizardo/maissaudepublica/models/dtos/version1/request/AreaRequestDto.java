package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/** Área de um território (ADR-0125). O território não muda na edição. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AreaRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe o território.")
    private UUID territorioId;

    @NotBlank(message = "Informe o código.")
    @Size(max = 20, message = "O código vai até 20 caracteres.")
    private String codigo;

    @Pattern(regexp = "^\\d{1,10}$", message = "O código CNES da área tem até 10 dígitos.")
    private String codigoCnes;

    @NotBlank(message = "Informe o nome.")
    @Size(max = 120, message = "O nome vai até 120 caracteres.")
    private String nome;

    /** GeoJSON (WGS84): Polygon, MultiPolygon ou uma Feature com um deles. Sem valor, sem geometria. */
    @Size(max = 2000000, message = "A geometria vai até 2 MB.")
    private String geometria;
}
