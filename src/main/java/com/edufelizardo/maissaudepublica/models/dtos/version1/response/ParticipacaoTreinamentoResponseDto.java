package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.ParticipacaoTreinamento;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ParticipacaoTreinamentoResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private String profissionalMatricula;
    private String profissionalNome;
    private String treinamentoNome;
    private LocalDate dataConclusao;
    private LocalDate dataValidade;
    private String certificadoUrl;

    public static ParticipacaoTreinamentoResponseDto fromParticipacaoTreinamento(ParticipacaoTreinamento participacao) {
        return new ParticipacaoTreinamentoResponseDto(
                participacao.getUuid(),
                participacao.getProfissional().getMatricula(),
                participacao.getProfissional().getNome(),
                participacao.getTreinamento().getNome(),
                participacao.getDataConclusao(),
                participacao.getDataValidade(),
                participacao.getCertificadoUrl()
        );
    }
}
