package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.Treinamento;
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
public class TreinamentoResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private String nome;
    private Integer cargaHoraria;
    private Integer validadeMeses;
    private boolean obrigatorio;

    public static TreinamentoResponseDto fromTreinamento(Treinamento treinamento) {
        return new TreinamentoResponseDto(
                treinamento.getUuid(),
                treinamento.getNome(),
                treinamento.getCargaHoraria(),
                treinamento.getValidadeMeses(),
                treinamento.isObrigatorio()
        );
    }
}
