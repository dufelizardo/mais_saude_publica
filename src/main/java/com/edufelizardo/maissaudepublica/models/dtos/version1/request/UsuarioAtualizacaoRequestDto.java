package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/** Nome e situação do usuário (ADR-0068). O CPF não muda: é a identidade. */
@Data
@Getter
@Setter
public class UsuarioAtualizacaoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank
    @Size(max = 160)
    private String nome;

    @NotNull
    private Boolean ativo;
}
