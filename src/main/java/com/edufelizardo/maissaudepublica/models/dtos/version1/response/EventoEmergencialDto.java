package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.FonteEventoEmergencial;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoEventoEmergencial;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/** Evento emergencial (ADR-0156), com a emergência que dele nasceu, se houver. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class EventoEmergencialDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private UUID tipoId;
    private String tipoNome;
    private String cobrade;
    private Instant ocorridoEm;
    private String local;
    private String geometria;
    private FonteEventoEmergencial fonte;
    private String descricao;
    private UUID surtoId;
    private String surtoNome;
    private SituacaoEventoEmergencial situacao;
    private String motivoDescarte;
    private UUID emergenciaId;
    private Instant registradoEm;
}
