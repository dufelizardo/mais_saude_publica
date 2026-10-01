package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * Nova identidade de login (ADR-0068). A senha é a inicial, definida por quem cadastra e entregue à
 * pessoa por canal próprio; troca obrigatória no primeiro acesso fica como pendência.
 */
@Data
@Getter
@Setter
public class UsuarioRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank
    private String cpf;

    @NotBlank
    @Size(max = 160)
    private String nome;

    @NotBlank
    @Size(min = 8, max = 72, message = "A senha precisa ter entre 8 e 72 caracteres")
    private String senha;
}
