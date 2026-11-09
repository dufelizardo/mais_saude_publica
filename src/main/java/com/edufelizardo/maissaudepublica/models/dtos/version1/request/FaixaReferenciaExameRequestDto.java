package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.Sexo;
import com.edufelizardo.maissaudepublica.models.enuns.UnidadeIdade;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Faixa de referência por sexo e idade de um exame numérico (ADR-0180). Sem sexo, vale para os dois; sem idade máxima, não
 * tem limite. Precisa de ao menos um limite de referência.
 */
@Data
public class FaixaReferenciaExameRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Sexo sexo;

    @NotNull
    @Min(0)
    @Max(200)
    private Integer idadeMinima;

    @NotNull
    private UnidadeIdade unidadeIdadeMinima;

    @Min(1)
    @Max(200)
    private Integer idadeMaxima;

    private UnidadeIdade unidadeIdadeMaxima;

    private BigDecimal referenciaMinima;

    private BigDecimal referenciaMaxima;

    private BigDecimal criticoMinimo;

    private BigDecimal criticoMaximo;

    @Size(max = 100)
    private String descricao;
}
