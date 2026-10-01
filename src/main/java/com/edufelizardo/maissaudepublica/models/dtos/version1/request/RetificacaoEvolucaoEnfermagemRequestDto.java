package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;

/**
 * Nova versão de a evolução de enfermagem que corrige a anterior (ADR-0062): os mesmos campos do registro,
 * mais o motivo da retificação. O atendimento precisa ser o mesmo do registro original.
 */
@Data
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class RetificacaoEvolucaoEnfermagemRequestDto extends EvolucaoEnfermagemRequestDto {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank
    @Size(max = 1000)
    private String motivoRetificacao;
}
