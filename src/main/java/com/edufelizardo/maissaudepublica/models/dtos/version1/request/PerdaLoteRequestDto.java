package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.MotivoPerdaImunobiologico;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** Perda física de imunobiológico (ADR-0148). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class PerdaLoteRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull
    @Positive
    private Integer doses;
    @NotNull
    private MotivoPerdaImunobiologico motivo;
    @Size(max = 500)
    private String justificativa;
}
