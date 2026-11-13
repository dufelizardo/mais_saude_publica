package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.InterpretacaoResultado;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * Laudo de imagem assinado pelo radiologista (ADR-0182), também usado no adendo, que exige o motivo. A classificação é NORMAL
 * (sem alterações), ALTERADO ou ACHADO_CRITICO.
 */
@Data
public class LaudoImagemRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank
    private String profissionalMatricula;

    @Size(max = 1000)
    private String tecnica;

    @NotBlank
    @Size(max = 4000)
    private String achados;

    @NotBlank
    @Size(max = 2000)
    private String conclusao;

    @NotNull
    private InterpretacaoResultado classificacao;

    @Size(max = 500)
    private String imagemUrl;

    /** Só no adendo, obrigatório. */
    @Size(max = 1000)
    private String motivo;
}
