package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** Um perfil operacional do catálogo (ADR-0117). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class PerfilOperacionalRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Informe o código.")
    @Size(max = 60, message = "O código vai até 60 caracteres.")
    @Pattern(regexp = "[A-Za-z0-9_]+", message = "O código usa só letras, números e sublinhado (ex.: ATENCAO_PRIMARIA).")
    private String codigo;

    @NotBlank(message = "Informe o nome.")
    @Size(max = 100, message = "O nome vai até 100 caracteres.")
    private String nome;

    private String descricao;

    /** Sem valor, ativo. */
    private Boolean ativo;
}
