package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.SituacaoAcaoControleVetorial;
import com.edufelizardo.maissaudepublica.models.enuns.TipoAcaoControleVetorial;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Uma ação de controle vetorial (ADR-0162). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AcaoControleVetorialDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private TipoAcaoControleVetorial tipo;
    private String descricao;
    private List<String> microareas;
    private List<UUID> microareaIds;
    private LocalDate dataPrevista;
    private LocalDate dataRealizada;
    private String responsavel;
    private Integer imoveisTrabalhados;
    private int vistorias;
    private UUID notificacaoId;
    private String agravoNome;
    private UUID surtoId;
    private String surtoNome;
    private SituacaoAcaoControleVetorial situacao;
    private String resultado;
    private String motivoCancelamento;
    private Instant registradoEm;
}
