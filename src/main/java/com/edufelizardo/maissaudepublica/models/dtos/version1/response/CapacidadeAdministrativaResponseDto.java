package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.CapacidadeAdministrativa;
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
public class CapacidadeAdministrativaResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private String codigo;
    private String nome;
    private String descricao;
    private boolean ativo;

    public static CapacidadeAdministrativaResponseDto fromCapacidadeAdministrativa(CapacidadeAdministrativa capacidade) {
        return new CapacidadeAdministrativaResponseDto(
                capacidade.getUuid(),
                capacidade.getCodigo(),
                capacidade.getNome(),
                capacidade.getDescricao(),
                capacidade.isAtivo()
        );
    }
}
