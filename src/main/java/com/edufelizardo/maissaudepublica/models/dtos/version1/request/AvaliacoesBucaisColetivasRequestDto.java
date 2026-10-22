package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.NecessidadeTratamentoOdontologico;
import com.edufelizardo.maissaudepublica.models.enuns.RiscoSaudeBucal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/** Resultados da avaliação coletiva por participante (ADR-0140). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AvaliacoesBucaisColetivasRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotEmpty(message = "Informe os resultados.")
    @Valid
    private List<Resultado> resultados;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Resultado implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @NotNull(message = "Informe o participante.")
        private UUID pacienteId;

        @NotNull(message = "Informe o risco em saúde bucal.")
        private RiscoSaudeBucal risco;

        @NotNull(message = "Informe a necessidade de tratamento.")
        private NecessidadeTratamentoOdontologico necessidadeTratamento;

        @Size(max = 500, message = "A observação vai até 500 caracteres.")
        private String observacao;
    }
}
