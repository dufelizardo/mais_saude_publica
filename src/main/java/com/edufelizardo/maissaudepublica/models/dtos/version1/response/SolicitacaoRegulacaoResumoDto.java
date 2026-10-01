package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.SolicitacaoRegulacao;
import com.edufelizardo.maissaudepublica.models.enuns.PrioridadeRegulacao;
import com.edufelizardo.maissaudepublica.models.enuns.StatusSolicitacaoRegulacao;
import com.edufelizardo.maissaudepublica.models.enuns.TipoProcedimentoRegulado;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A solicitação sem o dado clínico (CID e justificativa), para listagens, para a fila e para a recepção
 * informar o andamento ao paciente (ADR-0087). O detalhe clínico está em {@link SolicitacaoRegulacaoResponseDto}.
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class SolicitacaoRegulacaoResumoDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private StatusSolicitacaoRegulacao status;
    private PrioridadeRegulacao prioridade;
    /** Posição na fila do procedimento; só enquanto SOLICITADA. */
    private Integer posicaoNaFila;
    private UUID procedimentoId;
    private String procedimentoNome;
    private TipoProcedimentoRegulado procedimentoTipo;
    private UUID pacienteId;
    private String pacienteNome;
    private UUID unidadeSolicitanteId;
    private String unidadeSolicitanteNome;
    private String profissionalSolicitanteMatricula;
    private String profissionalSolicitanteNome;
    private Instant solicitadoEm;
    private UUID unidadeExecutanteId;
    private String unidadeExecutanteNome;
    private LocalDateTime dataHoraPrevista;

    public static SolicitacaoRegulacaoResumoDto fromSolicitacao(SolicitacaoRegulacao s, Integer posicaoNaFila) {
        SolicitacaoRegulacaoResumoDto dto = new SolicitacaoRegulacaoResumoDto();
        preencher(dto, s, posicaoNaFila);
        return dto;
    }

    static void preencher(SolicitacaoRegulacaoResumoDto dto, SolicitacaoRegulacao s, Integer posicaoNaFila) {
        dto.setUuid(s.getUuid());
        dto.setStatus(s.getStatus());
        dto.setPrioridade(s.getPrioridade());
        dto.setPosicaoNaFila(posicaoNaFila);
        dto.setProcedimentoId(s.getProcedimento().getUuid());
        dto.setProcedimentoNome(s.getProcedimento().getNome());
        dto.setProcedimentoTipo(s.getProcedimento().getTipo());
        dto.setPacienteId(s.getPaciente().getUuid());
        dto.setPacienteNome(s.getPaciente().getNome());
        dto.setUnidadeSolicitanteId(s.getUnidadeSolicitante().getUuid());
        dto.setUnidadeSolicitanteNome(s.getUnidadeSolicitante().getNome());
        dto.setProfissionalSolicitanteMatricula(s.getProfissionalSolicitante().getMatricula());
        dto.setProfissionalSolicitanteNome(s.getProfissionalSolicitante().getNome());
        dto.setSolicitadoEm(s.getSolicitadoEm());
        if (s.getUnidadeExecutante() != null) {
            dto.setUnidadeExecutanteId(s.getUnidadeExecutante().getUuid());
            dto.setUnidadeExecutanteNome(s.getUnidadeExecutante().getNome());
        }
        dto.setDataHoraPrevista(s.getDataHoraPrevista());
    }
}
