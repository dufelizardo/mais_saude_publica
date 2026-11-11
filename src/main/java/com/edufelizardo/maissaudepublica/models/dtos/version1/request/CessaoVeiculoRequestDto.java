package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/** A frota cede um veículo ao pedido da emergência (T3, ADR-0192). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class CessaoVeiculoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe o veículo.")
    private UUID veiculoId;
    @Size(max = 500, message = "A observação vai até 500 caracteres.")
    private String observacao;

    /** A devolução: a quilometragem do painel (opcional, não volta) e a observação. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Devolucao implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @PositiveOrZero(message = "A quilometragem não é negativa.")
        private Integer quilometragem;
        @Size(max = 500, message = "A observação vai até 500 caracteres.")
        private String observacao;
    }
}
