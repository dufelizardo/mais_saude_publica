package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.MeioExposicaoAmbiental;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/** Exposição ambiental no desastre (ADR-0161): o meio, o agente, a população exposta, as unidades e as medidas. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ExposicaoAmbientalRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe o meio.")
    private MeioExposicaoAmbiental meio;
    @NotBlank(message = "Informe o agente ou a substância.")
    @Size(max = 200, message = "O agente vai até 200 caracteres.")
    private String agente;
    @Size(max = 300, message = "A fonte vai até 300 caracteres.")
    private String fonte;
    @Min(value = 0, message = "A população exposta não fica abaixo de zero.")
    private Integer populacaoExposta;
    private List<UUID> unidadeIds;
    @Size(max = 1000, message = "As medidas vão até 1000 caracteres.")
    private String medidas;
    @Size(max = 1000, message = "A recomendação vai até 1000 caracteres.")
    private String recomendacao;
}
