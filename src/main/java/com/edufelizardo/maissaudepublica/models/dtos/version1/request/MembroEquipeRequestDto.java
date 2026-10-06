package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.FuncaoEquipe;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** Entrada de um profissional na equipe (ADR-0103). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class MembroEquipeRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Informe a matrícula do profissional.")
    private String profissionalMatricula;

    @NotNull(message = "Informe a função na equipe.")
    private FuncaoEquipe funcao;

    @Size(max = 20, message = "A microárea vai até 20 caracteres.")
    private String microarea;

    /** Sem data, hoje. */
    private LocalDate inicio;
}
