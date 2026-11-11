package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/** Reserva de material para a emergência (ADR-0191). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ReservaEstoqueRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe a emergência.")
    private UUID emergenciaId;
    @NotNull(message = "Informe a unidade.")
    private UUID unidadeId;
    @NotNull(message = "Informe o material.")
    private UUID materialId;
    @NotNull(message = "Informe a quantidade.")
    @Positive(message = "A quantidade precisa ser maior que zero.")
    private Integer quantidade;
    @NotBlank(message = "Informe a justificativa.")
    @Size(max = 500, message = "A justificativa vai até 500 caracteres.")
    private String justificativa;

    /** Liberação da reserva: só o motivo. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Liberacao implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @NotBlank(message = "Informe o motivo da liberação.")
        @Size(max = 500, message = "O motivo vai até 500 caracteres.")
        private String motivo;
    }
}
