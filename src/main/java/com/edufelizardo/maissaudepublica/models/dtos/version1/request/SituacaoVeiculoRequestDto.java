package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.SituacaoVeiculo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** Entrada em manutenção, volta a disponível ou inativação (ADR-0178). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class SituacaoVeiculoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe a situação.")
    private SituacaoVeiculo situacao;
    @NotBlank(message = "Informe o motivo.")
    @Size(max = 500, message = "O motivo vai até 500 caracteres.")
    private String motivo;
    @PositiveOrZero(message = "A quilometragem não pode ser negativa.")
    private Integer quilometragem;
}
