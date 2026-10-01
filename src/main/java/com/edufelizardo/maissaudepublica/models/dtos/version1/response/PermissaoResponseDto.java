package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.Permissao;
import com.edufelizardo.maissaudepublica.models.enuns.DimensaoPermissao;
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
public class PermissaoResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private String codigo;
    private String descricao;
    private DimensaoPermissao dimensao;

    public static PermissaoResponseDto fromPermissao(Permissao p) {
        return new PermissaoResponseDto(p.getUuid(), p.getCodigo(), p.getDescricao(), p.getDimensao());
    }
}
