package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** A revogação da contraindicação vacinal pela reavaliação no CRIE (ADR-0206). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class RevogacaoContraindicacaoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Informe o parecer da reavaliação.")
    @Size(max = 1000, message = "O parecer vai até 1000 caracteres.")
    private String parecer;
}
