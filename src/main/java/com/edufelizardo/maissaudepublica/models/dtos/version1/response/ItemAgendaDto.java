package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.StatusAgendamento;
import com.edufelizardo.maissaudepublica.models.enuns.TipoAgendamento;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** Um item da agenda de um dia (ADR-0091): vaga livre, marcação, encaixe, bloqueio ou afastamento. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ItemAgendaDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /** VAGA, MARCACAO, ENCAIXE, BLOQUEIO ou AFASTAMENTO. */
    private String tipo;
    private LocalDateTime inicio;
    private LocalDateTime fim;
    /** Tipo de atendimento do bloco ou da marcação (consulta, procedimento, retorno). */
    private TipoAgendamento tipoAtendimento;
    private UUID agendamentoId;
    private UUID pacienteId;
    private String pacienteNome;
    private StatusAgendamento status;
    /** Motivo do bloqueio ou do afastamento, ou a observação da marcação. */
    private String descricao;
}
