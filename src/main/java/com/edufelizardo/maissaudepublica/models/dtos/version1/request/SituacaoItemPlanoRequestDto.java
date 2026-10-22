package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.SituacaoItemPlanoTratamento;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/** Mudança de situação de um item pelo dentista (ADR-0137): em andamento, encaminhado ou cancelado. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class SituacaoItemPlanoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe a situação: EM_ANDAMENTO, ENCAMINHADO ou CANCELADO.")
    private SituacaoItemPlanoTratamento situacao;

    @Size(max = 500, message = "O motivo vai até 500 caracteres.")
    private String motivo;

    /** No encaminhamento, a solicitação de regulação do mesmo paciente (opcional). */
    private UUID solicitacaoRegulacaoId;
}
