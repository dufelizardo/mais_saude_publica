package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** O balanço do encerramento da campanha (ADR-0174), que alimenta a avaliação da C6. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class EncerramentoCampanhaRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Informe o balanço.")
    @Size(max = 2000, message = "O balanço vai até 2000 caracteres.")
    private String balanco;
}
