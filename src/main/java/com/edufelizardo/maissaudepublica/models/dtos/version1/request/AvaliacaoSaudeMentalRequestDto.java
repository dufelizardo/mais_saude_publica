package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** Avaliação em saúde mental de um caso (ADR-0120). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AvaliacaoSaudeMentalRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Descreva o contexto e a história.")
    @Size(max = 4000, message = "O contexto e a história vão até 4000 caracteres.")
    private String contextoHistoria;

    @Size(max = 4000, message = "Os aspectos psicossociais vão até 4000 caracteres.")
    private String aspectosPsicossociais;

    @Size(max = 2000, message = "Os fatores de risco vão até 2000 caracteres.")
    private String fatoresRisco;

    @Size(max = 2000, message = "Os fatores de proteção vão até 2000 caracteres.")
    private String fatoresProtecao;

    @NotBlank(message = "Descreva as necessidades identificadas.")
    @Size(max = 2000, message = "As necessidades vão até 2000 caracteres.")
    private String necessidades;

    /** Hipótese diagnóstica, opcional. */
    @Pattern(regexp = "^\\s*([A-Za-z][0-9]{2}(\\.?[0-9A-Za-z]{1,2})?)?\\s*$", message = "CID-10 inválido (ex.: F32.1).")
    private String cid;
}
