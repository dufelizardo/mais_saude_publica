package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.MotivoBloqueioAgenda;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/** Bloqueio da agenda: de um profissional, da unidade inteira, ou de um profissional numa unidade (ADR-0091). */
@Data
@Getter
@Setter
public class BloqueioAgendaRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /** Sem profissional, o bloqueio vale para todos da unidade. */
    private String profissionalMatricula;

    /** Sem unidade, vale para o profissional em todas as unidades. */
    private UUID unidadeId;

    @NotNull
    private LocalDateTime inicio;

    @NotNull
    private LocalDateTime fim;

    @NotNull
    private MotivoBloqueioAgenda motivo;

    @Size(max = 500)
    private String descricao;
}
