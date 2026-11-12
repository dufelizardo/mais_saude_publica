package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/** A verificação de eficácia do ciclo (ADR-0204); a não eficaz pede a observação. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class VerificacaoEficaciaRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe se foi eficaz.")
    private Boolean eficaz;
    @Size(max = 2000, message = "A observação vai até 2000 caracteres.")
    private String observacao;
    private LocalDate verificadaEm;
}
