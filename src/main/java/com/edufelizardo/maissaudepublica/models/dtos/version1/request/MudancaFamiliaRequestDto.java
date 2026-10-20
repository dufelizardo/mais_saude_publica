package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Mudança da família inteira para outro domicílio (ADR-0127). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class MudancaFamiliaRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe o domicílio de destino.")
    private UUID domicilioId;

    /** Sem valor, hoje. */
    private LocalDate data;

    @Size(max = 400, message = "O motivo vai até 400 caracteres.")
    private String motivo;
}
