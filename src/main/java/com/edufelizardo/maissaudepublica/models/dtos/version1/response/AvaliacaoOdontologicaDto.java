package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.NecessidadeTratamentoOdontologico;
import com.edufelizardo.maissaudepublica.models.enuns.RiscoSaudeBucal;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

/** Uma avaliação odontológica (ADR-0134), com a retificação (ADR-0062). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AvaliacaoOdontologicaDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private UUID atendimentoId;
    private UUID pacienteId;
    private String profissionalMatricula;
    private String profissionalNome;
    private LocalDateTime dataHora;
    private String queixaPrincipal;
    private String anamnese;
    private String exameClinico;
    private String diagnostico;
    private RiscoSaudeBucal risco;
    private NecessidadeTratamentoOdontologico necessidadeTratamento;
    private String planoInicial;
    private String observacoes;
    private UUID retificacaoDeUuid;
    private String motivoRetificacao;
    /** Corrigida por outra versão (ADR-0062). */
    private boolean retificado;
    private UUID retificadoPorUuid;
    private Instant registradoEm;
}
