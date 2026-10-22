package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/** Abertura ou atualização da investigação (ADR-0124). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class InvestigacaoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /** Sem matrícula, o profissional do usuário. */
    private String investigadorMatricula;

    @Size(max = 2000, message = "A exposição vai até 2000 caracteres.")
    private String exposicao;

    @Size(max = 500, message = "O local provável de infecção vai até 500 caracteres.")
    private String localProvavelInfeccao;

    @Size(max = 2000, message = "As medidas vão até 2000 caracteres.")
    private String medidas;

    private List<UUID> examesIds;
}
