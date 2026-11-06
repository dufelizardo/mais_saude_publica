package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** Nível de resposta do catálogo (ADR-0156). Sem {@code ativo}, nasce ativo e, no ajuste, mantém. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class NivelEmergenciaRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe a ordem.")
    @Min(value = 0, message = "A ordem começa em 0.")
    private Integer ordem;
    @NotBlank(message = "Informe o nome.")
    @Size(max = 120, message = "O nome vai até 120 caracteres.")
    private String nome;
    @Size(max = 500, message = "A descrição vai até 500 caracteres.")
    private String descricao;
    private Boolean ativo;
}
