package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.*;
import com.edufelizardo.maissaudepublica.models.enuns.PrioridadeRegulacao;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/** Encaminhamento do caso pela Regulação (ADR-0122): com {@code solicitacaoId}, vincula uma solicitação
 * existente do paciente; sem ele, cria a solicitação com o procedimento, o CID, a justificativa e a prioridade. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class EncaminhamentoSaudeMentalRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /** Vincular uma solicitação existente do mesmo paciente. */
    private UUID solicitacaoId;

    private UUID procedimentoId;

    /** Sem matrícula, o profissional do usuário logado. */
    private String profissionalMatricula;

    private String cid;

    @Size(max = 4000, message = "A justificativa vai até 4000 caracteres.")
    private String justificativa;

    private PrioridadeRegulacao prioridade;
}
