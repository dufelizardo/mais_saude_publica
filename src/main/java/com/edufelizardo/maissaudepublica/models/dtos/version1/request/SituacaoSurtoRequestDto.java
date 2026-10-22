package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.SituacaoSurto;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** Mudança de situação do surto (ADR-0131); encerrar pede a conclusão. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class SituacaoSurtoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe a situação.")
    private SituacaoSurto situacao;

    @Size(max = 2000, message = "A conclusão vai até 2000 caracteres.")
    private String conclusao;
}
