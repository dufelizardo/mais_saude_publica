package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.PerfilAdministrativo;
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
public class PerfilAdministrativoResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private String codigo;
    private String nome;
    private String descricao;
    private boolean ativo;

    public static PerfilAdministrativoResponseDto fromPerfilAdministrativo(PerfilAdministrativo perfil) {
        return new PerfilAdministrativoResponseDto(
                perfil.getUuid(),
                perfil.getCodigo(),
                perfil.getNome(),
                perfil.getDescricao(),
                perfil.isAtivo()
        );
    }
}
