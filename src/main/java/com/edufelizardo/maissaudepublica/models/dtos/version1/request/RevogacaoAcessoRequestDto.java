package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/** Revogação de um acesso: a atribuição não é apagada, fica com motivo, autor e hora (ADR-0066). */
@Data
@Getter
@Setter
public class RevogacaoAcessoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank
    @Size(max = 1000)
    private String motivo;
}
