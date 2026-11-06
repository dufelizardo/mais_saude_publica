package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/** Tipo de serviço do catálogo (ADR-0165). Sem {@code ativo}, nasce ativo. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class TipoServicoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Informe o código.")
    @Size(max = 60, message = "O código vai até 60 caracteres.")
    private String codigo;
    @NotBlank(message = "Informe o nome.")
    @Size(max = 100, message = "O nome vai até 100 caracteres.")
    private String nome;
    @Size(max = 500, message = "A descrição vai até 500 caracteres.")
    private String descricao;
    private UUID capacidadeExigidaId;
    private Boolean ativo;
}
