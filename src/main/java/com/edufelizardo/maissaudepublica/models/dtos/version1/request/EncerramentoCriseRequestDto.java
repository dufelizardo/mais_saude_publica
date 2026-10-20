package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.*;
import com.edufelizardo.maissaudepublica.models.enuns.DesfechoCriseSaudeMental;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** Encerramento da crise, com o desfecho (ADR-0123). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class EncerramentoCriseRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe o desfecho.")
    private DesfechoCriseSaudeMental desfecho;
}
