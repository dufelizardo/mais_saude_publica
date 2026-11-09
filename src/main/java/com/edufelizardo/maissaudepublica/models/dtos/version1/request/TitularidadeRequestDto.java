package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.PapelTitularidade;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/** Designação de quem responde pela área (ADR-0175). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class TitularidadeRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Informe a matrícula do profissional.")
    private String matriculaProfissional;
    @NotNull(message = "Informe o papel.")
    private PapelTitularidade papel;
    @NotNull(message = "Informe o início.")
    private LocalDate inicio;
    @Size(max = 120, message = "O ato vai até 120 caracteres.")
    private String ato;
}
