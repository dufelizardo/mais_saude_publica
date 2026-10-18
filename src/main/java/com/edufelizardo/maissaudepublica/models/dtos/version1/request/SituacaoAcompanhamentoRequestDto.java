package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.SituacaoAcompanhamento;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** Marca o acompanhamento como faltoso ou volta a ativo (ADR-0119). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class SituacaoAcompanhamentoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /** ATIVO ou FALTOSO; para encerrar, use o encerramento. */
    @NotNull(message = "Informe a situação.")
    private SituacaoAcompanhamento situacao;

    @NotBlank(message = "Informe o motivo.")
    @Size(max = 500, message = "O motivo vai até 500 caracteres.")
    private String motivo;
}
