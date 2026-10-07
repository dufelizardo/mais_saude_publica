package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** Saída de um profissional da equipe (ADR-0103). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class SaidaMembroEquipeRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /** Sem data, hoje. Não pode ser antes da entrada nem no futuro. */
    private LocalDate fim;

    @NotBlank(message = "Informe o motivo da saída.")
    @Size(max = 500, message = "O motivo vai até 500 caracteres.")
    private String motivo;
}
