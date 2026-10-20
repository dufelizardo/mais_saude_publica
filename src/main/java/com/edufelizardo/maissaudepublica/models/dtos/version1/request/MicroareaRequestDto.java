package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/** Microárea de uma área (ADR-0125). A área não muda na edição. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class MicroareaRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe a área.")
    private UUID areaId;

    @NotBlank(message = "Informe o código.")
    @Size(max = 20, message = "O código vai até 20 caracteres.")
    private String codigo;

    @Size(max = 120, message = "O nome vai até 120 caracteres.")
    private String nome;

    /** Pessoas de referência; acima de 750 (CNES), aviso. */
    @Min(value = 0, message = "A população de referência não é negativa.")
    @Max(value = 100000, message = "A população de referência vai até 100000.")
    private Integer populacaoReferencia;

    /** GeoJSON (WGS84): Polygon, MultiPolygon ou uma Feature com um deles. Sem valor, sem geometria. */
    @Size(max = 2000000, message = "A geometria vai até 2 MB.")
    private String geometria;
}
