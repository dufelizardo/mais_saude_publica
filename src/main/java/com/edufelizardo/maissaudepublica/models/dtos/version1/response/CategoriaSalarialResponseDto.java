package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.CategoriaSalarial;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class CategoriaSalarialResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private String nome;
    private String convencaoColetiva;

    public static CategoriaSalarialResponseDto fromCategoriaSalarial(CategoriaSalarial categoria) {
        return new CategoriaSalarialResponseDto(
                categoria.getUuid(),
                categoria.getNome(),
                categoria.getConvencaoColetiva()
        );
    }
}
