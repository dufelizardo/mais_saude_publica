package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.TipoSegmentoTerritorial;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/** Segmento territorial (ADR-0125). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class SegmentoTerritorialRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Informe o código.")
    @Size(max = 30, message = "O código vai até 30 caracteres.")
    private String codigo;

    @NotBlank(message = "Informe o nome.")
    @Size(max = 120, message = "O nome vai até 120 caracteres.")
    private String nome;

    @NotNull(message = "Informe o tipo.")
    private TipoSegmentoTerritorial tipo;

    /** Unidade de gestão (secretaria, distrito); sem valor, o segmento é da rede inteira. */
    private UUID unidadeGestaoId;

    /** GeoJSON (WGS84): Polygon, MultiPolygon ou uma Feature com um deles. Sem valor, sem geometria. */
    @Size(max = 2000000, message = "A geometria vai até 2 MB.")
    private String geometria;
}
