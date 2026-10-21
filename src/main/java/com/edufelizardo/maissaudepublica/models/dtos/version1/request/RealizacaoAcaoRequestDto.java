package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.ResultadoBuscaAtiva;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** A ação foi feita (ADR-0132): a data, o atendimento que a cumpriu e, na busca ativa, o resultado. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class RealizacaoAcaoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /** Sem data, hoje. */
    private LocalDate realizadaEm;

    private UUID atendimentoId;

    private ResultadoBuscaAtiva resultadoBusca;

    /** Coletiva: quem participou (substitui a lista prevista). */
    private List<UUID> participantesIds;

    @Size(max = 1000, message = "A observação vai até 1000 caracteres.")
    private String observacao;
}
