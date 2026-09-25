package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.Atendimento;
import com.edufelizardo.maissaudepublica.models.enuns.StatusAtendimento;
import com.edufelizardo.maissaudepublica.models.enuns.TipoAtendimento;
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
public class AtendimentoResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private UUID pacienteUuid;
    private String pacienteNome;
    private String profissionalMatricula;
    private String profissionalNome;
    private UUID unidadeUuid;
    private String unidadeNome;
    private UUID setorUuid;
    private String setorNome;
    private UUID agendamentoUuid;
    private TipoAtendimento tipo;
    private StatusAtendimento status;
    private LocalDateTime dataHora;

    public static AtendimentoResponseDto fromAtendimento(Atendimento atendimento) {
        UUID setorUuid = null;
        String setorNome = null;
        if (atendimento.getSetor() != null) {
            setorUuid = atendimento.getSetor().getUuid();
            setorNome = atendimento.getSetor().getNome();
        }
        UUID agendamentoUuid = null;
        if (atendimento.getAgendamento() != null) {
            agendamentoUuid = atendimento.getAgendamento().getUuid();
        }
        return new AtendimentoResponseDto(
                atendimento.getUuid(),
                atendimento.getPaciente().getUuid(),
                atendimento.getPaciente().getNome(),
                atendimento.getProfissional().getMatricula(),
                atendimento.getProfissional().getNome(),
                atendimento.getUnidade().getUuid(),
                atendimento.getUnidade().getNome(),
                setorUuid,
                setorNome,
                agendamentoUuid,
                atendimento.getTipo(),
                atendimento.getStatus(),
                atendimento.getDataHora()
        );
    }
}
