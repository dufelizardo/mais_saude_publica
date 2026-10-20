package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/** Intervenção prevista numa versão do PTS (ADR-0121). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class IntervencaoPrevistaRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe o tipo da intervenção prevista.")
    private UUID tipoId;

    @Size(max = 100, message = "A frequência vai até 100 caracteres.")
    private String frequencia;

    private String responsavelMatricula;

    @Size(max = 300, message = "O serviço vai até 300 caracteres.")
    private String servico;
}
