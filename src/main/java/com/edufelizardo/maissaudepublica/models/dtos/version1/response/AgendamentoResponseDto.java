package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.Agendamento;
import com.edufelizardo.maissaudepublica.models.enuns.StatusAgendamento;
import com.edufelizardo.maissaudepublica.models.enuns.TipoAgendamento;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AgendamentoResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private UUID pacienteUuid;
    private String pacienteNome;
    private String profissionalMatricula;
    private String profissionalNome;
    private LocalDateTime dataHora;
    private StatusAgendamento status;
    private TipoAgendamento tipo;
    private String observacao;
    private UUID unidadeUuid;
    private String unidadeNome;
    private boolean encaixe;
    /** O programa da marcação (ADR-0132). */
    private UUID programaId;
    private String programaNome;
    /** A especialidade (ADR-0134). */
    private UUID especialidadeId;
    private String especialidadeNome;

    public static AgendamentoResponseDto fromAgendamento(Agendamento agendamento) {
        return new AgendamentoResponseDto(
                agendamento.getUuid(),
                agendamento.getPaciente().getUuid(),
                agendamento.getPaciente().getNome(),
                agendamento.getProfissional().getMatricula(),
                agendamento.getProfissional().getNome(),
                agendamento.getDataHora(),
                agendamento.getStatus(),
                agendamento.getTipo(),
                agendamento.getObservacao(),
                agendamento.getUnidade() != null ? agendamento.getUnidade().getUuid() : null,
                agendamento.getUnidade() != null ? agendamento.getUnidade().getNome() : null,
                agendamento.isEncaixe(),
                agendamento.getPrograma() != null ? agendamento.getPrograma().getUuid() : null,
                agendamento.getPrograma() != null ? agendamento.getPrograma().getNome() : null,
                agendamento.getEspecialidade() != null ? agendamento.getEspecialidade().getUuid() : null,
                agendamento.getEspecialidade() != null ? agendamento.getEspecialidade().getNome() : null
        );
    }
}
