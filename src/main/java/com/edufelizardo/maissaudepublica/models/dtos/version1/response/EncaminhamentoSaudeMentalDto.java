package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.PrioridadeRegulacao;
import com.edufelizardo.maissaudepublica.models.enuns.StatusSolicitacaoRegulacao;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

/** Um encaminhamento do caso pela Regulação, com a contrarreferência (ADR-0122). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class EncaminhamentoSaudeMentalDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private UUID solicitacaoId;
    private String procedimentoNome;
    private StatusSolicitacaoRegulacao status;
    private PrioridadeRegulacao prioridade;
    private Instant solicitadoEm;
    private String unidadeExecutanteNome;
    private LocalDateTime dataHoraPrevista;
    private String contrarreferencia;
}
