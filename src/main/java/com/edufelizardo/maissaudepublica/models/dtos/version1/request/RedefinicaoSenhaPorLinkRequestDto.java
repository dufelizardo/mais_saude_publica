package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/** Nova senha a partir do link de recuperação (ADR-0081). */
@Data
@Getter
@Setter
public class RedefinicaoSenhaPorLinkRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Link inválido")
    private String token;

    @NotBlank
    @Size(min = 8, max = 72, message = "A nova senha precisa ter entre 8 e 72 caracteres")
    private String novaSenha;
}
