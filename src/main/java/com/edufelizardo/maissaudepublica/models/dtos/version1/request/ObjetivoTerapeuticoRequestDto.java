package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoObjetivoTerapeutico;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/** Objetivo de uma versão do PTS (ADR-0121). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ObjetivoTerapeuticoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Descreva o objetivo.")
    @Size(max = 1000, message = "O objetivo vai até 1000 caracteres.")
    private String descricao;

    @Size(max = 1000, message = "A meta vai até 1000 caracteres.")
    private String meta;

    private LocalDate prazo;

    /** Matrícula de um membro da equipe de referência; opcional. */
    private String responsavelMatricula;

    /** Sem situação, em andamento. */
    private SituacaoObjetivoTerapeutico situacao;
}
