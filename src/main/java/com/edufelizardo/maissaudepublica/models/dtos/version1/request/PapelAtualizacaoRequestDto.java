package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/** Atualização de papel: o código não muda (ADR-0066). */
@Data
@Getter
@Setter
public class PapelAtualizacaoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank
    @Size(max = 120)
    private String nome;

    @Size(max = 1000)
    private String descricao;

    @NotNull
    private Boolean ativo;

    @NotNull
    private List<String> permissoes;
}
