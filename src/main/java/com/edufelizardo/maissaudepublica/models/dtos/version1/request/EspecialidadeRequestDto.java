package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** Uma especialidade do catálogo (ADR-0134); o código não muda. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class EspecialidadeRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Informe o código.")
    @Pattern(regexp = "^[A-Z][A-Z0-9_]{1,39}$", message = "Use letras maiúsculas, números e sublinhado (ex.: ODONTOLOGIA).")
    private String codigo;

    @NotBlank(message = "Informe o nome.")
    @Size(max = 100, message = "O nome vai até 100 caracteres.")
    private String nome;

    private Boolean odontologica;

    private Boolean ativa;
}
