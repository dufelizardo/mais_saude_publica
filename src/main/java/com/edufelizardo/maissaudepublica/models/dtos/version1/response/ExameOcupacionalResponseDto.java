package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.ExameOcupacional;
import com.edufelizardo.maissaudepublica.models.enuns.ResultadoExameOcupacional;
import com.edufelizardo.maissaudepublica.models.enuns.TipoExameOcupacional;
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
public class ExameOcupacionalResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private String profissionalMatricula;
    private String profissionalNome;
    private TipoExameOcupacional tipo;
    private LocalDate dataRealizacao;
    private LocalDate dataValidade;
    private ResultadoExameOcupacional resultado;
    private String asoUrl;

    public static ExameOcupacionalResponseDto fromExameOcupacional(ExameOcupacional exame) {
        return new ExameOcupacionalResponseDto(
                exame.getUuid(),
                exame.getProfissional().getMatricula(),
                exame.getProfissional().getNome(),
                exame.getTipo(),
                exame.getDataRealizacao(),
                exame.getDataValidade(),
                exame.getResultado(),
                exame.getAsoUrl()
        );
    }
}
