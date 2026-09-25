package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.PerfilPorTipoUnidade;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
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
public class PerfilPorTipoUnidadeResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private TipoUnidadeDeSaude tipo;
    private UUID perfilAdministrativoUuid;
    private String perfilAdministrativoCodigo;
    private String perfilAdministrativoNome;

    public static PerfilPorTipoUnidadeResponseDto fromPerfilPorTipoUnidade(PerfilPorTipoUnidade associacao) {
        return new PerfilPorTipoUnidadeResponseDto(
                associacao.getUuid(),
                associacao.getTipo(),
                associacao.getPerfilAdministrativo().getUuid(),
                associacao.getPerfilAdministrativo().getCodigo(),
                associacao.getPerfilAdministrativo().getNome()
        );
    }
}
