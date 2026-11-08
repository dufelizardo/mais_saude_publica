package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.FuncaoTripulante;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** Plantão da viatura com a tripulação (ADR-0178). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class PlantaoViaturaRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe o veículo.")
    private java.util.UUID veiculoId;
    @NotNull(message = "Informe o início.")
    private java.time.Instant inicio;
    @NotNull(message = "Informe o fim.")
    private java.time.Instant fim;
    @NotEmpty(message = "Informe a tripulação.")
    private java.util.List<@jakarta.validation.Valid Tripulante> tripulacao;
    @Size(max = 500, message = "A observação vai até 500 caracteres.")
    private String observacao;
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Tripulante implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        @NotBlank(message = "Informe a matrícula do tripulante.")
        private String matricula;
        @NotNull(message = "Informe a função do tripulante.")
        private FuncaoTripulante funcao;
    }
}
