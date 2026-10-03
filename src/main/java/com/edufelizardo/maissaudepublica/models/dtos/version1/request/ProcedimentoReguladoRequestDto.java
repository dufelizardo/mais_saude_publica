package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.TipoProcedimentoRegulado;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/** Cadastro e edição do catálogo de procedimentos regulados (ADR-0087). Sem {@code ativo}, fica ativo. */
@Data
@Getter
@Setter
public class ProcedimentoReguladoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank
    @Size(max = 200)
    private String nome;

    @NotNull
    private TipoProcedimentoRegulado tipo;

    private Boolean ativo;
}
