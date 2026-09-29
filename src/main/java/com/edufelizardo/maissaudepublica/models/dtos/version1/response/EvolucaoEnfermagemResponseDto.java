package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import java.time.Instant;
import com.edufelizardo.maissaudepublica.models.EvolucaoEnfermagem;
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
public class EvolucaoEnfermagemResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private UUID atendimentoUuid;
    private String profissionalMatricula;
    private String profissionalNome;
    private LocalDateTime dataHora;
    private String descricao;

    // Retificação (ADR-0062)
    private UUID retificacaoDeUuid;
    private String motivoRetificacao;
    private Instant registradoEm;
    private String registradoPorCpf;
    /** Verdadeiro quando outra versão corrige este registro — ele deixa de ser o vigente. */
    private boolean retificado;
    private UUID retificadoPorUuid;

    public static EvolucaoEnfermagemResponseDto fromEvolucaoEnfermagem(EvolucaoEnfermagem evolucao) {
        return fromEvolucaoEnfermagem(evolucao, null);
    }

    /** {@code retificadoPor}: id da versão que corrige este registro, ou nulo se ele é o vigente (ADR-0062). */
    public static EvolucaoEnfermagemResponseDto fromEvolucaoEnfermagem(EvolucaoEnfermagem evolucao, UUID retificadoPor) {
        return new EvolucaoEnfermagemResponseDto(
                evolucao.getUuid(),
                evolucao.getAtendimento().getUuid(),
                evolucao.getProfissional().getMatricula(),
                evolucao.getProfissional().getNome(),
                evolucao.getDataHora(),
                evolucao.getDescricao(),
                evolucao.getRetificacaoDe() != null ? evolucao.getRetificacaoDe().getUuid() : null,
                evolucao.getMotivoRetificacao(),
                evolucao.getRegistradoEm(),
                evolucao.getRegistradoPorCpf(),
                retificadoPor != null,
                retificadoPor
        );
    }
}
