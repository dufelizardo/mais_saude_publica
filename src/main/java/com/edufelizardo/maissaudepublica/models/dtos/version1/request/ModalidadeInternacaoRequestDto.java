package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.*;
import com.edufelizardo.maissaudepublica.models.enuns.ModalidadeInternacao;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** Modalidade da internação psiquiátrica (ADR-0123). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ModalidadeInternacaoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe a modalidade.")
    private ModalidadeInternacao modalidade;

    /** Obrigatória na compulsória. */
    @Size(max = 60, message = "A ordem judicial vai até 60 caracteres.")
    private String ordemJudicial;
}
