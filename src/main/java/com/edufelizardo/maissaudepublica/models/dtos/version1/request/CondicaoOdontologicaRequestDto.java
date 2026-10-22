package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.AlcanceCondicaoOdontologica;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** Uma condição do catálogo do odontograma (ADR-0135); o código não muda. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class CondicaoOdontologicaRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Informe o código.")
    @Pattern(regexp = "^[A-Z][A-Z0-9_]{1,39}$", message = "Use letras maiúsculas, números e sublinhado (ex.: CARIE).")
    private String codigo;

    @NotBlank(message = "Informe o nome.")
    @Size(max = 100, message = "O nome vai até 100 caracteres.")
    private String nome;

    @NotNull(message = "Informe o alcance: FACE, DENTE ou AMBOS.")
    private AlcanceCondicaoOdontologica alcance;

    @NotBlank(message = "Informe a cor.")
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "A cor é #RRGGBB.")
    private String cor;

    @NotBlank(message = "Informe a sigla.")
    @Size(max = 4, message = "A sigla vai até 4 caracteres.")
    private String sigla;

    private Boolean ativa;
}
