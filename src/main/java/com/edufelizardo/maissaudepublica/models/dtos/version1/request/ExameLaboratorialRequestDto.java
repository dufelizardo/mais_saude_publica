package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.MaterialExame;
import com.edufelizardo.maissaudepublica.models.enuns.TipoResultadoExame;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/** Cadastro e edição do catálogo de exames (ADR-0093). Sem {@code ativo}, fica ativo. */
@Data
@Getter
@Setter
public class ExameLaboratorialRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank
    @Size(max = 200)
    private String nome;

    @NotNull
    private MaterialExame material;

    @NotNull
    private TipoResultadoExame tipoResultado;

    @Size(max = 30)
    private String unidadeMedida;

    private BigDecimal referenciaMinima;

    private BigDecimal referenciaMaxima;

    @Size(max = 200)
    private String referenciaTexto;

    @Size(max = 500)
    private String preparo;

    @Min(0)
    @Max(365)
    private Integer prazoDias;

    private Boolean ativo;
}
