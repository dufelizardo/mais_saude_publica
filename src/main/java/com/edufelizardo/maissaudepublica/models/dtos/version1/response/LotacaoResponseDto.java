package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.Lotacao;
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
public class LotacaoResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private String profissionalMatricula;
    private String profissionalNome;
    private String unidadeNome;
    private String cargoNome;
    private String categoriaNome;
    private Integer jornadaSemanalHoras;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private String motivo;

    public static LotacaoResponseDto fromLotacao(Lotacao lotacao) {
        return new LotacaoResponseDto(
                lotacao.getUuid(),
                lotacao.getProfissional().getMatricula(),
                lotacao.getProfissional().getNome(),
                lotacao.getUnidade().getNome(),
                lotacao.getCargo().getNome(),
                lotacao.getCargo().getCategoria().getNome(),
                lotacao.getJornadaSemanalHoras(),
                lotacao.getDataInicio(),
                lotacao.getDataFim(),
                lotacao.getMotivo()
        );
    }
}
