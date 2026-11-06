package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.TipoCanalComunicacao;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** Canal do catálogo (ADR-0168). Sem {@code ativo}, nasce ativo. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class CanalComunicacaoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe o tipo.")
    private TipoCanalComunicacao tipo;
    @NotBlank(message = "Informe o nome.")
    @Size(max = 100, message = "O nome vai até 100 caracteres.")
    private String nome;
    @Size(max = 300, message = "A descrição vai até 300 caracteres.")
    private String descricao;
    private Boolean ativo;
}
