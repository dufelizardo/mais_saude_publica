package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import java.time.LocalDate;
import com.edufelizardo.maissaudepublica.models.enuns.ClassificacaoRisco;
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

    // Paciente e resumo clínico para a listagem (ADR-0062). O resumo só é preenchido nas buscas de
    // atendimento; no prontuário os registros já vêm por inteiro.
    private String pacienteCpf;
    private LocalDate pacienteDataNascimento;
    /** Classificação de risco da triagem vigente mais recente; nula se não houve triagem. */
    private ClassificacaoRisco classificacaoRiscoAtual;
    private Long totalTriagens;
    private Long totalConsultas;
    private Long totalProcedimentos;
    private Long totalEvolucoes;

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
                atendimento.getDataHora(),
                atendimento.getPaciente().getCpf(),
                atendimento.getPaciente().getDataNascimento(),
                null, null, null, null, null
        );
    }
}
