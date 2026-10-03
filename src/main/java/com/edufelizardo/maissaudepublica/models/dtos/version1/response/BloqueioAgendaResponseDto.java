package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.BloqueioAgenda;
import com.edufelizardo.maissaudepublica.models.enuns.MotivoBloqueioAgenda;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** Bloqueio da agenda (ADR-0091). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class BloqueioAgendaResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private String profissionalMatricula;
    private String profissionalNome;
    private UUID unidadeId;
    private String unidadeNome;
    private LocalDateTime inicio;
    private LocalDateTime fim;
    private MotivoBloqueioAgenda motivo;
    private String descricao;

    public static BloqueioAgendaResponseDto fromBloqueio(BloqueioAgenda b) {
        return new BloqueioAgendaResponseDto(b.getUuid(),
                b.getProfissional() != null ? b.getProfissional().getMatricula() : null,
                b.getProfissional() != null ? b.getProfissional().getNome() : null,
                b.getUnidade() != null ? b.getUnidade().getUuid() : null,
                b.getUnidade() != null ? b.getUnidade().getNome() : null,
                b.getInicio(), b.getFim(), b.getMotivo(), b.getDescricao());
    }
}
