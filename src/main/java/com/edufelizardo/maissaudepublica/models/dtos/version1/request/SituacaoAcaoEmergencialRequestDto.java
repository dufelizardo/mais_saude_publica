package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.SituacaoAcaoEmergencial;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** A situação da ação (ADR-0158): cancelar pede o motivo. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class SituacaoAcaoEmergencialRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe a situação.")
    private SituacaoAcaoEmergencial situacao;
    @Size(max = 1000, message = "A observação vai até 1000 caracteres.")
    private String observacao;
    @Size(max = 500, message = "O motivo vai até 500 caracteres.")
    private String motivo;
}
