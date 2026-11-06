package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.DesfechoLeishmanioseCanina;
import com.edufelizardo.maissaudepublica.models.enuns.ResultadoExameAnimal;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** O resultado do exame do animal e, na leishmaniose reagente, o desfecho do cão (ADR-0163). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ResultadoExameAnimalRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe o resultado.")
    private ResultadoExameAnimal resultado;
    private DesfechoLeishmanioseCanina desfechoLeishmaniose;
}
