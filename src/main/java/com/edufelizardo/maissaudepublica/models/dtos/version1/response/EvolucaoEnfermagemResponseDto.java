package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.EvolucaoEnfermagem;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class EvolucaoEnfermagemResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private UUID atendimentoUuid;
    private String profissionalMatricula;
    private String profissionalNome;
    private LocalDateTime dataHora;
    private String descricao;

    public static EvolucaoEnfermagemResponseDto fromEvolucaoEnfermagem(EvolucaoEnfermagem evolucao) {
        return new EvolucaoEnfermagemResponseDto(
                evolucao.getUuid(),
                evolucao.getAtendimento().getUuid(),
                evolucao.getProfissional().getMatricula(),
                evolucao.getProfissional().getNome(),
                evolucao.getDataHora(),
                evolucao.getDescricao()
        );
    }
}
