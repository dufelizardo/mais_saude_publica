package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.SituacaoMobilizacao;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/** A situação da mobilização (ADR-0158): cancelar pede o motivo; concluir pode informar o fim. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class SituacaoMobilizacaoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe a situação.")
    private SituacaoMobilizacao situacao;
    @Size(max = 500, message = "O motivo vai até 500 caracteres.")
    private String motivo;
    private LocalDate fim;
}
