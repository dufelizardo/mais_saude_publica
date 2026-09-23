package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@Data
@Getter
@Setter
public class ProcessoAdministrativoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull
    private UUID capacidadeId;

    @NotBlank
    private String codigo;

    @NotBlank
    private String nome;

    private String descricao;

    @NotNull
    private Boolean ativo;
}
