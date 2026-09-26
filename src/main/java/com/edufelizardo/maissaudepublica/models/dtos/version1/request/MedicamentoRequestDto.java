package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Data
@Getter
@Setter
public class MedicamentoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank
    private String nome;

    private String principioAtivo;
    private String apresentacao;
    private String codigo;

    @NotNull(message = "Campo ativo precisa ser preenchido")
    private Boolean ativo;
}
