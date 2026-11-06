package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import com.edufelizardo.maissaudepublica.models.enuns.CausalidadeEsavi;
import com.edufelizardo.maissaudepublica.models.enuns.CondutaEsquemaEsavi;
import com.edufelizardo.maissaudepublica.models.enuns.EvolucaoCaso;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** Encerramento da notificação (ADR-0124). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class EncerramentoNotificacaoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe a evolução.")
    private EvolucaoCaso evolucao;

    /** Só no ESAVI, e obrigatórias nele (ADR-0150). */
    private CausalidadeEsavi causalidade;

    private CondutaEsquemaEsavi conduta;
}
