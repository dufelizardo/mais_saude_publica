package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.InsumoExame;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/** Um insumo do exame (ADR-0183). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class InsumoExameDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private UUID materialId;
    private String materialCodigo;
    private String materialNome;
    private int quantidade;
    private boolean soComContraste;

    public static InsumoExameDto fromInsumo(InsumoExame i) {
        return new InsumoExameDto(i.getUuid(), i.getMaterial().getUuid(), i.getMaterial().getCodigo(), i.getMaterial().getNome(), i.getQuantidade(),
                i.isSoComContraste());
    }
}
