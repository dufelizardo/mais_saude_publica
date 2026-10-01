package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/** Troca da própria senha (ADR-0069). {@code manterConectado} decide a validade do token novo, como no login. */
@Data
@Getter
@Setter
public class TrocaSenhaRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank
    private String senhaAtual;

    @NotBlank
    @Size(min = 8, max = 72, message = "A nova senha precisa ter entre 8 e 72 caracteres")
    private String novaSenha;

    private boolean manterConectado;
}
