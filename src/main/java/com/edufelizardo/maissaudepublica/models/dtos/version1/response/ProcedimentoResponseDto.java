package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import java.time.Instant;
import com.edufelizardo.maissaudepublica.models.Procedimento;
import com.edufelizardo.maissaudepublica.models.enuns.StatusProcedimento;
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
public class ProcedimentoResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private UUID consultaUuid;
    private String profissionalMatricula;
    private String profissionalNome;
    private String tipo;
    private String descricao;
    private LocalDateTime dataRealizacao;
    private StatusProcedimento status;

    // Retificação (ADR-0062)
    private UUID retificacaoDeUuid;
    private String motivoRetificacao;
    private Instant registradoEm;
    private String registradoPorCpf;
    /** Verdadeiro quando outra versão corrige este registro — ele deixa de ser o vigente. */
    private boolean retificado;
    private UUID retificadoPorUuid;

    // Mudança de status (ADR-0062)
    private LocalDateTime dataPrevista;
    private Instant statusAlteradoEm;
    private String profissionalStatusMatricula;
    private String profissionalStatusNome;
    private String justificativaStatus;

    public static ProcedimentoResponseDto fromProcedimento(Procedimento procedimento) {
        return fromProcedimento(procedimento, null);
    }

    /** {@code retificadoPor}: id da versão que corrige este registro, ou nulo se ele é o vigente (ADR-0062). */
    public static ProcedimentoResponseDto fromProcedimento(Procedimento procedimento, UUID retificadoPor) {
        return new ProcedimentoResponseDto(
                procedimento.getUuid(),
                procedimento.getConsulta().getUuid(),
                procedimento.getProfissional().getMatricula(),
                procedimento.getProfissional().getNome(),
                procedimento.getTipo(),
                procedimento.getDescricao(),
                procedimento.getDataRealizacao(),
                procedimento.getStatus(),
                procedimento.getRetificacaoDe() != null ? procedimento.getRetificacaoDe().getUuid() : null,
                procedimento.getMotivoRetificacao(),
                procedimento.getRegistradoEm(),
                procedimento.getRegistradoPorCpf(),
                retificadoPor != null,
                retificadoPor,
                procedimento.getDataPrevista(),
                procedimento.getStatusAlteradoEm(),
                procedimento.getProfissionalStatus() != null ? procedimento.getProfissionalStatus().getMatricula() : null,
                procedimento.getProfissionalStatus() != null ? procedimento.getProfissionalStatus().getNome() : null,
                procedimento.getJustificativaStatus()
        );
    }
}
