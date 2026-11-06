package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.SituacaoEncaminhamentoIntersetorial;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * O recebimento ou o retorno da instituição externa (ADR-0154). No retorno, o desfecho (CONCLUIDO ou RECUSADO) e o texto
 * são obrigatórios; no recebimento, só a data. Sem {@code data}, hoje.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class RetornoEncaminhamentoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private LocalDate data;

    private SituacaoEncaminhamentoIntersetorial desfecho;

    @Size(max = 2000, message = "O retorno vai até 2000 caracteres.")
    private String retorno;
}
