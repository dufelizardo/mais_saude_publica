package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.TipoRiscoAmbiental;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/** Risco ambiental permanente (ADR-0164); a latitude e a longitude vão juntas (400). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class RiscoAmbientalRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe o tipo do risco.")
    private TipoRiscoAmbiental tipo;
    @NotBlank(message = "Descreva o risco.")
    @Size(max = 1000, message = "A descrição vai até 1000 caracteres.")
    private String descricao;
    @NotBlank(message = "Informe o local.")
    @Size(max = 300, message = "O local vai até 300 caracteres.")
    private String local;
    @Min(value = 0, message = "A população exposta não fica abaixo de zero.")
    private Integer populacaoExposta;
    @DecimalMin(value = "-90", message = "A latitude vai de -90 a 90.")
    @DecimalMax(value = "90", message = "A latitude vai de -90 a 90.")
    private Double latitude;
    @DecimalMin(value = "-180", message = "A longitude vai de -180 a 180.")
    @DecimalMax(value = "180", message = "A longitude vai de -180 a 180.")
    private Double longitude;
    private UUID microareaId;
}
