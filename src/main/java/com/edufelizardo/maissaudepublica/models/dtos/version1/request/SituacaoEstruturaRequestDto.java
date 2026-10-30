package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.SituacaoEstruturaTemporaria;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** Pôr a estrutura em operação ou desativá-la (ADR-0160); desativar pede o motivo (400). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class SituacaoEstruturaRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe a situação.")
    private SituacaoEstruturaTemporaria situacao;
    @Size(max = 500, message = "O motivo vai até 500 caracteres.")
    private String motivo;
}
