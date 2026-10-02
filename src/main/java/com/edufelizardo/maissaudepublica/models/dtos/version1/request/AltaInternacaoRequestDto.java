package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.TipoAlta;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
/** Alta da internação, ato médico (ADR-0098). */
public class AltaInternacaoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe o tipo da alta.")
    private TipoAlta tipoAlta;

    @NotBlank(message = "Escreva o sumário de alta.")
    @Size(min = 10, max = 4000, message = "O sumário de alta precisa ter de 10 a 4000 caracteres.")
    private String sumario;

    @NotBlank(message = "Informe a matrícula do médico que dá a alta.")
    private String medicoMatricula;

    /** Quando a alta aconteceu; sem ela, agora. Não pode ser antes da admissão nem no futuro. */
    private Instant altaEm;
}
