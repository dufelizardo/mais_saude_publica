package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Saída de uma pessoa da família, com motivo (ADR-0127). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class SaidaMembroFamiliaRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /** Sem valor, hoje. */
    private LocalDate fim;

    @NotBlank(message = "Informe o motivo.")
    @Size(max = 400, message = "O motivo vai até 400 caracteres.")
    private String motivo;
}
