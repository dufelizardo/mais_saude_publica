package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/** Senha provisória definida pela administração — a pessoa troca no próximo acesso (ADR-0069). */
@Data
@Getter
@Setter
public class RedefinicaoSenhaRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank
    @Size(min = 8, max = 72, message = "A senha provisória precisa ter entre 8 e 72 caracteres")
    private String senhaProvisoria;
}
