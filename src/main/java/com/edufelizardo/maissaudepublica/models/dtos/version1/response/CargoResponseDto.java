package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.Cargo;
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
public class CargoResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private String nome;
    private UUID categoriaUuid;
    private String categoriaNome;
    private String descricao;

    public static CargoResponseDto fromCargo(Cargo cargo) {
        return new CargoResponseDto(
                cargo.getUuid(),
                cargo.getNome(),
                cargo.getCategoria().getUuid(),
                cargo.getCategoria().getNome(),
                cargo.getDescricao()
        );
    }
}
