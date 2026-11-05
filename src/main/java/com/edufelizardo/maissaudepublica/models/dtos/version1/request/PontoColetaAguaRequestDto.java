package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.TipoAbastecimentoAgua;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/** Ponto de coleta de água (ADR-0164); a latitude e a longitude vão juntas (400). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class PontoColetaAguaRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Informe o nome do ponto.")
    @Size(max = 200, message = "O nome vai até 200 caracteres.")
    private String nome;
    @NotNull(message = "Informe a forma de abastecimento.")
    private TipoAbastecimentoAgua tipo;
    @NotBlank(message = "Informe o local.")
    @Size(max = 300, message = "O local vai até 300 caracteres.")
    private String local;
    @DecimalMin(value = "-90", message = "A latitude vai de -90 a 90.")
    @DecimalMax(value = "90", message = "A latitude vai de -90 a 90.")
    private Double latitude;
    @DecimalMin(value = "-180", message = "A longitude vai de -180 a 180.")
    @DecimalMax(value = "180", message = "A longitude vai de -180 a 180.")
    private Double longitude;
    private UUID microareaId;
}
