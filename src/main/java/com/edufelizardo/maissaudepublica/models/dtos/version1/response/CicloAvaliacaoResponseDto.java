package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.CicloAvaliacao;
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
public class CicloAvaliacaoResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private String nome;
    private LocalDate dataInicio;
    private LocalDate dataFim;

    public static CicloAvaliacaoResponseDto fromCicloAvaliacao(CicloAvaliacao ciclo) {
        return new CicloAvaliacaoResponseDto(
                ciclo.getUuid(),
                ciclo.getNome(),
                ciclo.getDataInicio(),
                ciclo.getDataFim()
        );
    }
}
