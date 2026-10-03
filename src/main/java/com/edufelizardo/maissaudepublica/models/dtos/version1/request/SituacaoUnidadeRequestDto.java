package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.SituacaoOperacional;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** Mudança de situação operacional (ADR-0101). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class SituacaoUnidadeRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe a situação.")
    private SituacaoOperacional situacao;

    @Size(max = 500, message = "O motivo vai até 500 caracteres.")
    private String motivo;

    private LocalDate previsaoRetorno;
}
