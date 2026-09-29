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
 * Nova versão de uma administração que corrige a anterior (ADR-0062, ADR-0064): os mesmos campos, mais
 * o motivo. Atendimento e consulta precisam ser os mesmos do registro original; se a versão anterior
 * baixou estoque, a baixa é estornada antes da nova.
 */
@Data
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class RetificacaoAdministracaoMedicamentoRequestDto extends AdministracaoMedicamentoRequestDto {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank
    @Size(max = 1000)
    private String motivoRetificacao;
}
