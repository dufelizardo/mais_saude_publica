package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.RecursoCapacidade;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/** Capacidade emergencial da unidade (ADR-0158), com o que está ativado agora em emergências. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class CapacidadeEmergencialDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private UUID unidadeId;
    private String unidadeNome;
    private RecursoCapacidade recurso;
    private int normal;
    private int emergencial;
    private long ativadaAgora;
    /** Nos leitos, os leitos ativos da unidade (Leitos), para conferir a capacidade normal. */
    private Integer leitosAtivos;
    private String observacao;
    private boolean ativa;
    private Instant atualizadaEm;
}
