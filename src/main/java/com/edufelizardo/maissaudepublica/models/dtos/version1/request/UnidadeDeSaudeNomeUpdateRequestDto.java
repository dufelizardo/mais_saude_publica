package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class UnidadeDeSaudeNomeUpdateRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    @NotBlank(message = "Nome da Instituição precisa ser preenchido")
    private String nome;
}
