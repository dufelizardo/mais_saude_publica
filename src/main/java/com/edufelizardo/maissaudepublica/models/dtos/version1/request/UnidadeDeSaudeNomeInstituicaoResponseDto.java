package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class UnidadeDeSaudeNomeInstituicaoResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String nome;

    public static UnidadeDeSaudeNomeInstituicaoResponseDto fromUnidadeDeSaudeNomeInstituicao (UnidadeDeSaude unidadeDeSaude) {
        return new UnidadeDeSaudeNomeInstituicaoResponseDto (
                unidadeDeSaude.getNome()
        );
    }
}
