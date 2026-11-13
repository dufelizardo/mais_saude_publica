package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/** A suspensão do lote na rede pela coordenação (ADR-0206). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class SuspensaoLoteRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe o imunobiológico.")
    private UUID imunobiologicoId;
    @NotBlank(message = "Informe o número do lote.")
    @Size(max = 40, message = "O número do lote vai até 40 caracteres.")
    private String numeroLote;
    @NotBlank(message = "Informe o motivo da suspensão.")
    @Size(max = 1000, message = "O motivo vai até 1000 caracteres.")
    private String motivo;
}
