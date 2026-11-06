package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.*;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoPercursoLinha;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Encerramento do percurso (ADR-0152): concluído, com as obrigatórias cumpridas ou dispensadas; ou interrompido, com o motivo. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class EncerramentoPercursoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe se o percurso foi concluído ou interrompido.")
    private SituacaoPercursoLinha situacao;

    @Size(max = 500)
    private String motivo;
}
