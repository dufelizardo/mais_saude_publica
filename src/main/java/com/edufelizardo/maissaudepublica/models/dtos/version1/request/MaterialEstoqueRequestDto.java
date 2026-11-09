package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.CategoriaMaterial;
import com.edufelizardo.maissaudepublica.models.enuns.UnidadeMedidaMaterial;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** Material do catálogo do almoxarifado (ADR-0177). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class MaterialEstoqueRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Informe o código.")
    @Size(max = 30, message = "O código vai até 30 caracteres.")
    private String codigo;
    @NotBlank(message = "Informe o nome.")
    @Size(max = 150, message = "O nome vai até 150 caracteres.")
    private String nome;
    @Size(max = 20, message = "O CATMAT vai até 20 caracteres.")
    private String catmat;
    @NotNull(message = "Informe a categoria.")
    private CategoriaMaterial categoria;
    @NotNull(message = "Informe a unidade de medida.")
    private UnidadeMedidaMaterial unidadeMedida;
    /** Nulo: com lote. */
    private Boolean controlaLote;
    @Size(max = 500, message = "A descrição vai até 500 caracteres.")
    private String descricao;
    /** Nulo: ativo. */
    private Boolean ativo;
}
