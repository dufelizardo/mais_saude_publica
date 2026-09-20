package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.Epi;
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
public class EpiResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private String profissionalMatricula;
    private String profissionalNome;
    private String tipo;
    private String numeroCA;
    private LocalDate dataEntrega;
    private LocalDate dataDevolucao;

    public static EpiResponseDto fromEpi(Epi epi) {
        return new EpiResponseDto(
                epi.getUuid(),
                epi.getProfissional().getMatricula(),
                epi.getProfissional().getNome(),
                epi.getTipo(),
                epi.getNumeroCA(),
                epi.getDataEntrega(),
                epi.getDataDevolucao()
        );
    }
}
