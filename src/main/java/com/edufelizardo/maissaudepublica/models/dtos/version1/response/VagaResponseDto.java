package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.Vaga;
import com.edufelizardo.maissaudepublica.models.enuns.StatusVaga;
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
public class VagaResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private UUID unidadeUuid;
    private String unidadeNome;
    private UUID cargoUuid;
    private String cargoNome;
    private String cargoDescricao;
    private Integer quantidade;
    private StatusVaga status;

    public static VagaResponseDto fromVaga(Vaga vaga) {
        return new VagaResponseDto(
                vaga.getUuid(),
                vaga.getUnidade().getUuid(),
                vaga.getUnidade().getNome(),
                vaga.getCargo().getUuid(),
                vaga.getCargo().getNome(),
                vaga.getCargo().getDescricao(),
                vaga.getQuantidade(),
                vaga.getStatus()
        );
    }
}
