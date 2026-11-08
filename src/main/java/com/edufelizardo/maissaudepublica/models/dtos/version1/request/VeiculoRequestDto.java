package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.TipoVeiculo;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** Veículo da frota (ADR-0178). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class VeiculoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Informe a placa.")
    private String placa;
    @NotBlank(message = "Informe o prefixo.")
    @Size(max = 20, message = "O prefixo vai até 20 caracteres.")
    private String prefixo;
    @NotNull(message = "Informe o tipo.")
    private TipoVeiculo tipo;
    @Size(max = 80, message = "O modelo vai até 80 caracteres.")
    private String modelo;
    @Min(value = 1950, message = "Ano inválido.")
    @Max(value = 2100, message = "Ano inválido.")
    private Integer ano;
    @PositiveOrZero(message = "Os lugares não podem ser negativos.")
    private Integer lugares;
    private Boolean maca;
    private Boolean cadeiraDeRodas;
    @PositiveOrZero(message = "A quilometragem não pode ser negativa.")
    private Integer quilometragem;
    @NotNull(message = "Informe a unidade de lotação.")
    private java.util.UUID unidadeId;
    @Size(max = 500, message = "A observação vai até 500 caracteres.")
    private String observacao;
}
